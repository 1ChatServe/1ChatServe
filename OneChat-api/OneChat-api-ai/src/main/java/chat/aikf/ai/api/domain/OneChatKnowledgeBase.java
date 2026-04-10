package chat.aikf.ai.api.domain;

import chat.aikf.common.core.web.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName one_chat_knowledge_base
 */
@TableName(value ="one_chat_knowledge_base")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OneChatKnowledgeBase extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 知识库名称
     */
    @NotBlank(message = "名称不能为空")
    private String knowledgeBaseName;

    /**
     * 知识库描述
     */
    @NotBlank(message = "描述不能为空")
    private String knowledgeBaseDesc;


    /**
     * 模型名称
     */
    @NotBlank(message = "模型不能为空")
    private String modelName;

    /**
     * 知识库分段长度
     */
    private Integer segmentLength;

    /**
     * 知识库分段重叠字符数
     */
    private Integer overlapLength;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;


    // 非数据库字段，用于前端展示
    /**
     * 文档数量
     */
    @TableField(exist = false)
    private Integer documentCount;

    /**
     * 文档总大小
     */
    @TableField(exist = false)
    private String totalFileSize;

    /**
     * 解析状态：0-解析中，1-已解析，2-解析失败
     */
    @TableField(exist = false)
    private Integer parseStatus;

    /**
     * 解析进度
     */
    @TableField(exist = false)
    private String  progress;

    /**
     * 解析状态描述
     */
    @TableField(exist = false)
    private String parseStatusDesc;

    /**
     * 相似度阈值（0-1之间，默认0.7）
     */
    private Double similarityThreshold = 0.7;

} 