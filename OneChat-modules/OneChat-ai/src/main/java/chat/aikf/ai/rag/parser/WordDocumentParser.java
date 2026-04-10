package chat.aikf.ai.rag.parser;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Word文档解析器
 */
public class WordDocumentParser implements DocumentParser {
    
    private final ApacheTikaDocumentParser tikaParser = new ApacheTikaDocumentParser();
    
    @Override
    public Document parse(ByteArrayInputStream inputStream, Metadata metadata) throws IOException {
        return tikaParser.parse(inputStream);
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[] {"doc", "docx"};
    }
}
