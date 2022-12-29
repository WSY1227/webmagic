package com.example.webmagic.practice.fri;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.dao.XinXiDao;
import com.example.webmagic.entity.XinXi;
import com.example.webmagic.practice.jnjtj.jnjtjWebmagic;
import com.example.webmagic.util.CompleteAllLabel;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.HashMap;
import java.util.Map;

public class firWebMagic implements PageProcessor {
    //域名
    private String domain = "http://www.fri.com.cn";
    //列表页请求链接
    private String listUrl = "http://www.fri.com.cn/NoticeListShowPage.aspx?id=1";
    //详情请求模板
    private String detailLinkTemplate = "http://www.fri.com.cn/NoticeShowPage.aspx?id=";
    //最大页
    private int maxNum = 1;
    //当前页
    private int nowNum = 1;
    XinXiDao xinXiDao = new XinXiDao();
    public Map<String, XinXi> map = new HashMap<>();

    @Override
    public void process(Page page) {
        Document document = page.getHtml().getDocument();
        String pageUrl = page.getUrl().toString();
        //域名
        if (pageUrl.equals(listUrl)) {
            Elements h5s = document.select(".media>h5");
            if (h5s != null) {
                h5s.forEach(h5 -> {
                    //获取列表标题
                    String listTitle = h5.text();
                    System.out.println("listTitle:" + listTitle);
                    String onclickId = ReUtil.get("\\d", h5.attr("onclick"), 0);
                    //存入数据库的id
                    String id = "fri=" + onclickId;
                    System.out.println("id:" + onclickId);
                    //拼接链接
                    String detailLink = detailLinkTemplate + onclickId;
                    System.out.println("detailLink:" + detailLink);
                    page.addTargetRequest(detailLink);
                });
                if (nowNum == 1) {
                    //获取总页数
                    maxNum = Integer.parseInt(document.select("#page>li").get(1).text().replaceAll(".*(\\d+).*", "$1"));
                }
                if (nowNum < maxNum) {
                    //翻页
                    nowNum++;
                    String viewState = document.select("#__VIEWSTATE").attr("value");
                    String viewStateGenerator = document.select("#__VIEWSTATEGENERATOR").attr("value");
                    String eventValidation = document.select("#__EVENTVALIDATION").attr("value");
                    String hid_idd = document.select("#ContentPlaceHolder1_hid_idd").attr("value");
                    Request request = getListRequest(nowNum, viewState, hid_idd, eventValidation, viewStateGenerator);
                    page.addTargetRequest(request);
                }
            } else {
                System.out.println("获取失败");
            }
        } else {
            Elements context = document.select("#detailsContent");
            if (context != null) {
                //补全标签
                CompleteAllLabel.complete(context, domain);
                //获取详情标题
                Elements titleLabel = context.select(".details-title");
                String detailTitle = titleLabel.text();
                //获取详情时间
                Elements timeLabel = context.select(".details-desc.text-right>span");
                String timeText = timeLabel.text();
                String time = ReUtil.get("\\d+/\\d+/\\d+?", timeText, 1);

                titleLabel.remove();
                timeLabel.remove();

                System.out.println(time);

            }
        }
    }

    @Override
    public Site getSite() {
        return Site.me()
                .setTimeOut(15000) // 设置超时时间为3秒
                .setRetrySleepTime(1000) // 设置重试的间隔时间为1秒
                .setRetryTimes(3) // 设置重试次数为3次
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.54");
    }

    public static void main(String[] args) {
        Spider.create(new firWebMagic())
                .addUrl("http://www.fri.com.cn/NoticeListShowPage.aspx?id=1")
                .setScheduler(new QueueScheduler()
                        //设置布隆过滤器
                        .setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))) //参数设置需要对多少条数据去重
                .thread(1).run();
    }

    public Request getListRequest(int pageNumber, String viewState, String hid_idd, String eventValidation, String
            viewStateGenerator) {
        Request request = new Request(listUrl);
        request.setMethod(HttpConstant.Method.POST);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        //将键值对数组添加到map中
        Map<String, Object> params = new HashMap<>();
        params.put("__EVENTTARGET", "ctl00$ContentPlaceHolder1$btnpagetz");
        params.put("__EVENTARGUMENT", "");
        params.put("__VIEWSTATE", viewState);
        params.put("__VIEWSTATEGENERATOR", viewStateGenerator);
        params.put("__EVENTVALIDATION", eventValidation);
        params.put("ctl00$ContentPlaceHolder1$txt_title", "");
        params.put("ctl00$ContentPlaceHolder1$page_number", pageNumber + "");
        params.put("ctl00$ContentPlaceHolder1$hid_id", "");
        params.put("ctl00$ContentPlaceHolder1$hid_idd", hid_idd);
        //设置request参数
        request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        return request;

    }
}
