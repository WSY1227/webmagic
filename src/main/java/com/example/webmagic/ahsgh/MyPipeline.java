package com.example.webmagic.ahsgh;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.List;

public class MyPipeline implements Pipeline {

    public void process(ResultItems resultItems, Task task) {
        ArrayList<String> fl = resultItems.get("fl");
        ArrayList<String> fr = resultItems.get("fr");
        ArrayList<String> href = resultItems.get("href");
        content content = new content();
        String url = "http://www.ahsgh.com/ahghjtweb/web/view?strId=[value]&strColId=[value]&strWebSiteId=[value]";
        for (int i = 0, j = 0; i < fl.size() && j < href.size(); i++) {
            System.out.println("标题:" + fl.get(i));
            String newUrl = url;
            for (int m = 0; m < 3; m++) {
                newUrl = newUrl.replaceFirst("\\[value\\]", href.get(j++));
            }
            System.out.println("url:" + newUrl);
            content.spider(newUrl);
            System.out.println("时间:" + fr.get(i));
        }
    }
}
