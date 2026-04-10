package chat.aikf.ai.utils;

/**
 * 文本清理工具类
 * 用于清理解析后的文本中的无效字符，提高文本片段的质量
 */
public class TextCleaner {
    
    /**
     * 清理文本中的无效字符
     * @param text 原始文本
     * @return 清理后的文本
     */
    public static String cleanText(String text) {
        if (text == null) {
            return "";
        }
        
        // 1. 移除控制字符（除了换行、回车、制表符）
        text = text.replaceAll("[\\p{Cntrl}&&[^\\n\\r\\t]]", "");
        
        // 2. 移除连续的空白字符（保留一个空格）
        text = text.replaceAll("\\s+", " ");
        
        // 3. 移除行首行尾的空白字符
        text = text.trim();
        
        // 4. 移除HTML实体编码
        text = text.replaceAll("&[a-zA-Z]+;", "");
        
        // 5. 移除特殊字符（保留常见标点符号）
        text = text.replaceAll("[^\\p{L}\\p{N}\\s.,?!;:()\\[\\]{}'\"-]", "");
        
        // 6. 移除连续的标点符号（保留一个）
        text = text.replaceAll("([.,?!;:])\\1+", "$1");
        
        return text;
    }
    
    /**
     * 检查文本是否有效
     * @param text 文本
     * @return 是否有效
     */
    public static boolean isValidText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // 检查文本长度
        if (text.length() < 3) {
            return false;
        }
        
        // 检查文本是否主要由空白字符组成
        int nonWhitespaceCount = text.replaceAll("\\s", "").length();
        return nonWhitespaceCount > 2;
    }
}