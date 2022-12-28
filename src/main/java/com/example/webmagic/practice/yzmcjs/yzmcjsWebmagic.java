package com.example.webmagic.practice.yzmcjs;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.dao.XinXiDao;
import com.example.webmagic.entity.XinXi;
import com.example.webmagic.practice.ahsgh.ahsghSpider;
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
    XinXiDao xinXiDao = new XinXiDao();
    public Map<String, XinXi> map = new HashMap<>();

    @Override
    public void process(Page page) {
        Document document = page.getHtml().getDocument();
        String pageUrl = page.getUrl().toString();
        if (pageUrl.equals(ListUrl)) {
            Elements aList = document.select(".lmy_info_list a");
            for (Element a : aList) {
                String id = ReUtil.get("id=(\\d+)", a.attr("href"), 1);
                //存入数据库的id拼接一下避免冲突
                String yzmcjsId = "yzmcjs=" + id;
                if (OracleUtils.existsById(yzmcjsId, "XIN_XI_INFO_TEST")) {
                    System.out.println("已存在");
                    continue;
                }
                //拼接生成详情链接
                String detailLink = detailLinkTemplate + id;
                //获取列表标题
                String listTitle = a.select(".title").text();
                //获取对应时间
                String pageTime = a.select(".time").text().replaceAll("/","-");

                XinXi xinXi = new XinXi();
                xinXi.setID(yzmcjsId);
                xinXi.setDETAIL_LINK(detailLink);
                xinXi.setLIST_TITLE(listTitle);
                xinXi.setPAGE_TIME(pageTime);
                map.put(detailLink, xinXi);
                page.addTargetRequest(detailLink);
            }
            if (nowNum == 1) {
                String href = document.select("a:contains(尾页)").first().attr("href");
                maxNum = Integer.parseInt(ReUtil.get("'(\\d+)'", href, 1));
                System.out.println(maxNum);
            }
            if (nowNum <= maxNum) {
                nowNum++;
                Request request = getListRequest(nowNum);
                page.addTargetRequest(request);
            }
        } else {
            XinXi xinXi = map.get(pageUrl);
            if (xinXi != null) {
                Elements content = document.select(".page-con");
                if (content != null) {
                    //详情标题
                    String detailTitle = content.select(".wzy_t1").text();
                    System.out.println("详情标题" + detailTitle);
                    //详情内容
                    Elements context = content.select(".wzy_bd");
                    Elements contentAList = context.select("a");
                    //补全a标签
                    for (Element a : contentAList) {
                        String href = a.attr("href");
                        if (href.startsWith("/")) {
                            href = "http://www.yzmcjs.com" + href;
                            a.attr("href", href);
                        }
                    }
                    //详情html
                    String detailContent = context.outerHtml();
                    xinXi.setDETAIL_TITLE(detailTitle);
                    xinXi.setDETAIL_CONTENT(detailContent);
                    xinXi.setSOURCE_NAME("扬州市名城建设有限公司");
                    xinXiDao.saveXinxi(xinXi);
                } else {
                    System.out.println("未获能正确获取正文内容");
                }
            }
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
        params.put("__VIEWSTATEGENERATOR", "CA8C29DA");
        params.put("__EVENTTARGET", "ctl00$ContentPlaceHolder1$AspNetPager1");
        params.put("__EVENTARGUMENT", pageNumber + "");
        //设置request参数
        request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        return request;

    }
}
