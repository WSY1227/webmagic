package com.example.webmagic.util;

import com.alibaba.fastjson.JSON;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @Description: HttpClient工具类(这里用一句话描述这个类的作用)
 */
public class HttpClientUtils {
    /*最大总连接数*/
    private static final int MAX_TOTAL_CONN = 600;
    /*单主机最大连接数*/
    private static final int MAX_CONN_PER_HOST = 300;
    private static final int SOCKET_TIMEOUT = 18000000;
    private static final int CONNECTION_TIMEOUT = 200;
    private static final int CONNECTION_MANAGER_TIMEOUT = 100;


    private static CloseableHttpClient httpclient;
    private static PoolingHttpClientConnectionManager connMrg;
    // 默认字符集
    private static String encoding = "UTF-8";

    private static Logger log = LoggerFactory.getLogger(HttpClientUtils.class);
    /**
     * 设置定时任务清理连接
     */
    private static final ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(2);

    static {
        init();
        destroyByJvmExit();
    }

    private static void destroyByJvmExit() {
        Thread hook = new Thread(() -> {
            try {
                httpclient.close();
            } catch (IOException e) {
                //
            }
        });
        Runtime.getRuntime().addShutdownHook(hook);
    }

    private static void init() {
        connMrg = new PoolingHttpClientConnectionManager();
        connMrg.setMaxTotal(MAX_TOTAL_CONN); // 最大连接数
        connMrg.setDefaultMaxPerRoute(MAX_CONN_PER_HOST);//每个路由基础的连接

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)//设置连接超时时间，单位毫秒。
                .setSocketTimeout(SOCKET_TIMEOUT)//请求获取数据的超时时间，单位毫秒
                .setConnectionRequestTimeout(CONNECTION_MANAGER_TIMEOUT)//设置从连接池获取连接超时时间，单位毫秒
                .build();
        httpclient = HttpClients.custom()
                .setConnectionManager(connMrg)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
    }

    /**
     * @param url      请求地址
     * @param headers  请求头
     * @param data     请求实体
     * @param encoding 字符集
     * @return String
     * @throws
     * @Title: sendPost
     * @Description: TODO(发送post请求)
     */
    public static String sendPost(String url, Map<String, String> headers, ArrayList<NameValuePair> data, String encoding) {
        log.info("进入post请求方法..." + encoding);
        log.info("请求入参：headers=" + JSON.toJSONString(headers));
        log.info("请求入参：URL= " + url);
        log.info("请求入参：data=" + JSON.toJSONString(data));
        // 创建Client
        CloseableHttpClient client = HttpClients.createDefault();
        // 创建HttpPost对象
        HttpPost httpPost = new HttpPost();
        try {
            // 设置请求地址
            httpPost.setURI(new URI(url));
            // 设置请求头
            if (headers != null) {
                Header[] allHeader = new BasicHeader[headers.size()];
                int i = 0;
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    allHeader[i] = new BasicHeader(entry.getKey(), entry.getValue());
                    i++;
                }
                httpPost.setHeaders(allHeader);
            }
            // 设置实体
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(data, "UTF-8");
            httpPost.setEntity(formEntity);
            // 发送请求,返回响应对象
            CloseableHttpResponse response = client.execute(httpPost);
            return parseData(response, encoding);

        } catch (Exception e) {
            log.error("发送post请求失败", e);
        } finally {
            httpPost.releaseConnection();
        }
        return null;
    }

    /**
     * @param url  请求地址
     * @param data 请求实体
     * @return String
     * @throws
     * @Title: sendPost
     * @Description: TODO(发送post请求 ， 请求数据默认使用json格式 ， 默认使用UTF - 8编码)
     */
    public static String sendPost(String url, ArrayList<NameValuePair> data) {
        // 设置默认请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");

        return sendPost(url, headers, data, encoding);
    }

    /**
     * @param url     请求地址
     * @param headers 请求头
     * @param data    请求实体
     * @return String
     * @throws
     * @Title: sendPost
     * @Description: TODO(发送post请求 ， 请求数据默认使用UTF - 8编码)
     */
    public static String sendPost(String url, Map<String, String> headers, ArrayList<NameValuePair> data) {
        return sendPost(url, headers, data, encoding);
    }

    /**
     * @param url      请求地址
     * @param params   请求参数
     * @param encoding 编码
     * @return String
     * @throws
     * @Title: sendGet
     * @Description: TODO(发送get请求)
     */
    public static String sendGet(String url, Map<String, Object> params, String encoding) {
        log.info("进入get请求方法...");
        log.info("请求入参：URL= " + url);
        log.info("请求入参：params=" + JSON.toJSONString(params));
        // 创建client
        CloseableHttpClient client = HttpClients.createDefault();
        // 创建HttpGet
        HttpGet httpGet = new HttpGet();
        httpGet.addHeader("Accept-Charset", "utf-8");
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            // 封装参数
            if (params != null) {
                for (String key : params.keySet()) {
                    builder.addParameter(key, params.get(key).toString());
                }
            }
            URI uri = builder.build();
            log.info("请求地址：" + uri);
            // 设置请求地址
            httpGet.setURI(uri);
            // 发送请求，返回响应对象
            CloseableHttpResponse response = client.execute(httpGet);
            return parseData(response, encoding);
        } catch (Exception e) {
            log.error("发送get请求失败", e);
            System.out.println(1);
        } finally {
            httpGet.releaseConnection();
        }
        return null;
    }

    /**
     * @param url    请求地址
     * @param params 请求参数
     * @return String
     * @throws
     * @Title: sendGet
     * @Description: TODO(发送get请求)
     */
    public static String sendGet(String url, Map<String, Object> params) {
        return sendGet(url, params, encoding);
    }

    /**
     * @param url 请求地址
     * @return String
     * @throws
     * @Title: sendGet
     * @Description: TODO(发送get请求)
     */
    public static String sendGet(String url) {
        return sendGet(url, null, encoding);
    }

    /**
     * 解析response
     *
     * @param response
     * @return
     * @throws Exception
     */
    public static String parseData(CloseableHttpResponse response, String encoding) throws Exception {
        // 获取响应状态
        int status = response.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_OK) {
            // 获取响应数据
            if (encoding != null) {
                return EntityUtils.toString(response.getEntity(), encoding);
            } else {
                return EntityUtils.toString(response.getEntity());
            }
        } else {
            log.error("响应失败，状态码：" + status);
        }
        System.out.println("no");
        return null;
    }
}
