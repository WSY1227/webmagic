package com.example.webmagic.ahsgh;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class timeTitle implements PageProcessor {
    private static int sumPage = 0;


    @Override
    public void process(Page page) {
        //页码
        if (sumPage == 0) {
            String yeMa = page.getHtml().$(".pagenub i", "text").toString();
            int integer = Integer.valueOf(yeMa);
            sumPage = integer / 10;
            if (integer % 10 != 0) {
                ++sumPage;
            }
        }
        //标题
        page.putField("fl", page.getHtml().$("h2.fl", "text").all());
        //时间
        page.putField("fr", page.getHtml().$("i.fr", "text").all());
        //href

        page.putField("href", page.getHtml().xpath("//ul[@class=\"tab01open tab01tca\"]/li/a/@href").regex("'(.+?)'").all());

    }

    private Site site = Site.me();

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        timeTitle timeTitle = new timeTitle();
        timeTitle.spider(1);
        System.out.println("第1页");

        System.out.println("sumPage"+sumPage);
        for (int i = 2; i <= sumPage; i++) {

            timeTitle.spider(i);
            System.out.println("第" + i + "页");
        }

    }


    public void spider(int page) {
        //设置Post请求
        Request request = new Request("http://www.ahsgh.com/ahghjtweb/web/list");
        request.setMethod(HttpConstant.Method.POST);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        //将键值对数组添加到map中
        Map<String, Object> params = new HashMap<>();
        params.put("listPage", "list");
        params.put("intCurPage", page + "");
        params.put("intPageSize", "10");
        params.put("strColId", "20782f569264489f87995ad0773ff626");
        params.put("strWebSiteId", "4c5fcf57602b48a0acde5a4ef3ede48d");
        params.put("nowPage", page + "");
        //设置request参数
        request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        // 开始执行
        try {
            Spider.create(new timeTitle()).addRequest(request).addPipeline(new MyPipeline()).thread(1).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
