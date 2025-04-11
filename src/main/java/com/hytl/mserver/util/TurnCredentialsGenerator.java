package com.hytl.mserver.util;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.util.Base64;
import java.util.Date;

public class TurnCredentialsGenerator {

    public static String generateUsername(String user_id, int validityDuration) {
        long timestamp = (new Date().getTime() / 1000L) + validityDuration; // 当前时间 + 凭证的有效时长（秒），例如，validityDuration=3600 表示凭证有效时长为 1 小时
        return timestamp + ":" + user_id;
    }

    public static String generatePassword(String username, String sharedSecret) {
        // 使用 Apache Commons Codec 的 HmacUtils
        byte[] hmacData = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, sharedSecret).hmac(username);
        return Base64.getEncoder().encodeToString(hmacData);
    }

    public static void main(String[] args) {
        String sharedSecret = "9D92C921-434F-4D7B-BA68-F6E60F447182";
        String username = "user123";

        String turnUsername = generateUsername(username, 7200);
        String turnPassword = generatePassword(turnUsername, sharedSecret);

        System.out.println("TURN Username: " + turnUsername);
        System.out.println("TURN Password: " + turnPassword);
    }
}