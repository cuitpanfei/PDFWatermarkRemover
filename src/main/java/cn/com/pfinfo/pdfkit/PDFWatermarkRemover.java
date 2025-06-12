package cn.com.pfinfo.pdfkit;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PDFWatermarkRemover {

    public static void tryDoWatermarkRemove(String inputPath,int targetPageIndex) throws IOException {
        File f = new File(inputPath);
        try (PDDocument document = PDDocument.load(f)) {
            PDPage page = document.getPage(targetPageIndex);
            PDFStreamParser parser = new PDFStreamParser(page);
            parser.parse();
            List<Object> tokens = parser.getTokens();
            // 创建只包含修改页面的新文档
            try (PDDocument newDoc = new PDDocument()) {
                for (int i = 0; i < tokens.size(); i++) {
                    // 创建删除指定元素后的页面（传入原页面以复制属性）
                    PDPage modifiedPage = createModifiedPage(document, page, tokens, i);
                    newDoc.addPage(modifiedPage);
                }
                // 生成带时间戳的文件名
                String outputPath = String.format("%s_output_%04d.pdf",inputPath.substring(0,inputPath.lastIndexOf('.')), 1);
                // 保存文档
                newDoc.save(outputPath);
                System.out.println("Generated: " + outputPath);
            }
        }
    }


    // 创建删除指定元素后的页面（添加参数originalPage用于复制属性）
    private static PDPage createModifiedPage(PDDocument doc, PDPage originalPage, List<Object> tokens, int removeIndex)
            throws IOException {

        // 创建新页面，复制原页面的媒体框
        PDPage newPage = new PDPage(originalPage.getMediaBox());

        // 复制其他页面属性
        newPage.setCropBox(originalPage.getCropBox());
        newPage.setBleedBox(originalPage.getBleedBox());
        newPage.setTrimBox(originalPage.getTrimBox());
        newPage.setArtBox(originalPage.getArtBox());
        newPage.setRotation(originalPage.getRotation());
        // 复制资源
        newPage.setResources(originalPage.getResources());

        PDStream newContent = new PDStream(doc);

        try (OutputStream os = newContent.createOutputStream()) {
            org.apache.pdfbox.pdfwriter.ContentStreamWriter tokenWriter = new org.apache.pdfbox.pdfwriter.ContentStreamWriter(os);
            Operator EMPTY_TOKEN = Operator.getOperator(OperatorName.BEGIN_INLINE_IMAGE_DATA);
            // 替换目标元素为 EMPTY_TOKEN
            List<Object> handleTokens = new ArrayList<>();
            for (int i = 0; i < tokens.size(); i++) {
                Object token = tokens.get(i);
                if (i != removeIndex) {
                    handleTokens.add(token);
                } else {
                    handleTokens.add(EMPTY_TOKEN);
                }
            }
            tokenWriter.writeTokens(handleTokens);
        }
        newPage.setContents(newContent);
        return newPage;
    }

}