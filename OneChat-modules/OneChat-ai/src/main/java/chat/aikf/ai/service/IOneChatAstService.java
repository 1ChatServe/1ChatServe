package chat.aikf.ai.service;

import chat.aikf.ai.api.domain.SessionAnalysisResult;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IOneChatAstService {


    /**
     * 推荐回复
     * @param visitorMessage
     * @return
     */
    public String recommendReply(String visitorMessage);

    /**
     * 会话分析
     * @param kfVisitorId 访客ID
     * @return 会话分析结果
     */
    public SessionAnalysisResult analyzeSession(Long kfVisitorId) throws JsonProcessingException;

    /**
     * 生成回复推荐
     * @param kfVisitorId 访客ID
     * @return 推荐回复列表（JSON格式）
     */
    public String generateReplyRecommendations(Long kfVisitorId);

    /**
     * 优化角色设定
     * @param roleSetting 原始角色设定
     * @param modelName 模型名称
     * @return 优化后的角色设定
     */
    public String optimizeRoleSetting(String roleSetting, String modelName);

    /**
     * 根据智能体ID和问题获取知识库总结
     * @param agentId 智能体ID
     * @param question 问题
     * @return 总结结果
     */
    public String getKnowledgeSummaryByKfRule(Long agentId, String question);
}
