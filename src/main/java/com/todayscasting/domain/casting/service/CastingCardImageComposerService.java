package com.todayscasting.domain.casting.service;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// 다운로드용 캐스팅 카드 이미지를 만든다. (오늘의 결과 화면 "오늘의 카드 다운로드" 버튼용)
// 배경 이미지 위에 날짜 / "TODAY'S CASTING" 라벨 / 배역명만 합성한다.
// 팀 논의 결과, 하트 아이콘과 하단 정보 패널(장르/한줄기록/기억에 남은 장면)은 제외하기로 함
// (텍스트 박스가 없는 쪽이 더 "저장하고 싶은" 느낌을 준다는 의견, 2026-08-19).
@Component
public class CastingCardImageComposerService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final String LABEL_TEXT = "TODAY'S CASTING";

    private static final Color GOLD = new Color(240, 200, 140);
    private static final Color GOLD_DIM = new Color(220, 180, 130, 220);

    private final Font labelFont;
    private final Font dateFont;
    private final Font roleNameFontBase;

    public CastingCardImageComposerService() {
        this.labelFont = loadFont("fonts/NanumGothic-Bold.ttf").deriveFont(24f);
        this.dateFont = loadFont("fonts/NanumGothic-Bold.ttf").deriveFont(34f);
        this.roleNameFontBase = loadFont("fonts/BlackHanSans-Regular.ttf");
    }

    /**
     * 배경 이미지 바이트 위에 날짜/라벨/배역명을 그려서 최종 PNG 바이트를 반환한다.
     */
    public byte[] compose(byte[] backgroundBytes, LocalDate recordDate, String roleName) {
        try {
            BufferedImage background = ImageIO.read(new ByteArrayInputStream(backgroundBytes));
            if (background == null) {
                throw new IllegalStateException("배경 이미지를 읽을 수 없습니다 (지원하지 않는 형식이거나 손상됨).");
            }

            int width = background.getWidth();
            int height = background.getHeight();

            BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g.drawImage(background, 0, 0, width, height, null);

            drawTopScrim(g, width, height);
            drawTexts(g, width, height, recordDate, roleName);

            g.dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(canvas, "png", output);
            return output.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("카드 이미지 합성에 실패했습니다: " + e.getMessage(), e);
        }
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
        int y = (int) (height * 0.08);

        // 날짜
        g.setFont(dateFont);
        g.setColor(GOLD);
        String dateText = recordDate.format(DATE_FORMAT);
        drawCentered(g, dateText, centerX, y);
        y += 44;

        // TODAY'S CASTING 라벨
        g.setFont(labelFont);
        g.setColor(GOLD_DIM);
        drawCentered(g, LABEL_TEXT, centerX, y);
        y += 60;

        // 배역명 (길면 자동으로 줄바꿈, 최대 2줄)
        String safeRoleName = (roleName == null || roleName.isBlank()) ? "오늘의 주인공" : roleName;
        drawRoleName(g, safeRoleName, centerX, y, width);
    }

    // 배역명은 화면 폭에 맞춰 필요하면 두 줄로 자동 줄바꿈한다 (긴 배역명 대비).
    private void drawRoleName(Graphics2D g, String roleName, int centerX, int startY, int canvasWidth) {
        float fontSize = 72f;
        int maxWidth = (int) (canvasWidth * 0.85);

        Font font = roleNameFontBase.deriveFont(fontSize);
        g.setFont(font);
        FontRenderContext frc = g.getFontRenderContext();

        java.util.List<String> lines = wrapToLines(roleName, font, frc, maxWidth, 2);

        // 두 줄인데도 여전히 넘치면 폰트를 줄여서 다시 시도
        while (lines.size() > 2 || exceedsWidth(lines, font, frc, maxWidth)) {
            fontSize -= 6f;
            if (fontSize < 36f) {
                break;
            }
            font = roleNameFontBase.deriveFont(fontSize);
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

    private boolean exceedsWidth(java.util.List<String> lines, Font font, FontRenderContext frc, int maxWidth) {
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
    private java.util.List<String> wrapToLines(String text, Font font, FontRenderContext frc, int maxWidth, int maxLines) {
        java.util.List<String> lines = new java.util.ArrayList<>();
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

    private java.util.List<String> splitByCharacter(String word, Font font, FontRenderContext frc, int maxWidth) {
        java.util.List<String> result = new java.util.ArrayList<>();
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