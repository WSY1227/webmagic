package com.example.webmagic.demo.ahsgh;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class contPip implements Pipeline {
    @Override
    public void process(ResultItems resultItems, Task task) {
        String context = resultItems.get("context");
        int startIndex = context.indexOf("<!-- 正文内容 -->")+"<!-- 正文内容 -->".length();
        int endIndex = context.indexOf("<!-- 文后 -->");
        if (startIndex != -1 && endIndex != -1) {
            String contentHtml = context.substring(startIndex, endIndex);
            System.out.println(contentHtml);
        }
    }
}
