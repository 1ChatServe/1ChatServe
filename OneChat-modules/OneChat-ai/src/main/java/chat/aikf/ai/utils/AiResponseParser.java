package chat.aikf.ai.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI响应解析工具类
 * 用于处理AI模型返回的各种格式响应，提取有效的JSON数据
 */
@Slf4j
public class AiResponseParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 匹配 ```json ... ``` 或 ``` ... ``` 格式的代码块
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");

    // 匹配 { ... } 格式的JSON对象
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{[\\s\\S]*\\}");

    /**
     * 解析AI响应，提取并清理JSON数据
     *
     * @param aiResponse AI模型的原始响应
     * @return 清理后的JSON字符串
     */
    public static String extractJson(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            log.warn("AI响应为空");
            return null;
        }

        String cleaned = aiResponse.trim();

        // 1. 尝试提取代码块中的内容
        Matcher codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(cleaned);
        if (codeBlockMatcher.find()) {
            cleaned = codeBlockMatcher.group(1).trim();
            log.debug("从代码块中提取JSON");
        }

        // 2. 移除可能的markdown标记
        cleaned = cleaned.replaceAll("^\\s*```\\s*json?\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```\\s*$", "");

        // 3. 提取JSON对象
        Matcher jsonMatcher = JSON_OBJECT_PATTERN.matcher(cleaned);
        if (jsonMatcher.find()) {
            cleaned = jsonMatcher.group();
            log.debug("提取JSON对象");
        }

        // 4. 清理特殊字符
        cleaned = cleaned.replaceAll("\\n\\s*", ""); // 移除换行和多余空格
        cleaned = cleaned.replaceAll("\\r", "");    // 移除回车符

        log.debug("清理后的JSON: {}", cleaned);
        return cleaned;
    }

    /**
     * 解析AI响应并转换为指定类型的对象
     *
     * @param aiResponse AI模型的原始响应
     * @param targetClass 目标类型
     * @param <T> 泛型类型
     * @return 解析后的对象
     * @throws JsonProcessingException 当JSON解析失败时抛出
     */
    public static <T> T parseResponse(String aiResponse, Class<T> targetClass) throws JsonProcessingException {
        String json = extractJson(aiResponse);
        if (json == null || json.isEmpty()) {
            throw new JsonProcessingException("无法从AI响应中提取有效的JSON数据") {};
        }

        try {
            return objectMapper.readValue(json, targetClass);
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败，原始响应: {}", aiResponse);
            log.error("清理后的JSON: {}", json);
            throw e;
        }
    }

    /**
     * 验证字符串是否为有效的JSON格式
     *
     * @param jsonString 待验证的字符串
     * @return 是否为有效的JSON
     */
    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 清理并格式化JSON字符串
     *
     * @param jsonString 原始JSON字符串
     * @return 格式化后的JSON字符串
     * @throws JsonProcessingException 当JSON解析失败时抛出
     */
    public static String formatJson(String jsonString) throws JsonProcessingException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }

        Object json = objectMapper.readValue(jsonString, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    /**
     * 处理AI响应中的常见格式问题
     * - 移除控制字符
     * - 修复转义问题
     * - 处理Unicode字符
     *
     * @param aiResponse AI模型的原始响应
     * @return 处理后的字符串
     */
    public static String sanitizeResponse(String aiResponse) {
        if (aiResponse == null) {
            return null;
        }

        String sanitized = aiResponse;

        // 移除控制字符（保留换行和制表符）
        sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");

        // 修复常见的转义问题
        sanitized = sanitized.replace("\\\\", "\\");
        sanitized = sanitized.replace("\\'", "'");

        // 处理多余的空格
        sanitized = sanitized.replaceAll("\\s+", " ");

        return sanitized.trim();
    }
}
