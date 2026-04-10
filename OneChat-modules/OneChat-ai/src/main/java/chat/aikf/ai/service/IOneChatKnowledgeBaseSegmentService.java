package chat.aikf.ai.service;

import chat.aikf.ai.api.domain.OneChatKnowledgeBaseSegment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_knowledge_base_segment(知识库文档文本片段)】的数据库操作Service
* @createDate 2026-03-25
*/
public interface IOneChatKnowledgeBaseSegmentService extends IService<OneChatKnowledgeBaseSegment> {

    /**
     * 查询列表
     * @param oneChatKnowledgeBaseSegment
     * @return
     */
    List<OneChatKnowledgeBaseSegment> findList(OneChatKnowledgeBaseSegment oneChatKnowledgeBaseSegment);

    /**
     * 根据附件ID查询片段列表
     * @param attachmentId
     * @return
     */
    List<OneChatKnowledgeBaseSegment> findByAttachmentId(Long attachmentId);

}