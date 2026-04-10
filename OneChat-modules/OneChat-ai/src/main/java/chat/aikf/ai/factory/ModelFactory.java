package chat.aikf.ai.factory;

import chat.aikf.ai.api.domain.OneChatModelConfig;
import chat.aikf.ai.config.AiModelsProperties;
import chat.aikf.ai.service.IOneChatModelConfigService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.*;


@Component
public class ModelFactory {

    private static final Logger log = LoggerFactory.getLogger(ModelFactory.class);

    private final IOneChatModelConfigService modelConfigService;

    private final AiModelsProperties aiModelsProperties;

    // 聊天模型缓存
    private final Map<String, ChatLanguageModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<String, StreamingChatLanguageModel> streamingModelCache = new ConcurrentHashMap<>();
    // 嵌入模型缓存
    private final Map<String, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();
    // 图像模型缓存
    private final Map<String, Object> imageModelCache = new ConcurrentHashMap<>();

    public ModelFactory(IOneChatModelConfigService modelConfigService,AiModelsProperties aiModelsProperties) {
        this.modelConfigService = modelConfigService;
        this.aiModelsProperties =aiModelsProperties;
    }

    @PostConstruct
    public void initializeModels() {
        // 从数据库获取所有模型配置
        List<OneChatModelConfig> modelConfigs = modelConfigService.list();
        
        // 存储当前启用的模型名称，用于后续清理已暂停的模型
        Set<String> enabledModelNames = new HashSet<>();

        for (OneChatModelConfig config : modelConfigs) {
            // 只处理启用状态的模型
            if (config.getModelStatus() == 1) {
                String modelName = config.getModelName().trim();
                enabledModelNames.add(modelName);

                // 验证配置有效性
                if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
                    log.warn("模型 [{}] API Key 为空，跳过初始化", modelName);
                    continue;
                }

                try {
                    // 根据模型类型创建不同类型的模型
                    String modelType = config.getModelType();
                    if (OneChatModelConfig.MODEL_TYPE_CHAT.equals(modelType)) {
                        // 对话模型
                        // 构建同步模型
                        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                                .apiKey(config.getApiKey())
                                .baseUrl(config.getBaseUrl())
                                .modelName(config.getModelName())
                                .timeout(Duration.ofSeconds(30))
                                .build();
                        chatModelCache.put(modelName, chatModel);

                        // 构建流式模型
                        StreamingChatLanguageModel streamingModel = OpenAiStreamingChatModel.builder()
                                .apiKey(config.getApiKey())
                                .baseUrl(config.getBaseUrl())
                                .modelName(config.getModelName())
                                .timeout(Duration.ofSeconds(30))
                                .build();
                        streamingModelCache.put(modelName, streamingModel);

                        log.info("已加载对话模型: {} | 同步={}, 流式={}",
                                modelName,
                                chatModel.getClass().getSimpleName(),
                                streamingModel.getClass().getSimpleName());
                    } else if (OneChatModelConfig.MODEL_TYPE_EMBEDDING.equals(modelType)) {
                        // 嵌入(向量)模型
                        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                                .apiKey(config.getApiKey())
                                .baseUrl(config.getBaseUrl())
                                .modelName(config.getModelName())
                                .dimensions(aiModelsProperties.getMilvus().getDimension())
                                .build();
                        embeddingModelCache.put(modelName, embeddingModel);

                        log.info("已加载嵌入模型: {} | 类型={}",
                                modelName,
                                embeddingModel.getClass().getSimpleName());







                    } else if (OneChatModelConfig.MODEL_TYPE_IMAGE.equals(modelType)) {
                        // 图像模型
                        // 这里可以根据需要实现图像模型的创建
                        // 暂时使用占位符
                        imageModelCache.put(modelName, new Object());
                        log.info("已加载图像模型: {}", modelName);
                    } else {
                        log.warn("未知模型类型: {}，跳过初始化", modelType);
                    }
                } catch (Exception e) {
                    log.error("加载模型 [{}] 失败: {}", modelName, e.getMessage());
                }
            }
        }

        // 清理已暂停的模型
        cleanupDisabledModels(enabledModelNames);

        if (chatModelCache.isEmpty()) {
            log.warn("未加载任何 AI 模型！请检查模型配置");
        }
    }

    /**
     * 清理已暂停的模型
     * @param enabledModelNames 当前启用的模型名称集合
     */
    private void cleanupDisabledModels(Set<String> enabledModelNames) {
        // 清理同步模型
        chatModelCache.keySet().removeIf(key -> !enabledModelNames.contains(key));
        // 清理流式模型
        streamingModelCache.keySet().removeIf(key -> !enabledModelNames.contains(key));
        // 清理嵌入模型
        embeddingModelCache.keySet().removeIf(key -> !enabledModelNames.contains(key));
        // 清理图像模型
        imageModelCache.keySet().removeIf(key -> !enabledModelNames.contains(key));
    }

    /**
     * 刷新模型配置
     * 当模型配置发生变化时调用此方法
     */
    public void refreshModels() {
        log.info("开始刷新模型配置...");
        initializeModels();
        log.info("模型配置刷新完成");
    }

    public ChatLanguageModel getChatModel(String modelName) {
        ChatLanguageModel model = chatModelCache.get(modelName);
        if (model == null) {
            throw new IllegalArgumentException("同步模型未启用或配置缺失: " + modelName +
                    "，可用模型: " + chatModelCache.keySet());
        }
        return model;
    }

    public StreamingChatLanguageModel getStreamingModel(String modelName) {
        StreamingChatLanguageModel model = streamingModelCache.get(modelName);
        if (model == null) {
            throw new IllegalArgumentException("流式模型未启用或配置缺失: " + modelName +
                    "，可用模型: " + streamingModelCache.keySet());
        }
        return model;
    }

    public EmbeddingModel getEmbeddingModel(String modelName) {
        EmbeddingModel model = embeddingModelCache.get(modelName);
        if (model == null) {
            throw new IllegalArgumentException("嵌入模型未启用或配置缺失: " + modelName +
                    "，可用模型: " + embeddingModelCache.keySet());
        }
        return model;
    }

    public List<String> getEnabledModels() {
        return new ArrayList<>(chatModelCache.keySet());
    }
}