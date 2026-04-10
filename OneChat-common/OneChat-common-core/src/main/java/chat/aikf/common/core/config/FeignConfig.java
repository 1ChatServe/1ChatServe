package chat.aikf.common.core.config;

import feign.Logger;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

                // 1. 获取所有头部名称
                Enumeration<String> headerNames = request.getHeaderNames();
                List<String> passedHeaders = new ArrayList<>();

                // 2. 遍历并传递所有头部
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = request.getHeader(headerName);

                    // 跳过一些不需要传递的头部
                    if (shouldSkipHeader(headerName)) {
                        log.debug("跳过头部: {}", headerName);
                        continue;
                    }

                    if (headerValue != null && !headerValue.trim().isEmpty()) {
                        template.header(headerName, headerValue);
                        passedHeaders.add(headerName);
                        log.debug("传递头部: {} = {}", headerName, headerValue);
                    }
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