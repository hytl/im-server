package com.hytl.mserver.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;

@Slf4j
public class HttpClientUtil {

    public static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_VALUE_CONTENT_TYPE_JSON = "application/json";

    // 默认超时时间（5 秒）
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    // 默认请求头（Content-Type: application/json）
    private static final Map<String, String> DEFAULT_HEADERS;

    static {
        DEFAULT_HEADERS = Map.of(HEADER_KEY_CONTENT_TYPE, HEADER_VALUE_CONTENT_TYPE_JSON);
    }

    // HTTP 方法枚举
    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

    // 创建信任所有证书的 HttpClient
    private static HttpClient createTrustAllHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        // 创建信任所有证书的 TrustManager
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        // 初始化 SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // 创建 HttpClient
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(new SSLParameters())
                .connectTimeout(DEFAULT_TIMEOUT) // 设置默认连接超时时间
                .build();
    }

    // 发送 HTTP 请求（使用默认超时时间和默认请求头）
    public static <T> T sendRequest(HttpMethod method, String url, Class<T> clazz) throws Exception {
        return sendRequest(method, url, DEFAULT_HEADERS, null, clazz, DEFAULT_TIMEOUT);
    }

    // 发送 HTTP 请求（使用默认请求头，自定义超时时间）
    public static <T> T sendRequest(HttpMethod method, String url, Class<T> clazz, Duration timeout) throws Exception {
        return sendRequest(method, url, DEFAULT_HEADERS, null, clazz, timeout);
    }

    // 发送 HTTP 请求（使用默认超时时间，自定义请求头）
    public static <T> T sendRequest(HttpMethod method, String url, Map<String, String> headers, Class<T> clazz) throws Exception {
        return sendRequest(method, url, headers, null, clazz, DEFAULT_TIMEOUT);
    }

    // 发送 HTTP 请求（自定义请求头和超时时间）
    public static <T> T sendRequest(HttpMethod method, String url, Map<String, String> headers, Object requestBody, Class<T> clazz, Duration timeout) throws Exception {
        // 创建 HttpClient
        HttpClient client = createTrustAllHttpClient();

        // 创建 HttpRequest.Builder
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout); // 设置请求超时时间

        // 设置请求方法
        switch (method) {
            case GET:
                requestBuilder.GET();
                break;
            case POST:
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(requestBody)));
                break;
            case PUT:
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(requestBody)));
                break;
            case DELETE:
                requestBuilder.DELETE();
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        // 添加请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        // 发送请求并返回响应
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        // 如果返回类型是 String，直接返回响应体
        if (clazz == String.class) {
            return (T) response.body();
        }

        // 将响应体反序列化为指定类型
        return JSON.parseObject(response.body(), clazz);
    }

    // 发送 HTTP 请求（使用默认超时时间和默认请求头）
    public static <T> T sendRequest(HttpMethod method, String url, TypeReference<T> typeReference) throws Exception {
        return sendRequest(method, url, DEFAULT_HEADERS, null, typeReference, DEFAULT_TIMEOUT);
    }

    // 发送 HTTP 请求（使用默认请求头，自定义超时时间）
    public static <T> T sendRequest(HttpMethod method, String url, TypeReference<T> typeReference, Duration timeout) throws Exception {
        return sendRequest(method, url, DEFAULT_HEADERS, null, typeReference, timeout);
    }

    // 发送 HTTP 请求（使用默认超时时间，自定义请求头）
    public static <T> T sendRequest(HttpMethod method, String url, Map<String, String> headers, TypeReference<T> typeReference) throws Exception {
        return sendRequest(method, url, headers, null, typeReference, DEFAULT_TIMEOUT);
    }

    // 发送 HTTP 请求（自定义请求头和超时时间）
    public static <T> T sendRequest(HttpMethod method, String url, Map<String, String> headers, Object requestBody, TypeReference<T> typeReference, Duration timeout) throws Exception {
        // 创建 HttpClient
        HttpClient client = createTrustAllHttpClient();

        // 创建 HttpRequest.Builder
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout); // 设置请求超时时间

        // 设置请求方法
        switch (method) {
            case GET:
                requestBuilder.GET();
                break;
            case POST:
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(requestBody)));
                break;
            case PUT:
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(requestBody)));
                break;
            case DELETE:
                requestBuilder.DELETE();
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        // 添加请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        // 发送请求并返回响应
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        // 如果返回类型是 String，直接返回响应体
        String respBody = response.body();
        if (typeReference.getType() == String.class) {
            return (T) respBody;
        }

        // 将响应体反序列化为指定类型
        T parseObject = null;
        try {
            parseObject = JSON.parseObject(respBody, typeReference);
        } catch (JSONException e) {
            log.error("JSONException respBody:{}", respBody);
            throw e;
        }

        return parseObject;
    }

    // GET 请求（使用默认请求头和默认超时时间）
    public static <T> T get(String url, Class<T> clazz) throws Exception {
        return sendRequest(HttpMethod.GET, url, clazz);
    }

    // GET 请求（使用默认请求头，自定义超时时间）
    public static <T> T get(String url, Class<T> clazz, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.GET, url, clazz, timeout);
    }

    // GET 请求（自定义请求头，使用默认超时时间）
    public static <T> T get(String url, Map<String, String> headers, Class<T> clazz) throws Exception {
        return sendRequest(HttpMethod.GET, url, headers, clazz);
    }

    // GET 请求（自定义请求头和超时时间）
    public static <T> T get(String url, Map<String, String> headers, Class<T> clazz, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.GET, url, headers, null, clazz, timeout);
    }

    // GET 请求（使用默认请求头和默认超时时间）
    public static <T> T get(String url, TypeReference<T> typeReference) throws Exception {
        return sendRequest(HttpMethod.GET, url, typeReference);
    }

    // GET 请求（使用默认请求头，自定义超时时间）
    public static <T> T get(String url, TypeReference<T> typeReference, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.GET, url, typeReference, timeout);
    }

    // GET 请求（自定义请求头，使用默认超时时间）
    public static <T> T get(String url, Map<String, String> headers, TypeReference<T> typeReference) throws Exception {
        return sendRequest(HttpMethod.GET, url, headers, typeReference);
    }

    // GET 请求（自定义请求头和超时时间）
    public static <T> T get(String url, Map<String, String> headers, TypeReference<T> typeReference, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.GET, url, headers, null, typeReference, timeout);
    }

    // POST 请求（使用默认请求头和默认超时时间）
    public static <T> T post(String url, Object requestBody, Class<T> clazz) throws Exception {
        return sendRequest(HttpMethod.POST, url, DEFAULT_HEADERS, requestBody, clazz, DEFAULT_TIMEOUT);
    }

    // POST 请求（使用默认请求头，自定义超时时间）
    public static <T> T post(String url, Object requestBody, Class<T> clazz, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.POST, url, DEFAULT_HEADERS, requestBody, clazz, timeout);
    }

    // POST 请求（自定义请求头，使用默认超时时间）
    public static <T> T post(String url, Map<String, String> headers, Object requestBody, Class<T> clazz) throws Exception {
        return sendRequest(HttpMethod.POST, url, headers, requestBody, clazz, DEFAULT_TIMEOUT);
    }

    // POST 请求（自定义请求头和超时时间）
    public static <T> T post(String url, Map<String, String> headers, Object requestBody, Class<T> clazz, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.POST, url, headers, requestBody, clazz, timeout);
    }

    // POST 请求（使用默认请求头和默认超时时间）
    public static <T> T post(String url, Object requestBody, TypeReference<T> typeReference) throws Exception {
        return sendRequest(HttpMethod.POST, url, DEFAULT_HEADERS, requestBody, typeReference, DEFAULT_TIMEOUT);
    }

    // POST 请求（使用默认请求头，自定义超时时间）
    public static <T> T post(String url, Object requestBody, TypeReference<T> typeReference, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.POST, url, DEFAULT_HEADERS, requestBody, typeReference, timeout);
    }

    // POST 请求（自定义请求头，使用默认超时时间）
    public static <T> T post(String url, Map<String, String> headers, Object requestBody, TypeReference<T> typeReference) throws Exception {
        return sendRequest(HttpMethod.POST, url, headers, requestBody, typeReference, DEFAULT_TIMEOUT);
    }

    // POST 请求（自定义请求头和超时时间）
    public static <T> T post(String url, Map<String, String> headers, Object requestBody, TypeReference<T> typeReference, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.POST, url, headers, requestBody, typeReference, timeout);
    }

    // PUT 请求（使用默认请求头和默认超时时间）
    public static <T> T put(String url, Object requestBody, Class<T> clazz) throws Exception {
        return sendRequest(HttpMethod.PUT, url, DEFAULT_HEADERS, requestBody, clazz, DEFAULT_TIMEOUT);
    }

    // PUT 请求（使用默认请求头，自定义超时时间）
    public static <T> T put(String url, Object requestBody, Class<T> clazz, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.PUT, url, DEFAULT_HEADERS, requestBody, clazz, timeout);
    }

    // PUT 请求（自定义请求头，使用默认超时时间）
    public static <T> T put(String url, Map<String, String> headers, Object requestBody, Class<T> clazz) throws Exception {
        return sendRequest(HttpMethod.PUT, url, headers, requestBody, clazz, DEFAULT_TIMEOUT);
    }

    // PUT 请求（自定义请求头和超时时间）
    public static <T> T put(String url, Map<String, String> headers, Object requestBody, Class<T> clazz, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.PUT, url, headers, requestBody, clazz, timeout);
    }

    // PUT 请求（使用默认请求头和默认超时时间）
    public static <T> T put(String url, Object requestBody, TypeReference<T> typeReference) throws Exception {
        return sendRequest(HttpMethod.PUT, url, DEFAULT_HEADERS, requestBody, typeReference, DEFAULT_TIMEOUT);
    }

    // PUT 请求（使用默认请求头，自定义超时时间）
    public static <T> T put(String url, Object requestBody, TypeReference<T> typeReference, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.PUT, url, DEFAULT_HEADERS, requestBody, typeReference, timeout);
    }

    // PUT 请求（自定义请求头，使用默认超时时间）
    public static <T> T put(String url, Map<String, String> headers, Object requestBody, TypeReference<T> typeReference) throws Exception {
        return sendRequest(HttpMethod.PUT, url, headers, requestBody, typeReference, DEFAULT_TIMEOUT);
    }

    // PUT 请求（自定义请求头和超时时间）
    public static <T> T put(String url, Map<String, String> headers, Object requestBody, TypeReference<T> typeReference, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.PUT, url, headers, requestBody, typeReference, timeout);
    }

    // DELETE 请求（使用默认请求头和默认超时时间）
    public static <T> T delete(String url, Class<T> clazz) throws Exception {
        return sendRequest(HttpMethod.DELETE, url, clazz);
    }

    // DELETE 请求（使用默认请求头，自定义超时时间）
    public static <T> T delete(String url, Class<T> clazz, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.DELETE, url, clazz, timeout);
    }

    // DELETE 请求（自定义请求头，使用默认超时时间）
    public static <T> T delete(String url, Map<String, String> headers, Class<T> clazz) throws Exception {
        return sendRequest(HttpMethod.DELETE, url, headers, clazz);
    }

    // DELETE 请求（自定义请求头和超时时间）
    public static <T> T delete(String url, Map<String, String> headers, Class<T> clazz, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.DELETE, url, headers, null, clazz, timeout);
    }

    // DELETE 请求（使用默认请求头和默认超时时间）
    public static <T> T delete(String url, TypeReference<T> typeReference) throws Exception {
        return sendRequest(HttpMethod.DELETE, url, typeReference);
    }

    // DELETE 请求（使用默认请求头，自定义超时时间）
    public static <T> T delete(String url, TypeReference<T> typeReference, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.DELETE, url, typeReference, timeout);
    }

    // DELETE 请求（自定义请求头，使用默认超时时间）
    public static <T> T delete(String url, Map<String, String> headers, TypeReference<T> typeReference) throws Exception {
        return sendRequest(HttpMethod.DELETE, url, headers, typeReference);
    }

    // DELETE 请求（自定义请求头和超时时间）
    public static <T> T delete(String url, Map<String, String> headers, TypeReference<T> typeReference, Duration timeout) throws Exception {
        return sendRequest(HttpMethod.DELETE, url, headers, null, typeReference, timeout);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(get("https://www.baidu.com", String.class));
    }
}