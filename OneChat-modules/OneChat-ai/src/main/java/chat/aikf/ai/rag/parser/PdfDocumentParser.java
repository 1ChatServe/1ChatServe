package chat.aikf.ai.rag.parser;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * PDF文档解析器
 */
public class PdfDocumentParser implements DocumentParser {
    
    private static final Logger log = LoggerFactory.getLogger(PdfDocumentParser.class);
    private final ApacheTikaDocumentParser tikaParser = new ApacheTikaDocumentParser();
    
    @Override
    public Document parse(ByteArrayInputStream inputStream, Metadata metadata) throws IOException {

        return tikaParser.parse(inputStream);
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[] {"pdf"};
    }
    
    @Override
    public int extractPageCount(ByteArrayInputStream inputStream) throws IOException {
        try (PDDocument pdfDocument = PDDocument.load(inputStream)) {
            return pdfDocument.getNumberOfPages();
        } catch (Exception e) {
            log.warn("提取PDF页码失败：{}", e.getMessage());
            return 1;
        }
    }
}
