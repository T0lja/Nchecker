package dev.tolja.Configuration;

import dev.tolja.Utils.yaml.YamlMapper;
import lombok.Getter;

@Getter
public class ConfigManager {

    private SettingsConfig settingsConfig;

    public void loadConfigs() {
        settingsConfig = YamlMapper.loadYamlByClass(SettingsConfig.class);
    }

}
