package chat.aikf.ai.domain;

import lombok.Data;

/**
 * 知识库附件查询DTO
 */
@Data
public class KnowledgeBaseAttachmentQueryDto {

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 附件名称（模糊查询）
     */
    private String fileName;

    /**
     * 附件类型
     */
    private String fileType;

}
