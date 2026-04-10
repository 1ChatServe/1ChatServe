package chat.aikf.ai.rag.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档解析器工厂
 */
public class DocumentParserFactory {
    
    private static final Map<String, DocumentParser> parserMap = new HashMap<>();
    
    static {
        // 初始化所有解析器
        registerParser(new PdfDocumentParser());
        registerParser(new WordDocumentParser());
        registerParser(new MarkdownDocumentParser());
        registerParser(new TextDocumentParser());
        registerParser(new HtmlDocumentParser());
    }
    
    /**
     * 注册解析器
     * @param parser 解析器实例
     */
    private static void registerParser(DocumentParser parser) {
        for (String extension : parser.getSupportedExtensions()) {
            parserMap.put(extension.toLowerCase(), parser);
        }
    }
    
    /**
     * 根据文件扩展名获取解析器
     * @param extension 文件扩展名
     * @return 对应的解析器
     * @throws IllegalArgumentException 如果没有找到对应的解析器
     */
    public static DocumentParser getParser(String extension) {
        DocumentParser parser = parserMap.get(extension.toLowerCase());
        if (parser == null) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }
        return parser;
    }
    
    /**
     * 检查文件类型是否支持
     * @param extension 文件扩展名
     * @return 是否支持
     */
    public static boolean isSupported(String extension) {
        return parserMap.containsKey(extension.toLowerCase());
    }
}
