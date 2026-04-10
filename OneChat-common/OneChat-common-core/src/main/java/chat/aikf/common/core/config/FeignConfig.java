package chat.aikf.common.core.config;

import feign.Logger;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import chat.aikf.common.core.utils.RequestContextHelper;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public RequestInterceptor passAllHeadersInterceptor() {
        return template -> {
            try {
                // 获取当前请求
                ServletRequestAttributes attributes = (ServletRequestAttributes)
                        RequestContextHolder.currentRequestAttributes();
                HttpServletRequest request = attributes.getRequest();

                log.info("Feign拦截器开始，当前请求URL: {}", request.getRequestURL());
                log.info("Feign目标URL: {}", template.url());

                List<String> passedHeaders = new ArrayList<>();

                // 1. 传递真实IP
                String originalIp = RequestContextHelper.getOriginalIp(request);
                if (originalIp != null && !originalIp.trim().isEmpty()) {
                    template.header("X-Original-Client-IP", originalIp);
                    passedHeaders.add("X-Original-Client-IP");
                    log.debug("传递头部: X-Original-Client-IP = {}", originalIp);
                }

                // 2. 传递User-Agent
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && !userAgent.trim().isEmpty()) {
                    template.header("User-Agent", userAgent);
                    passedHeaders.add("User-Agent");
                    log.debug("传递头部: User-Agent = {}", userAgent);
                }

                // 3. 传递Accept-Language
                String acceptLanguage = request.getHeader("Accept-Language");
                if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
                    template.header("Accept-Language", acceptLanguage);
                    passedHeaders.add("Accept-Language");
                    log.debug("传递头部: Accept-Language = {}", acceptLanguage);
                }

                log.info("Feign传递了 {} 个头部: {}", passedHeaders.size(), passedHeaders);
                log.info("Feign最终头部: {}", template.headers());

            } catch (IllegalStateException e) {
                log.warn("没有Web请求上下文");
            } catch (Exception e) {
                log.error("Feign拦截器异常", e);
            }
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 判断是否需要跳过某些头部
     */
    private boolean shouldSkipHeader(String headerName) {
        String lowerHeader = headerName.toLowerCase();

        // 跳过以下头部
        return lowerHeader.contains("content-length") ||  // 长度会变
                lowerHeader.contains("host") ||            // 主机名会变
                lowerHeader.contains("connection") ||      // 连接头部
                lowerHeader.contains("accept-encoding") || // 编码头部
                lowerHeader.contains("cookie");           // Cookie敏感
    }
}