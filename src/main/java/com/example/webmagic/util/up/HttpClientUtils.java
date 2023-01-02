package com.example.webmagic.util.up;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HttpClientUtils {

    private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    /**
     * @param url      请求链接
     * @param headers  请求头
     * @param encoding 响应编码
     * @return
     */
    public static String doGet(String url, Map<String, String> headers, String encoding) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return EntityUtils.toString(response.getEntity(), encoding);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String doGet(String url, Map<String, String> headers) {
        return doGet(url, headers, DEFAULT_ENCODING);
    }

    public static String doGet(String url) {
        return doGet(url, null, DEFAULT_ENCODING);
    }

    /**
     * @param url      请求链接
     * @param headers  请求头
     * @param params   请求参数
     * @param encoding 响应编码
     * @return
     */
    public static String doPost(String url, Map<String, String> headers, List<NameValuePair> params, String encoding) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params, encoding));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity(), encoding);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String doPost(String url, Map<String, String> headers, List<NameValuePair> params) {
        return doPost(url, headers, params, DEFAULT_ENCODING);
    }

    public static String doPost(String url, List<NameValuePair> params) {
        return doPost(url, null, params, DEFAULT_ENCODING);
    }
}