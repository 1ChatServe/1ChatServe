package chat.aikf.ai.rag.parser;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 文档解析器接口
 */
public interface DocumentParser {
    
    /**
     * 解析文档
     * @param inputStream 输入流
     * @param metadata 元数据
     * @return 解析后的文档
     * @throws IOException 解析异常
     */
    Document parse(ByteArrayInputStream inputStream, Metadata metadata) throws IOException;
    
    /**
     * 获取支持的文件扩展名
     * @return 支持的文件扩展名数组
     */
    String[] getSupportedExtensions();
    
    /**
     * 提取文档页码（如果支持）
     * @param inputStream 输入流
     * @return 页码数
     * @throws IOException 提取异常
     */
    default int extractPageCount(ByteArrayInputStream inputStream) throws IOException {
        return 1; // 默认返回1页
    }
}
