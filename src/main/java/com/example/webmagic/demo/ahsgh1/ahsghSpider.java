package com.example.webmagic.demo.ahsgh1;

import org.jsoup.nodes.Document;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.utils.HttpConstant;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ahsghSpider
 * @Description:
 * @author: XU
 * @date: 2022年12月24日 1:37
 **/

public class ahsghSpider implements PageProcessor {
    public Request getListRequest(int pageNumber) {
        Request request = new Request("http://www.ahsgh.com/ahghjtweb/web/list");
        request.setMethod(HttpConstant.Method.POST);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        //将键值对数组添加到map中
        Map<String, Object> params = new HashMap<>();
        params.put("listPage", "list");
        params.put("intCurPage", pageNumber + "");
        params.put("intPageSize", "10");
        params.put("strColId", "20782f569264489f87995ad0773ff626");
        params.put("strWebSiteId", "4c5fcf57602b48a0acde5a4ef3ede48d");
        params.put("nowPage", pageNumber + "");
        //设置request参数
        request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        return request;
    }

    private String PageUrl = "http://www.ahsgh.com/ahghjtweb/web/list";
    private int maxNum = 1;
    private int nowNum = 1;

    @Override
    public void process(Page page) {
        Document document = page.getHtml().getDocument();
//        Document document1 = Jsoup.parse(page.getRawText());
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
            if (nowNum == 1) {
                //开始获取翻页链接
                maxNum = Integer.parseInt(page.getHtml().$(".pagenub", "text").all().get(1).substring(3));
            }
            if(nowNum < maxNum){
                nowNum++;
                page.addTargetRequest(getListRequest(nowNum));
            }
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

    public static void main(String[] args) {
        ahsghSpider ahsghSpider = new ahsghSpider();

        //设置Post请求
        Request request = ahsghSpider.getListRequest(1);
        // 开始执行
        Spider.create(new ahsghSpider())
                .addRequest(request)
//                .addPipeline(new ahsghPipeline())
                .setScheduler(new QueueScheduler()
                        //设置布隆过滤器
                        .setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))) //参数设置需要对多少条数据去重
                .thread(1).run();
    }
}
