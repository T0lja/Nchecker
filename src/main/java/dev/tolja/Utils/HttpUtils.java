package dev.tolja.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dev.tolja.Data.ProxyInfo;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import static dev.tolja.Nchecker.configManager;

public class HttpUtils {
    public static JSONObject doPost(String url, ProxyInfo proxy, String data, int timeout) {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;

        RequestConfig.Builder requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout);

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
        httpPost.setConfig(requestConfig.build());
        httpPost.setEntity(new StringEntity(data, "UTF-8"));
        httpPost.setHeader("Content-Type", "application/json;charset=utf8");

        try {
            if (proxy != null) {
                HttpHost prox = null;
                if (proxy.isSocks()) {
                    Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", new HTTPSocketFactory())
                            .register("https", new HTTPSSocketFactory()).build();
                    PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(reg);
                    httpClient = HttpClients.custom()
                            .setConnectionManager(connManager)
                            .build();
                    try {
                        InetSocketAddress socksaddr = new InetSocketAddress(proxy.getHost(), proxy.getPort());
                        HttpClientContext context = HttpClientContext.create();
                        context.setAttribute("socks.address", socksaddr);
                        response = httpClient.execute(httpPost, context);

                        if (response.getEntity() != null) {
                            return JSON.parseObject(EntityUtils.toString(response.getEntity()));
                        }
                    } finally {
                        try {
                            if (httpClient != null) {
                                httpClient.close();
                            }
                            if (response != null) {
                                response.close();
                            }
                        } catch (IOException e) {
                            if (configManager.getSettingsConfig().isShowConnectError()) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    prox = new HttpHost(proxy.getHost(), proxy.getPort(), "http");
                    if (proxy.getUsername() != null) {
                        CredentialsProvider provider = new BasicCredentialsProvider();
                        provider.setCredentials(new AuthScope(prox), new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
                        httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
                    }
                }
                requestConfig.setProxy(prox);
            }

            httpPost.setConfig(requestConfig.build());
            response = httpClient.execute(httpPost);
            if (response.getEntity() != null) {
                return JSON.parseObject(EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException | ParseException e) {
            if (configManager.getSettingsConfig().isShowConnectError()) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                if (configManager.getSettingsConfig().isShowConnectError()) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static JSONObject doGet(String url, ProxyInfo proxy, boolean raw, int timeout) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;

        RequestConfig.Builder requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout);

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
        httpGet.setConfig(requestConfig.build());
        try {
            if (proxy != null) {
                HttpHost prox = null;
                if (proxy.isSocks()) {
                    Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", new HTTPSocketFactory())
                            .register("https", new HTTPSSocketFactory()).build();
                    PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(reg);
                    httpClient = HttpClients.custom()
                            .setConnectionManager(connManager)
                            .build();
                    try {
                        InetSocketAddress socksaddr = new InetSocketAddress(proxy.getHost(), proxy.getPort());
                        HttpClientContext context = HttpClientContext.create();
                        context.setAttribute("socks.address", socksaddr);

                        response = httpClient.execute(httpGet, context);
                        if (response.getEntity() != null) {
                            if (raw) {
                                JSONObject tmp = new JSONObject();
                                tmp.put("raw", EntityUtils.toString(response.getEntity()));
                                return tmp;
                            }
                            return JSON.parseObject(EntityUtils.toString(response.getEntity()));
                        }

                    } finally {
                        try {
                            if (httpClient != null) {
                                httpClient.close();
                            }
                            if (response != null) {
                                response.close();
                            }
                        } catch (IOException e) {
                            if (configManager.getSettingsConfig().isShowConnectError()) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    prox = new HttpHost(proxy.getHost(), proxy.getPort(), "http");
                    if (proxy.isHasAuthentication()) {
                        CredentialsProvider provider = new BasicCredentialsProvider();
                        provider.setCredentials(new AuthScope(prox), new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
                        httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
                    }
                }
                requestConfig.setProxy(prox);
            }
            httpGet.setConfig(requestConfig.build());
            response = httpClient.execute(httpGet);
            if (response.getEntity() != null) {
                if (raw) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("raw", EntityUtils.toString(response.getEntity()));
                    return tmp;
                }
                return JSON.parseObject(EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException | ParseException e) {
            if (configManager.getSettingsConfig().isShowConnectError()) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                if (configManager.getSettingsConfig().isShowConnectError()) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    static class HTTPSocketFactory extends PlainConnectionSocketFactory {
        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }
    }

    static class HTTPSSocketFactory extends SSLConnectionSocketFactory {
        public HTTPSSocketFactory() {
            super(SSLContexts.createDefault(), getDefaultHostnameVerifier());
        }

        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }
    }
}
