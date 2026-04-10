package chat.aikf.ai.domain;

import chat.aikf.common.core.web.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话分析结果表
 */
@TableName(value = "one_chat_session_analysis")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OneChatSessionAnalysis extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 访客ID
     */
    private Long kfVisitorId;

    /**
     * 客户综合评分（0-100）
     */
    private Integer overallScore;

    /**
     * 客户意向级别（A、B、C）
     */
    private String intentionLevel;

    /**
     * 会话智能总结
     */
    private String sessionSummary;

    /**
     * 购买意向百分比（0-100）
     */
    private Integer intentionPercentage;

    /**
     * 意向等级（高、中、低）
     */
    private String intentionLevelText;

    /**
     * 购买意向话术总结
     */
    private String intentionSummary;

    /**
     * 满意度百分比（0-100）
     */
    private Integer satisfactionPercentage;

    /**
     * 满意度等级（满意、一般、不满意）
     */
    private String satisfactionLevel;

    /**
     * 满意度话术总结
     */
    private String satisfactionSummary;

    /**
     * 分析时间
     */
    private String analysisTime;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;

}
