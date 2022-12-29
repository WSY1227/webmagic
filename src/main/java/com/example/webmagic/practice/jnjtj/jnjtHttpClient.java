package com.example.webmagic.practice.jnjtj;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.dao.XinXiDao;
import com.example.webmagic.entity.XinXi;
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
import java.util.List;
import java.util.Map;

public class jnjtHttpClient {
    String domain = "http://jnjtj.jinan.gov.cn";
    //    //当前页
//    int nowPage = 1;
    //总条目
    int countRecord = 1;
    //展示量
    int perPage = 15;
    //总页数
    int countPage = 1;
    XinXiDao xinXiDao = new XinXiDao();

    public static void main(String[] args) {
        jnjtHttpClient jnjtHttpClient = new jnjtHttpClient();
        jnjtHttpClient.listPage(1);

    }

    public void listPage(int nowPage) {
        int startRecord = (nowPage - 1) * perPage * 3 + 1;
        int endRecord = nowPage * perPage * 3;
        if (nowPage != 1 && endRecord > countRecord) {
            endRecord = countRecord;
        }
        //将键值对数组添加到map中
        Map<String, String> headers = new HashMap<>();
        //设置cookie
        headers.put("Cookie", "JSESSIONID=9E632AE3D9146217C1A961144817F20C");
        //设置请求方式
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        //伪装浏览器
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        //设置请求数据
        ArrayList<NameValuePair> requestData = new ArrayList<>();
        requestData.add(new BasicHeader("col", "1"));
        requestData.add(new BasicHeader("webid", "22"));
        requestData.add(new BasicHeader("path", "/"));
        requestData.add(new BasicHeader("columnid", "57329"));
        requestData.add(new BasicHeader("sourceContentType", "1"));
        requestData.add(new BasicHeader("unitid", "84137"));
        requestData.add(new BasicHeader("webname", "济南市城乡交通运输局"));
        requestData.add(new BasicHeader("permissiontype", "0"));
        String PageUrl = "http://jnjtj.jinan.gov.cn/module/web/jpage/dataproxy.jsp?startrecord=" + startRecord + "&endrecord=" + endRecord + "&perpage=" + perPage;
        //发送请求
        String content = HttpClientUtils.sendPost(PageUrl, headers, requestData);
        Document document = Jsoup.parse(content.replace("<![CDATA[", "<table>").replace("]]>", "</table>"));

        Elements records = document.select("record");
        if (records != null) {
            for (Element record : records) {
                Elements listDate = record.select("td");
                //获取列表的a标签
                Elements listA = listDate.get(1).select("a");
                //获取a的href属性值
                String aHref = listA.attr("href");
                //将href处理一下可以当唯一ID
                String id = ReUtil.get("_(\\d+)\\.", aHref, 1);
                //如果已存在就跳过
                if (OracleUtils.existsById(id, "XIN_XI_INFO_TEST")) {
                    System.out.println("数据已存在");
                    continue;
                }
                //获取a标签的标题
                String listTitle = listA.text();
                //补充完整链接
                String detailLink = domain + aHref;
                //获取详情的时间并清理空格
                String pageTime = listDate.get(2).text().trim();
                XinXi xinXi = new XinXi();
                xinXi.setID(id);
                xinXi.setPAGE_TIME(pageTime);
                xinXi.setPAGE_TIME(pageTime);
                xinXi.setLIST_TITLE(listTitle);
                xinXi.setSOURCE_NAME("济南市城乡交通运输局");
                xinXi.setDETAIL_LINK(detailLink);

                //发送详情页请求
                String detailContent = HttpClientUtils.sendGet(detailLink);
                //处理内容页
                Document context = Jsoup.parse(detailContent);
                if (context != null) {
                    //补全iframes的src
                    Elements iframes = context.select("iframe");
                    iframes.forEach(iframe -> {
                        String iframeSrc = domain + iframe.attr("src");
                        iframe.attr("src", iframeSrc);
                    });
                    //获取内容时间
                    String detailTitle = context.select(".sp_title").text();
                    //获取详细内容html
                    String detailContentHtml = context.select("#zoom").outerHtml();
                    xinXi.setDETAIL_TITLE(detailTitle);
                    xinXi.setDETAIL_CONTENT(detailContentHtml);
                    xinXiDao.saveXinxi(xinXi);

                } else {
                    System.out.println("内容获取失败");
                }
            }
        } else {
            System.out.println("获取失败");
        }
        if (nowPage == 1) {
            //更新总条目
            countRecord = Integer.parseInt(document.select("totalrecord").text());
            //界面页数
            int totalPage = Integer.parseInt(document.select("totalpage").text());
            //获得实际页数
            countPage = totalPage / 3;
            if (countRecord % totalPage != 0) {
                countPage++;
            }
        }
        //翻页处理
        if (nowPage < countPage) {
            nowPage++;
            //翻页
            System.out.println("开始处理第" + nowPage + "页");
            listPage(nowPage);
        }
    }
}
