package chat.aikf.ai.api.domain;

import chat.aikf.common.core.web.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName one_chat_knowledge_base_segment
 */
@TableName(value ="one_chat_knowledge_base_segment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OneChatKnowledgeBaseSegment extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 附件ID
     */
    private Long attachmentId;

    /**
     * 文本片段内容
     */
    private String segmentContent;

    /**
     * 片段索引
     */
    private Integer segmentIndex;

    /**
     * 向量维度
     */
    private Integer vectorDimension;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;

}