package chat.aikf.ai.service.impl;

import chat.aikf.ai.api.domain.OneChatAgent;
import chat.aikf.ai.api.domain.OneChatExclModelConfig;
import chat.aikf.ai.api.domain.SessionAnalysisResult;
import chat.aikf.ai.domain.OneChatSessionAnalysis;
import chat.aikf.ai.domain.OneChatSessionKeyStep;
import chat.aikf.ai.factory.ModelFactory;
import chat.aikf.ai.prompt.PromptManager;
import chat.aikf.ai.service.IOneChatAgentService;
import chat.aikf.ai.service.IOneChatAstService;
import chat.aikf.ai.service.IOneChatExclModelConfigService;
import chat.aikf.ai.service.IOneChatKnowledgeBaseAttachmentService;
import chat.aikf.ai.service.IOneChatSessionAnalysisService;
import chat.aikf.ai.service.IOneChatSessionKeyStepService;
import chat.aikf.ai.utils.AiResponseParser;
import chat.aikf.ai.utils.PromptUtils;
import chat.aikf.common.core.constant.SecurityConstants;
import chat.aikf.common.core.domain.R;
import chat.aikf.common.core.utils.DateUtils;
import chat.aikf.common.security.utils.SecurityUtils;
import chat.aikf.ops.api.RemoteKfVisitorService;
import chat.aikf.ops.api.domain.OneChatKfVisitorMsg;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class OneChatAstServiceImpl implements IOneChatAstService {
    @Autowired
    private ModelFactory modelFactory;

    @Autowired
    private PromptManager promptManager;

    @Autowired
    private RemoteKfVisitorService remoteKfVisitorService;

    @Autowired
    private IOneChatSessionAnalysisService oneChatSessionAnalysisService;

    @Autowired
    private IOneChatSessionKeyStepService oneChatSessionKeyStepService;

    @Autowired
    private IOneChatExclModelConfigService oneChatExclModelConfigService;

    @Autowired
    private IOneChatKnowledgeBaseAttachmentService oneChatKnowledgeBaseAttachmentService;

    @Autowired
    private IOneChatAgentService oneChatAgentService;


    @Override
    public String recommendReply(String visitorMessage) {
        // 1. 构造消息
        PromptManager.PromptConfig prompt = promptManager.get("recommend-reply.recommend_reply");
        String userMsg = prompt.getUser().replace("{visitorMessage}", PromptUtils.escape(visitorMessage));

        List<ChatMessage> messages = Arrays.asList(
                SystemMessage.from(prompt.getSystem()),
                UserMessage.from(userMsg)
        );

        // 2. 获取当前用户的模型配置
        OneChatExclModelConfig modelConfig = oneChatExclModelConfigService.getOne(new LambdaQueryWrapper<OneChatExclModelConfig>()
                .eq(OneChatExclModelConfig::getUserId, SecurityUtils.getUserId())
                .last("LIMIT 1"));
        
        String modelName = null;
        if (modelConfig != null && modelConfig.getModelName() != null) {
            modelName = modelConfig.getModelName();
            log.info("使用用户配置的模型: {}", modelName);
        } else {
             throw new RuntimeException("当前未设置AI模型,请先设置AI模型");
            // // 如果用户没有配置模型，使用默认模型
            // List<String> models = modelFactory.getEnabledModels();
            // if (models.isEmpty()) {
            //     throw new RuntimeException("无可用 AI 模型，请检查 ai.models 配置");
            // }
            // modelName = models.get(0);
            // log.info("用户未配置模型，使用默认模型: {}", modelName);
        }

        try {
            // 直接获取同步模型
            ChatLanguageModel model = modelFactory.getChatModel(modelName);

            // 同步调用，直接返回完整响应
            ChatResponse chatResponse = model.chat(messages);

            String reply = Optional.ofNullable(chatResponse.aiMessage())
                    .map(AiMessage::text)
                    .filter(s -> !s.trim().isEmpty())
                    .map(String::trim)
                    .orElseThrow(() -> new RuntimeException("模型返回内容为空或无效"));

            log.info("使用模型 [{}] 成功生成回复", modelName);

            return reply;
        } catch (Exception e) {
            log.error("模型 [{}] 调用失败: {}", modelName, e.getMessage());
            throw new RuntimeException("AI 模型调用失败", e);
        }
    }

    @Override
    public SessionAnalysisResult analyzeSession(Long kfVisitorId) throws JsonProcessingException {
        // 1. 获取访客的消息列表
        log.info("开始分析访客 {} 的会话", kfVisitorId);
        R<List<OneChatKfVisitorMsg>> result = remoteKfVisitorService.getMsgList(kfVisitorId,  SecurityConstants.INNER);
        
        if (result.getData() == null || result.getData().isEmpty()) {
            throw new RuntimeException("获取访客消息列表失败或消息为空");
        }
        
        List<OneChatKfVisitorMsg> messages = result.getData();
        log.info("获取到 {} 条消息", messages.size());
        
        // 2. 构建会话文本
        StringBuilder sessionText = new StringBuilder();
        sessionText.append("会话分析开始:\n");
        
        for (OneChatKfVisitorMsg msg : messages) {
            String sender = msg.getMsgSource() == 0 ? "访客" : (msg.getMsgSource() == 1 ? "客服" : "AI");
            sessionText.append(String.format("[%s] %s: %s\n",
                    DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS,msg.getSendTime()), sender, msg.getContent()));
        }


        sessionText.append("\n会话分析结束");
        
        // 3. 从PromptManager获取提示词
        PromptManager.PromptConfig promptConfig = promptManager.get("session-analysis.session_analysis");
        String userPrompt = promptConfig.getUser().replace("{sessionText}", sessionText.toString());
        
        // 4. 调用AI模型进行分析
        List<ChatMessage> chatMessages = Arrays.asList(
                SystemMessage.from(promptConfig.getSystem()),
                UserMessage.from(userPrompt)
        );
        
        // 获取当前用户的模型配置
        OneChatExclModelConfig modelConfig = oneChatExclModelConfigService.getOne(new LambdaQueryWrapper<OneChatExclModelConfig>()
                .eq(OneChatExclModelConfig::getUserId, SecurityUtils.getUserId())
                .last("LIMIT 1"));
        
        String modelName = null;
        if (modelConfig != null && modelConfig.getModelName() != null) {
            modelName = modelConfig.getModelName();
            log.info("使用用户配置的模型: {}", modelName);
        } else {
            throw new RuntimeException("当前未设置AI模型,请先设置AI模型");
//            // 如果用户没有配置模型，使用默认模型
//            List<String> models = modelFactory.getEnabledModels();
//            if (models.isEmpty()) {
//                throw new RuntimeException("无可用 AI 模型，请检查 ai.models 配置");
//            }
//            modelName = models.get(0);
//            log.info("用户未配置模型，使用默认模型: {}", modelName);
        }
        
        String aiResponse = null;
        try {
            ChatLanguageModel model = modelFactory.getChatModel(modelName);
            ChatResponse chatResponse = model.chat(chatMessages);
            
            aiResponse = Optional.ofNullable(chatResponse.aiMessage())
                    .map(AiMessage::text)
                    .filter(s -> !s.trim().isEmpty())
                    .map(String::trim)
                    .orElseThrow(() -> new RuntimeException("模型返回内容为空或无效"));
            
            log.info("使用模型 [{}] 成功生成会话分析", modelName);
            log.info("AI返回结果: {}", aiResponse);
        } catch (Exception e) {
            log.error("模型 [{}] 调用失败: {}", modelName, e.getMessage());
            throw new RuntimeException("AI 模型调用失败", e);
        }
        
        // 5. 解析AI返回的结果并构建SessionAnalysisResult
        // 使用AiResponseParser工具类处理各种格式的AI响应
        SessionAnalysisResult analysisResult = AiResponseParser.parseResponse(aiResponse, SessionAnalysisResult.class);
        
        // 6. 将分析结果存入数据库
        try {
            // 先删除该访客的所有历史分析记录，避免数据重复
            boolean deleted = oneChatSessionAnalysisService.deleteByKfVisitorId(kfVisitorId);
            log.info("删除访客 {} 的历史分析记录: {}", kfVisitorId, deleted);
            
            // 创建会话分析记录
            OneChatSessionAnalysis sessionAnalysis = new OneChatSessionAnalysis();
            sessionAnalysis.setKfVisitorId(kfVisitorId);
            sessionAnalysis.setOverallScore(analysisResult.getComprehensiveSummary().getOverallScore());
            sessionAnalysis.setIntentionLevel(analysisResult.getComprehensiveSummary().getIntentionLevel());
            sessionAnalysis.setSessionSummary(analysisResult.getComprehensiveSummary().getSessionSummary());
            sessionAnalysis.setIntentionPercentage(analysisResult.getCustomerIntention().getIntentionPercentage());
            sessionAnalysis.setIntentionLevelText(analysisResult.getCustomerIntention().getIntentionLevel());
            sessionAnalysis.setIntentionSummary(analysisResult.getCustomerIntention().getIntentionSummary());
            sessionAnalysis.setSatisfactionPercentage(analysisResult.getSentimentAnalysis().getSatisfactionPercentage());
            sessionAnalysis.setSatisfactionLevel(analysisResult.getSentimentAnalysis().getSatisfactionLevel());
            sessionAnalysis.setSatisfactionSummary(analysisResult.getSentimentAnalysis().getSatisfactionSummary());
            sessionAnalysis.setAnalysisTime(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, new java.util.Date()));

            oneChatSessionAnalysisService.save(sessionAnalysis);
            log.info("会话分析记录保存成功，ID: {}", sessionAnalysis.getId());
            
            // 保存关键步骤
            if (analysisResult.getKeySteps() != null && !analysisResult.getKeySteps().isEmpty()) {
                List<OneChatSessionKeyStep> keySteps = new ArrayList<>();
                for (SessionAnalysisResult.KeyStep step : analysisResult.getKeySteps()) {
                    OneChatSessionKeyStep keyStep = new OneChatSessionKeyStep();
                    keyStep.setSessionAnalysisId(sessionAnalysis.getId());
                    keyStep.setStepDescription(step.getStepDescription());
                    keyStep.setTimestamp(step.getTimestamp());
                    keySteps.add(keyStep);
                }
                oneChatSessionKeyStepService.saveBatch(keySteps);
                log.info("关键步骤保存成功，数量: {}", keySteps.size());
            }
        } catch (Exception e) {
            log.error("保存会话分析结果到数据库失败", e);
            // 数据库保存失败不影响返回结果，继续执行
        }
        
        log.info("会话分析完成，访客 {} 的综合评分为 {}", kfVisitorId, analysisResult.getComprehensiveSummary().getOverallScore());
        return analysisResult;
    }

    @Override
    public String generateReplyRecommendations(Long kfVisitorId) {
        // 1. 获取访客的消息列表
        log.info("开始生成访客 {} 的回复推荐", kfVisitorId);
        R<List<OneChatKfVisitorMsg>> result = remoteKfVisitorService.getMsgList(kfVisitorId, SecurityConstants.INNER);
        
        if (result.getData() == null || result.getData().isEmpty()) {
            throw new RuntimeException("获取访客消息列表失败或消息为空");
        }
        
        List<OneChatKfVisitorMsg> messages = result.getData();
        log.info("获取到 {} 条消息", messages.size());
        
        // 2. 构建会话上下文
        StringBuilder sessionContext = new StringBuilder();
        sessionContext.append("会话上下文开始:\n");
        
        for (OneChatKfVisitorMsg msg : messages) {
            String sender = msg.getMsgSource() == 0 ? "访客" : (msg.getMsgSource() == 1 ? "客服" : "AI");
            sessionContext.append(String.format("[%s] %s: %s\n",
                    DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, msg.getSendTime()), sender, msg.getContent()));
        }
        
        sessionContext.append("\n会话上下文结束");
        
        // 3. 从PromptManager获取提示词
        PromptManager.PromptConfig promptConfig = promptManager.get("reply-recommendations.reply-recommendations");
        String userPrompt = promptConfig.getUser().replace("{sessionContext}", sessionContext.toString());
        
        // 4. 调用AI模型生成推荐回复
        List<ChatMessage> chatMessages = Arrays.asList(
                SystemMessage.from(promptConfig.getSystem()),
                UserMessage.from(userPrompt)
        );
        
        // 获取当前用户的模型配置
        OneChatExclModelConfig modelConfig = oneChatExclModelConfigService.getOne(new LambdaQueryWrapper<OneChatExclModelConfig>()
                .eq(OneChatExclModelConfig::getUserId, SecurityUtils.getUserId())
                .last("LIMIT 1"));
        
        String modelName = null;
        if (modelConfig != null && modelConfig.getModelName() != null) {
            modelName = modelConfig.getModelName();
            log.info("使用用户配置的模型: {}", modelName);
        } else {
            throw new RuntimeException("当前未设置AI模型,请先设置AI模型");
//            // 如果用户没有配置模型，使用默认模型
//            List<String> models = modelFactory.getEnabledModels();
//            if (models.isEmpty()) {
//                throw new RuntimeException("无可用 AI 模型，请检查 ai.models 配置");
//            }
//            modelName = models.get(0);
//            log.info("用户未配置模型，使用默认模型: {}", modelName);
        }
        
        String aiResponse = null;
        try {
            ChatLanguageModel model = modelFactory.getChatModel(modelName);
            ChatResponse chatResponse = model.chat(chatMessages);
            
            aiResponse = Optional.ofNullable(chatResponse.aiMessage())
                    .map(AiMessage::text)
                    .filter(s -> !s.trim().isEmpty())
                    .map(String::trim)
                    .orElseThrow(() -> new RuntimeException("模型返回内容为空或无效"));
            
            log.info("使用模型 [{}] 成功生成回复推荐", modelName);
            log.info("AI返回结果: {}", aiResponse);
        } catch (Exception e) {
            log.error("模型 [{}] 调用失败: {}", modelName, e.getMessage());
            throw new RuntimeException("AI 模型调用失败", e);
        }
        
        // 5. 处理AI返回的结果
        String cleanResponse = aiResponse;
        if (cleanResponse.contains("```json")) {
            cleanResponse = cleanResponse.replace("```json", "").replace("```", "").trim();
        }
        
        log.info("回复推荐生成完成，访客 {}", kfVisitorId);
        return cleanResponse;
    }

    @Override
    public String optimizeRoleSetting(String roleSetting, String modelName) {
        // 1. 从PromptManager获取提示词
        PromptManager.PromptConfig promptConfig = promptManager.get("role-setting.role-setting");
        String userPrompt = promptConfig.getUser().replace("{roleSetting}", roleSetting);

        List<ChatMessage> messages = Arrays.asList(
                SystemMessage.from(promptConfig.getSystem()),
                UserMessage.from(userPrompt)
        );

        try {
            // 直接获取同步模型
            ChatLanguageModel model = modelFactory.getChatModel(modelName);

            // 同步调用，直接返回完整响应
            ChatResponse chatResponse = model.chat(messages);

            String optimizedRoleSetting = Optional.ofNullable(chatResponse.aiMessage())
                    .map(AiMessage::text)
                    .filter(s -> !s.trim().isEmpty())
                    .map(String::trim)
                    .orElseThrow(() -> new RuntimeException("模型返回内容为空或无效"));

            log.info("使用模型 [{}] 成功优化角色设定", modelName);

            return optimizedRoleSetting;
        } catch (Exception e) {
            log.error("模型 [{}] 调用失败: {}", modelName, e.getMessage());
            throw new RuntimeException("AI 模型调用失败", e);
        }
    }

    @Override
    public String getKnowledgeSummaryByKfRule(Long agentId, String question) {
        StringBuilder summary = new StringBuilder();

        try {
            OneChatAgent oneChatAgent = oneChatAgentService.getById(agentId);
            if (null != oneChatAgent && StringUtils.isNotEmpty(oneChatAgent.getModelName())) {

                // 1. 获取知识库相关知识片段
                List<IOneChatKnowledgeBaseAttachmentService.HitResult> hitResults = oneChatKnowledgeBaseAttachmentService.getKnowledgeByKfRule(oneChatAgent, question);

                // 2. 构建知识片段文本
                StringBuilder knowledgeText = new StringBuilder();
                if (hitResults.isEmpty()) {
                    // 知识库中没有检索到信息，返回固定提示
                    log.info("知识库中未检索到相关信息，返回固定提示");
                    return "当前智能客服无法为你提供需要的服务,请转接人工";
                } else {
                    for (IOneChatKnowledgeBaseAttachmentService.HitResult result : hitResults) {
                        knowledgeText.append("文档: " + result.getDocumentName() + "\n");
                        knowledgeText.append("内容: " + result.getContent() + "\n\n");
                    }
                }

                // 3. 从PromptManager获取提示词
                PromptManager.PromptConfig promptConfig;
                if (oneChatAgent.getAdvancedConfig() != null && oneChatAgent.getAdvancedConfig() == 1) {
                    // 开启了问题推荐
                    promptConfig = promptManager.get("knowledge-summary.session-test-with-questions");
                } else {
                    // 未开启问题推荐
                    promptConfig = promptManager.get("knowledge-summary.session-test");
                }

                // 4. 构建提示词
                String userPrompt = promptConfig.getUser()
                        .replace("{agentName}", oneChatAgent.getAgentName())
                        .replace("{roleSetting}", oneChatAgent.getRoleSetting())
                        .replace("{knowledgeText}", knowledgeText.toString())
                        .replace("{question}", question);

                List<ChatMessage> messages = Arrays.asList(
                        SystemMessage.from(promptConfig.getSystem()),
                        UserMessage.from(userPrompt)
                );

                log.info("会话测试提示词: {}", userPrompt);

                // 5. 调用AI模型获取总结结果
                ChatLanguageModel model = modelFactory.getChatModel(oneChatAgent.getModelName());
                ChatResponse chatResponse = model.chat(messages);

                String response = Optional.ofNullable(chatResponse.aiMessage())
                        .map(AiMessage::text)
                        .filter(s -> !s.trim().isEmpty())
                        .map(String::trim)
                        .orElseThrow(() -> new RuntimeException("模型返回内容为空或无效"));

                summary.append(response);

            } else {
                return "智能体信息不完整";
            }

        } catch (Exception e) {
            log.error("获取知识库总结失败：" + e.getMessage(), e);
            return "处理失败：" + e.getMessage();
        }
        return summary.toString();
    }

}
