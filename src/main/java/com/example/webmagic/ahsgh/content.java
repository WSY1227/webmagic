package com.example.webmagic.ahsgh;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class content implements PageProcessor {
    @Override
    public void process(Page page) {
        page.putField("context",page.getHtml().$(".article-conca.clearfix").toString());
    }

    private Site site = Site.me();

    @Override
    public Site getSite() {
        return site;
    }

    public void spider(String url){
        try {
            Spider.create(new content()).addUrl(url).addPipeline(new contPip()).thread(1).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
