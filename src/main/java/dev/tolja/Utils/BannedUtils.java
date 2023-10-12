package dev.tolja.Utils;

import com.alibaba.fastjson.JSONObject;
import dev.tolja.Data.ProxyInfo;
import soterdev.SoterObfuscator;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

import static dev.tolja.Nchecker.configManager;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BannedUtils {

    public static PublicKey decodePublicKey(byte[] encodedKey) {
        try {
            EncodedKeySpec encodedkeyspec = new X509EncodedKeySpec(encodedKey);
            KeyFactory keyfactory = KeyFactory.getInstance("RSA");
            return keyfactory.generatePublic(encodedkeyspec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ignored) {
        }
        return null;
    }

    public static byte[] encryptionRequest(byte[] publickey, byte[] verifyToken, byte[] sharedSecret) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream encryption = new DataOutputStream(buffer);
        PublicKey publicKey = decodePublicKey(publickey);
        encryption.write(0x01);
        byte[] ess = encryptData(publicKey, sharedSecret);
        byte[] evt = encryptData(publicKey, verifyToken);
        writeVarInt(encryption, ess.length);
        encryption.write(ess);
        writeVarInt(encryption, evt.length);
        encryption.write(evt);
        return buffer.toByteArray();
    }

    public static byte[] encryptData(Key key, byte[] data) {
        return cipherOperation(key, data);
    }

    private static byte[] cipherOperation(Key key, byte[] data) {
        try {
            return Objects.requireNonNull(createTheCipherInstance(key.getAlgorithm(), key)).doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException ignored) {
        }
        return null;
    }

    private static Cipher createTheCipherInstance(String transformation, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(1, key);
            return cipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException ignored) {
        }
        return null;
    }

    private static byte[] loginStart(String playerName) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream loginStart = new DataOutputStream(buffer);
        loginStart.write(0x00);
        writeVarString(loginStart, playerName, UTF_8);
        return buffer.toByteArray();
    }

    public static byte[] getServerIdHash(String serverid, byte[] publicKey, byte[] secretKey) {
        try {
            return digestOperation(serverid.getBytes("ISO_8859_1"),
                    secretKey,
                    publicKey);
        } catch (UnsupportedEncodingException unsupportedencodingexception) {
            return null;
        }
    }

    private static byte[] digestOperation(byte[]... data) {
        try {
            MessageDigest messagedigest = MessageDigest.getInstance("SHA-1");

            for (byte[] abyte : data) {
                messagedigest.update(abyte);
            }
            return messagedigest.digest();
        } catch (NoSuchAlgorithmException nosuchalgorithmexception) {
            return null;
        }
    }

    private static byte[] createHandshake() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream handshake = new DataOutputStream(buffer);
        handshake.write(0x00);
        writeVarInt(handshake, 47);
        writeVarString(handshake, "mc.hypixel.net", UTF_8);
        handshake.writeShort(25565);
        writeVarInt(handshake, 2);
        return buffer.toByteArray();
    }

    public static void writeVarString(DataOutputStream out, String string, Charset charset) throws IOException {
        byte[] bytes = string.getBytes(charset);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    public static int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        return i;
    }

    public static int readVarInt(CipherInputStream in) throws IOException {
        int numRead = 0;
        int result = 0;
        byte[] read = new byte[1];
        do {
            in.read(read);
            int value = (read[0] & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read[0] & 0b10000000) != 0);

        return result;
    }

    public static SecretKey createNewSharedKey() {
        try {
            KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
            keygenerator.init(128);
            return keygenerator.generateKey();
        } catch (NoSuchAlgorithmException nosuchalgorithmexception) {
            throw new Error(nosuchalgorithmexception);
        }
    }

    @SoterObfuscator.Obfuscation(flags = "+native,+encstr")
    public static BannedType isBanned(String playerName, String accessToken, String uuid, ProxyInfo proxy) {
        String host = "172.65.223.54";
        int port = 25565;
        InetSocketAddress address = new InetSocketAddress(host, port);


        int recheckCount = 0;

        do {
            try {
                Socket socket = new Socket();
                socket.connect(address, 10000);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                //HandShake
                byte[] handshake = createHandshake();
                writeVarInt(dataOutputStream, handshake.length);
                dataOutputStream.write(handshake);

                //Login Start
                byte[] loginStart = loginStart(playerName);
                writeVarInt(dataOutputStream, loginStart.length);
                dataOutputStream.write(loginStart);

                readVarInt(dataInputStream);
                int id = readVarInt(dataInputStream);
                if (id == 0x01) {
                    int serverIDLength = readVarInt(dataInputStream);
                    byte[] serverID = new byte[serverIDLength];
                    dataInputStream.read(serverID); //读入ServerID [String]

                    int publicKeyLength = readVarInt(dataInputStream);
                    byte[] publicKey = new byte[publicKeyLength];
                    dataInputStream.read(publicKey); //读入public key [byte array]

                    int verifyTokenLength = readVarInt(dataInputStream);
                    byte[] verifyToken = new byte[verifyTokenLength];
                    dataInputStream.read(verifyToken);//读入VerifyToken [byte array]

                    //加密:/
                    String sid = new String(serverID);
                    final SecretKey secretkey = createNewSharedKey();
                    String serverHash = (new BigInteger(Objects.requireNonNull(getServerIdHash(sid, publicKey, secretkey.getEncoded())))).toString(16);

                    JSONObject joinJson = new JSONObject();
                    joinJson.put("accessToken", accessToken);
                    joinJson.put("selectedProfile", uuid);
                    joinJson.put("serverId", serverHash);
                    String joininfo = joinJson.toString();
                    if (configManager.getSettingsConfig().getProxyType() != Proxy.Type.DIRECT) {
                        HttpUtils.doPost("https://sessionserver.mojang.com/session/minecraft/join", proxy,  joininfo,50000);
                    } else {
                        HttpUtils.doPost("https://sessionserver.mojang.com/session/minecraft/join", null,  joininfo,50000);
                    }
                    byte[] encryption = encryptionRequest(publicKey, verifyToken, secretkey.getEncoded());
                    writeVarInt(dataOutputStream, encryption.length);
                    dataOutputStream.write(encryption);

                    try {
                        Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
                        cipher.init(2, secretkey, (new IvParameterSpec(secretkey.getEncoded())));
                        CipherInputStream input = new CipherInputStream(dataInputStream, cipher);
                        readVarInt(input);
                        readVarInt(input);
                        int packetSize = readVarInt(input);
                        StringBuilder results = new StringBuilder();
                        for (int i = 0; i < packetSize; i++) {
                            results.append(new String(new byte[]{(byte) readVarInt(input)}, UTF_8));
                        }
                        if (results.toString().contains("$")) {
                            return BannedType.UNBANNED;
                        } else {
                            if (results.toString().contains("Your account has a security alert")) {
                                return BannedType.IPBAN;
                            } else {
                                return BannedType.OTHERBAN;
                            }
                        }

                    } catch (Exception ex) {

                    } finally {
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                    }
                }
            } catch (Exception ex) {
            }
        } while (configManager.getSettingsConfig().getMaxReconnectCount() > recheckCount++);
        return BannedType.ERROR;

    }

    public enum BannedType {
        UNBANNED,
        IPBAN,
        OTHERBAN,
        ERROR
    }

}
