package chat.aikf.ai.rag.parser;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 文本文件解析器
 */
public class TextDocumentParser implements DocumentParser {
    
    private final ApacheTikaDocumentParser tikaParser = new ApacheTikaDocumentParser();
    
    @Override
    public Document parse(ByteArrayInputStream inputStream, Metadata metadata) throws IOException {
        return tikaParser.parse(inputStream);
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[] {"txt"};
    }
}
