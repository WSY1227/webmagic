package com.example.webmagic.demo.pc;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class MyProcessor implements PageProcessor {
    private final Site site = Site.me();

    //负责解析页面
    public void process(Page page) {
        //解析page并将解析结果存入ResultItems里面（map构造）
        page.putField("li", page.getHtml().css("body > div.xing_vb > ul > li").all());
    }
    //site
    public Site getSite() {
        return site;
    }
    //主函数，执行爬虫

    public static void main(String[] args) {
        Spider.create(new MyProcessor())
                //设置地址
                .addUrl("http://okzyw.com/?m=vod-type-id-1.html")
                //调取执行方法
                .run();
    }
}