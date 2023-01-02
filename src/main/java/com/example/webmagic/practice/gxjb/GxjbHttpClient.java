package com.example.webmagic.practice.gxjb;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.dao.XinXiDao;
import com.example.webmagic.entity.XinXi;
import com.example.webmagic.util.CompleteAllLabel;
import com.example.webmagic.util.up.HttpClientUtils;
import com.example.webmagic.util.OracleUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GxjbHttpClient {
    //域名
    private String domain = "http://www.gxjb.com.cn";
    //列表页请求链接
    private String listUrl = "http://www.gxjb.com.cn/NewsList.aspx?TypeKeyID=300bde4c-c544-45d2-9839-ad4f9a5c3176";
    //最大页
    private int maxNum = 1;
    //当前页
    private int nowNum = 1;
    XinXiDao xinXiDao = new XinXiDao();

    public static void main(String[] args) {
        GxjbHttpClient gxjbHttpClient = new GxjbHttpClient();
        gxjbHttpClient.listPage(1, null, null, null);
    }

    public void listPage(int pageNumber, String viewState, String viewStateGenerator, String eventValidation) {
        Document document;
        if (pageNumber == 1) {
            document = Jsoup.parse(HttpClientUtils.doGet(listUrl, null, "GBK"));
            //获取总页数
            maxNum = Integer.parseInt(document.select("#grdNews_Lbl共几页").text());
        } else {
            //将键值对数组添加到map中
            Map<String, String> headers = new HashMap<>();
            //设置请求方式
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            headers.put("Accept-Charset", "UTF-8");
            //伪装浏览器
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
            //设置请求数据
            ArrayList<NameValuePair> data = new ArrayList<>();
            data.add(new BasicHeader("__EVENTTARGET", "grdNews$ctl23$LnkBtnҳ" + pageNumber));
            data.add(new BasicHeader("__EVENTARGUMENT", ""));
            data.add(new BasicHeader("__VIEWSTATE", viewState));
            data.add(new BasicHeader("__VIEWSTATEGENERATOR", viewStateGenerator));
            data.add(new BasicHeader("__VIEWSTATEENCRYPTED", ""));
            data.add(new BasicHeader("__EVENTVALIDATION", eventValidation));
            //发送请求
            String content = HttpClientUtils.doPost(listUrl, headers, data, "GBK");
            document = Jsoup.parse(content);
        }

        String ye = document.select("#grdNews_Lbl第几页").text();
        System.out.println("第" + ye + "页");
        Elements trList = document.select("#grdNews tr:not([class])");
        if (trList.size() > 0) {

            trList.forEach(tr -> {
                Elements listA = tr.select("a");
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
                    Elements context = Jsoup.parse(HttpClientUtils.doGet(detailLink, null, "GBK")).select(".neiyeconbox_R_con");
                    if (context != null) {
                        //详情标题
                        Elements detailTitleLabel = context.select(".neiyeconbox_R_con_tit");
                        String detailTitle = detailTitleLabel.text();
                        //补全链接并清除无用标签
                        CompleteAllLabel.complete(context, domain);
                        detailTitleLabel.remove();
                        context.select(".neiyeconbox_R_con_time").remove();
                        //删除无用标签
                        xinXi.setDETAIL_TITLE(detailTitle);
                        xinXi.setDETAIL_CONTENT(context.outerHtml());
                        xinXiDao.saveXinxi(xinXi);
                    }
                }
            });

        } else {
            System.out.println("获取失败");
        }

        if (nowNum < maxNum) {
            //翻页
            nowNum++;
            String value1 = document.select("#__VIEWSTATE").attr("value");
            String value2 = document.select("#__VIEWSTATEGENERATOR").attr("value");
            listPage(nowNum,
                    value1,
                    value2,
                    document.select("#__EVENTVALIDATION").attr("value"));
        }
    }
}
