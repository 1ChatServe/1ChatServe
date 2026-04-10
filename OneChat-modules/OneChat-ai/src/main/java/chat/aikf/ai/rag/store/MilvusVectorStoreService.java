package chat.aikf.ai.rag.store;

import chat.aikf.ai.api.domain.OneChatKnowledgeBase;
import chat.aikf.ai.api.domain.OneChatModelConfig;
import chat.aikf.ai.config.AiModelsProperties;
import chat.aikf.ai.factory.ModelFactory;
import chat.aikf.ai.service.IOneChatModelConfigService;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Milvus向量存储服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MilvusVectorStoreService implements VectorStoreService {

    private final ModelFactory modelFactory;
    private final IOneChatModelConfigService modelConfigService;
    private final AiModelsProperties aiModelsProperties;

    // 缓存不同集合与 autoFlush 配置的 Milvus 连接
    private final Map<String, EmbeddingStore<TextSegment>> storeCache = new ConcurrentHashMap<>();

    /**
     * 获取 Milvus Store，支持动态维度
     */
    private EmbeddingStore<TextSegment> getMilvusStore(String collectionName, int dimension, boolean autoFlushOnInsert) {
        return storeCache.computeIfAbsent(collectionName + "_" + autoFlushOnInsert, key -> {
            // 从配置中获取Milvus参数
            AiModelsProperties.MilvusConfig milvusConfig = aiModelsProperties.getMilvus();

            return MilvusEmbeddingStore.builder()
                    .host(milvusConfig.getHost())
                    .port(Integer.parseInt(milvusConfig.getPort()))
                    .collectionName(collectionName)
                    .dimension(dimension)
                    .indexType(IndexType.IVF_FLAT)
                    .metricType(MetricType.COSINE) // 使用余弦相似度，与集合创建时的设置一致
                    .autoFlushOnInsert(autoFlushOnInsert)
                    .idFieldName("id")
                    .textFieldName("text")
                    .metadataFieldName("metadata")
                    .vectorFieldName("vector")
                    .build();
        });
    }

    /**
     * 构建带有元数据的文本片段
     * @param text 文本内容
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @param segmentId 片段ID
     * @return 带有元数据的文本片段
     */
    private TextSegment buildTextSegmentWithMetadata(String text, Long knowledgeBaseId, String documentId, String segmentId) {
        Metadata metadata = new Metadata();
        metadata.put("knowledgeBaseId", knowledgeBaseId.toString());
        metadata.put("documentId", documentId);
        metadata.put("segmentId", segmentId);
        return TextSegment.from(text, metadata);
    }



    @Override
    public List<String> storeEmbeddings(List<TextSegment> segments, OneChatKnowledgeBase knowledgeBase, String modelName) {
        List<String> vectorIds = new ArrayList<>();
        try {
            // 从ModelFactory获取嵌入模型
            EmbeddingModel embeddingModel = modelFactory.getEmbeddingModel(modelName);

            // 构建集合名称
            String collectionName = aiModelsProperties.getMilvus().getCollection() + "_" + knowledgeBase.getId();

            // 初始化向量存储（当前使用Milvus），为每个知识库创建唯一的集合
            // 写入场景使用 autoFlush=false 以提升批量插入性能
            EmbeddingStore<TextSegment> embeddingStore = getMilvusStore(collectionName, aiModelsProperties.getMilvus().getDimension(), false);

            log.info("Milvus向量存储条数记录: {}", segments.size());
            long startTime = System.currentTimeMillis();

            // 生成嵌入并存储
            for (TextSegment segment : segments) {
                // 确保片段包含必要的元数据
                Metadata metadata = segment.metadata();
                if (metadata == null) {
                    metadata = new Metadata();
                }
                // 确保metadata中包含知识库ID
                if (!metadata.containsKey("knowledgeBaseId")) {
                    metadata.put("knowledgeBaseId", knowledgeBase.getId().toString());
                }
                
                // 重新构建带有完整元数据的片段
                TextSegment segmentWithMetadata = TextSegment.from(segment.text(), metadata);
                
                Embedding embedding = embeddingModel.embed(segmentWithMetadata).content();
                // 假设add方法返回向量ID
                String vectorId = embeddingStore.add(embedding, segmentWithMetadata).toString();
                vectorIds.add(vectorId);
            }

            long endTime = System.currentTimeMillis();
            log.info("Milvus向量存储完成消耗时间：{}秒", (endTime - startTime) / 1000);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("存储向量失败: {}", e.getMessage());
        }
        return vectorIds;
    }



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
    public List<String> storeEmbeddingsWithMetadata(List<String> texts, Long knowledgeBaseId, String documentId, List<String> segmentIds, OneChatKnowledgeBase knowledgeBase, String modelName, String documentName, String documentUrl) {
        List<String> vectorIds = new ArrayList<>();
        try {
            // 从ModelFactory获取嵌入模型
            EmbeddingModel embeddingModel = modelFactory.getEmbeddingModel(modelName);
            log.info("获取到嵌入模型: {}", modelName);

            // 构建集合名称
            String collectionName = aiModelsProperties.getMilvus().getCollection() + "_" + knowledgeBaseId;
            log.info("使用集合: {}", collectionName);

            // 初始化向量存储
            EmbeddingStore<TextSegment> embeddingStore = getMilvusStore(collectionName, aiModelsProperties.getMilvus().getDimension(), false);
            log.info("向量存储初始化完成，维度: {}", aiModelsProperties.getMilvus().getDimension());

            log.info("开始存储向量，文本数: {}", texts.size());
            long startTime = System.currentTimeMillis();

            // 生成嵌入并存储
            for (int i = 0; i < texts.size(); i++) {
                String text = texts.get(i);
                String segmentId = segmentIds.get(i);
                
                // 构建带有元数据的文本片段
                TextSegment segment = buildTextSegmentWithMetadata(text, knowledgeBaseId, documentId, segmentId, documentName, documentUrl);
                
                // 生成嵌入向量
                Embedding embedding = embeddingModel.embed(segment).content();
                log.debug("生成向量长度: {}", embedding.vector().length);
                
                // 存储向量
                String vectorId = embeddingStore.add(embedding, segment).toString();
                vectorIds.add(vectorId);
                
                // 每100个向量打印一次进度
                if ((i + 1) % 100 == 0) {
                    log.info("已存储向量: {}/{}", i + 1, texts.size());
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("Milvus向量存储完成，耗时: {}秒，生成向量数: {}", (endTime - startTime) / 1000, vectorIds.size());
        } catch (Exception e) {
            log.error("存储向量失败: {}", e.getMessage(), e);
        }
        return vectorIds;
    }

    /**
     * 构建带有元数据的文本片段
     * @param text 文本内容
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @param segmentId 片段ID
     * @param documentName 文档名称
     * @param documentUrl 文档URL
     * @return 带有元数据的文本片段
     */
    private TextSegment buildTextSegmentWithMetadata(String text, Long knowledgeBaseId, String documentId, String segmentId, String documentName, String documentUrl) {
        Metadata metadata = new Metadata();
        metadata.put("knowledgeBaseId", knowledgeBaseId.toString());
        metadata.put("documentId", documentId);
        metadata.put("segmentId", segmentId);
        metadata.put("documentName", documentName);
        metadata.put("documentUrl", documentUrl);
        return TextSegment.from(text, metadata);
    }


    @Override
    public List<EmbeddingMatch<TextSegment>> searchEmbeddings(String query, OneChatKnowledgeBase knowledgeBase, String modelName, int topK) {
        try {
            // 从ModelFactory获取嵌入模型
            EmbeddingModel embeddingModel = modelFactory.getEmbeddingModel(modelName);
            log.info("使用模型: {} 进行向量搜索", modelName);

            // 构建集合名称
            String collectionName = aiModelsProperties.getMilvus().getCollection() + "_" + knowledgeBase.getId();
            log.info("搜索集合: {}", collectionName);

            // 初始化向量存储（当前使用Milvus），使用知识库ID创建唯一的集合
            // 查询复用连接，autoFlush 对查询无影响，此处保持 true
            EmbeddingStore<TextSegment> embeddingStore = getMilvusStore(collectionName, aiModelsProperties.getMilvus().getDimension(), true);
            log.info("向量存储初始化完成，维度: {}", aiModelsProperties.getMilvus().getDimension());

            // 对查询进行预处理，去除无关字符，提高搜索质量
            String processedQuery = preprocessQuery(query);
            log.info("原始查询: '{}'，处理后查询: '{}'", query, processedQuery);

            // 检查查询是否为空
            if (processedQuery == null || processedQuery.trim().isEmpty()) {
                log.warn("搜索查询为空，返回空结果");
                return List.of();
            }

            // 将查询文本转换为嵌入向量
            Embedding queryEmbedding = embeddingModel.embed(processedQuery).content();
            log.debug("生成查询向量长度: {}", queryEmbedding.vector().length);

            // 执行搜索，获取原始结果
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(topK) // 直接获取指定数量的结果
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
            log.info("搜索完成，原始结果数: {}", matches.size());

            // 对结果按相似度排序（确保结果是按相似度降序排列的）
            matches.sort((a, b) -> Double.compare(b.score(), a.score()));

            // 获取相似度阈值，使用知识库配置或默认值
            double threshold = knowledgeBase.getSimilarityThreshold() != null ? knowledgeBase.getSimilarityThreshold() : 0.7;
            log.info("使用相似度阈值: {}", threshold);
            
            // 过滤出相似度大于等于阈值的结果
            // 限制返回结果数量为topK
            List<EmbeddingMatch<TextSegment>> result = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : matches) {
                double score = match.score();
                if (score >= threshold) { // 只返回相似度大于等于阈值的结果
                    result.add(match);
                    log.debug("匹配结果 - 相似度: {}, 内容: '{}'", score, match.embedded().text().length() > 100 ? match.embedded().text().substring(0, 100) + "..." : match.embedded().text());
                    
                    // 只返回指定数量的结果
                    if (result.size() >= topK) {
                        break;
                    }
                } else {
                    break; // 由于已经排序，后面的相似度更低，直接 break
                }
            }

            log.info("返回结果数: {}, 最高相似度: {}", result.size(), result.isEmpty() ? 0 : result.get(0).score());

            return result;
        } catch (Exception e) {
            log.error("搜索向量失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 预处理查询文本，提高搜索质量
     * @param query 原始查询
     * @return 处理后的查询
     */
    private String preprocessQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return query;
        }
        
        // 去除多余的空格
        query = query.trim().replaceAll("\\s+|", " ");
        
        // 可以根据需要添加更多预处理逻辑，例如：
        // 1. 去除标点符号
        // 2. 进行同义词替换
        // 3. 提取关键词
        
        return query;
    }

    @Override
    public boolean deleteEmbeddingsByKnowledgeBaseId(Long knowledgeBaseId) {
        try {
            // 从配置中获取Milvus参数
            AiModelsProperties.MilvusConfig milvusConfig = aiModelsProperties.getMilvus();
            
            // 构建集合名称
            String collectionName = milvusConfig.getCollection() + "_" + knowledgeBaseId;
            
            // 清理缓存
            storeCache.keySet().removeIf(key -> key.startsWith(collectionName));
            
            // 使用默认维度，因为删除操作不需要精确的维度信息
            EmbeddingStore<TextSegment> embeddingStore = getMilvusStore(collectionName,  aiModelsProperties.getMilvus().getDimension(), false);
            
            try {
                log.info("尝试删除知识库ID={}的向量库数据: {}", knowledgeBaseId, collectionName);
                
                // 创建过滤器，匹配所有属于该知识库的向量
                Filter filter = MetadataFilterBuilder.metadataKey("knowledgeBaseId").isEqualTo(knowledgeBaseId.toString());
                
                // 删除所有匹配的向量
                embeddingStore.removeAll(filter);
                
                log.info("Milvus成功删除知识库ID={}的所有向量数据", knowledgeBaseId);
                return true;
            } catch (Exception e) {
                log.error("删除向量库失败: {}", e.getMessage(), e);
                return false;
            }
        } catch (Exception e) {
            log.error("删除向量库失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据知识库ID和文档ID删除向量数据
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    public boolean deleteEmbeddingsByDocumentId(Long knowledgeBaseId, String documentId) {
        try {
            // 构建集合名称
            String collectionName = aiModelsProperties.getMilvus().getCollection() + "_" + knowledgeBaseId;
            
            // 使用默认维度，因为删除操作不需要精确的维度信息
            EmbeddingStore<TextSegment> embeddingStore = getMilvusStore(collectionName,  aiModelsProperties.getMilvus().getDimension(), false);
            
            try {
                log.info("尝试删除知识库ID={}，文档ID={}的向量数据", knowledgeBaseId, documentId);
                
                // 创建过滤器，匹配属于该知识库和文档的向量
                Filter filter = MetadataFilterBuilder.metadataKey("knowledgeBaseId").isEqualTo(knowledgeBaseId.toString())
                        .and(MetadataFilterBuilder.metadataKey("documentId").isEqualTo(documentId));
                
                // 删除所有匹配的向量
                embeddingStore.removeAll(filter);
                
                log.info("Milvus成功删除知识库ID={}，文档ID={}的向量数据", knowledgeBaseId, documentId);
                return true;
            } catch (Exception e) {
                log.error("删除向量库失败: {}", e.getMessage(), e);
                return false;
            }
        } catch (Exception e) {
            log.error("删除向量库失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据知识库ID、文档ID和片段ID删除向量数据
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @param segmentId 片段ID
     * @return 是否删除成功
     */
    public boolean deleteEmbeddingsBySegmentId(Long knowledgeBaseId, String documentId, String segmentId) {
        try {
            // 构建集合名称
            String collectionName = aiModelsProperties.getMilvus().getCollection() + "_" + knowledgeBaseId;
            
            // 使用默认维度，因为删除操作不需要精确的维度信息
            EmbeddingStore<TextSegment> embeddingStore = getMilvusStore(collectionName,  aiModelsProperties.getMilvus().getDimension(), false);
            
            try {
                log.info("尝试删除知识库ID={}，文档ID={}，片段ID={}的向量数据", knowledgeBaseId, documentId, segmentId);
                
                // 创建过滤器，匹配属于该知识库、文档和片段的向量
                Filter filter = MetadataFilterBuilder.metadataKey("knowledgeBaseId").isEqualTo(knowledgeBaseId.toString())
                        .and(MetadataFilterBuilder.metadataKey("documentId").isEqualTo(documentId))
                        .and(MetadataFilterBuilder.metadataKey("segmentId").isEqualTo(segmentId));
                
                // 删除所有匹配的向量
                embeddingStore.removeAll(filter);
                
                log.info("Milvus成功删除知识库ID={}，文档ID={}，片段ID={}的向量数据", knowledgeBaseId, documentId, segmentId);
                return true;
            } catch (Exception e) {
                log.error("删除向量库失败: {}", e.getMessage(), e);
                return false;
            }
        } catch (Exception e) {
            log.error("删除向量库失败: {}", e.getMessage(), e);
            return false;
        }
    }

}