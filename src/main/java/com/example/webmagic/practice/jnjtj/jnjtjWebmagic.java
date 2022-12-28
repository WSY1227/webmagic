package com.example.webmagic.practice.jnjtj;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.practice.yzmcjs.yzmcjsWebmagic;
import org.jsoup.Jsoup;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class jnjtjWebmagic implements PageProcessor {
    String domain = "http://jnjtj.jinan.gov.cn";
    int nowPage = 1;
    int countRecord = 1;
    int perPage = 15;

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    @Override
    public void process(Page page) {
        Document document = page.getHtml().getDocument();
        //page.getUrl().regex("`http://jnjtj.jinan.gov.cn/module\\(.*?)`");
        Elements records = document.select("record");
        for (Element record : records) {
            //获取CDATA中的内容
            String data = record.data();
            System.out.println(data);
            //获取每个td
            List<String> listDate = ReUtil.findAll("<td(.*?)>(.*?)</td>", data, 2);
            //获取列表的a标签
            String listA = listDate.get(1);
            //获取a标签的标题
            String listTitle = ReUtil.get("<a(.*?)>(.*?)</a>", listA, 2);
            System.out.println("listTitle:" + listTitle);
            //获取a的href属性值并补充完整
            String detailLink = domain + ReUtil.get("href=\"(.*?)\"", listA, 1);
            System.out.println("detailLink:" + detailLink);
            //获取详情的时间并清理空格
            String pageTime = listDate.get(2).trim();
            System.out.println("pageTime:" + pageTime);
            page.addTargetRequest(detailLink);
        }


    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {

        //设置Post请求
        Request request = new jnjtjWebmagic().getListRequest(1);
        // 开始执行
        Spider.create(new jnjtjWebmagic())
                .addRequest(request)
                .setScheduler(new QueueScheduler()
                        //设置布隆过滤器
                        .setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))) //参数设置需要对多少条数据去重
                .thread(1).run();
    }

    public Request getListRequest(int nowPage) {
        int startRecord = (nowPage - 1) * perPage * 3 + 1;
        int endRecord = nowPage * perPage * 3;
        if (nowPage != 1 && endRecord > countRecord) {
            endRecord = countRecord;
        }
        String tableListUrl = "http://jnjtj.jinan.gov.cn/module/web/jpage/dataproxy.jsp?startrecord=" + startRecord + "&endrecord=" + endRecord + "&perpage=" + perPage;
        Request request = new Request(tableListUrl);
        request.setMethod(HttpConstant.Method.POST);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        request.addHeader("Cookie", "JSESSIONID=9E632AE3D9146217C1A961144817F20C; JSESSIONID=614A625081002732C1DE8FDB22DE5637");
        //将键值对数组添加到map中
        Map<String, Object> params = new HashMap<>();
        params.put("col", "1");
        params.put("webid", "22");
        params.put("path", "/");
        params.put("columnid", "57329");
        params.put("sourceContentType", "1");
        params.put("unitid", "84137");
        params.put("webname", "济南市城乡交通运输局");
        params.put("permissiontype", "0");
        //设置request参数
        request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        return request;
    }
}
