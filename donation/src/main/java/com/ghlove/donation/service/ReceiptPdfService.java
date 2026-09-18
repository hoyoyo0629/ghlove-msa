package com.ghlove.donation.service;

import com.ghlove.donation.service.OfficialReceiptService.OfficialReceipt;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;

/**
 * 기부금영수증을 <b>서버사이드에서 단일 래스터 이미지로 합성</b>해 1페이지 PDF로 반출한다.
 * 상용 OZ Report(뷰어 렌더) + Fasoo Secure Web(클라이언트 화면보호) 조합을 자체 구현으로
 * 대체하기 위한 것으로, 슬라이드 "TO-BE PLAN"의 세 원칙을 코드로 실현한다:
 *
 * <ul>
 *   <li><b>직인 미전송</b> - 직인 이미지는 브라우저 DOM으로 내려보내지 않고, 서버 메모리에서
 *       복호화해 래스터에 바로 병합한다. 조회 화면(HTML)에는 직인이 존재하지 않는다.</li>
 *   <li><b>비트맵 병합 + 해상도 상한</b> - 본문 텍스트와 직인을 하나의 {@link BufferedImage}로
 *       평탄화(flatten)한 뒤 JPEG(해상도 {@value #DPI}dpi 상한)로 인코딩한다. 텍스트/이미지가
 *       개별 객체로 분리 추출되지 않으므로 소스보기·부분 반출이 원천 차단된다.</li>
 *   <li><b>서버사이드 PDF 합성</b> - 평탄화된 이미지 한 장만 A4 페이지에 배치한다. 폰트를
 *       임베딩하지 않으므로(텍스트가 이미 래스터임) 폰트 라이선스/렌더 편차 문제도 없다.</li>
 * </ul>
 */
@Service
@Slf4j
public class ReceiptPdfService {

    /** 해상도 상한 - A4 기준 150dpi(1240x1754px). 화면 캡처 대비 충분하되 파일 크기는 억제. */
    private static final int DPI = 150;
    private static final int PAGE_W = (int) Math.round(210 / 25.4 * DPI); // 1240
    private static final int PAGE_H = (int) Math.round(297 / 25.4 * DPI); // 1754
    private static final float JPEG_QUALITY = 0.85f;

    private final OfficialReceiptService officialReceiptService;
    private final LocgovSealService locgovSealService;
    private final Font baseFont;
    private final Font boldFont;

    public ReceiptPdfService(OfficialReceiptService officialReceiptService,
                             LocgovSealService locgovSealService) {
        this.officialReceiptService = officialReceiptService;
        this.locgovSealService = locgovSealService;
        this.baseFont = loadFont("fonts/NotoSansKR-Regular.otf");
        this.boldFont = loadFont("fonts/NotoSansKR-Bold.otf");
    }

    private static Font loadFont(String classpath) {
        try (InputStream is = new ClassPathResource(classpath).getInputStream()) {
            // TRUETYPE_FONT 상수는 OpenType(CFF) 파일도 로드한다.
            return Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            // 폰트 반입 실패 시 시스템 한글 폰트로 폴백(개발 환경 안전판).
            log.warn("Bundled font {} load failed, falling back to Malgun Gothic", classpath, e);
            return new Font("Malgun Gothic", Font.PLAIN, 12);
        }
    }

    /** 소유권 검증 → 데이터 조립 → 직인 병합 → 단일 이미지 PDF. 반환 즉시 인쇄 대상 바이트. */
    public byte[] render(Long userId, String cntrSn) {
        OfficialReceipt r = officialReceiptService.build(userId, cntrSn);
        Optional<byte[]> seal = locgovSealService.decryptedSealBytes(r.locgovCode());

        BufferedImage canvas = drawReceipt(r, seal.orElse(null));
        try {
            byte[] jpeg = toJpeg(canvas);
            return wrapAsPdf(jpeg, canvas.getWidth(), canvas.getHeight());
        } catch (Exception e) {
            log.error("Failed to compose receipt PDF for cntrSn={}", cntrSn, e);
            throw new DonationException("영수증 PDF 생성에 실패했습니다.");
        }
    }

    private BufferedImage drawReceipt(OfficialReceipt r, byte[] sealBytes) {
        BufferedImage img = new BufferedImage(PAGE_W, PAGE_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, PAGE_W, PAGE_H);

        int margin = 90;
        int boxX = margin, boxY = margin, boxW = PAGE_W - margin * 2, boxH = PAGE_H - margin * 2;

        // 외곽 테두리(이중선)
        g.setColor(new Color(0x1b3a6b));
        g.setStroke(new BasicStroke(4f));
        g.drawRect(boxX, boxY, boxW, boxH);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(boxX + 12, boxY + 12, boxW - 24, boxH - 24);

        int cx = PAGE_W / 2;

        // 제목
        g.setColor(new Color(0x111111));
        Font title = boldFont.deriveFont(Font.BOLD, 46f);
        drawCentered(g, "고향사랑 기부금 영수증", cx, boxY + 150, title);

        // 근거 문구
        Font sub = baseFont.deriveFont(Font.PLAIN, 20f);
        drawCentered(g, "「고향사랑 기부금에 관한 법률」에 따라 아래와 같이", cx, boxY + 215, sub);
        drawCentered(g, "기부금을 접수하였음을 증명합니다.", cx, boxY + 245, sub);

        // 본문 표
        String[][] rows = {
                {"접수번호", nz(r.cntrSn())},
                {"전자납부번호", r.elctrnPayNo() == null ? "-" : r.elctrnPayNo()},
                {"기부자 성명", nz(r.userName())},
                {"생년월일", nz(r.birthdayDisplay())},
                {"기부지자체", locgovLine(r)},
                {"기부금액", formatAmount(r.cntrAmt()) + "원"},
                {"기부일자", nz(r.cntrDeDisplay())},
                {"발급일자", nz(r.issueDateDisplay())},
        };
        Font th = boldFont.deriveFont(Font.BOLD, 24f);
        Font td = baseFont.deriveFont(Font.PLAIN, 24f);
        int tableX = boxX + 70, tableW = boxW - 140;
        int labelW = 320;
        int rowH = 74;
        int tableY = boxY + 320;
        g.setColor(new Color(0xcfd6e0));
        g.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < rows.length; i++) {
            int ry = tableY + rowH * i;
            g.setColor(new Color(0xf3f5f9));
            g.fillRect(tableX, ry, labelW, rowH);
            g.setColor(new Color(0xcfd6e0));
            g.drawRect(tableX, ry, tableW, rowH);
            g.drawLine(tableX + labelW, ry, tableX + labelW, ry + rowH);
            g.setColor(new Color(0x333333));
            drawVCentered(g, rows[i][0], tableX + 28, ry, rowH, th);
            g.setColor(new Color(0x111111));
            drawVCentered(g, rows[i][1], tableX + labelW + 28, ry, rowH, td);
        }

        // 발급자 + 직인 병합
        int issueY = tableY + rowH * rows.length + 90;
        Font issuer = boldFont.deriveFont(Font.BOLD, 26f);
        String issuerText = (nz(r.locgovDisplay()) + " " + (r.offcsNm() == null ? "지자체" : r.offcsNm()) + " 발급").trim();
        g.setColor(new Color(0x111111));
        FontMetrics fm = g.getFontMetrics(issuer);
        int textW = fm.stringWidth(issuerText);
        int sealSize = 150;
        int totalW = textW + 30 + sealSize;
        int startX = cx - totalW / 2;
        g.setFont(issuer);
        g.drawString(issuerText, startX, issueY + fm.getAscent());

        if (sealBytes != null) {
            try {
                BufferedImage seal = ImageIO.read(new ByteArrayInputStream(sealBytes));
                if (seal != null) {
                    int sx = startX + textW + 30;
                    int sy = issueY + (fm.getAscent() - sealSize) / 2;
                    g.drawImage(seal, sx, sy, sealSize, sealSize, null);
                }
            } catch (Exception e) {
                log.warn("Seal image decode failed; receipt rendered without seal", e);
            }
        }

        // 하단 고지
        Font note = baseFont.deriveFont(Font.PLAIN, 16f);
        g.setColor(new Color(0x888888));
        drawCentered(g, "본 영수증은 고향사랑e음에서 서버 발급되었으며, 국세청 전자기부금영수증이 정본입니다.",
                cx, boxY + boxH - 60, note);

        g.dispose();
        return img;
    }

    private void drawCentered(Graphics2D g, String text, int cx, int baselineY, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, cx - fm.stringWidth(text) / 2, baselineY);
    }

    private void drawVCentered(Graphics2D g, String text, int x, int rowY, int rowH, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int y = rowY + (rowH - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
    }

    private String locgovLine(OfficialReceipt r) {
        String base = nz(r.locgovDisplay());
        return r.bizrno() != null ? base + " (사업자번호 : " + r.bizrno() + ")" : base;
    }

    private String formatAmount(BigDecimal amt) {
        return amt == null ? "0" : new DecimalFormat("#,##0").format(amt);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private byte[] toJpeg(BufferedImage img) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        javax.imageio.ImageWriteParam p = writer.getDefaultWriteParam();
        p.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        p.setCompressionQuality(JPEG_QUALITY);
        try (javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(bos)) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(img, null, null), p);
        } finally {
            writer.dispose();
        }
        return bos.toByteArray();
    }

    private byte[] wrapAsPdf(byte[] jpeg, int imgW, int imgH) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDImageXObject image = JPEGFactory.createFromByteArray(doc, jpeg);

            float pw = PDRectangle.A4.getWidth();
            float ph = PDRectangle.A4.getHeight();
            // 이미지 종횡비를 유지하며 A4에 꽉 맞춘다(둘 다 210:297 비율이라 사실상 전면).
            float scale = Math.min(pw / imgW, ph / imgH);
            float w = imgW * scale, h = imgH * scale;
            float x = (pw - w) / 2, y = (ph - h) / 2;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(image, x, y, w, h);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.save(bos);
            return bos.toByteArray();
        }
    }
}
