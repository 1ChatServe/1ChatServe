package chat.aikf.ai.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件类型工具类
 */
public class FileTypeUtils {
    
    // MIME类型到文件类型的映射
    private static final Map<String, String> MIME_TYPE_MAP = new HashMap<>();
    
    static {
        // 文档类型
        MIME_TYPE_MAP.put("application/pdf", "PDF");
        MIME_TYPE_MAP.put("application/msword", "Word");
        MIME_TYPE_MAP.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Word");
        MIME_TYPE_MAP.put("application/vnd.ms-excel", "Excel");
        MIME_TYPE_MAP.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel");
        MIME_TYPE_MAP.put("application/vnd.ms-powerpoint", "PowerPoint");
        MIME_TYPE_MAP.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "PowerPoint");
        
        // 文本类型
        MIME_TYPE_MAP.put("text/plain", "文本");
        MIME_TYPE_MAP.put("text/markdown", "Markdown");
        MIME_TYPE_MAP.put("text/html", "HTML");
        
        // 图片类型
        MIME_TYPE_MAP.put("image/jpeg", "图片");
        MIME_TYPE_MAP.put("image/png", "图片");
        MIME_TYPE_MAP.put("image/gif", "图片");
        MIME_TYPE_MAP.put("image/webp", "图片");
        
        // 其他类型
        MIME_TYPE_MAP.put("application/json", "JSON");
        MIME_TYPE_MAP.put("application/xml", "XML");
    }
    
    /**
     * 获取友好的文件类型名称
     * @param mimeType MIME类型
     * @return 友好的文件类型名称
     */
    public static String getFriendlyFileType(String mimeType) {
        if (mimeType == null) {
            return "未知";
        }
        
        // 从映射中获取友好名称
        String friendlyType = MIME_TYPE_MAP.get(mimeType);
        if (friendlyType != null) {
            return friendlyType;
        }
        
        // 如果没有找到映射，返回MIME类型的主类型
        int slashIndex = mimeType.indexOf('/');
        if (slashIndex != -1) {
            return mimeType.substring(0, slashIndex);
        }
        
        return mimeType;
    }
    
    /**
     * 根据文件名获取文件类型
     * @param fileName 文件名
     * @return 文件类型
     */
    public static String getFileTypeByFileName(String fileName) {
        if (fileName == null) {
            return "未知";
        }
        
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "PDF";
            case "doc":
            case "docx": return "Word";
            case "xls":
            case "xlsx": return "Excel";
            case "ppt":
            case "pptx": return "PowerPoint";
            case "txt": return "文本";
            case "md": return "Markdown";
            case "html": return "HTML";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "webp": return "图片";
            case "json": return "JSON";
            case "xml": return "XML";
            default: return "其他";
        }
    }
}
