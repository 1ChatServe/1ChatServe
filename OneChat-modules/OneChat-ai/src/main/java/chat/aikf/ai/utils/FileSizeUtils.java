package chat.aikf.ai.utils;

/**
 * 文件大小工具类
 */
public class FileSizeUtils {
    
    /**
     * 格式化文件大小
     * @param bytes 文件大小（字节）
     * @return 格式化后的文件大小，带单位
     */
    public static String formatFileSize(double bytes) {
        if (bytes < 0) {
            return "0KB";
        }
        
        // 小于1KB
        if (bytes < 1024) {
            return Math.round(bytes) + "B";
        }
        // 小于1MB
        else if (bytes < 1024 * 1024) {
            return Math.round(bytes / 1024.0) + "KB";
        }
        // 小于1GB
        else if (bytes < 1024 * 1024 * 1024) {
            return Math.round(bytes / (1024.0 * 1024.0)) + "MB";
        }
        // 大于等于1GB
        else {
            return Math.round(bytes / (1024.0 * 1024.0 * 1024.0)) + "GB";
        }
    }
}
