package com.example.webmagic.practice.jnjtj;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.dao.XinXiDao;
import com.example.webmagic.entity.XinXi;
import com.example.webmagic.practice.yzmcjs.yzmcjsWebmagic;
import com.example.webmagic.util.OracleUtils;
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
    //当前页
    int nowPage = 1;
    //总条目
    int countRecord = 1;
    //展示量
    int perPage = 15;
    //总页数
    int countPage = 1;
    XinXiDao xinXiDao = new XinXiDao();
    public Map<String, XinXi> map = new HashMap<>();

    private Site site = Site.me()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.54")
            .addCookie("JSESSIONID", "9E632AE3D9146217C1A961144817F20C")
            .setRetryTimes(3).setSleepTime(1000);

    @Override
    public void process(Page page) {
        Document document = page.getHtml().getDocument();
        String pageUrl = page.getUrl().toString();
        if (pageUrl.startsWith("http://jnjtj.jinan.gov.cn/module/web/jpage/dataproxy.jsp")) {
            Elements records = document.select("record");
            if (records != null) {
                for (Element record : records) {
                    //获取CDATA中的内容
                    String data = record.data();
                    //获取每个td
                    List<String> listDate = ReUtil.findAll("<td(.*?)>(.*?)</td>", data, 2);
                    //获取列表的a标签
                    String listA = listDate.get(1);
                    //获取a的href属性值
                    String aHref = ReUtil.get("href=\"(.*?)\"", listA, 1);
                    //将href处理一下可以当唯一ID
                    String id = ReUtil.get("_(\\d+)\\.", aHref, 1);
                    //如果已存在就跳过
                    if (OracleUtils.existsById(id, "XIN_XI_INFO_TEST")) {
                        System.out.println("数据已存在");
                        continue;
                    }
                    //获取a标签的标题
                    String listTitle = ReUtil.get("<a(.*?)>(.*?)</a>", listA, 2);
                    //补充完整链接
                    String detailLink = domain + aHref;
                    //获取详情的时间并清理空格
                    String pageTime = listDate.get(2).trim();
                    XinXi xinXi = new XinXi();
                    xinXi.setID(id);
                    xinXi.setPAGE_TIME(pageTime);
                    xinXi.setLIST_TITLE(listTitle);
                    xinXi.setSOURCE_NAME("济南市城乡交通运输局");
                    xinXi.setDETAIL_LINK(detailLink);
                    map.put(detailLink, xinXi);
                    page.addTargetRequest(detailLink);
                }
                if (nowPage == 1) {
                    //更新总条目
                    countRecord = Integer.parseInt(document.select("totalrecord").text());
                    //每页展示量
                    int dataCount = perPage * 3;
                    //获得实际页数
                    countPage = countRecord / dataCount;
                    if (countRecord % dataCount != 0) {
                        countPage++;
                    }
                }
                //翻页处理
                if (nowPage < countPage) {
                    nowPage++;
                    page.addTargetRequest(getListRequest(nowPage));
                }

            } else {
                System.out.println("获取失败");
            }
        } else {
            Elements context = document.select(".bt-article-02");
            if (context != null) {
                XinXi xinXi = map.get(pageUrl);
                if (xinXi != null) {
                    //获取内容时间
                    String detailTitle = context.select(".sp_title").text();
                    //获取详细内容html
                    String detailContent = context.select("#zoom").outerHtml();
                    xinXi.setDETAIL_TITLE(detailTitle);
                    xinXi.setDETAIL_CONTENT(detailContent);
                    xinXiDao.saveXinxi(xinXi);
                } else {
                    System.out.println("获取xinxi示例失败");
                }
            } else {
                System.out.println("内容获取失败");
            }
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
//        request.addCookie("JSESSIONID", "5E44BB70254AA0F0A6AD4E58356DF11B");
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
