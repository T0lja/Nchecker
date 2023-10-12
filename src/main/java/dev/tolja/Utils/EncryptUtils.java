package dev.tolja.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EncryptUtils {

    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final long JWT_EXPIRED_TIME = 1000 * 30;
    private static String AES_SECRET;
    private static String JWT_SECRET;
    private static Key key;

    static {
        init();
    }

    private static void init() {
        AES_SECRET = "A4zWf2gduI1GUNae";
        JWT_SECRET = "A4zWf2gduMxTX5A4zWf2gduMxTX5nI1GUsnI1GUNaeA4zWf2gdup0cyQiE3sFp0cyQiE3s";
        key = new SecretKeySpec(AES_SECRET.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);
    }

    public static String generateJwt(HashMap<String, Object> keys) {
        JwtBuilder jb = Jwts.builder();
        for (Map.Entry<String, Object> entry : keys.entrySet()) {
            jb.claim(entry.getKey(), entry.getValue());
        }
        return jb
                .setSubject("NcheckerDevTeam")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRED_TIME))
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compressWith(CompressionCodecs.DEFLATE)
                .compact();
    }

    public static Claims verifyJwt(String token) {
        try {
            return Jwts.parserBuilder()
                    .setAllowedClockSkewSeconds(180L)
                    .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String encrypt(String content) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(byteContent);
            return Base64.encodeBase64String(result);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String decrypt(String content) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(Base64.decodeBase64(content));
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

}
