package dev.tolja.Checkers.Modules;

import com.alibaba.fastjson.JSONObject;
import dev.tolja.Checkers.Checker;
import dev.tolja.Checkers.OutputManager;
import dev.tolja.Configuration.ConfigManager;
import dev.tolja.Data.ProxyInfo;
import dev.tolja.Nchecker;
import dev.tolja.Utils.HttpUtils;
import dev.tolja.Utils.IOUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.internal.Kernel32;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxiesChecker extends Checker {

    private final AtomicInteger checked = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger working = new AtomicInteger();

    @Override
    public void runChecker() {
        // 加载资源
        Object[] resources = IOUtils.loadResources(false, true, false);
        if (resources == null) return;
        List<ProxyInfo> proxies = (List<ProxyInfo>) resources[0];
        ConfigManager configManager = Nchecker.getConfigManager();
        OutputManager outputManager = new OutputManager(new File(".", "results/ProxiesChecker"), "4");

        // 准备开始
        long startTime = System.currentTimeMillis();
        Kernel32.SetConsoleTitle("NChecker | Starting thread...");
        ExecutorService threadPool = Executors.newFixedThreadPool(configManager.getSettingsConfig().getThreads());

        // 开始
        for (ProxyInfo proxyThis : proxies) {
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    int recheckCount = 0;
                    boolean success = false;
                    do {
                        try {
                            int timeout = configManager.getSettingsConfig().getProxyTimeout();
                            JSONObject proxyProfile = HttpUtils.doGet("https://status.mojang.com/check", proxyThis, true,timeout);

                            String res = proxyProfile.toString();
                            if (res.contains("textures.minecraft.net")) success = true;

                        } catch (Exception ignored) {
                        }
                    } while (!success && configManager.getSettingsConfig().getMaxRecheckCount() > recheckCount++);
                    if (success) {
                        working.getAndIncrement();
                        AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.GREEN).a("[Working] " + proxyThis.toString()).reset());
                        outputManager.writeLine("working-proxy.txt", proxyThis.toString());

                    } else {
                        AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.CYAN).a("[Failed] " + proxyThis.toString()).reset());
                        outputManager.writeLine("failed-proxy.txt", proxyThis.toString());
                        failed.getAndIncrement();
                    }
                    checked.getAndIncrement();
                }
            });
        }
        threadPool.shutdown();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        while (!threadPool.isTerminated()) {
            try {
                int time = (int) ((System.currentTimeMillis() - startTime) / 1000);
                Kernel32.SetConsoleTitle("NChecker"
                        + " | Checked: " + checked.get() + "/" + proxies.size() + " (" + String.format("%.2f", (double) checked.get() / proxies.size() * 100) + "%)"
                        + " | Working: " + working.get() + " - " + "Failed: " + failed.get() + "| Cpm: " + (int) Math.ceil((double) checked.get() / time * 60)
                        + " | Elapsed: " + dateFormat.format(new Date(System.currentTimeMillis() - startTime - (8 * 60 * 60 * 1000))));
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
        Kernel32.SetConsoleTitle("NChecker | Ver " + Nchecker.VERSION);
        AnsiConsole.out.println(Ansi.ansi().fg(Ansi.Color.CYAN).a(
                "\r\nAll done.\r\n"
                        + "Working: " + working.get() + " - " + "Failed: " + failed.get()
                        + " | Elapsed: " + dateFormat.format(new Date(System.currentTimeMillis() - startTime - (8 * 60 * 60 * 1000)))
        ).reset());
    }
}
