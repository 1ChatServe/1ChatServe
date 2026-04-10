package chat.aikf.ai.rag;

import chat.aikf.ai.api.domain.OneChatKnowledgeBase;
import chat.aikf.ai.api.domain.OneChatKnowledgeBaseAttachment;
import chat.aikf.ai.api.domain.OneChatKnowledgeBaseSegment;
import chat.aikf.ai.api.domain.OneChatModelConfig;
import chat.aikf.ai.config.AiModelsProperties;
import chat.aikf.ai.rag.parser.DocumentParser;
import chat.aikf.ai.rag.parser.DocumentParserFactory;
import chat.aikf.ai.rag.store.VectorStoreService;
import chat.aikf.ai.service.IOneChatKnowledgeBaseSegmentService;
import chat.aikf.ai.service.IOneChatKnowledgeBaseAttachmentService;
import chat.aikf.ai.service.IOneChatModelConfigService;
import chat.aikf.ai.utils.FileFormatUtils;
import chat.aikf.ai.utils.TextCleaner;
import chat.aikf.common.core.domain.R;
import chat.aikf.common.core.utils.SnowFlakeUtils;
import chat.aikf.system.api.RemoteFileService;
import chat.aikf.system.api.domain.SysFile;
import java.util.concurrent.CompletableFuture;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RAG文档处理工厂
 */
@Service
@RequiredArgsConstructor
public class RagProcessFactory {

    private static final Logger log = LoggerFactory.getLogger(RagProcessFactory.class);


    private final RemoteFileService remoteFileService;
    private final VectorStoreService vectorStoreService;
    private final IOneChatKnowledgeBaseSegmentService segmentService;
    private final IOneChatKnowledgeBaseAttachmentService attachmentService;
    private final AiModelsProperties aiModelsProperties;
    private final IOneChatModelConfigService modelConfigService;

    /**
     * 处理文档
     * @param file 上传的文件
     * @param knowledgeBase 知识库信息
     * @return 知识库附件信息
     * @throws IOException
     */
    // 文件大小限制（100MB）
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    public OneChatKnowledgeBaseAttachment processDocument(MultipartFile file, OneChatKnowledgeBase knowledgeBase) throws IOException {
        // 创建附件记录
        OneChatKnowledgeBaseAttachment attachment = new OneChatKnowledgeBaseAttachment();
        attachment.setId(SnowFlakeUtils.nextId());
        attachment.setKnowledgeBaseId(knowledgeBase.getId());
        
        // 上传文件到文件服务
        R<SysFile> uploadResult = remoteFileService.upload(file);
        if (uploadResult.getData() == null) {
            throw new IOException("文件上传失败：" + uploadResult.getMsg());
        }
        SysFile sysFile = uploadResult.getData();
        
        // 设置附件信息
        attachment.setFileName(sysFile.getName());
        attachment.setFileType(FileFormatUtils.getFriendlyFileType(file.getOriginalFilename()));
        attachment.setFileSize(file.getSize());
        attachment.setFileUrl(sysFile.getUrl());
        attachment.setParseStatus(0); // 0-解析中
        
        // 异步处理文档解析、分段和向量计算
        CompletableFuture.runAsync(() -> {
            try {
                processDocument(file, knowledgeBase, attachment.getId());
                
                // 处理成功，更新状态为已解析
                OneChatKnowledgeBaseAttachment updateAttachment = new OneChatKnowledgeBaseAttachment();
                updateAttachment.setId(attachment.getId());
                updateAttachment.setParseStatus(1); // 1-已解析
                attachmentService.updateById(updateAttachment);
            } catch (Exception e) {
                log.error("异步处理文档失败：{}", e.getMessage());
                // 处理失败，更新状态为解析失败
                OneChatKnowledgeBaseAttachment updateAttachment = new OneChatKnowledgeBaseAttachment();
                updateAttachment.setId(attachment.getId());
                updateAttachment.setParseStatus(2); // 2-解析失败
                attachmentService.updateById(updateAttachment);
            }
        });
        
        return attachment;
    }
    
    public void processDocument(MultipartFile file, OneChatKnowledgeBase knowledgeBase, Long attachmentId) throws IOException {
        // 1. 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("文件大小超过限制，最大支持100MB");
        }
        
        // 2. 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IOException("文件名不能为空");
        }
        
        if (!FileFormatUtils.isSupportedFile(fileName)) {
            throw new IOException("仅支持 " + FileFormatUtils.getSupportedFileTypesDescription() + " 格式的文件");
        }

        // 2. 上传文件到文件服务
        R<SysFile> uploadResult = remoteFileService.upload(file);
        if ( uploadResult.getData() == null) {
            throw new IOException("文件上传失败：" + uploadResult.getMsg());
        }
        SysFile sysFile = uploadResult.getData();

        // 3. 创建附件记录
        OneChatKnowledgeBaseAttachment attachment = new OneChatKnowledgeBaseAttachment();
        attachment.setId(attachmentId);
        attachment.setKnowledgeBaseId(knowledgeBase.getId());
        attachment.setFileName(sysFile.getName());
        
        // 根据文件扩展名设置友好的文件类型
        String fileType = FileFormatUtils.getFriendlyFileType(fileName);
        
        attachment.setFileType(fileType);
        attachment.setFileSize(file.getSize());
        attachment.setFileUrl(sysFile.getUrl());
        // 设置初始解析状态为解析中
        attachment.setParseStatus(0);

        // 4. 提取文件内容并分片
        List<TextSegment> segments = new ArrayList<>();
        try {
            // 读取文件内容到字节数组，以便多次使用
            byte[] fileBytes = file.getBytes();
            
            // 获取文件扩展名
            String extension = FileFormatUtils.getFileExtension(fileName);
            
            // 获取对应的解析器
            DocumentParser parser = DocumentParserFactory.getParser(extension);
            
            // 创建文档元数据
            Metadata metadata = new Metadata();
            metadata.put("documentName", sysFile.getName());
            metadata.put("documentUrl", sysFile.getUrl());
            metadata.put("knowledgeBaseId", knowledgeBase.getId().toString());

            // 使用字节数组创建输入流进行文档解析
            try (var inputStream = new ByteArrayInputStream(fileBytes)) {
                // 使用对应的解析器解析文档
                Document document = parser.parse(inputStream, metadata);

                // 根据知识库配置进行文档分片
                int segmentLength = knowledgeBase.getSegmentLength() != null ? knowledgeBase.getSegmentLength() : 1000;
                int overlapLength = knowledgeBase.getOverlapLength() != null ? knowledgeBase.getOverlapLength() : 100;
                
                // 确保重合长度小于分段长度，避免分割器报错
                if (overlapLength >= segmentLength) {
                    overlapLength = Math.max(0, segmentLength - 1);
                }

                // 使用LangChain4j的文档分割器
                DocumentSplitter splitter = DocumentSplitters.recursive(segmentLength, overlapLength);
                segments = splitter.split(document);
                log.info("文档分割完成，原始文档长度: {}，分割后片段数: {}", document.text().length(), segments.size());

                // 为每个文本片段添加元数据并清理无效字符
                List<TextSegment> segmentsWithMetadata = new ArrayList<>();
                for (int i = 0; i < segments.size(); i++) {
                    TextSegment segment = segments.get(i);
                    
                    // 清理文本中的无效字符
                    String cleanedText = TextCleaner.cleanText(segment.text());
                    
                    // 过滤掉无效文本
                    if (!TextCleaner.isValidText(cleanedText)) {
                        continue;
                    }
                    
                    // 创建包含元数据
                    Metadata segmentMetadata = new Metadata();
                    // 逐个添加元数据键值对
                    if (metadata != null) {
                        // 使用 toMap() 方法获取元数据的键值对
                        java.util.Map<String, Object> metadataMap = metadata.toMap();
                        for (java.util.Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            // 根据值的类型调用相应的 put 方法
                            if (value instanceof String) {
                                segmentMetadata.put(key, (String) value);
                            } else if (value instanceof Integer) {
                                segmentMetadata.put(key, (Integer) value);
                            } else if (value instanceof Long) {
                                segmentMetadata.put(key, (Long) value);
                            } else if (value instanceof Float) {
                                segmentMetadata.put(key, (Float) value);
                            } else if (value instanceof Double) {
                                segmentMetadata.put(key, (Double) value);
                            }
                        }
                    }
                    
                    TextSegment segmentWithMetadata = TextSegment.from(cleanedText, segmentMetadata);
                    segmentsWithMetadata.add(segmentWithMetadata);
                }
                segments = segmentsWithMetadata;
                log.info("清理后有效片段数: {}", segments.size());
            }
        } catch (IOException e) {
            log.error("处理文档失败：{}", e.getMessage());
            throw e;
        }

        // 4. 存储向量
        if (!segments.isEmpty()) {
            // 准备存储向量所需的数据
            List<String> texts = new ArrayList<>();
            List<String> segmentIds = new ArrayList<>();
            String documentId = attachment.getId().toString();
            Long knowledgeBaseId = knowledgeBase.getId();
            String documentName = attachment.getFileName();
            String documentUrl = attachment.getFileUrl();
            
            log.info("开始存储向量，文档: {}, 片段数: {}", documentName, segments.size());
            
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                texts.add(segment.text());
                segmentIds.add("segment_" + knowledgeBaseId + "_" + documentId + "_" + i);
            }
            
            // 从数据库获取默认嵌入模型
            String embeddingModelName = getDefaultEmbeddingModel();
            if (embeddingModelName != null) {
                log.info("使用模型: {} 存储向量", embeddingModelName);
                // 使用新方法存储向量，包含详细元数据
                List<String> vectorIds = vectorStoreService.storeEmbeddingsWithMetadata(texts, knowledgeBaseId, documentId, segmentIds, knowledgeBase, embeddingModelName, documentName, documentUrl);
                log.info("向量存储完成，生成向量数: {}", vectorIds.size());
            } else {
                log.error("未找到可用的嵌入模型，跳过向量存储");
            }
        }

        // 5. 保存文本片段到数据库
        if (!segments.isEmpty() && attachment.getId() != null) {
            List<OneChatKnowledgeBaseSegment> dbSegments = new ArrayList<>();
            // 使用Set去重，避免重复片段
            java.util.Set<String> segmentContentSet = new java.util.HashSet<>();
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                String segmentContent = segment.text();
                
                // 去重：只添加内容唯一的片段
                if (segmentContentSet.add(segmentContent)) {
                    OneChatKnowledgeBaseSegment dbSegment = OneChatKnowledgeBaseSegment.builder()
                            .attachmentId(attachment.getId())
                            .segmentContent(segmentContent)
                            .segmentIndex(i)
                            .vectorDimension(aiModelsProperties.getMilvus().getDimension())
                            .build();
                    dbSegments.add(dbSegment);
                }
            }
            log.info("去重后保存片段数: {}", dbSegments.size());
            segmentService.saveBatch(dbSegments);
        }

        // 设置解析状态为已解析
        attachment.setParseStatus(1);
    }

    /**
     * 获取默认嵌入模型
     * @return 嵌入模型名称
     */
    private String getDefaultEmbeddingModel() {
        // 查询启用的嵌入模型
        List<OneChatModelConfig> embeddingModels = modelConfigService.list();
        for (OneChatModelConfig model : embeddingModels) {
            if (model.getModelStatus() == 1 && OneChatModelConfig.MODEL_TYPE_EMBEDDING.equals(model.getModelType())) {
                return model.getModelName();
            }
        }
        return null;
    }

}
