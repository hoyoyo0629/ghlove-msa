package com.ghlove.gift.service;

import com.ghlove.gift.repository.ItemImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 답례품 이미지 썸네일 자동생성 (SFR-005 "비동기 이미지 처리: 썸네일 자동생성 소/중/대
 * 3종"). 원본 업로드 응답은 기다리지 않도록 @Async로 돌린다 - 호출부(GiftController)가
 * giftService.register()의 트랜잭션이 커밋된 *이후*에 호출해야 한다(같은 트랜잭션
 * 안에서 호출하면 별도 스레드가 아직 커밋 전인 ITEM_IMAGE 행을 못 찾을 수 있음).
 */
@Service
@Slf4j
public class ThumbnailService {

    private static final int SMALL_WIDTH = 150;
    private static final int MEDIUM_WIDTH = 400;
    private static final int LARGE_WIDTH = 800;

    private final ItemImageRepository itemImageRepository;
    private final String uploadDir;

    public ThumbnailService(ItemImageRepository itemImageRepository,
                             @Value("${gift.upload.dir}") String uploadDir) {
        this.itemImageRepository = itemImageRepository;
        this.uploadDir = uploadDir;
    }

    @Async
    @Transactional
    public void generateThumbnails(Long itemImageId, String storedFileName) {
        try {
            Path source = Paths.get(uploadDir, storedFileName);
            BufferedImage original = ImageIO.read(source.toFile());
            if (original == null) {
                log.warn("Not a raster image - skipping thumbnail generation for {}", storedFileName);
                return;
            }
            String baseName = baseNameOf(storedFileName);
            String small = resizeAndSave(original, baseName, "small", SMALL_WIDTH);
            String medium = resizeAndSave(original, baseName, "medium", MEDIUM_WIDTH);
            String large = resizeAndSave(original, baseName, "large", LARGE_WIDTH);

            itemImageRepository.findById(itemImageId).ifPresent(img -> {
                img.setThumbnailSmall(small);
                img.setThumbnailMedium(medium);
                img.setThumbnailLarge(large);
                itemImageRepository.save(img);
            });
            log.info("Generated thumbnails for itemImageId={}: small={}, medium={}, large={}",
                    itemImageId, small, medium, large);
        } catch (IOException e) {
            log.error("Thumbnail generation failed for {}", storedFileName, e);
        }
    }

    /** 원본 비율을 유지한 채 지정 너비로 축소해 PNG로 저장한다 (원본이 이미 더 작으면 원본 크기 그대로). */
    private String resizeAndSave(BufferedImage original, String baseName, String suffix, int maxWidth) throws IOException {
        int width = Math.min(maxWidth, original.getWidth());
        int height = Math.max(1, (int) ((double) width / original.getWidth() * original.getHeight()));

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();

        String fileName = baseName + "_" + suffix + ".png";
        ImageIO.write(resized, "png", Paths.get(uploadDir, fileName).toFile());
        return fileName;
    }

    private String baseNameOf(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(0, idx) : fileName;
    }
}
