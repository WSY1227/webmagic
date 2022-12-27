package com.example.webmagic.practice.ahsgh;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.dao.XinXiDao;
import com.example.webmagic.entity.XinXi;
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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ahsghJsoup implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);
    private String outUrl = "http://www.ahsgh.com/ahghjtweb/web/view?strId={0}&strColId={1}&strWebSiteId={2}";
    private String PageUrl = "http://www.ahsgh.com/ahghjtweb/web/list";
    //最大页
    private int maxNum = 1;
    //当前页
    private int nowNum = 1;
    XinXiDao xinXiDao = new XinXiDao();
    public Map<String, XinXi> map = new HashMap<>();


    public Request getListRequest(int pageNumber) {
        Request request = new Request(PageUrl);
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


    @Override
    public void process(Page page) {
        Document document = page.getHtml().getDocument();
        String url = page.getUrl().toString();
        if (url.equals(PageUrl)) {
            //获取列表
            Elements aList = document.select("ul.tab01open.tab01tca>li>a");
            if (aList.size() > 0) {
                for (int i = 0; i < aList.size(); i++) {
                    Element a = aList.get(i);
                    String href = a.attr("href");
                    String title = a.select("h2").first().text();

                    //拿到所有''之间的内容
                    List<String> hrefs = ReUtil.findAll("'(.*?)'", href, 1);
                    //根据strId判断当前值是否存在
                    String id = "strId=" + hrefs.get(0);
                    if (OracleUtils.existsById(id, "XIN_XI_INFO_TEST")) {
                        System.out.println("已存在");
                        continue;
                    }
                    //拼接url
                    String detailLink = MessageFormat.format(outUrl, hrefs.get(0), hrefs.get(1), hrefs.get(2));
                    //开始封装
                    XinXi xinXi = new XinXi();
                    //封装id
                    xinXi.setID(id);
                    //封装列表标题
                    xinXi.setLIST_TITLE(title);
                    //封装链接
                    xinXi.setDETAIL_LINK(detailLink);
                    //封装文章时间
                    xinXi.setPAGE_TIME(a.select("i").first().text());
                    //添加到集合
                    map.put(detailLink, xinXi);
                    //将
                    page.addTargetRequest(detailLink);
                }
                if (nowNum == 1) {
                    //开始获取翻页链接
                    maxNum = Integer.parseInt(document.select(".pagenub").last().text().substring(4));
                    System.out.println(maxNum);
                }
                if (nowNum < maxNum) {
                    nowNum++;
                    page.addTargetRequest(getListRequest(nowNum));
                }
            } else {

            }
        } else {
            //解析内容详情
            XinXi xinXi = map.get(url);
            //
            if (xinXi != null) {
                Element element = document.select(".article-conca.clearfix").first();
                if (element != null) {
                    Elements imgList = element.select("img");
                    for (Element img : imgList) {
                        String src = img.attr("src");
                        if (src.startsWith("/")) {
                            src = "https://www.ahsgh.com" + src;
                        } else {
                            System.out.println("");
                        }
                        img.attr("src", src);
                    }
                    Elements aList = element.select("a");
                    for (Element a : aList) {
                        String href = a.attr("href");
                        if (href.startsWith("/")) {
                            href = "https://www.ahsgh.com" + href;
                            a.attr("href", href);
                        }
                    }
                    String context = element.outerHtml();
                    xinXi.setDETAIL_TITLE(element.select("h2").text());
                    xinXi.setDETAIL_CONTENT(ReUtil.get("<p></p>([\\s\\S]*?)<p></p>", context, 1));
                    xinXiDao.saveXinxi(xinXi);
                } else {
                    System.out.println("未获能正确获取正文内容");
                }
            } else {
                System.out.println("未获得:" + url + "对应的xinXi对象");
            }
        }

    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        ahsghSpider ahsghSpider = new ahsghSpider();

        //设置Post请求
        Request request = ahsghSpider.getListRequest(1);
        // 开始执行
        Spider.create(new ahsghJsoup())
                .addRequest(request)
                .setScheduler(new QueueScheduler()
                        //设置布隆过滤器
                        .setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))) //参数设置需要对多少条数据去重
                .thread(1).run();
    }
}
