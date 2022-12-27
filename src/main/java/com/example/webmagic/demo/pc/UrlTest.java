package com.example.webmagic.demo.pc;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.HashMap;
import java.util.Map;

public class UrlTest  implements PageProcessor {

    @Override
    public void process(Page page) {
        String s = page.getHtml().$(".tab01open.tab01tca li a").links().all().toString();
        System.out.println(s);
    }

    private Site site = Site.me();

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        //设置Post请求
        Request request = new Request("http://www.ahsgh.com/ahghjtweb/web/list");
        request.setMethod(HttpConstant.Method.POST);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        //将键值对数组添加到map中
        Map<String, Object> params = new HashMap<>();
        params.put("listPage", "list");
        params.put("intCurPage", 1 + "");
        params.put("intPageSize", "10");
        params.put("strColId", "20782f569264489f87995ad0773ff626");
        params.put("strWebSiteId", "4c5fcf57602b48a0acde5a4ef3ede48d");
        params.put("nowPage", 1 + "");
        //设置request参数
        request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        // 开始执行
        try {
            us.codecraft.webmagic.Spider.create(new UrlTest()).addRequest(request).thread(1).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
