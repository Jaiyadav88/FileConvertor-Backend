package net.convertor.application.FileConvertorApp.Utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ConversionUtils {
    public static Path convert(Path input, String targetFormat) throws IOException {
        String inExt = FilenameUtils.getExtension(input.toString()).toLowerCase();
        String base = FilenameUtils.removeExtension(input.toString());
        Path output = Paths.get(base + "." + targetFormat);
        switch (inExt + "->" + targetFormat) {
            case "txt->pdf":
                txtToPdf(input, output);
                break;
            case "jpg->png":
                jpgToPng(input, output);
                break;
            case "png->jpg":
                imageConvert(input, output);
                break;
            case "pdf->txt":
                pdfToTxt(input, output);
            case "pdf->jpg":
                pdfTojpg(input, output);
                break;
            case "pdf->docx":
                pdfToword(input, output);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported conversion: " + inExt + " to " + targetFormat);
        }
        return output;
    }
    private static void pdfToword(Path pdfPath, Path wordPath) throws IOException {
        //logic for converting PDF to Word
        String text;
        try (PDDocument pdf = PDDocument.load(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            text = stripper.getText(pdf);
            try (XWPFDocument docx = new XWPFDocument();
                 FileOutputStream out = new FileOutputStream(wordPath.toFile())) {
                XWPFParagraph para = docx.createParagraph();
                XWPFRun run = para.createRun();
                for (String line : text.split("\\r?\\n")) {
                    run.setText(line);
                    run.addBreak();
                }
                docx.write(out);
            }
        }
    }
    private static void pdfTojpg(Path pdfPath, Path jpgPath) throws IOException {
        try (PDDocument doc = PDDocument.load(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage image = renderer.renderImageWithDPI(0, 300, ImageType.RGB);
            ImageIO.write(image, "jpg", jpgPath.toFile());
        }
    }
    private static void pdfToTxt(Path pdfPath, Path txtPath) throws IOException {
        try (PDDocument doc = PDDocument.load(pdfPath.toFile(), (String) null)) {
            if (doc.isEncrypted()) {
                doc.setAllSecurityToBeRemoved(true);
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(doc.getNumberOfPages());
            String text = stripper.getText(doc);
            Files.writeString(txtPath, text, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }
    }
    private static void jpgToPng(Path jpgPath, Path pngPath) throws IOException {
        BufferedImage img = ImageIO.read(jpgPath.toFile());
        ImageIO.write(img, "png", pngPath.toFile());
    }
    private static void txtToPdf(Path txtPath, Path pdfPath) throws IOException {
        // Constants
        final PDFont FONT = PDType1Font.HELVETICA;
        final float FONT_SIZE = 12;
        final float LEADING = 1.2f * FONT_SIZE;
        final PDRectangle PAGE_SIZE = PDRectangle.LETTER;
        final float MARGIN = 50;
        final float WIDTH = PAGE_SIZE.getWidth()  - 2 * MARGIN;
        final float START_Y = PAGE_SIZE.getHeight() - MARGIN;
        try (PDDocument doc = new PDDocument();
             BufferedReader reader = Files.newBufferedReader(txtPath)) {
            PDPage page = new PDPage(PAGE_SIZE);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.beginText();
            cs.setFont(FONT, FONT_SIZE);
            cs.newLineAtOffset(MARGIN, START_Y);
            float currentY = START_Y;

            String rawLine;
            while ((rawLine = reader.readLine()) != null) {
                for (String wrapped : wrapLine(rawLine, FONT, FONT_SIZE, WIDTH)) {
                    if (currentY - LEADING <= MARGIN) {
                        cs.endText();
                        cs.close();
                        page = new PDPage(PAGE_SIZE);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        cs.beginText();
                        cs.setFont(FONT, FONT_SIZE);
                        currentY = START_Y;
                        cs.newLineAtOffset(MARGIN, currentY);
                    }
                    cs.showText(wrapped);
                    cs.newLineAtOffset(0, -LEADING);
                    currentY -= LEADING;
                }
            }

            cs.endText();
            cs.close();

            doc.save(pdfPath.toFile());
        }
    }
    private static List<String> wrapLine(String text,
                                         PDFont font,
                                         float fontSize,
                                         float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String candidate = line.length() == 0
                    ? word
                    : line + " " + word;

            float size = font.getStringWidth(candidate) / 1000 * fontSize;
            if (size > maxWidth) {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    lines.add(candidate);
                    line = new StringBuilder();
                }
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }
    private static void imageConvert(Path in, Path out) throws IOException {
        BufferedImage img = ImageIO.read(in.toFile());
        ImageIO.write(img, FilenameUtils.getExtension(out.toString()), out.toFile());
    }
}
