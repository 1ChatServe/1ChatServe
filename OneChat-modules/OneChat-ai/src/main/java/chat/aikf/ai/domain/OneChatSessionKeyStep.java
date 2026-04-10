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
 * 会话分析关键步骤表
 */
@TableName(value = "one_chat_session_key_step")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OneChatSessionKeyStep extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 会话分析ID
     */
    private Long sessionAnalysisId;

    /**
     * 步骤描述
     */
    private String stepDescription;

    /**
     * 时间节点
     */
    private String timestamp;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;

}
