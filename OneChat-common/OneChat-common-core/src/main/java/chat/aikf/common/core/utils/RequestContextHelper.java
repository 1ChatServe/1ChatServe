package chat.aikf.common.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class RequestContextHelper {

    /**
     * 获取原始客户端IP
     */
    public static String getOriginalIp(HttpServletRequest request) {
        // 1. 从网关传递的头部获取
        String originalIp = request.getHeader("X-Original-Client-IP");
        if (originalIp != null && !originalIp.isEmpty()) {
            return originalIp;
        }

        // 2. 从当前请求获取
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取原始请求方法
     */
    public static String getOriginalMethod(HttpServletRequest request) {
        String method = request.getHeader("X-Original-Method");
        return method != null ? method : request.getMethod();
    }

    /**
     * 获取原始URI
     */
    public static String getOriginalUri(HttpServletRequest request) {
        String uri = request.getHeader("X-Original-URI");
        return uri != null ? uri : request.getRequestURL().toString();
    }

    /**
     * 获取完整原始信息
     */
    public static Map<String, String> getOriginalRequestInfo(HttpServletRequest request) {
        Map<String, String> info = new HashMap<>();

        info.put("ip", getOriginalIp(request));
        info.put("method", getOriginalMethod(request));
        info.put("uri", getOriginalUri(request));
        info.put("path", request.getHeader("X-Original-Path"));
        info.put("userAgent", request.getHeader("User-Agent"));

        // 获取所有头部（用于调试）
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String name = headers.nextElement();
            info.put("header_" + name, request.getHeader(name));
        }

        return info;
    }
}