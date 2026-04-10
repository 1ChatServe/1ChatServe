package chat.aikf.ai.api.domain;

import lombok.Data;

import java.util.List;

@Data
public class SessionAnalysisResult {
    // 综合汇总模块
    private ComprehensiveSummary comprehensiveSummary;
    
    // 客户意向模块
    private CustomerIntention customerIntention;
    
    // 情感分析模块
    private SentimentAnalysis sentimentAnalysis;
    
    // 关键步骤总结
    private List<KeyStep> keySteps;
    
    @Data
    public static class ComprehensiveSummary {
        // 客户综合评分（0-100）
        private int overallScore;
        // 客户意向级别（A、B、C）
        private String intentionLevel;
        // 会话智能总结
        private String sessionSummary;
    }
    
    @Data
    public static class CustomerIntention {
        // 购买意向百分比（0-100）
        private int intentionPercentage;
        // 意向等级（高、中、低）
        private String intentionLevel;
        // 购买意向话术总结
        private String intentionSummary;
    }
    
    @Data
    public static class SentimentAnalysis {
        // 满意度百分比（0-100）
        private int satisfactionPercentage;
        // 满意度等级（满意、一般、不满意）
        private String satisfactionLevel;
        // 满意度话术总结
        private String satisfactionSummary;
    }
    
    @Data
    public static class KeyStep {
        // 步骤描述
        private String stepDescription;
        // 时间节点
        private String timestamp;
    }
}