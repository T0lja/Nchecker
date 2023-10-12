package dev.tolja.Checkers;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class OutputManager {

    private HashMap<String, BufferedWriter> writerMap;

    private File outputFolder;

    public OutputManager(File resultsFolder, String moduleName) {
        if (moduleName.equals("1")) {
            outputFolder = new File(resultsFolder, new SimpleDateFormat("[yyyy MM dd] - HH mm ss").format(new Date(System.currentTimeMillis())) + " - " + "NormalChecker");
            outputFolder.mkdirs();
            writerMap = new HashMap<>();
        } else if (moduleName.equals("2")) {
            outputFolder = new File(resultsFolder, new SimpleDateFormat("[yyyy MM dd] - HH mm ss").format(new Date(System.currentTimeMillis())) + " - " + "BanChecker");
            outputFolder.mkdirs();
            writerMap = new HashMap<>();
        } else if (moduleName.equals("3")) {
            outputFolder = new File(resultsFolder, new SimpleDateFormat("[yyyy MM dd] - HH mm ss").format(new Date(System.currentTimeMillis())) + " - " + "SkyblockChecker");
            outputFolder.mkdirs();
            writerMap = new HashMap<>();
        } else if (moduleName.equals("4")) {
            outputFolder = new File(resultsFolder, new SimpleDateFormat("[yyyy MM dd] - HH mm ss").format(new Date(System.currentTimeMillis())) + " - " + "ProxiesChecker");
            outputFolder.mkdirs();
            writerMap = new HashMap<>();
        } else if (moduleName.equals("5")) {
            outputFolder = new File(resultsFolder, new SimpleDateFormat("[yyyy MM dd] - HH mm ss").format(new Date(System.currentTimeMillis())) + " - " + "HypixelChecker");
            outputFolder.mkdirs();
            writerMap = new HashMap<>();
        } else if (moduleName.equals("6")) {
            outputFolder = new File(resultsFolder, new SimpleDateFormat("[yyyy MM dd] - HH mm ss").format(new Date(System.currentTimeMillis())) + " - " + "Hits Checker");
            outputFolder.mkdirs();
            writerMap = new HashMap<>();
        }
    }

    public void writeLine(String fileName, String text) {
        try {
            BufferedWriter writer = writerMap.get(fileName);
            if (writer == null) {
                File file = new File(outputFolder, fileName);
                file.createNewFile();
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
                writerMap.put(fileName, writer);
            }
            writer.write(text + System.lineSeparator());
            writer.flush();
        } catch (Exception ignored) {
        }
    }
}
