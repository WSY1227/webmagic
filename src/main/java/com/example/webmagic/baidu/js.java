package com.example.webmagic.baidu;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;

/**
 * @ClassName: js
 * @Description:
 * @author: XU
 * @date: 2022年12月24日 13:50
 **/

public class js implements PageProcessor {
    @Override
    public void process(Page page) {
        List<String> all = page.getHtml().links().all();
        all.forEach(System.out::println);
    }

    private Site site = Site.me();

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new js()).addUrl("https://cn.bing.com/").thread(1).run();
    }
}
