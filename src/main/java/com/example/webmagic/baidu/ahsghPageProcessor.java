package com.example.webmagic.baidu;

import org.apache.logging.log4j.message.Message;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.utils.HttpConstant;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ahsghPageProcessor
 * @Description:
 * @author: XU
 * @date: 2022年12月24日 1:35
 **/

public class ahsghPageProcessor implements PageProcessor {
    private String PageUrl = "http://www.ahsgh.com/ahghjtweb/web/list";
    private int PageCount = 0;

    @Override
    public void process(Page page) {

        if (page.getUrl().toString().equals(PageUrl)) {
            List<String> href = page.getHtml().xpath("//ul[@class=\"tab01open tab01tca\"]/li/a/@href").regex("\\((.*?)\\)").replace("'", "").all();
            String outUrl = "http://www.ahsgh.com/ahghjtweb/web/view?strId={0}&strColId={1}&strWebSiteId={2}";
            href.forEach(s -> {
                String[] hrefs = s.split(",");
                String url = MessageFormat.format(outUrl, hrefs[0], hrefs[1], hrefs[2]);
                //添加内容页
                page.addTargetRequest(url);
            });
            if (PageCount == 0) {
                //开始获取翻页链接
                ahsghSpider ahsghSpider = new ahsghSpider();
                PageCount = Integer.parseInt(page.getHtml().$(".pagenub", "text").all().get(1).substring(3));
                for (int i = 2; i <= 3; i++) {
                    //包装每页的链接并放入请求队列
                    page.addTargetRequest(ahsghSpider.getNextPageRequest(i));
                }
            }
        } else {
            //解析内容详情
            String title = page.getHtml().$(".article-conca.clearfix h2", "text").toString();
            String time = page.getHtml().$(".article-information span", "text").regex("\\d{4}-\\d{2}-\\d{2}").toString();
            System.out.println("标题：" + title);
            System.out.println("时间：" + time);
            String context = page.getHtml().$(".article-conca.clearfix").toString();
            System.out.println(context);
        }

    }

    private Site site = Site.me();

    @Override
    public Site getSite() {
        return site;
    }
}
