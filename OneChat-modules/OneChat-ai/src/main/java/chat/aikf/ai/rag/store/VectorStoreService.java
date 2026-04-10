package chat.aikf.ai.rag.store;

import chat.aikf.ai.api.domain.OneChatKnowledgeBase;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

/**
 * 向量存储服务接口
 */
public interface VectorStoreService {


    /**
     * 存储向量（支持指定嵌入模型）
     * @param segments 文本片段列表
     * @param knowledgeBase 知识库信息
     * @param modelName 嵌入模型名称
     * @return 向量ID列表
     */
    List<String> storeEmbeddings(List<TextSegment> segments, OneChatKnowledgeBase knowledgeBase, String modelName);


    /**
     * 搜索向量（支持指定嵌入模型）
     * @param query 搜索查询
     * @param knowledgeBase 知识库信息
     * @param modelName 嵌入模型名称
     * @param topK 返回结果数量
     * @return 搜索结果
     */
    List<dev.langchain4j.store.embedding.EmbeddingMatch<TextSegment>> searchEmbeddings(String query, OneChatKnowledgeBase knowledgeBase, String modelName, int topK);

    /**
     * 根据知识库ID删除对应的向量库
     * @param knowledgeBaseId 知识库ID
     * @return 是否删除成功
     */
    boolean deleteEmbeddingsByKnowledgeBaseId(Long knowledgeBaseId);

    /**
     * 存储带有详细元数据的向量
     * @param texts 文本内容列表
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @param segmentIds 片段ID列表
     * @param knowledgeBase 知识库信息
     * @param modelName 模型名称
     * @param documentName 文档名称
     * @param documentUrl 文档URL
     * @return 向量ID列表
     */
    List<String> storeEmbeddingsWithMetadata(List<String> texts, Long knowledgeBaseId, String documentId, List<String> segmentIds, OneChatKnowledgeBase knowledgeBase, String modelName, String documentName, String documentUrl);

    /**
     * 根据知识库ID和文档ID删除向量数据
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    boolean deleteEmbeddingsByDocumentId(Long knowledgeBaseId, String documentId);

    /**
     * 根据知识库ID、文档ID和片段ID删除向量数据
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @param segmentId 片段ID
     * @return 是否删除成功
     */
    boolean deleteEmbeddingsBySegmentId(Long knowledgeBaseId, String documentId, String segmentId);

}