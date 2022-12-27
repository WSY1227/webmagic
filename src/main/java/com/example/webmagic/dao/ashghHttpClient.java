package com.example.webmagic.dao;

import cn.hutool.core.util.ReUtil;
import com.example.webmagic.entity.XinXi;
import com.example.webmagic.util.HttpClientUtils;
import com.example.webmagic.util.OracleUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ashghHttpClient {
    private String PageUrl = "http://www.ahsgh.com/ahghjtweb/web/list";
    private String contentOutUrl = "http://www.ahsgh.com/ahghjtweb/web/view?strId={0}&strColId={1}&strWebSiteId={2}";
    //最大页
    private int maxNum = 1;

    public static void main(String[] args) throws IOException {
        ashghHttpClient ashghHttpClient = new ashghHttpClient();
        ashghHttpClient.listPage(1);
    }

    public void listPage(int nowNum) {
        //将键值对数组添加到map中
        Map<String, String> headers = new HashMap<>();
        //设置请求方式
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        //伪装浏览器
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        //设置请求数据
        ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
        data.add(new BasicHeader("listPage", "list"));
        data.add(new BasicHeader("intCurPage", nowNum + ""));
        data.add(new BasicHeader("intPageSize", "10"));
        data.add(new BasicHeader("strColId", "20782f569264489f87995ad0773ff626"));
        data.add(new BasicHeader("strWebSiteId", "4c5fcf57602b48a0acde5a4ef3ede48d"));
        data.add(new BasicHeader("nowPage", nowNum + ""));
        //发送请求
        String content = HttpClientUtils.sendPost(PageUrl, headers, data);
        Document document = Jsoup.parse(content);
        //获取所有li标签
        Elements liList = document.select("ul.tab01open.tab01tca>li");
        //获取所有列表标题
        for (int i = 0; i < liList.size(); i++) {
            String href = liList.get(i).select("a").attr("href");
            //拿到所有''之间的内容
            List<String> hrefs = ReUtil.findAll("'(.*?)'", href, 1);
            String strId = "strId=" + hrefs.get(0);
            //根据strId判断当前值是否存在
            if (OracleUtils.existsById(strId, "XIN_XI_INFO_TEST")) {
                System.out.println("已存在");
                continue;
            }
            //拼接url
            String detailLink = MessageFormat.format(contentOutUrl, hrefs.get(0), hrefs.get(1), hrefs.get(2));
            //封装
            XinXi xinXi = new XinXi();
            xinXi.setID(strId);
            //封装列表标题
            xinXi.setLIST_TITLE(liList.select(".fl").first().text());
            //封装详情链接
            xinXi.setDETAIL_LINK(detailLink);
            //封装列表时间
            xinXi.setPAGE_TIME(liList.select("i.fr").first().text());
            //发送详情页请求
            String detailContent = HttpClientUtils.sendGet(detailLink);
            //处理内容页
            Document detailDocument = Jsoup.parse(detailContent);
            Element context = detailDocument.select(".article-conca.clearfix").first();
            if (context != null) {
                Elements imgList = context.select("img");
                for (Element img : imgList) {
                    String src = img.attr("src");
                    if (src.startsWith("/")) {
                        src = "https://www.ahsgh.com" + src;
                    } else {
                        System.out.println("");
                    }
                    img.attr("src", src);
                }
                Elements aList = context.select("a");
                for (Element a : aList) {
                    String detailHref = a.attr("href");
                    if (detailHref.startsWith("/")) {
                        detailHref = "https://www.ahsgh.com" + detailHref;
                        a.attr("href", detailHref);
                    }
                }
                //封装内容
                xinXi.setDETAIL_CONTENT(ReUtil.get("<p></p>([\\s\\S]*?)<p></p>", context.outerHtml(), 1));
                xinXi.setDETAIL_TITLE(context.select("h2").first().text());
                //保存数据库
                new XinXiDao().saveXinxi(xinXi);
            } else {
                System.out.println("未获能正确获取正文内容");
            }
        }
        if (nowNum == 1) {
            maxNum = Integer.parseInt(document.select(".pagenub").last().text().substring(4));
        }
        if (nowNum < maxNum) {
            nowNum++;
            //翻页处理
            listPage(nowNum);
        }
    }

}
