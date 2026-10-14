package com.ghlove.admin.web.support;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * AS-IS {@code saleson.common.security.crypto.RsaCryptor} - 기탁서 등록의 외국인 행정주소검색이
 * 외국인등록번호를 브라우저(JSEncrypt)에서 RSA로 암호화해 보내고 서버가 세션의 개인키로 푼다.
 * 키는 2048bit, 공개키 X.509/개인키 PKCS#8을 Base64 문자열로 주고받는다(AS-IS와 동일).
 */
@Slf4j
public final class RsaCryptor {

    private RsaCryptor() {
    }

    /** @return {publicKeyStr, privateKeyStr}, 실패하면 null */
    public static Map<String, String> createKeypairAsString() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048, new SecureRandom());
            KeyPair keyPair = kpg.genKeyPair();
            Map<String, String> keys = new HashMap<>();
            keys.put("publicKeyStr", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
            keys.put("privateKeyStr", Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
            return keys;
        } catch (Exception e) {
            log.error("RsaCryptor createKeypairAsString error", e);
            return null;
        }
    }

    /** @return 평문, 실패하면 빈 문자열(AS-IS와 동일) */
    public static String decrypt(String encryptedText, String privateKeyStr) {
        try {
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr)));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RsaCryptor decrypt error", e);
            return "";
        }
    }
}
