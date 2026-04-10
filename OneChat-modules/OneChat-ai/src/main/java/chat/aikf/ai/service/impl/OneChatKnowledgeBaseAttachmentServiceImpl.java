package chat.aikf.ai.service.impl;

import chat.aifk.common.datascope.annotation.DataScope;
import chat.aikf.ai.api.domain.OneChatAgent;
import chat.aikf.ai.api.domain.OneChatKnowledgeBaseSegment;
import chat.aikf.ai.service.IOneChatKnowledgeBaseSegmentService;
import chat.aikf.common.core.utils.StringUtils;
import chat.aikf.ai.api.domain.OneChatKnowledgeBase;
import chat.aikf.ai.api.domain.OneChatKnowledgeBaseAttachment;
import chat.aikf.ai.rag.RagProcessFactory;
import chat.aikf.ai.rag.store.VectorStoreService;
import chat.aikf.ai.service.IOneChatKnowledgeBaseService;
import chat.aikf.ai.service.IOneChatAgentService;
import chat.aikf.ai.factory.ModelFactory;
import chat.aikf.ai.utils.FileSizeUtils;
import chat.aikf.ai.utils.FileTypeUtils;
import chat.aikf.ai.utils.FileFormatUtils;
import chat.aikf.common.core.utils.SnowFlakeUtils;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import chat.aikf.ai.service.IOneChatKnowledgeBaseAttachmentService;
import chat.aikf.ai.mapper.OneChatKnowledgeBaseAttachmentMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import chat.aikf.ops.api.domain.OneChatKfRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_knowledge_base_attachment(知识库附件)】的数据库操作Service实现
* @createDate 2026-03-24
*/
@Service
public class OneChatKnowledgeBaseAttachmentServiceImpl extends ServiceImpl<OneChatKnowledgeBaseAttachmentMapper, OneChatKnowledgeBaseAttachment>
    implements IOneChatKnowledgeBaseAttachmentService {

    private static final Logger log = LoggerFactory.getLogger(OneChatKnowledgeBaseAttachmentServiceImpl.class);

    @Resource
    private IOneChatKnowledgeBaseService oneChatKnowledgeBaseService;

    @Resource
    private IOneChatAgentService oneChatAgentService;

    @Resource
    private ModelFactory modelFactory;

    @Resource
    @Lazy
    private RagProcessFactory ragProcessFactory;

    @Resource
    private VectorStoreService vectorStoreService;

    @Resource
    private IOneChatKnowledgeBaseSegmentService segmentService;

    @Override
    @DataScope
    public List<OneChatKnowledgeBaseAttachment> findList(OneChatKnowledgeBaseAttachment oneChatKnowledgeBaseAttachment) {
        List<OneChatKnowledgeBaseAttachment> attachments = this.list(new LambdaQueryWrapper<OneChatKnowledgeBaseAttachment>()
                .eq(oneChatKnowledgeBaseAttachment.getKnowledgeBaseId() != null, OneChatKnowledgeBaseAttachment::getKnowledgeBaseId, oneChatKnowledgeBaseAttachment.getKnowledgeBaseId())
                .like(StringUtils.isNotEmpty(oneChatKnowledgeBaseAttachment.getSearchValue()), OneChatKnowledgeBaseAttachment::getFileName, oneChatKnowledgeBaseAttachment.getSearchValue())
                .eq(StringUtils.isNotEmpty(oneChatKnowledgeBaseAttachment.getFileType()), OneChatKnowledgeBaseAttachment::getFileType, oneChatKnowledgeBaseAttachment.getFileType())
                .eq(StringUtils.isNotEmpty(oneChatKnowledgeBaseAttachment.getDelFlag()), OneChatKnowledgeBaseAttachment::getDelFlag, oneChatKnowledgeBaseAttachment.getDelFlag())
                .orderByDesc(OneChatKnowledgeBaseAttachment::getCreateTime));
        
        // 为每个附件设置格式化后的文件大小和友好的文件类型名称
        for (OneChatKnowledgeBaseAttachment attachment : attachments) {
            // 设置格式化后的文件大小
            if (attachment.getFileSize() != null) {
                String formattedSize = FileSizeUtils.formatFileSize(attachment.getFileSize());
                attachment.setFormattedFileSize(formattedSize);
            }
            
            // 设置友好的文件类型名称
            if (attachment.getFileType() != null) {
                String friendlyFileType = FileTypeUtils.getFriendlyFileType(attachment.getFileType());
                attachment.setFileTypeStr(friendlyFileType);
            }
        }
        
        return attachments;
    }

    @Override
    public boolean uploadDocument(org.springframework.web.multipart.MultipartFile file, Long knowledgeBaseId) {
        // 1. 获取知识库信息
        OneChatKnowledgeBase knowledgeBase = oneChatKnowledgeBaseService.getById(knowledgeBaseId);
        if (knowledgeBase == null) {
            throw new RuntimeException("知识库不存在");
        }

        // 2. 调用RagProcessFactory处理文档（包含异步处理逻辑）
        OneChatKnowledgeBaseAttachment attachment = null;
        try {
            attachment = ragProcessFactory.processDocument(file, knowledgeBase);
        } catch (Exception e) {
            throw new RuntimeException("文档上传失败：" + e.getMessage(), e);
        }

        // 3. 保存附件记录
        if (!this.save(attachment)) {
            throw new RuntimeException("保存附件记录失败");
        }

        return true;
    }

    @Override
    public List<IOneChatKnowledgeBaseAttachmentService.HitResult> testHit(Long knowledgeBaseId, String question) {
        try {
            // 1. 获取知识库信息
            OneChatKnowledgeBase knowledgeBase = oneChatKnowledgeBaseService.getById(knowledgeBaseId);
            if (knowledgeBase == null) {
                throw new RuntimeException("知识库不存在");
            }

            log.info("开始命中测试，知识库ID: {}, 查询: '{}', 使用模型: {}", knowledgeBaseId, question, knowledgeBase.getModelName());

            // 2. 搜索向量
            List<dev.langchain4j.store.embedding.EmbeddingMatch<dev.langchain4j.data.segment.TextSegment>> matches = 
                    vectorStoreService.searchEmbeddings(question, knowledgeBase, knowledgeBase.getModelName(), 5);

            log.info("搜索完成，返回结果数: {}", matches.size());

            // 3. 处理搜索结果
            List<IOneChatKnowledgeBaseAttachmentService.HitResult> results = new java.util.ArrayList<>();
            for (dev.langchain4j.store.embedding.EmbeddingMatch<dev.langchain4j.data.segment.TextSegment> match : matches) {
                IOneChatKnowledgeBaseAttachmentService.HitResult result = new IOneChatKnowledgeBaseAttachmentService.HitResult();
                
                // 设置内容片段
                String content = match.embedded().text();
                result.setContent(content);
                
                // 设置相似度（转换为百分比并四舍五入为整数）
                double score = match.score();
                int percentage = (int) Math.round(score * 100);
                result.setSimilarity(percentage);
                
                // 从文本片段的元数据中提取文档信息
                Metadata metadata = match.embedded().metadata();
                // 设置文档名称
                if (metadata != null && metadata.containsKey("documentName")) {
                    String documentName = metadata.getString("documentName");
                    result.setDocumentName(documentName);
                    log.info("找到文档: {}, 相似度: {}%, 内容预览: '{}'", documentName, percentage, content.length() > 50 ? content.substring(0, 50) + "..." : content);
                } else {
                    result.setDocumentName("未知文档");
                    log.info("找到未知文档, 相似度: {}%, 内容预览: '{}'", percentage, content.length() > 50 ? content.substring(0, 50) + "..." : content);
                }
                
                // 设置文档地址
                if (metadata != null && metadata.containsKey("documentUrl")) {
                    result.setDocumentUrl(metadata.getString("documentUrl"));
                } else {
                    result.setDocumentUrl("#");
                }
                
                results.add(result);
            }

            log.info("命中测试完成，返回结果数: {}", results.size());
            return results;
        } catch (Exception e) {
            log.error("命中测试失败: {}", e.getMessage(), e);
            throw new RuntimeException("命中测试失败：" + e.getMessage(), e);
        }
    }

    @Override
    public List<IOneChatKnowledgeBaseAttachmentService.HitResult> getKnowledgeByKfRule(OneChatAgent agent, String question) {
        List<IOneChatKnowledgeBaseAttachmentService.HitResult> results = new java.util.ArrayList<>();
        try {

            // 2. 从agent对象中获取knowledgeIds
            List<String> knowledgeIds = agent.getKnowledgeIds();
            if (knowledgeIds == null || knowledgeIds.isEmpty()) {
                return results;
            }

            // 3. 遍历知识库ID，分别查询每个知识库的相关知识片段
            for (String knowledgeIdStr : knowledgeIds) {
                try {
                    Long knowledgeBaseId = Long.parseLong(knowledgeIdStr.trim());
                    // 获取知识库信息
                    OneChatKnowledgeBase knowledgeBase = oneChatKnowledgeBaseService.getById(knowledgeBaseId);
                    if (knowledgeBase == null) {
                        continue;
                    }

                    // 搜索向量
                    List<dev.langchain4j.store.embedding.EmbeddingMatch<dev.langchain4j.data.segment.TextSegment>> matches = 
                            vectorStoreService.searchEmbeddings(question, knowledgeBase, knowledgeBase.getModelName(),5);

                    // 处理搜索结果
                    for (dev.langchain4j.store.embedding.EmbeddingMatch<dev.langchain4j.data.segment.TextSegment> match : matches) {
                        IOneChatKnowledgeBaseAttachmentService.HitResult result = new IOneChatKnowledgeBaseAttachmentService.HitResult();
                        
                        // 设置内容片段
                        result.setContent(match.embedded().text());
                        
                        // 设置相似度（转换为百分比并四舍五入为整数）
                        double score = match.score();
                        int percentage = (int) Math.round(score * 100);
                        result.setSimilarity(percentage);
                        
                        // 从文本片段的元数据中提取文档信息
                        Metadata metadata = match.embedded().metadata();
                        // 设置文档名称
                        if (metadata != null && metadata.containsKey("documentName")) {
                            result.setDocumentName(metadata.getString("documentName"));
                        } else {
                            result.setDocumentName("未知文档");
                        }
                        
                        // 设置文档地址
                        if (metadata != null && metadata.containsKey("documentUrl")) {
                            result.setDocumentUrl(metadata.getString("documentUrl"));
                        } else {
                            result.setDocumentUrl("#");
                        }
                        
                        // 从文本片段的元数据中提取页码信息
                        if (metadata != null && metadata.containsKey("pageNumber")) {
                            try {
                                result.setPageNumber(Integer.parseInt(metadata.getString("pageNumber")));
                            } catch (NumberFormatException e) {
                                result.setPageNumber(1);
                            }
                        } else {
                            result.setPageNumber(1);
                        }
                        
                        results.add(result);
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException("获取知识库信息失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String getKnowledgeSummaryByKfRule(Long agentId, String question) {
        StringBuilder summary = new StringBuilder();

        try {
            OneChatAgent oneChatAgent = oneChatAgentService.getById(agentId);
            if (null != oneChatAgent && StringUtils.isNotEmpty(oneChatAgent.getModelName())) {

                // 1. 获取知识库相关知识片段
                List<IOneChatKnowledgeBaseAttachmentService.HitResult> hitResults = getKnowledgeByKfRule(oneChatAgent, question);

                // 2. 构建知识片段文本
                StringBuilder knowledgeText = new StringBuilder();
                if (hitResults.isEmpty()) {
                    // 知识库中没有检索到信息，返回固定提示
                    log.info("知识库中未检索到相关信息，返回固定提示");
                    return "当前智能客服无法为你提供需要的服务,请转接人工";
                } else {
                    for (IOneChatKnowledgeBaseAttachmentService.HitResult result : hitResults) {
                        knowledgeText.append("文档: ").append(result.getDocumentName()).append("\n");
                        knowledgeText.append("内容: ").append(result.getContent()).append("\n\n");
                    }
                }

                // 3. 构建LLM提示词
                String prompt=null;
                if (knowledgeText.length() > 0) {
                    if (oneChatAgent.getAdvancedConfig() != null && oneChatAgent.getAdvancedConfig() == 1) {
                        // 开启了问题推荐
                        prompt = "你是" + oneChatAgent.getAgentName() + "。\n\n" +
                                "角色设定：" + oneChatAgent.getRoleSetting() + "\n\n" +
                                "请根据以下知识库内容，针对用户问题做一次言简意赅的总结回复，不要添加任何无关信息：\n\n" +
                                "知识库内容：\n" + knowledgeText.toString() + "\n" +
                                "用户问题：" + question + "\n\n" +
                                "同时，请生成最多3条与用户问题相关的后续问题，用于推荐给用户。\n\n" +
                                "输出格式要求：\n" +
                                "1. 首先输出总结回复内容\n" +
                                "2. 然后在新的一行输出 '### 相关问题'\n" +
                                "3. 然后每条问题占一行，以 '-' 开头\n" +
                                "4. 最多生成3条问题\n" +
                                "5. 问题要具体、有针对性\n" +
                                "6. 问题要使用中文";
                    } else {
                        // 未开启问题推荐
                        prompt = "你是" + oneChatAgent.getAgentName() + "。\n\n" +
                                "角色设定：" + oneChatAgent.getRoleSetting() + "\n\n" +
                                "请根据以下知识库内容，针对用户问题做一次言简意赅的总结回复，不要添加任何无关信息：\n\n" +
                                "知识库内容：\n" + knowledgeText.toString() + "\n" +
                                "用户问题：" + question;
                    }
                } 



                if(StringUtils.isNotEmpty(prompt)){
                    // 4. 调用LLM获取总结结果和相关问题
                    ChatLanguageModel chatModel = modelFactory.getChatModel(oneChatAgent.getModelName());
                    String response = chatModel.chat(prompt);
                    summary.append(response);
                }

            } else {
                return "智能体信息不完整";
            }

        } catch (Exception e) {
            log.error("获取知识库总结失败：" + e.getMessage(), e);
            return "处理失败：" + e.getMessage();
        }
        return summary.toString();
    }

    @Override
    public void deleteAttachment(Long id) {
        OneChatKnowledgeBaseAttachment baseAttachment = this.getById(id);
        if(null != baseAttachment){
            log.info("开始删除知识库附件，ID: {}", id);
            
            // 1. 删除附件对应的向量数据
            Long knowledgeBaseId = baseAttachment.getKnowledgeBaseId();
            String documentId = baseAttachment.getId().toString();
            boolean vectorDeleteResult = vectorStoreService.deleteEmbeddingsByDocumentId(knowledgeBaseId, documentId);
            log.info("删除向量数据结果: {}", vectorDeleteResult);
            
            // 2. 删除附件对应的片段数据
            try {
                boolean segmentDeleteResult = segmentService.remove(new LambdaQueryWrapper<OneChatKnowledgeBaseSegment>()
                        .eq(OneChatKnowledgeBaseSegment::getAttachmentId, id));
                log.info("删除片段数据结果: {}", segmentDeleteResult);
            } catch (Exception e) {
                log.error("删除附件片段失败: {}", e.getMessage());
            }
            
            // 3. 删除附件本身
            boolean attachmentDeleteResult = this.removeById(id);
            log.info("删除附件结果: {}", attachmentDeleteResult);
            
            log.info("知识库附件删除完成，ID: {}", id);
        }

    }
}
