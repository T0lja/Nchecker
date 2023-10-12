package dev.tolja.Utils;

import dev.tolja.Data.MojangAccount;
import dev.tolja.Data.ProxyInfo;
import org.fusesource.jansi.internal.Kernel32;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.fusesource.jansi.AnsiConsole;

import java.net.URL;
import java.net.URLConnection;

import static dev.tolja.Nchecker.configManager;

public class IOUtils {
    public static void saveResource(String resourcePath, boolean replace, File dataFolder) {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + dataFolder);
            } else {
                File outFile = new File(dataFolder, resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(dataFolder, resourcePath.substring(0, Math.max(lastIndex, 0)));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }
                try {
                    if (outFile.exists() && !replace) {
                        System.out.println("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
                    } else {
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    System.out.println("Could not save " + outFile.getName() + " to " + outFile);
                }
            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }

    public static InputStream getResource(String filename) {

        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        } else {
            try {
                URL url = IOUtils.class.getClassLoader().getResource(filename);
                if (url == null) {
                    return null;
                } else {
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    return connection.getInputStream();
                }
            } catch (IOException var4) {
                return null;
            }
        }
    }


    private static ArrayList<ProxyInfo> loadProxies() {
        ArrayList<ProxyInfo> results = new ArrayList<>();
        try {
//            InputStream resource = new FileInputStream("proxies.txt");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
//            String line;
//            while ((line = reader.readLine()) != null && !line.isEmpty()) {
//                String[] split = line.split(":");
//                if (split.length == 2) {
//                    results.add(new ProxyInfo(split[0], Integer.parseInt(split[1]), false, null, null, line));
//                } else if (split.length == 4) {
//                    results.add(new ProxyInfo(split[0], Integer.parseInt(split[1]), true, split[2], split[3], line));
//                }
//            }
            File f2 = new File("proxies.txt");
            final Scanner scanner = new Scanner(f2, "utf-8");
            while (scanner.hasNext()) {
                String raw = scanner.nextLine();
                String[] split = raw.split(":");
                boolean isSocks = false;
                if (configManager.getSettingsConfig().getProxyType() == Proxy.Type.SOCKS) {
                    isSocks = true;
                }
                if (split.length == 2) {
                    results.add(new ProxyInfo(split[0], Integer.parseInt(split[1]), false,  isSocks,null, null, raw));
                } else if (split.length == 4) {
                    results.add(new ProxyInfo(split[0], Integer.parseInt(split[1]), true, isSocks,split[2], split[3], raw));
                }
            }
            return results;
        } catch (Exception e) {
            return null;
        }
    }


    private static ArrayList<MojangAccount> loadComboList() {
        ArrayList<MojangAccount> results = new ArrayList<>();
        try {
//            InputStream resource = new FileInputStream("accounts.txt");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
//            String line;
//            while ((line = reader.readLine()) != null && !line.isEmpty()) {
//                String[] split = line.split(":");
//                results.add(new MojangAccount(split[0], split[1]));
//            }
            File f1 = new File("accounts.txt");
            final Scanner scanner = new Scanner(f1, "utf-8");
            while (scanner.hasNext()) {
                String[] split = scanner.nextLine().split(":");
                if (split.length == 2) {
                    results.add(new MojangAccount(split[0], split[1]));
                } else if (split.length == 3 && split[0].toString().contains("@")) {
                    results.add(new MojangAccount(split[0], split[1]));
                } else if (split.length == 3 && !split[0].toString().contains("@")) {
                    results.add(new MojangAccount(split[1], split[2]));
                }
            }
            return results;
        } catch (Exception e) {
            return null;
        }
    }


    private static ArrayList<String> loadAPIkeys() {
        ArrayList<String> results = new ArrayList<>();
        try {
            InputStream resource = new FileInputStream("apiKeys.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                results.add(line);
            }
            return results;
        } catch (Exception e) {
            return null;
        }
    }


    public static Object[] loadResources(boolean isLoadAccounts, boolean isLoadProxies, boolean isLoadAPIKeys) {
        // 切换Title
        Kernel32.SetConsoleTitle("NChecker | Loading resource");

        List<ProxyInfo> proxies = null;
        List<MojangAccount> combos = null;
        List<String> apikeys = null;

        if (isLoadProxies) {
            AnsiConsole.out().println("loading proxies list...");
            if ((proxies = loadProxies()) == null) {
                AnsiConsole.out().println("Failed to load proxies file... Please check if the file exists");
                AnsiConsole.out().println("Program exit");
                return null;
            }
            AnsiConsole.out().println("loaded proxies: " + proxies.size() + "\r\n");
        }

        if (isLoadAccounts) {
            AnsiConsole.out().println("loading accounts list...");
            if ((combos = loadComboList()) == null) {
                System.out.println(loadComboList());
                AnsiConsole.out().println("Failed to load accounts file... Please check if the file exists");
                AnsiConsole.out().println("Program exit");
                return null;
            }
            AnsiConsole.out().println("loaded accounts list: " + combos.size() + "\r\n");
        }

        if (isLoadAPIKeys) {
            AnsiConsole.out().println("loading apikeys list...");
            if ((apikeys = loadAPIkeys()) == null) {
                AnsiConsole.out().println("Failed to load apikeys file... Please check if the file exists");
                AnsiConsole.out().println("program exit");
                return null;
            }
            AnsiConsole.out().println("loaded apikeys: " + apikeys.size() + "\r\n");
        }

        return new Object[]{proxies, combos, apikeys};
    }
}
