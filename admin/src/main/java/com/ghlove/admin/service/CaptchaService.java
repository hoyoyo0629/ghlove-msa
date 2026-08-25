package com.ghlove.admin.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.SecureRandom;

import javax.imageio.ImageIO;

/**
 * 1:1 문의 등록 폼의 이미지 캡차 (AS-IS 실사이트 화면 재현 - 숫자 6자리를 왜곡된 글꼴로
 * 그린 PNG + 노이즈 선/점). 정답은 HttpSession에 저장해두고 제출 시 대조한다(1회용 -
 * 검증 성공/실패 여부와 무관하게 검증 직후 세션에서 제거해 재사용을 막는다).
 */
@Service
public class CaptchaService {

    public static final String SESSION_KEY = "QNA_CAPTCHA_ANSWER";

    private static final int LENGTH = 6;
    private static final int WIDTH = 160;
    private static final int HEIGHT = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateAnswer() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /** @return PNG bytes of the distorted captcha image for the given answer text. */
    public byte[] renderImage(String text) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(235, 235, 235));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // noise lines
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(150 + RANDOM.nextInt(80), 150 + RANDOM.nextInt(80), 150 + RANDOM.nextInt(80)));
            g.drawLine(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT));
        }

        int charWidth = WIDTH / (LENGTH + 1);
        for (int i = 0; i < text.length(); i++) {
            AffineTransform base = g.getTransform();
            int x = charWidth * (i + 1) - 8;
            int y = HEIGHT / 2 + 8 + RANDOM.nextInt(10) - 5;
            double angle = Math.toRadians(RANDOM.nextInt(40) - 20);
            g.translate(x, y);
            g.rotate(angle);
            g.setFont(new Font("Serif", Font.BOLD, 26 + RANDOM.nextInt(8)));
            g.setColor(new Color(RANDOM.nextInt(100), RANDOM.nextInt(100), RANDOM.nextInt(100)));
            g.drawString(String.valueOf(text.charAt(i)), 0, 0);
            g.setTransform(base);
        }

        // noise dots
        for (int i = 0; i < 40; i++) {
            g.setColor(new Color(180 + RANDOM.nextInt(60), 180 + RANDOM.nextInt(60), 180 + RANDOM.nextInt(60)));
            g.fillOval(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 2, 2);
        }

        g.dispose();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
