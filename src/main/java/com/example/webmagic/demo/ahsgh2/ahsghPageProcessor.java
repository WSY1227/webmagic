package com.example.webmagic.demo.ahsgh2;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.text.MessageFormat;
import java.util.List;

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
//            String s = page.getHtml().xpath("//div[@class='sunui-sunpage']/a/@href").regex("gotoPage\\(\\d+\\)").all().get(0);
//            String s = page.getHtml().xpath("//div[@class='sunui-sunpage']/a/@href").regex("gotoPage\\(\\d+\\)").all().get(0);
//            System.out.println("下一页："+s);
            if (PageCount == 0) {
                //开始获取翻页链接
                ahsghSpider ahsghSpider = new ahsghSpider();
                PageCount = Integer.parseInt(page.getHtml().$(".pagenub", "text").all().get(1).substring(3));
                for (int i = 2; i <= 3; i++) {
                    //包装每页的链接并放入请求队列
                    page.addTargetRequest(ahsghSpider.getListRequest(i));
                }
            }
            //列表界面跳过Pipeline
            page.setSkip(true);
        } else {
            //解析内容详情
            String title = page.getHtml().$(".article-conca.clearfix h2", "text").toString();
            String time = page.getHtml().$(".article-information span", "text").regex("\\d{4}-\\d{2}-\\d{2}").toString();
            String context = page.getHtml().$(".article-conca.clearfix").toString();
            int startIndex = context.indexOf("<!-- 正文内容 -->");
            int endIndex = context.indexOf("<!-- 文后 -->");
            page.putField("title", title);
            page.putField("time", time);
            if (startIndex != -1 && endIndex != -1) {
                String contentHtml = context.substring(startIndex + "<!-- 正文内容 -->".length(), endIndex);
                page.putField("contentHtml", contentHtml);
            }
        }

    }

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    @Override
    public Site getSite() {
        return site;
    }
}
