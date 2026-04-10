package chat.aikf.ai.service.impl;

import chat.aifk.common.datascope.annotation.DataScope;
import chat.aikf.common.core.utils.StringUtils;
import chat.aikf.ai.api.domain.OneChatKnowledgeBaseSegment;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import chat.aikf.ai.service.IOneChatKnowledgeBaseSegmentService;
import chat.aikf.ai.mapper.OneChatKnowledgeBaseSegmentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_knowledge_base_segment(知识库文档文本片段)】的数据库操作Service实现
* @createDate 2026-03-25
*/
@Service
public class OneChatKnowledgeBaseSegmentServiceImpl extends ServiceImpl<OneChatKnowledgeBaseSegmentMapper, OneChatKnowledgeBaseSegment>
    implements IOneChatKnowledgeBaseSegmentService {

    @Override
    @DataScope
    public List<OneChatKnowledgeBaseSegment> findList(OneChatKnowledgeBaseSegment oneChatKnowledgeBaseSegment) {
        List<OneChatKnowledgeBaseSegment> oneChatKnowledgeBaseSegments = this.list(new LambdaQueryWrapper<OneChatKnowledgeBaseSegment>()
                .orderByDesc(OneChatKnowledgeBaseSegment::getCreateTime));
        return oneChatKnowledgeBaseSegments;
    }

    @Override
    public List<OneChatKnowledgeBaseSegment> findByAttachmentId(Long attachmentId) {
        return this.list(new LambdaQueryWrapper<OneChatKnowledgeBaseSegment>()
                .eq(OneChatKnowledgeBaseSegment::getAttachmentId, attachmentId)
                .orderByAsc(OneChatKnowledgeBaseSegment::getSegmentIndex));
    }

}