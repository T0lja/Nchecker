package dev.tolja.Configuration;

import dev.tolja.Utils.yaml.annotations.YamlEntity;
import dev.tolja.Utils.yaml.annotations.YamlPath;
import lombok.Getter;
import soterdev.SoterObfuscator;

import java.net.Proxy;

@Getter
@YamlEntity(filePath = "settings.yml")

@SoterObfuscator.Obfuscation(flags = "+native,+encstr")
public class SettingsConfig {

    @YamlPath("checker.username")
    private String username;

    @YamlPath("checker.password")
    private String password;

    @YamlPath("checker.threads")
    private int threads;

    @YamlPath("checker.timeout")
    private int timeout;

    @YamlPath("checker.proxy-type")
    private Proxy.Type proxyType;

    @YamlPath("checker.recheck")
    private int maxRecheckCount;

    @YamlPath("checker.reconnect")
    private int maxReconnectCount;

    @YamlPath("checker.save-failed")
    private boolean saveFailed;

    @YamlPath("checker.print-failed")
    private boolean printFailed;

    @YamlPath("checker.cape-check")
    private boolean capeCheck;

    @YamlPath("checker.mfa-check")
    private boolean mfaCheck;

    @YamlPath("checker.hypixel-check")
    private boolean HypixelCheck;

    @YamlPath("checker.leveled-check")
    private boolean checkLeveled;

    @YamlPath("checker.ranked-check")
    private boolean checkRanked;

    @YamlPath("checker.minlevel")
    private int minLevel;

    @YamlPath("checker.maxlevel")
    private int maxLevel;

    @YamlPath("checker.UHC-check")
    private boolean checkUHC;

    @YamlPath("checker.MW-check")
    private boolean checkMW;

    @YamlPath("checker.skyblock-check")
    private boolean checkSkyblock;

    @YamlPath("checker.proxy-timeout")
    private int proxyTimeout;

    @YamlPath("checker.uhc-star")
    private int uhcStar;

    @YamlPath("checker.hypixel-json")
    private boolean hypixelJson;

    @YamlPath("checker.show-connect-error")
    private boolean showConnectError;
}
