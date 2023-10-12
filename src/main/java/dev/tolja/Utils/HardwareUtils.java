package dev.tolja.Utils;


import soterdev.SoterObfuscator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HardwareUtils {
    public static int i;
    private static String sn = null;

    @SoterObfuscator.Obfuscation(flags = "+native")
    public static String getHWID() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                return hug(System.getenv("PROCESSOR_IDENTIFIER")
                        + System.getenv("COMPUTERNAME")
                        + System.getProperty("os.name")
                        + System.getProperty("user.home"));
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                return hug(getMacSerialNumber());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @SoterObfuscator.Obfuscation(flags = "+native")
    public static String getMacSerialNumber() {

        if (sn != null) {
            return sn;
        }

        OutputStream os;
        InputStream is;

        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec(new String[]{"/usr/sbin/system_profiler", "SPHardwareDataType"});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        os = process.getOutputStream();
        is = process.getInputStream();

        try {
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        String marker = "Serial Number";
        try {
            while ((line = br.readLine()) != null) {
                if (line.contains(marker)) {
                    sn = line.split(":")[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (sn == null) {
            throw new RuntimeException("Cannot find computer SN");
        }

        return sn;
    }

    @SoterObfuscator.Obfuscation(flags = "+native")
    private static String hug(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
        return zerodayisaminecraftcheat(md.digest());
    }

    @SoterObfuscator.Obfuscation(flags = "+native")
    private static String zerodayisaminecraftcheat(byte[] data) {
        StringBuilder buf = new StringBuilder();

        for (byte datum : data) {
            int halfbyte = datum >>> 4 & 15;
            int two_halfs = 0;

            do {
                if (halfbyte <= 9) {
                    buf.append((char) (48 + halfbyte));
                } else {
                    buf.append((char) (97 + (halfbyte - 10)));
                }

                halfbyte = datum & 15;
            } while (two_halfs++ < 1);
        }

        return buf.toString();
    }

    @SoterObfuscator.Obfuscation(flags = "+native")
    public static String getMD5(String str) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(str.getBytes());
        byte[] md5Bytes = md5.digest();
        StringBuilder res = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            int temp = md5Byte & 0xFF;
            if (temp <= 0XF) {
                res.append("0");
            }
            res.append(Integer.toHexString(temp));
        }
        return res.toString();
    }

    @SoterObfuscator.Obfuscation(flags = "+native")
    public static String dx(String x) {
        StringBuilder tmp1 = new StringBuilder(x);
        return tmp1.reverse().toString();
    }

    @SoterObfuscator.Obfuscation(flags = "+native")
    public static String encode(String string) throws Exception {
        return getMD5("p" + dx("f" + Base64.getEncoder().encodeToString((dx("s" + Base64.getEncoder().encodeToString((dx(Base64.getEncoder().encodeToString(string.getBytes()).replaceAll("\\d+", "").replaceAll("=", "").toLowerCase().substring(0, 8)
                + Base64.getEncoder().encodeToString(string.getBytes()).replaceAll("\\d+", "").replaceAll("=", "").toLowerCase().substring(
                Base64.getEncoder().encodeToString(string.getBytes()).replaceAll("\\d+", "").replaceAll("=", "").toLowerCase().length() - 8
        ))).getBytes())) + "SFNERkhBU0pLRkhFSEZLU0pESERWRsUHIksK7SJhjo92HSVW").getBytes())));
    }
}
