package dev.tolja.Utils;

import dev.tolja.Data.ProxyInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;

import static dev.tolja.Nchecker.configManager;

public class ProxiesAPI {
    public static ArrayList<ProxyInfo> getProxies() {
        ArrayList<ProxyInfo> results = new ArrayList<>();
        try {
            URL url = new URL("");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((br.readLine()) != null) {
                boolean isSocks = false;
                if (configManager.getSettingsConfig().getProxyType() == Proxy.Type.SOCKS) {
                    isSocks = true;
                }
                String[] split = br.readLine().split(":");
                results.add(new ProxyInfo(split[0], Integer.parseInt(split[1]),false,  isSocks,null, null, br.readLine()));
            }
        } catch (IOException e) {
            return null;
        }
        return results;
    }
}
