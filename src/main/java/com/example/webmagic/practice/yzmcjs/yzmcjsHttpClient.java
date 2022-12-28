package com.example.webmagic.practice.yzmcjs;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.dao.XinXiDao;
import com.example.webmagic.entity.XinXi;
import com.example.webmagic.practice.ahsgh.ashghHttpClient;
import com.example.webmagic.util.HttpClientUtils;
import com.example.webmagic.util.OracleUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class yzmcjsHttpClient {
    //列表链接
    private String listUrl = "http://www.yzmcjs.com/news.aspx?id=17";
    //详情链接模板
    private String detailLinkTemplate = "http://www.yzmcjs.com/detail.aspx?id=";
    //最大页
    private int maxNum = 1;
    XinXiDao xinXiDao = new XinXiDao();

    public static void main(String[] args) {
        yzmcjsHttpClient yzmcjsHttpClient = new yzmcjsHttpClient();
        yzmcjsHttpClient.listPage(1);
    }

    public void listPage(int nowNum) {
        //将键值对数组添加到map中
        Map<String, String> headers = new HashMap<>();
        //设置请求方式
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        //伪装浏览器
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");
        //设置请求数据
        ArrayList<NameValuePair> data = new ArrayList<>();
        data.add(new BasicHeader("__VIEWSTATEGENERATOR", "CA8C29DA"));
        data.add(new BasicHeader("__EVENTTARGET", "ctl00$ContentPlaceHolder1$AspNetPager1"));
        data.add(new BasicHeader("__EVENTARGUMENT", nowNum + ""));
        //发送请求
        String listContent = HttpClientUtils.sendPost(listUrl, headers, data);
        Document document = Jsoup.parse(listContent);
        //拿到列表内所有a标签
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

            String pageTime = a.select(".time").text().replaceAll("/", "-");
            XinXi xinXi = new XinXi();
            xinXi.setID(yzmcjsId);
            xinXi.setDETAIL_LINK(detailLink);
            xinXi.setLIST_TITLE(listTitle);
            xinXi.setPAGE_TIME(pageTime);
            //发送详情页请求
            String detailRequest = HttpClientUtils.sendGet(detailLink);
            //处理内容页
            Document detailDocument = Jsoup.parse(detailRequest);
            Elements content = detailDocument.select(".page-con");
            if (content != null) {
                //详情标题
                String detailTitle = content.select(".wzy_t1").text();
                //详情内容
                Elements context = content.select(".wzy_bd");
                //补全a标签
                Elements contentAList = context.select("a");
                for (Element aContent : contentAList) {
                    String href = aContent.attr("href");
                    if (href.startsWith("/")) {
                        href = "http://www.yzmcjs.com" + href;
                        aContent.attr("href", href);
                    }
                }
                //补全image标签
                Elements contentImageAList = context.select("image");
                for (Element imageContent : contentImageAList) {
                    String src = imageContent.attr("src");
                    if (src.startsWith("/")) {
                        src = "http://www.yzmcjs.com" + src;
                        imageContent.attr("src", src);
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
        if (nowNum == 1) {
            String href = document.select("a:contains(尾页)").first().attr("href");
            maxNum = Integer.parseInt(ReUtil.get("'(\\d+)'", href, 1));
            System.out.println(maxNum);
        }
        if (nowNum < maxNum) {
            nowNum++;
            //翻页处理
            listPage(nowNum);
        }
    }
}
