package retwis.util;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Slf4j
public class MD5Utils {

    /**
     * 把字节数组转换为16进制字符串
     * @param text
     * @return
     */
    private static String hex(byte[] text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length; i++) {
            sb.append(Integer.toHexString((text[i] & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }

    /**
     * 把字符串进行MD5加密并转化为16进制字符串
     * @param text
     * @return
     */
    private static String md5Hex(String text) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(text.getBytes());
            return hex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("md5加密错误!",e);
        }
    }

    /**
     * 生成含有随机盐的MD5字符串
     * @param text
     * @return
     */
    public static String saltAndMD5(String text) {
        // 生成随机的16位数
        Random random = new Random();
        StringBuilder sb = new StringBuilder(16);
        final int num = 99999999;
        sb.append(random.nextInt(num)).append(random.nextInt(num));
        if (sb.length() < 16) {
            for (int i = 0; i < 16 - sb.length(); i++) {
                sb.append(0);
            }
        }
        // 生成盐
        String salt = sb.toString();
        // md5加密
        String md5Str = md5Hex(text + salt);
        char[] cs = new char[48];
        for (int i = 0; i < 48; i += 3) {
            cs[i] = md5Str.charAt(i / 3 * 2);
            char c = salt.charAt(i / 3);
            cs[i + 1] = c;
            cs[i + 2] = md5Str.charAt(i / 3 * 2 + 1);
        }
        return String.valueOf(cs);
    }

    /**
     * 验证加盐后的字符串和原字符串是否一样
     * @param text
     * @param md5Str
     * @return
     */
    public static boolean saltverifyMD5(String text, String md5Str) {
        char[] cs1 = new char[32], cs2 = new char[16];
        for (int i = 0; i < 48; i += 3) {
            cs1[i / 3 * 2] = md5Str.charAt(i);
            cs1[i / 3 * 2 + 1] = md5Str.charAt(i + 2);
            cs2[i / 3] = md5Str.charAt(i + 1);
        }
        String salt = String.valueOf(cs2);
        return md5Hex(text + salt).equals(String.valueOf(cs1));
    }
}
