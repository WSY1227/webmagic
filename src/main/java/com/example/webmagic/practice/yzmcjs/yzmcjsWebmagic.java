package com.example.webmagic.practice.yzmcjs;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.practice.ahsgh.ahsghSpider;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: yzmcjsWebmagic
 * @Description:
 * @author: XU
 * @date: 2022年12月27日 22:36
 **/

public class yzmcjsWebmagic implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);
    private String ListUrl = "http://www.yzmcjs.com/news.aspx?id=17";
    private String detailLinkTemplate = "http://www.yzmcjs.com/detail.aspx?id=";
    //最大页
    private int maxNum = 1;
    //当前页
    private int nowNum = 1;
    @Override
    public void process(Page page) {
        Document document = page.getHtml().getDocument();
        String pageUrl = page.getUrl().toString();
        if (pageUrl.equals(ListUrl)) {
            Elements aList = document.select(".lmy_info_list a");
            for (Element a : aList) {
                String detailLink = detailLinkTemplate + ReUtil.get("id=(\\d+)", a.attr("href"), 1);
                System.out.println(detailLink);
                String listTitle = a.select(".title").text();
                System.out.println(listTitle);
                String pageTime = a.select(".time").text();
                System.out.println(pageTime);
                page.addTargetRequest(detailLink);
            }
        }else {
            Elements content = document.select(".page-con");
            String detailTitle=content.select(".wzy_t1").text();
            Elements context = content.select(".wzy_bd");
            Elements aList = context.select("a");
            for (Element a : aList) {
                String href = a.attr("href");
                if (href.startsWith("/")) {
                    href = "http://www.yzmcjs.com" + href;
                    a.attr("href", href);
                }
            }
            String detailContent =context.outerHtml();
            System.out.println(detailContent);
        }
        if (nowNum == 1) {
            String href = document.select("a:contains(尾页)").first().attr("href");
            maxNum = Integer.parseInt(ReUtil.get("'(\\d+)'", href, 1));
            System.out.println(maxNum);
        }
        if (nowNum < maxNum) {
            nowNum++;
            page.addTargetRequest(getListRequest(nowNum));
        }

    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {

        //设置Post请求
        Request request = new yzmcjsWebmagic().getListRequest(1);
        // 开始执行
        Spider.create(new yzmcjsWebmagic())
                .addRequest(request)
                .setScheduler(new QueueScheduler()
                        //设置布隆过滤器
                        .setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))) //参数设置需要对多少条数据去重
                .thread(1).run();
    }

    public Request getListRequest(int pageNumber) {
        Request request = new Request(ListUrl);
        request.setMethod(HttpConstant.Method.POST);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        //将键值对数组添加到map中
        Map<String, Object> params = new HashMap<>();
        params.put("__EVENTTARGET", "ContentPlaceHolder1");
        params.put("__EVENTARGUMENT", pageNumber + "");
        //设置request参数
        request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        return request;

    }
}
