package dev.tolja.Utils;

import dev.tolja.Data.MojangAccount;
import dev.tolja.Data.ProxyInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MailUtils {
    public static void isMFA(MojangAccount account, ProxyInfo proxy) {
        CheckFrommailcom(account,proxy);
        CheckFrommycom(account,proxy);
    }

    public static void CheckFrommycom(MojangAccount account, ProxyInfo proxy) {

        String results = "";
        try {
            URL url = new URL("https://aj-https.my.com/cgi-bin/auth?timezone=GMT%2B2&reqmode=fg&ajax_call=1&udid=16cbef29939532331560e4eafea6b95790a743e9&device_type=Tablet&mp=iOS¤t=MyCom&mmp=mail&os=iOS&md5_signature=6ae1accb78a8b268728443cba650708e&os_version=9.2&model=iPad%202%3B%28WiFi%29&simple=1&Login=" + account.getAccount() + "&ver=4.2.0.12436&DeviceID=D3E34155-21B4-49C6-ABCD-FD48BB02560D&country=GB&language=fr_FR&LoginType=Direct&Lang=en_FR&Password=" + account.getPassword() + "&device_vendor=Apple&mob_json=1&DeviceInfo=%7B%22Timezone%22%3A%22GMT%2B2%22%2C%22OS%22%3A%22iOS%209.2%22%2C?%22AppVersion%22%3A%224.2.0.12436%22%2C%22DeviceName%22%3A%22iPad%22%2C%22Device?%22%3A%22Apple%20iPad%202%3B%28WiFi%29%22%7D&device_name=iPad&");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            results = getStreamString(urlConnection.getInputStream());
            if (results.equals("Ok=1")) account.setMfa(true);
        } catch (Exception ex) {
            account.setMfa(false);
        }
    }

    public static void CheckFrommailcom(MojangAccount account, ProxyInfo proxy) {
        String results = "";
        try {
            URL url = new URL("https://oauth2.mail.com/token/grant_type=password&username=" + account.getAccount() + "&password=" + account.getAccount() + "&device_name=samsung+SM-G930L");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("Authorization", "Basic bWFpbGNvbV9tYWlsYXBwX2FuZHJvaWQ6a2luMmxTU2tVUXRRQ0NsWG9YZklOaEp1bUc2SmQwM0taNVdMN05KOQ==");
            urlConnection.addRequestProperty("User-Agent", "mailcom.android.androidmail/6.20.3 Dalvik/2.1.0 (Linux; U; Android 5.1.1; SM-G930L Build/NRD90M)");
            urlConnection.addRequestProperty("X-UI-APP", "mailcom.android.androidmail/6.20.3");
            urlConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=\"UTF-8\"");
            urlConnection.addRequestProperty("Host", "oauth2.mail.com");
            urlConnection.addRequestProperty("Connection", "Keep-Alive");
            urlConnection.addRequestProperty("Accept-Encoding", "gzip");
            results = getStreamString(urlConnection.getInputStream());
            if (results.contains("access_token")) account.setMfa(true);
        } catch (Exception ex) {
            account.setMfa(false);
        }
    }


    private static String getStreamString(InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();

            String temp;
            while((temp = reader.readLine()) != null) {
                stringBuilder.append(temp);
            }

            reader.close();
            return stringBuilder.toString();
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }
}
