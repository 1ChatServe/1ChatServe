package chat.aikf.ai.api.domain;

import chat.aikf.common.core.web.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName one_chat_knowledge_base_attachment
 */
@TableName(value ="one_chat_knowledge_base_attachment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OneChatKnowledgeBaseAttachment extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小
     */
    private Long fileSize;




    /**
     * 文件存储路径
     */
    private String fileUrl;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;


    /**
     * 解析状态：0-解析中，1-已解析，2-解析失败
     */
    private Integer parseStatus;

    // 非数据库字段，用于前端展示
    /**
     * 格式化后的文件大小
     */
    @TableField(exist = false)
    private String formattedFileSize;
    
    /**
     * 友好的文件类型名称
     */
    @TableField(exist = false)
    private String fileTypeStr;

}