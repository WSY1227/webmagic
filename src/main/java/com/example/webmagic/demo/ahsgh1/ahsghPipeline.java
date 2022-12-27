package com.example.webmagic.demo.ahsgh1;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * @ClassName: ahsghPipeline
 * @Description:
 * @author: XU
 * @date: 2022年12月24日 1:39
 **/

public class ahsghPipeline implements Pipeline {
    @Override
    public void process(ResultItems resultItems, Task task) {
        String title = resultItems.get("title");
        String time = resultItems.get("time");
        String contentHtml = resultItems.get("contentHtml");
        System.out.println("标题：" + title);
        System.out.println("时间：" + time);
        System.out.println(contentHtml);
    }
}
