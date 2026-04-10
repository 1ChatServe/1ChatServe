package chat.aikf.ai.service;

import chat.aikf.ai.api.domain.OneChatAgent;
import chat.aikf.ai.api.domain.OneChatKnowledgeBaseAttachment;
import chat.aikf.ops.api.domain.OneChatKfRule;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_knowledge_base_attachment(知识库附件)】的数据库操作Service
* @createDate 2026-03-24
*/
public interface IOneChatKnowledgeBaseAttachmentService extends IService<OneChatKnowledgeBaseAttachment> {

    /**
     * 查询列表
     * @param oneChatKnowledgeBaseAttachment
     * @return
     */
    List<OneChatKnowledgeBaseAttachment> findList(OneChatKnowledgeBaseAttachment oneChatKnowledgeBaseAttachment);

    /**
     * 上传文档
     * @param file 上传的文件
     * @param knowledgeBaseId 知识库ID
     * @return 上传结果
     */
    boolean uploadDocument(MultipartFile file, Long knowledgeBaseId);

    /**
     * 命中测试
     * @param knowledgeBaseId 知识库ID
     * @param question 问题
     * @return 命中结果列表
     */
    List<HitResult> testHit(Long knowledgeBaseId, String question);

    /**
     * 根据客服规则获取知识库相关知识片段
     * @param agent 客服规则对象
     * @param question 问题
     * @return 命中结果列表，如果知识库中没有检索到，返回空列表
     */
    List<HitResult> getKnowledgeByKfRule(OneChatAgent agent ,String question);

    /**
     * 根据客服规则获取知识库相关知识片段并通过LLM总结，一次性返回所有文字结果
     * @param agentId 客服规则对象
     * @param question 问题
     * @return 总结结果，如果知识库中没有检索到，返回固定标识
     */
    String getKnowledgeSummaryByKfRule(Long agentId, String question);


    /**
     * 删除知识库附件
     * @param id
     */
     void deleteAttachment(Long id);

    /**
     * 命中结果
     */
    class HitResult {
        private String documentName; // 文档名称
        private String documentUrl; // 文档地址
        private String content; // 内容片段
        private Integer pageNumber; // 页码
        private Integer similarity; // 相似度（百分比）

        // getters and setters
        public String getDocumentName() {
            return documentName;
        }

        public void setDocumentName(String documentName) {
            this.documentName = documentName;
        }

        public String getDocumentUrl() {
            return documentUrl;
        }

        public void setDocumentUrl(String documentUrl) {
            this.documentUrl = documentUrl;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Integer getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(Integer pageNumber) {
            this.pageNumber = pageNumber;
        }

        public Integer getSimilarity() {
            return similarity;
        }

        public void setSimilarity(Integer similarity) {
            this.similarity = similarity;
        }
    }

}