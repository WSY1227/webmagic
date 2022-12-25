package com.example.webmagic.baidu;

import com.example.webmagic.ahsgh.MyPipeline;
import com.example.webmagic.ahsgh.timeTitle;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: ahsghSpider
 * @Description:
 * @author: XU
 * @date: 2022年12月24日 1:37
 **/

public class ahsghSpider {
    public Request getNextPageRequest(int pageNumber) {
        Request request = new Request("http://www.ahsgh.com/ahghjtweb/web/list");
        request.setMethod(HttpConstant.Method.POST);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        //将键值对数组添加到map中
        Map<String, Object> params = new HashMap<>();
        params.put("listPage", "list");
        params.put("intCurPage", pageNumber + "");
        params.put("intPageSize", "10");
        params.put("strColId", "20782f569264489f87995ad0773ff626");
        params.put("strWebSiteId", "4c5fcf57602b48a0acde5a4ef3ede48d");
        params.put("nowPage", pageNumber + "");
        //设置request参数
        request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        return request;
    }
    public static void main(String[] args) {
        ahsghSpider ahsghSpider = new ahsghSpider();

        //设置Post请求
        Request request = ahsghSpider.getNextPageRequest(1);
        // 开始执行
        Spider.create(new ahsghPageProcessor()).addRequest(request).thread(1).run();
    }
}
