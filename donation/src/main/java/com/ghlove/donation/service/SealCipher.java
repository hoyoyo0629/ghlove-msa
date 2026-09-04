package com.ghlove.donation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 지자체 직인 이미지 등 저장 시 암호화가 필요한 파일을 AES-256-GCM으로 암/복호화한다.
 * AS-IS는 상용 JNI 모듈(com.privacy.pCrypto, libs/privacy.jar - native pdbJava.dll을 감싼
 * 개인정보 암호화 솔루션)을 썼지만, 이건 특정 벤더 제품 라이선스 없이도 동등하게 구현
 * 가능한 범용 요구사항이라 자바 표준 암호화로 대체했다 (자세한 배경은 프로젝트 메모
 * isp_detailed_design_summary/asis_ozreport_gap 참고).
 *
 * 저장 포맷: [12바이트 IV][GCM 암호문+16바이트 태그] - IV를 매 암호화마다 새로 생성해
 * 암호문 앞에 붙여서 같이 저장하므로 복호화 시 별도 IV 저장소가 필요 없다.
 */
@Component
public class SealCipher {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public SealCipher(@Value("${ghlove.seal.encryption-key-base64}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalStateException("ghlove.seal.encryption-key-base64 must decode to 32 bytes (AES-256), got " + keyBytes.length);
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public byte[] encrypt(byte[] plain) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain);
            byte[] out = new byte[IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, out, 0, IV_LENGTH);
            System.arraycopy(cipherText, 0, out, IV_LENGTH, cipherText.length);
            return out;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("직인 이미지 암호화에 실패했습니다.", e);
        }
    }

    public byte[] decrypt(byte[] ivAndCipherText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(ivAndCipherText, 0, iv, 0, IV_LENGTH);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return cipher.doFinal(ivAndCipherText, IV_LENGTH, ivAndCipherText.length - IV_LENGTH);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("직인 이미지 복호화에 실패했습니다.", e);
        }
    }
}
