package dev.tolja;

import com.alibaba.fastjson.JSONObject;
import dev.tolja.Checkers.Checker;
import dev.tolja.Checkers.Modules.*;
import dev.tolja.Configuration.ConfigManager;
import dev.tolja.Utils.EncryptUtils;
import dev.tolja.Utils.HardwareUtils;
import dev.tolja.Utils.HttpUtils;
import dev.tolja.Utils.StringUtils;
import io.jsonwebtoken.Claims;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.internal.Kernel32;
import soterdev.SoterObfuscator;

import java.util.HashMap;
import java.util.Scanner;

public class Nchecker {


    public static final String VERSION = "3.0";
    public static final String API_URL = "http://39.103.145.89:60001";

    public static ConfigManager configManager;
    public static String auth;

    @SoterObfuscator.Entry
    @SoterObfuscator.Obfuscation(flags = "+native")
    public static void main(String[] args) {

        // 输出标题 - 设置Title
        Kernel32.SetConsoleTitle("NChecker | Ver " + VERSION);

        // 输出标题 - 打印Name
        AnsiConsole.systemInstall();
        AnsiConsole.out.println(Ansi.ansi().fgBrightCyan().a(
                "  _   _      _               _             \n" +
                        " | \\ | |    | |             | |            \n" +
                        " |  \\| | ___| |__   ___  ___| | _____ _ __ \n" +
                        " | . ` |/ __| '_ \\ / _ \\/ __| |/ / _ \\ '__|\n" +
                        " | |\\  | (__| | | |  __/ (__|   <  __/ |   \n" +
                        " |_| \\_|\\___|_| |_|\\___|\\___|_|\\_\\___|_|   \n" +
                        "                                           \n" +
                        "                                           \r\n\r\nDev: Dertarer_NPC"
        ).reset());

        // 加载配置
        configManager = new ConfigManager();
        configManager.loadConfigs();

        AnsiConsole.out.println(Ansi.ansi().fgBrightCyan().a("\r\n[+]Checking version").reset());
        if (!checkUpdate()) {
            System.exit(0);
            return;
        }

        AnsiConsole.out.println(Ansi.ansi().fgBrightCyan().a("\r\n[+]Checking license").reset());
        if (!checkLicense()) {
            System.exit(0);
            return;
        }

        // 选择模块 - 输出选项
        AnsiConsole.out().println(Ansi.ansi().fgBrightCyan().a("\r\n[1] Normal Checker\r\n[2] Ban Checker\r\n[3] Skyblock Checker\r\n[4] Proxies Checker\r\n[5] All Checker\r\n[6] Hits Checker\r\n\r\n> "));

        // 选择模块 - 获取输入项
        Scanner scanner = new Scanner(System.in);
        String text = scanner.next();
        Checker selectedModule = null;

        // 选择模块 - 判断输入项
        if (StringUtils.isNumeric(text)) {
            if (Integer.parseInt(text) == 1) {
                selectedModule = new NormalChecker();
            } else if (Integer.parseInt(text) == 2) {
                selectedModule = new BanChecker();
            } else if (Integer.parseInt(text) == 3) {
                selectedModule = new SkyblockChecker();
            } else if (Integer.parseInt(text) == 4) {
                selectedModule = new ProxiesChecker();
            } else if (Integer.parseInt(text) == 5) {
                selectedModule = new HypixelChecker();
            } else if (Integer.parseInt(text) == 6) {
                selectedModule = new HitsChecker();
            }
        }

        // 选择模块 - 为空退出 正常开始
        if (selectedModule != null) {
            selectedModule.runChecker();
        } else {
            AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nExiting...").reset());
            System.exit(0);
        }
    }

    // Getter
    public static ConfigManager getConfigManager() {
        return configManager;
    }

    //检查更新
    @SoterObfuscator.Obfuscation(flags = "+native")
    private static boolean checkUpdate() {
        try {
            JSONObject res = HttpUtils.doGet(API_URL + "/npc/dataLookup/version", null, false, 10000);
            if (!res.getBoolean("success")) {
                AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nVersion data err, pls contact npc.").reset());
                AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nExiting...").reset());
                return false;
            } else {
                if (!res.getString("version").equals(VERSION)) {
                    AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nOutdated version " + VERSION + " , now at " + res.getString("version")).reset());
                    AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nExiting...").reset());
                    return false;
                }
            }
            return true;
        } catch (Exception ignored) {
            AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nVersion data err, pls contact npc.").reset());
            AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nExiting...").reset());
            return false;
        }
    }

    //检查密钥
    @SoterObfuscator.Obfuscation(flags = "+native")
    private static boolean checkLicense() {
        try {
            JSONObject res = HttpUtils.doPost(API_URL + "/npc/auth", null, new JSONObject(new HashMap<>() {{
                put("loginName", configManager.getSettingsConfig().getUsername());
                put("password", configManager.getSettingsConfig().getPassword());
                put("hwid", HardwareUtils.getHWID());
            }}).toString(), 10000);
            //System.out.println(res);
            if (!res.getBoolean("success")) {
                AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nInvalid license.Reason => " + res.getString("reason")).reset());
                AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nExiting...").reset());
                return false;
            } else {
                Claims cl = EncryptUtils.verifyJwt(res.getString("token"));
                if (cl == null) {
                    AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nInvalid license.Reason => Token expired.").reset());
                    AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nExiting...").reset());
                    return false;
                }
                if (!HardwareUtils.getHWID().equalsIgnoreCase(cl.get("hwid", String.class))) {
                    AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nInvalid license.Reason => Token invalid.").reset());
                    AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nExiting...").reset());
                    return false;
                }
                AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.GREEN).a("\r\nDear " + cl.get("nickname", String.class) + ", welcome to use Nchecker.").reset());
                return true;
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nInvalid license.Reason => Req error.").reset());
            AnsiConsole.out().println(Ansi.ansi().fg(Ansi.Color.RED).a("\r\nExiting...").reset());
            return false;
        }
    }
}
