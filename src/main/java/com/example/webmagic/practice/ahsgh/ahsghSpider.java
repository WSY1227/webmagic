package com.example.webmagic.practice.ahsgh;

import com.example.webmagic.dao.XinXiDao;
import com.example.webmagic.entity.XinXi;
import com.example.webmagic.util.OracleUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.selector.Html;
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
    //最大页
    private int maxNum = 1;
    //当前页
    private int nowNum = 1;
    XinXiDao xinXiDao = new XinXiDao();
    public Map<String, XinXi> map = new HashMap<>();

    @Override
    public void process(Page page) {
        Html html = page.getHtml();
//        Document document = html.getDocument();
//        Document document1 = Jsoup.parse(page.getRawText());
        String pageUrl = page.getUrl().toString();
        if (pageUrl.equals(PageUrl)) {
            List<String> href = html.xpath("//ul[@class=\"tab01open tab01tca\"]/li/a/@href").regex("\\((.*?)\\)").replace("'", "").all();
            String outUrl = "http://www.ahsgh.com/ahghjtweb/web/view?strId={0}&strColId={1}&strWebSiteId={2}";
            List<String> titleList = html.$(".tit-time>h2", "text").all();
            for (int i = 0; i < href.size(); i++) {
                String[] hrefs = href.get(i).split(",");
                //根据strId判断当前值是否存在
                if (OracleUtils.existsById("strId=" + hrefs[0], "XIN_XI_INFO_TEST")) {
                    System.out.println("已存在");
                    continue;
                }
                String url = MessageFormat.format(outUrl, hrefs[0], hrefs[1], hrefs[2]);
                //添加内容页
                XinXi xinXi = new XinXi();
                //添加列表标题
                xinXi.setLIST_TITLE(titleList.get(i));
                map.put(url, xinXi);
                page.addTargetRequest(url);
            }
            if (nowNum == 1) {
                //开始获取翻页链接
                maxNum = Integer.parseInt(html.$(".pagenub", "text").all().get(1).substring(3));
            }
            if (nowNum < maxNum) {
                nowNum++;
                page.addTargetRequest(getListRequest(nowNum));
            }
        } else {
            //解析内容详情
            String url = pageUrl;
            XinXi xinXi = map.get(url);
            String strId = url.substring(url.indexOf("?") + 1, url.indexOf("&"));
            String title = html.$(".article-conca.clearfix h2", "text").toString();
            String time = html.$(".article-information span", "text").regex("\\d{4}-\\d{2}-\\d{2}").toString();
            String context = html.$(".article-conca.clearfix").toString();
            int startIndex = context.indexOf("<!-- 正文内容 -->");
            int endIndex = context.indexOf("<!-- 文后 -->");
            String contentHtml = context.substring(startIndex + "<!-- 正文内容 -->".length(), endIndex);
            xinXi.setID(strId);
            xinXi.setDETAIL_LINK(url);
            xinXi.setDETAIL_TITLE(title);
            xinXi.setDETAIL_CONTENT(contentHtml);
            xinXi.setPAGE_TIME(time);
            xinXi.setSOURCE_NAME("安徽省港航集团有限公司");
            xinXiDao.saveXinxi(xinXi);
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
                .setScheduler(new QueueScheduler()
                        //设置布隆过滤器
                        .setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))) //参数设置需要对多少条数据去重
                .thread(1).run();
    }
}
