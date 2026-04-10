package chat.aikf.ai.service.impl;

import chat.aikf.ai.api.domain.OneChatKnowledgeBaseSegment;
import chat.aikf.ai.rag.store.VectorStoreService;
import chat.aikf.ai.service.IOneChatKnowledgeBaseSegmentService;
import chat.aikf.common.core.constant.Constants;
import chat.aikf.common.core.domain.R;
import chat.aikf.common.core.utils.StringUtils;
import chat.aikf.ai.api.domain.OneChatKnowledgeBase;
import chat.aikf.ai.api.domain.OneChatKnowledgeBaseAttachment;
import chat.aikf.ai.utils.FileSizeUtils;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import chat.aikf.ai.service.IOneChatKnowledgeBaseService;
import chat.aikf.ai.service.IOneChatKnowledgeBaseAttachmentService;
import chat.aikf.ai.mapper.OneChatKnowledgeBaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author robin
* @description 针对表【one_chat_knowledge_base(企业知识库)】的数据库操作Service实现
* @createDate 2026-03-24
*/
@Service
@Slf4j
public class OneChatKnowledgeBaseServiceImpl extends ServiceImpl<OneChatKnowledgeBaseMapper, OneChatKnowledgeBase>
    implements IOneChatKnowledgeBaseService {

    @Autowired
    private IOneChatKnowledgeBaseAttachmentService oneChatKnowledgeBaseAttachmentService;

    @Autowired
    private IOneChatKnowledgeBaseSegmentService oneChatKnowledgeBaseSegmentService;

    @Autowired
    private VectorStoreService vectorStoreService;


    @Override
    public List<OneChatKnowledgeBase> findList(OneChatKnowledgeBase oneChatKnowledgeBase) {
        LambdaQueryWrapper<OneChatKnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        
        // 模糊查询知识库名称或描述
        if (StringUtils.isNotBlank(oneChatKnowledgeBase.getSearchValue())) {
            String searchKey = oneChatKnowledgeBase.getSearchValue();
            queryWrapper.and(wrapper -> {
                wrapper.like(OneChatKnowledgeBase::getKnowledgeBaseName, searchKey)
                       .or()
                       .like(OneChatKnowledgeBase::getKnowledgeBaseDesc, searchKey);
            });
        }
        
        // 按创建时间降序排列
        queryWrapper.orderByDesc(OneChatKnowledgeBase::getCreateTime);
        
        List<OneChatKnowledgeBase> knowledgeBaseList = this.list(queryWrapper);
        
        // 为每个知识库计算文档数、总文件大小和解析状态
        for (OneChatKnowledgeBase knowledgeBase : knowledgeBaseList) {
            // 查询该知识库的所有附件
            OneChatKnowledgeBaseAttachment attachmentQuery = new OneChatKnowledgeBaseAttachment();
            attachmentQuery.setKnowledgeBaseId(knowledgeBase.getId());
            List<OneChatKnowledgeBaseAttachment> attachments = oneChatKnowledgeBaseAttachmentService.findList(attachmentQuery);
            
            // 计算文档数
            int documentCount = attachments.size();
            knowledgeBase.setDocumentCount(documentCount);
            
            // 计算总文件大小
            double totalFileSizeBytes = 0.0;
            for (OneChatKnowledgeBaseAttachment attachment : attachments) {
                if (attachment.getFileSize() != null) {
                    totalFileSizeBytes += attachment.getFileSize();
                }
            }
            
            // 使用工具类格式化文件大小
            String storageSize = FileSizeUtils.formatFileSize(totalFileSizeBytes);
            knowledgeBase.setTotalFileSize(storageSize);
            
            // 计算解析状态
            int parseStatus = 1; // 默认已解析
            for (OneChatKnowledgeBaseAttachment attachment : attachments) {
                Integer attachmentStatus = attachment.getParseStatus();
                if (attachmentStatus == null) {
                    // 未设置状态，视为解析中
                    parseStatus = 0;
                } else if (attachmentStatus == 2) {
                    // 有解析失败的附件
                    parseStatus = 2;
                    break;
                } else if (attachmentStatus == 0) {
                    // 有解析中的附件
                    parseStatus = 0;
                }
            }
            knowledgeBase.setParseStatus(parseStatus);
            
            // 设置解析状态描述
            String parseStatusDesc;
            switch (parseStatus) {
                case 0:
                    parseStatusDesc = "解析中";
                    break;
                case 1:
                    parseStatusDesc = "已解析";
                    break;
                case 2:
                    parseStatusDesc = "解析失败";
                    break;
                default:
                    parseStatusDesc = "未知";
            }
            knowledgeBase.setParseStatusDesc(parseStatusDesc);
        }
        
        return knowledgeBaseList;
    }

    @Override
    public java.util.Map<String, Object> getStatistics() {
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        
        // 1. 统计知识库数量（未删除的）
        long knowledgeBaseCount = this.count(new LambdaQueryWrapper<OneChatKnowledgeBase>()
                .eq(OneChatKnowledgeBase::getDelFlag, Constants.COMMON));
        statistics.put("knowledgeBaseCount", knowledgeBaseCount);
        
        // 2. 统计文档总数（未删除的）
        OneChatKnowledgeBaseAttachment attachmentQuery = new OneChatKnowledgeBaseAttachment();
        attachmentQuery.setDelFlag(Constants.COMMON);
        List<OneChatKnowledgeBaseAttachment> allAttachments = oneChatKnowledgeBaseAttachmentService.findList(attachmentQuery);
        long documentCount = allAttachments.size();
        statistics.put("documentCount", documentCount);
        
        // 3. 统计存储量（未删除的所有文档的大小总和）
        double totalFileSizeBytes = 0.0;
        for (OneChatKnowledgeBaseAttachment attachment : allAttachments) {
            if (attachment.getFileSize() != null) {
                totalFileSizeBytes += attachment.getFileSize();
            }
        }
        
        // 使用工具类格式化文件大小
        String storageSize = FileSizeUtils.formatFileSize(totalFileSizeBytes);
        statistics.put("totalFileSize", storageSize);
        
        // 4. 统计解析率（未删除的所有文档）
        if (documentCount > 0) {
            long parsedCount = 0;
            for (OneChatKnowledgeBaseAttachment attachment : allAttachments) {
                if (attachment.getParseStatus() != null && attachment.getParseStatus() == 1) {
                    parsedCount++;
                }
            }
            double parseRate = (double) parsedCount / documentCount * 100;
            statistics.put("parseRate", parseRate);
        } else {
            statistics.put("parseRate", 0.0);
        }
        
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeKnowledgeBase(Long[] ids) {

        for (Long id : ids) {
            // 1. 查询该知识库的所有附件
            List<OneChatKnowledgeBaseAttachment> attachments = oneChatKnowledgeBaseAttachmentService.list(new LambdaQueryWrapper<OneChatKnowledgeBaseAttachment>()
                    .eq(OneChatKnowledgeBaseAttachment::getKnowledgeBaseId,id));

            // 删除附件对应的所有片段
            if(CollectionUtil.isNotEmpty(attachments)){
                oneChatKnowledgeBaseSegmentService.remove(new LambdaQueryWrapper<OneChatKnowledgeBaseSegment>()
                        .in(OneChatKnowledgeBaseSegment::getAttachmentId,attachments.stream().map(OneChatKnowledgeBaseAttachment::getId).collect(Collectors.toList())));
            }

            // 3. 删除知识库的所有附件
            oneChatKnowledgeBaseAttachmentService.remove(new LambdaQueryWrapper<OneChatKnowledgeBaseAttachment>()
                    .eq(OneChatKnowledgeBaseAttachment::getKnowledgeBaseId,id));

            // 4. 删除知识库对应的向量库
            try {
                // 调用VectorStoreService的删除方法
                boolean deleteResult = vectorStoreService.deleteEmbeddingsByKnowledgeBaseId(id);
                if (deleteResult) {
                    log.info("向量库删除成功，知识库ID: {}", id);
                } else {
                    log.warn("向量库删除失败，知识库ID: {}", id);
                }
            } catch (Exception e) {
                log.warn("删除向量库失败: {}", e.getMessage());
                // 向量库删除失败不影响知识库删除
            }

            // 5. 删除知识库本身
            this.removeById(id);
        }

    }
}