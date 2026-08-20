package com.todayscasting.domain.casting.service;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// 다운로드용 캐스팅 카드 이미지를 만든다. (오늘의 결과 화면 "오늘의 카드 다운로드" 버튼용)
// 배경 이미지 위에 날짜 / "TODAY'S CASTING" 라벨 / 배역명만 합성하고, 프론트 ResultScreen의
// CastingCardFront와 정확히 동일한 카드 외곽 모양(SVG path)으로 잘라낸다. (2026-08-20, 프론트 스펙 반영)
// 팀 논의 결과, 하트 아이콘과 하단 정보 패널(장르/한줄기록/기억에 남은 장면)은 여전히 제외한다
// (텍스트 박스가 없는 쪽이 더 "저장하고 싶은" 느낌을 준다는 의견, 2026-08-19).
@Component
public class CastingCardImageComposerService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final String LABEL_TEXT = "TODAY'S CASTING";

    private static final Color GOLD = new Color(240, 200, 140);
    private static final Color GOLD_DIM = new Color(220, 180, 130, 220);

    // 프론트 CastingCardFront와 동일한 SVG path 좌표계 (viewBox="0 0 404 584")
    private static final double SVG_VIEWBOX_WIDTH = 404;
    private static final double SVG_VIEWBOX_HEIGHT = 584;

    // 프론트가 다운로드용으로 권장하는 고해상도(2배) 출력 크기
    private static final int OUTPUT_WIDTH = 568;
    private static final int OUTPUT_HEIGHT = 968;

    // 카드 내부 텍스트 작업은 이 배율의 중간 캔버스에서 먼저 처리한 뒤, 최종 크기로 늘린다
    // (viewBox 비율을 유지한 상태에서 텍스트를 그려야 최종 비율 변형(늘리기) 때 텍스트가
    // 이상하게 뭉개지지 않는다).
    private static final int INTERMEDIATE_SCALE = 3;
    private static final int INTERMEDIATE_WIDTH = (int) (SVG_VIEWBOX_WIDTH * INTERMEDIATE_SCALE);
    private static final int INTERMEDIATE_HEIGHT = (int) (SVG_VIEWBOX_HEIGHT * INTERMEDIATE_SCALE);

    // 프론트 스펙: 이미지 위 오버레이 rgba(25, 9, 43, 0.12), 테두리 rgba(214, 115, 92, 0.82)
    private static final Color OVERLAY_TINT = new Color(25, 9, 43, 31);
    private static final Color BORDER_COLOR = new Color(214, 115, 92, 209);
    private static final float BORDER_WIDTH = 3f;

    private final Font baseFont;

    public CastingCardImageComposerService() {
        // 팀 논의 결과, 프론트와 동일하게 세 텍스트(날짜/라벨/배역명) 모두 MaruBuri-SemiBold
        // 하나로 통일한다. 별도로 굵게(Bold) 처리를 얹지 않고, 폰트 파일 자체의 두께(SemiBold)
        // 그대로 사용한다 - Font.BOLD 스타일을 적용하면 합성 볼드(synthetic bold)가 걸려서
        // 폰트 파일 고유의 두께와 미묘하게 달라 보일 수 있기 때문이다. (2026-08-20)
        this.baseFont = loadFont("fonts/MaruBuri-SemiBold.ttf");
    }

    private Font labelFont() {
        return baseFont.deriveFont(34f * INTERMEDIATE_SCALE / 3f);
    }

    private Font dateFont() {
        return baseFont.deriveFont(48f * INTERMEDIATE_SCALE / 3f);
    }

    /**
     * 배경 이미지 바이트 위에 날짜/라벨/배역명을 그리고, 프론트와 동일한 카드 외곽 모양으로
     * 잘라낸 최종 PNG 바이트를 반환한다. 카드 바깥 영역은 투명하다.
     */
    public byte[] compose(byte[] backgroundBytes, LocalDate recordDate, String roleName) {
        try {
            BufferedImage background = ImageIO.read(new ByteArrayInputStream(backgroundBytes));
            if (background == null) {
                throw new IllegalStateException("배경 이미지를 읽을 수 없습니다 (지원하지 않는 형식이거나 손상됨).");
            }

            // 1단계: viewBox 비율(404:584)의 중간 캔버스에 배경(cover-fit)+텍스트를 그린다.
            BufferedImage content = new BufferedImage(INTERMEDIATE_WIDTH, INTERMEDIATE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D contentG = content.createGraphics();
            applyQualityHints(contentG);
            drawCoverFit(contentG, background, INTERMEDIATE_WIDTH, INTERMEDIATE_HEIGHT);
            drawTopScrim(contentG, INTERMEDIATE_WIDTH, INTERMEDIATE_HEIGHT);
            drawTexts(contentG, INTERMEDIATE_WIDTH, INTERMEDIATE_HEIGHT, recordDate, roleName);
            contentG.dispose();

            // 2단계: 프론트의 preserveAspectRatio="none"과 동일하게, 중간 캔버스를 최종 출력
            // 크기(568x968)로 가로/세로 비율을 독립적으로 늘려서 그린다.
            BufferedImage canvas = new BufferedImage(OUTPUT_WIDTH, OUTPUT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            applyQualityHints(g);

            Shape cardShape = buildCardShape();

            g.setClip(cardShape);
            g.drawImage(content, 0, 0, OUTPUT_WIDTH, OUTPUT_HEIGHT, null);
            g.setColor(OVERLAY_TINT);
            g.fill(cardShape);
            g.setClip(null);

            g.setColor(BORDER_COLOR);
            g.setStroke(new BasicStroke(BORDER_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(cardShape);

            g.dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(canvas, "png", output);
            return output.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("카드 이미지 합성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private void applyQualityHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setComposite(AlphaComposite.SrcOver);
    }

    // 프론트 CastingCardFront의 카드 외곽 path를 그대로 옮긴 것 (viewBox 404x584 좌표계).
    // "M31 1 H174 C179 14 188 21 202 21 C216 21 225 14 230 1 H373 C383 1 390 8 390 18
    //  C399 19 404 26 404 36 V555 C404 565 398 571 388 571 C388 579 381 583 372 583
    //  H32 C23 583 16 579 16 571 C6 571 0 565 0 555 V36 C0 26 6 20 14 18 C14 8 21 1 31 1 Z"
    // 이 path를 최종 출력 크기(568x968)에 맞춰 가로/세로 독립 배율로 늘려서 반환한다
    // (프론트의 svg width/height + viewBox + preserveAspectRatio="none" 조합과 동일한 결과).
    private Shape buildCardShape() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(31, 1);
        path.lineTo(174, 1);
        path.curveTo(179, 14, 188, 21, 202, 21);
        path.curveTo(216, 21, 225, 14, 230, 1);
        path.lineTo(373, 1);
        path.curveTo(383, 1, 390, 8, 390, 18);
        path.curveTo(399, 19, 404, 26, 404, 36);
        path.lineTo(404, 555);
        path.curveTo(404, 565, 398, 571, 388, 571);
        path.curveTo(388, 579, 381, 583, 372, 583);
        path.lineTo(32, 583);
        path.curveTo(23, 583, 16, 579, 16, 571);
        path.curveTo(6, 571, 0, 565, 0, 555);
        path.lineTo(0, 36);
        path.curveTo(0, 26, 6, 20, 14, 18);
        path.curveTo(14, 8, 21, 1, 31, 1);
        path.closePath();

        AffineTransform transform = AffineTransform.getScaleInstance(
                OUTPUT_WIDTH / SVG_VIEWBOX_WIDTH, OUTPUT_HEIGHT / SVG_VIEWBOX_HEIGHT);
        return transform.createTransformedShape(path);
    }

    // 배경 이미지를 대상 영역에 "cover"(xMidYMid slice)로 채운다 - 비율은 유지한 채
    // 잘리는 부분이 생기더라도 대상 영역을 빈틈없이 꽉 채우고, 중앙 기준으로 자른다.
    private void drawCoverFit(Graphics2D g, BufferedImage image, int targetWidth, int targetHeight) {
        double scale = Math.max(
                (double) targetWidth / image.getWidth(),
                (double) targetHeight / image.getHeight()
        );
        int drawWidth = (int) Math.ceil(image.getWidth() * scale);
        int drawHeight = (int) Math.ceil(image.getHeight() * scale);
        int offsetX = (targetWidth - drawWidth) / 2;
        int offsetY = (targetHeight - drawHeight) / 2;

        g.drawImage(image, offsetX, offsetY, drawWidth, drawHeight, null);
    }

    // 상단 텍스트 영역의 가독성을 위해, 배경 위에 위→아래로 옅어지는 어두운 그라데이션을 살짝 깐다.
    private void drawTopScrim(Graphics2D g, int width, int height) {
        int scrimHeight = (int) (height * 0.35);
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(0, 0, 0, 140),
                0, scrimHeight, new Color(0, 0, 0, 0)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, width, scrimHeight);
    }

    private void drawTexts(Graphics2D g, int width, int height, LocalDate recordDate, String roleName) {
        int centerX = width / 2;
        int y = (int) (height * 0.09);

        // 날짜
        g.setFont(dateFont());
        g.setColor(GOLD);
        String dateText = recordDate.format(DATE_FORMAT);
        drawCentered(g, dateText, centerX, y);
        y += 56 * INTERMEDIATE_SCALE / 3;

        // TODAY'S CASTING 라벨
        g.setFont(labelFont());
        g.setColor(GOLD_DIM);
        drawCentered(g, LABEL_TEXT, centerX, y);
        y += 78 * INTERMEDIATE_SCALE / 3;

        // 배역명 (길면 자동으로 줄바꿈, 최대 2줄)
        String safeRoleName = (roleName == null || roleName.isBlank()) ? "오늘의 주인공" : roleName;
        drawRoleName(g, safeRoleName, centerX, y, width);
    }

    // 배역명은 화면 폭에 맞춰 필요하면 두 줄로 자동 줄바꿈한다 (긴 배역명 대비).
    private void drawRoleName(Graphics2D g, String roleName, int centerX, int startY, int canvasWidth) {
        float fontSize = 90f * INTERMEDIATE_SCALE / 3f;
        int maxWidth = (int) (canvasWidth * 0.85);

        Font font = baseFont.deriveFont(fontSize);
        g.setFont(font);
        FontRenderContext frc = g.getFontRenderContext();

        List<String> lines = wrapToLines(roleName, font, frc, maxWidth, 2);

        // 두 줄인데도 여전히 넘치면 폰트를 줄여서 다시 시도
        while (lines.size() > 2 || exceedsWidth(lines, font, frc, maxWidth)) {
            fontSize -= 5f;
            if (fontSize < 30f) {
                break;
            }
            font = baseFont.deriveFont(fontSize);
            g.setFont(font);
            frc = g.getFontRenderContext();
            lines = wrapToLines(roleName, font, frc, maxWidth, 2);
        }

        g.setColor(Color.WHITE);
        int lineHeight = (int) (fontSize * 1.15);
        int y = startY + lineHeight;
        for (String line : lines) {
            drawCentered(g, line, centerX, y);
            y += lineHeight;
        }
    }

    private boolean exceedsWidth(List<String> lines, Font font, FontRenderContext frc, int maxWidth) {
        for (String line : lines) {
            Rectangle2D bounds = font.getStringBounds(line, frc);
            if (bounds.getWidth() > maxWidth) {
                return true;
            }
        }
        return false;
    }

    // 공백 기준으로 단어를 쌓아가며 maxWidth를 넘기 직전에 줄바꿈한다. 공백이 없는 긴 문자열은
    // 글자 단위로 잘라 붙인다 (한글은 보통 띄어쓰기 없이 붙어 나오는 경우가 많아서 글자 단위 폴백 필요).
    private List<String> wrapToLines(String text, Font font, FontRenderContext frc, int maxWidth, int maxLines) {
        List<String> lines = new ArrayList<>();
        String[] words = text.trim().split("\\s+");

        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (font.getStringBounds(candidate, frc).getWidth() <= maxWidth) {
                current = new StringBuilder(candidate);
            } else {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    // 단어 하나가 이미 maxWidth를 넘는 경우 (띄어쓰기 없는 긴 한글 등) 글자 단위로 자름
                    lines.addAll(splitByCharacter(word, font, frc, maxWidth));
                    current = new StringBuilder();
                }
                if (lines.size() >= maxLines) {
                    return lines;
                }
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private List<String> splitByCharacter(String word, Font font, FontRenderContext frc, int maxWidth) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (char c : word.toCharArray()) {
            String candidate = current.toString() + c;
            if (font.getStringBounds(candidate, frc).getWidth() <= maxWidth) {
                current.append(c);
            } else {
                if (!current.isEmpty()) {
                    result.add(current.toString());
                }
                current = new StringBuilder(String.valueOf(c));
            }
        }
        if (!current.isEmpty()) {
            result.add(current.toString());
        }
        return result;
    }

    private void drawCentered(Graphics2D g, String text, int centerX, int y) {
        FontRenderContext frc = g.getFontRenderContext();
        Rectangle2D bounds = g.getFont().getStringBounds(text, frc);
        float x = centerX - (float) bounds.getWidth() / 2;
        g.drawString(text, x, y);
    }

    private Font loadFont(String classpathPath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(classpathPath)) {
            if (inputStream == null) {
                throw new IllegalStateException("폰트 파일을 찾을 수 없습니다: " + classpathPath);
            }
            return Font.createFont(Font.TRUETYPE_FONT, inputStream);
        } catch (IOException | FontFormatException e) {
            throw new IllegalStateException("폰트를 불러올 수 없습니다: " + classpathPath, e);
        }
    }

}