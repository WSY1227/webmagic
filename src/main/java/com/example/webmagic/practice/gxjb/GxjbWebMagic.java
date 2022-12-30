package com.example.webmagic.practice.gxjb;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.dao.XinXiDao;
import com.example.webmagic.entity.XinXi;
import com.example.webmagic.util.CompleteAllLabel;
import com.example.webmagic.util.OracleUtils;
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

public class GxjbWebMagic implements PageProcessor {
    //域名
    private String domain = "http://www.gxjb.com.cn";
    //列表页请求链接
    private String listUrl = "http://www.gxjb.com.cn/NewsList.aspx?TypeKeyID=300bde4c-c544-45d2-9839-ad4f9a5c3176";
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
            String ye = document.select("#grdNews_Lbl第几页").text();
            System.out.println("第" + ye + "页");
            Elements trList = document.select("#grdNews tr:not([class])");
            if (trList.size() > 0) {
                trList.forEach(tr -> {
                    Element listA = tr.select("a").first();
                    String detailLinkHref = listA.attr("href");
                    //获取id
                    String id = ReUtil.get("NewsKeyID=([^&]*)", detailLinkHref, 0);
                    if (OracleUtils.existsById(id, "XIN_XI_INFO_TEST")) {
                        //判断是否已存在
                        System.out.println("已存在");
                    } else {
                        //列表标题
                        String listTitle = listA.text();
                        //获取详情链接
                        String detailLink = domain + "/" + detailLinkHref;
                        //获取详情时间
                        String pageTime = tr.select(".mainbox_dy_main_1_content_time").text();
                        XinXi xinXi = new XinXi();
                        xinXi.setID(id);
                        xinXi.setSOURCE_NAME("广西建标建设工程咨询有限责任公司");
                        xinXi.setLIST_TITLE(listTitle);
                        xinXi.setDETAIL_LINK(detailLink);
                        xinXi.setPAGE_TIME(pageTime);
                        map.put(detailLink, xinXi);
//                        System.out.println("detailLink:" + detailLink);
                        page.addTargetRequest(detailLink);
                    }
                });
                if (nowNum == 1) {
                    //获取总页数
                    String maxNumString = document.select("#grdNews_Lbl共几页").text();
                    if (maxNumString == null) {
                        System.out.println("总页数获取异常");
                        return;
                    }
                    maxNum = Integer.parseInt(maxNumString);
                }
                if (nowNum < maxNum) {
                    //翻页
                    nowNum++;
                    String viewState = document.select("#__VIEWSTATE").attr("value");
                    String viewStateGenerator = document.select("#__VIEWSTATEGENERATOR").attr("value");
                    String eventValidation = document.select("#__EVENTVALIDATION").attr("value");
                    Request request = getListRequest(nowNum, viewState, viewStateGenerator, eventValidation);
                    page.addTargetRequest(request);
                }

            } else {
                System.out.println("获取列表失败");
            }
        } else {
            Elements context = document.select(".neiyeconbox_R_con");
            if (context.size() > 0) {
                XinXi xinXi = map.get(pageUrl);
                if (xinXi != null) {
                    //详情标题
                    Element detailTitleLabel = context.select(".neiyeconbox_R_con_tit").first();
                    String detailTitle = detailTitleLabel.text();
                    //补全链接并清除无用标签
                    CompleteAllLabel.complete(context, domain);
                    detailTitleLabel.remove();
                    context.select(".neiyeconbox_R_con_time").remove();
                    //删除无用标签
                    xinXi.setDETAIL_TITLE(detailTitle);
                    xinXi.setDETAIL_CONTENT(context.outerHtml());
                    xinXiDao.saveXinxi(xinXi);
                } else {
                    System.out.println("获取xinxi对象失败");
                }
            } else {
                System.out.println("获取内容失败");
            }
        }
    }

    @Override
    public Site getSite() {
        return Site.me()
                .setCharset("GBK")//设置编码
                .setTimeOut(15000) // 设置超时时间为3秒
                .setRetrySleepTime(1000) // 设置重试的间隔时间为1秒
                .setRetryTimes(3) // 设置重试次数为3次
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.54");
    }

    public static void main(String[] args) {
        Spider.create(new GxjbWebMagic())
                .addUrl("http://www.gxjb.com.cn/NewsList.aspx?TypeKeyID=300bde4c-c544-45d2-9839-ad4f9a5c3176")
                .setScheduler(new QueueScheduler()
                        //设置布隆过滤器
                        .setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))) //参数设置需要对多少条数据去重
                .thread(1).run();
    }

    public Request getListRequest(int pageNumber, String viewState, String viewStateGenerator, String eventValidation) {
        Request request = new Request(listUrl);
        request.setMethod(HttpConstant.Method.POST);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        //将键值对数组添加到map中
        Map<String, Object> params = new HashMap<>();
        params.put("__EVENTTARGET", "grdNews$ctl23$LnkBtnҳ" + pageNumber);
        params.put("__EVENTARGUMENT", "");
        params.put("__VIEWSTATE", viewState);
        params.put("__VIEWSTATEGENERATOR", viewStateGenerator);
        params.put("__VIEWSTATEENCRYPTED", "");
        params.put("__EVENTVALIDATION", eventValidation);
        params.put("Top1$txtContent", "");

        //设置request参数
        request.setRequestBody(HttpRequestBody.form(params, "utf-8"));
        return request;
    }
}
