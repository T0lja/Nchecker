package dev.tolja.Utils.yaml;

import dev.tolja.Utils.IOUtils;
import dev.tolja.Utils.yaml.annotations.YamlEntity;
import dev.tolja.Utils.yaml.annotations.YamlPath;
import dev.tolja.Utils.yaml.file.YamlConfiguration;

import java.io.*;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Yaml 转换工具
 * <p>
 * 可以自动扫描注解，将配置文件直接读取到 配置bean 中
 */
public class YamlMapper {

    private static final File dataFolder = new File(".");

    public static <T> T loadYamlByClass(Class<T> type) {
        // Check whether this class is a configuration entity class
        if (!type.isAnnotationPresent(YamlEntity.class)) {
            throw new IllegalArgumentException("the class is not a yaml entity class");
        }
        // Check if the plugin folder is created
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        // Generate configuration file
        String filePath = type.getAnnotation(YamlEntity.class).filePath();
        File file = new File(dataFolder, filePath);
        if (!file.exists()) {
            IOUtils.saveResource(filePath, false, dataFolder);
        }
        // Load configuration file
        YamlConfiguration yamlConfiguration = load(file);
        // Create a config entity object through reflection
        T object = null;
        try {
            object = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        // Scan all fields and load the value based on their Annotation
        for (Field field : type.getDeclaredFields()) {
            // No YamlPath annotation
            if (!field.isAnnotationPresent(YamlPath.class)) {
                continue;
            }
            // Get the path from the annotation
            String path = field.getAnnotation(YamlPath.class).value();
            if (yamlConfiguration.contains(path)) {
                parseValueToField(object, field, yamlConfiguration, path);
            } else {
                // TODO?
                // Generate default value?
            }
        }
        return object;
    }

    private static <T> void parseValueToField(T object, Field field, YamlConfiguration configuration, String path) {
        Class<?> type = field.getType();
        field.setAccessible(true);
        try {
            if (type == int.class || type == Integer.TYPE) {
                field.set(object, configuration.getInt(path));

            } else if (type == long.class || type == Long.TYPE) {
                field.set(object, configuration.getLong(path));

            } else if (type == double.class || type == Double.TYPE || type == float.class || type == Float.TYPE) {
                field.set(object, configuration.getDouble(path));

            } else if (type == String.class) {
                field.set(object, configuration.getString(path));

            } else if (type == boolean.class || type == Boolean.class) {
                field.set(object, configuration.getBoolean(path));

            } else if (type == List.class) {
                field.set(object, configuration.getList(path));

            } else if (type == Map.class) {
                Map<String, Object> map = new HashMap<>();
                configuration.getConfigurationSection(path)
                        .getKeys(false)
                        .forEach(key -> map.put(key, configuration.get(path + "." + key)));
                field.set(object, map);

            } else if (type == Proxy.Type.class) {
                String string = configuration.getString(path);
                switch (string.toLowerCase()) {
                    case "none":
                        field.set(object, Proxy.Type.DIRECT);
                        break;
                    case "https":
                        field.set(object, Proxy.Type.HTTP);
                        break;
                    case "socks":
                        field.set(object, Proxy.Type.SOCKS);
                        break;
                    default:
                        field.set(object, string);
                        break;
                }
            } else {
                throw new IllegalArgumentException(String.format("Not support yaml value type: %s", type.getName()));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static YamlConfiguration load(File file) {
        YamlConfiguration yamlConfiguration = null;
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            yamlConfiguration = YamlConfiguration.loadConfiguration((reader));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return yamlConfiguration;
    }
}
