package dev.tolja.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dev.tolja.Data.ProxyInfo;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CapeUtils {
    public static boolean checkOf(String username) {
        try {
            URL url = new URL("http://s.optifine.net/capes/" + username + ".png");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            if (connection.getResponseCode() == 200) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean checkMinecon(String uuid, ProxyInfo proxy) {
        JSONObject MineconProfile = HttpUtils.doGet("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid, proxy, false,10000);
        if (MineconProfile.containsKey("properties")) {
            JSONObject res1 = MineconProfile.getJSONArray("properties").getJSONObject(0);
            if (res1.containsKey("value")) {
                String res2 = res1.getString("value");
                String res3 = new String(Base64.getDecoder().decode(res2), StandardCharsets.UTF_8);
                JSONObject res4 = JSON.parseObject(res3);
                if (res4.containsKey("textures")) {
                    JSONObject res5 = res4.getJSONObject("textures");
                    if (res5.containsKey("CAPE")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
