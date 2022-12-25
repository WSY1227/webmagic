package com.example.webmagic.jyj;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;

/**
 * @ClassName: jyj
 * @Description:
 * @author: XU
 * @date: 2022年12月25日 14:11
 **/

public class jyj implements PageProcessor {
    private int PageCount = 0;

    @Override
    public void process(Page page) {
        if (PageCount == 0) {
            PageCount = Integer.parseInt(page.getHtml().$("script").regex("'page-div',([0-9]+),").toString());
            System.out.println("共：" + PageCount + "页");
            for (int i = 2; i <= PageCount; i++) {
                String url = "https://jyj.abazhou.gov.cn/abzjyj/c100004/common_list_" + i + ".shtml";
                System.out.println(url);
                page.addTargetRequest(url);
            }
        }

        if (page.getUrl().regex("https://jyj.abazhou.gov.cn/abzjyj/c100004/common_list_\\d+\\.shtml").match()) {
            List<String> all = page.getHtml().$("ul.list-li li a").all();
            all.forEach(System.out::println);
        }

    }

    @Override
    public Site getSite() {
        return Site.me();
    }

    public static void main(String[] args) {
        Spider.create(new jyj()).addUrl("https://jyj.abazhou.gov.cn/abzjyj/c100004/common_list.shtml").thread(1).run();
    }
}
