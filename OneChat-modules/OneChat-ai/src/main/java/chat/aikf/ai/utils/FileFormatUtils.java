package chat.aikf.ai.utils;

import java.util.Arrays;
import java.util.List;

/**
 * 文件格式工具类
 */
public class FileFormatUtils {
    
    // 支持的文件扩展名列表
    public static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
        "pdf", "doc", "docx", "txt", "md", "html", "htm"
    );
    
    /**
     * 检查文件类型是否支持
     * @param fileName 文件名
     * @return 是否支持
     */
    public static boolean isSupportedFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        String extension = getFileExtension(fileName);
        return SUPPORTED_EXTENSIONS.contains(extension.toLowerCase());
    }
    
    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 根据文件扩展名获取友好的文件类型
     * @param fileName 文件名
     * @return 友好的文件类型
     */
    public static String getFriendlyFileType(String fileName) {
        if (fileName == null) {
            return "未知";
        }
        
        String extension = getFileExtension(fileName);
        return getFriendlyFileTypeByExtension(extension);
    }
    
    /**
     * 根据文件扩展名获取友好的文件类型
     * @param extension 文件扩展名
     * @return 友好的文件类型
     */
    public static String getFriendlyFileTypeByExtension(String extension) {
        if (extension == null) {
            return "未知";
        }
        
        extension = extension.toLowerCase();
        switch (extension) {
            case "doc":
            case "docx":
                return "Word";
            case "xls":
            case "xlsx":
                return "Excel";
            case "ppt":
            case "pptx":
                return "PowerPoint";
            case "pdf":
                return "PDF";
            case "txt":
                return "TXT";
            case "md":
                return "Markdown";
            case "html":
            case "htm":
                return "HTML";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取支持的文件类型列表
     * @return 支持的文件类型列表
     */
    public static List<String> getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }
    
    /**
     * 获取支持的文件类型描述
     * @return 支持的文件类型描述
     */
    public static String getSupportedFileTypesDescription() {
        return "PDF, DOCX, PPTX, TXT, Markdown, HTML, Excel";
    }
}
