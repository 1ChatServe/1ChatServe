package chat.aikf.ai.controller;

import chat.aikf.ai.utils.FileSizeUtils;
import chat.aikf.common.core.domain.R;
import chat.aikf.ai.api.domain.OneChatKnowledgeBase;
import chat.aikf.ai.api.domain.OneChatKnowledgeBaseAttachment;
import chat.aikf.ai.domain.KnowledgeBaseAttachmentQueryDto;
import chat.aikf.ai.service.IOneChatKnowledgeBaseService;
import chat.aikf.ai.service.IOneChatKnowledgeBaseAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import chat.aikf.common.core.web.controller.BaseController;
import chat.aikf.common.core.web.page.TableDataInfo;
import chat.aikf.common.log.annotation.Log;
import chat.aikf.common.log.enums.BusinessType;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_knowledge_base(企业知识库)】的控制器
* @createDate 2026-03-24
*/
@RestController
@RequestMapping("/knowledgeBase")
public class OneChatKnowledgeBaseController extends BaseController {

    @Autowired
    private IOneChatKnowledgeBaseService oneChatKnowledgeBaseService;

    @Autowired
    private IOneChatKnowledgeBaseAttachmentService oneChatKnowledgeBaseAttachmentService;



    /**
     * 查询列表
     */
    @GetMapping("/list")
    public R list(OneChatKnowledgeBase oneChatKnowledgeBase) {
        List<OneChatKnowledgeBase> list = oneChatKnowledgeBaseService.findList(oneChatKnowledgeBase);
        
        // 为每个知识库计算解析进度
        for (OneChatKnowledgeBase kb : list) {
            // 查询该知识库的所有附件
            OneChatKnowledgeBaseAttachment attachmentQuery = new OneChatKnowledgeBaseAttachment();
            attachmentQuery.setKnowledgeBaseId(kb.getId());
            List<OneChatKnowledgeBaseAttachment> attachments = oneChatKnowledgeBaseAttachmentService.findList(attachmentQuery);
            
            // 计算文档数
            int documentCount = attachments.size();
            kb.setDocumentCount(documentCount);
            
            // 计算总文件大小
            double totalFileSizeBytes = 0.0;
            for (OneChatKnowledgeBaseAttachment attachment : attachments) {
                if (attachment.getFileSize() != null) {
                    totalFileSizeBytes += attachment.getFileSize();
                }
            }
            
            // 使用工具类格式化文件大小
            String storageSize = FileSizeUtils.formatFileSize(totalFileSizeBytes);
            kb.setTotalFileSize(storageSize);
            
            // 计算解析进度
            if (documentCount > 0) {
                long parsedCount = 0;
                for (OneChatKnowledgeBaseAttachment attachment : attachments) {
                    if (attachment.getParseStatus() != null && attachment.getParseStatus() == 1) {
                        parsedCount++;
                    }
                }
                int progressPercentage = (int) Math.round((double) parsedCount / documentCount * 100);
                kb.setProgress(progressPercentage + "%");
            } else {
                kb.setProgress("0%");
            }
        }
        
        return R.ok(list);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/get/{id}")
    public R get(@PathVariable Long id) {
        // 获取知识库基本信息
        OneChatKnowledgeBase knowledgeBase = oneChatKnowledgeBaseService.getById(id);
        if (knowledgeBase == null) {
            return R.fail("知识库不存在");
        }
        
        // 查询该知识库的所有附件
        OneChatKnowledgeBaseAttachment attachmentQuery = new OneChatKnowledgeBaseAttachment();
        attachmentQuery.setKnowledgeBaseId(id);
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
        
        // 计算解析进度
        if (documentCount > 0) {
            long parsedCount = 0;
            for (OneChatKnowledgeBaseAttachment attachment : attachments) {
                if (attachment.getParseStatus() != null && attachment.getParseStatus() == 1) {
                    parsedCount++;
                }
            }
            int progressPercentage = (int) Math.round((double) parsedCount / documentCount * 100);
            knowledgeBase.setProgress(progressPercentage + "%");
        } else {
            knowledgeBase.setProgress("0%");
        }
        
        return R.ok(knowledgeBase);
    }

    /**
     * 创建
     */
    @Log(title = "创建知识库", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public R create(@Validated @RequestBody OneChatKnowledgeBase oneChatKnowledgeBase) {
        oneChatKnowledgeBaseService.save(oneChatKnowledgeBase);
        return R.ok();
    }

    /**
     * 更新
     */
    @Log(title = "更新知识库", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public R update(@Validated @RequestBody OneChatKnowledgeBase oneChatKnowledgeBase) {
        oneChatKnowledgeBaseService.updateById(oneChatKnowledgeBase);
        return R.ok();
    }

    /**
     * 删除
     */
    @Log(title = "删除知识库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R delete(@PathVariable Long[] ids) {
        oneChatKnowledgeBaseService.removeKnowledgeBase(ids);
        return R.ok();
    }

    /**
     * 获取知识库附件列表
     */
    @GetMapping("/attachment/list")
    public R getAttachmentList(KnowledgeBaseAttachmentQueryDto queryDto) {
        OneChatKnowledgeBaseAttachment attachment = new OneChatKnowledgeBaseAttachment();
        attachment.setKnowledgeBaseId(queryDto.getKnowledgeBaseId());
        if (queryDto.getFileName() != null) {
            attachment.setSearchValue(queryDto.getFileName());
        }
        if (queryDto.getFileType() != null) {
            attachment.setFileType(queryDto.getFileType());
        }
        List<OneChatKnowledgeBaseAttachment> list = oneChatKnowledgeBaseAttachmentService.findList(attachment);
        return R.ok(list);
    }

    /**
     * 删除知识库附件
     */
    @Log(title = "删除知识库附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/attachment/{id}")
    public R deleteAttachment(@PathVariable Long id) {
        oneChatKnowledgeBaseAttachmentService.deleteAttachment(id);
        return R.ok();
    }

    /**
     * 上传文档
     */
    @Log(title = "上传知识库文档", businessType = BusinessType.INSERT)
    @PostMapping("/attachment/upload")
    public R uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam("knowledgeBaseId") Long knowledgeBaseId) {
        try {
            boolean result = oneChatKnowledgeBaseAttachmentService.uploadDocument(file, knowledgeBaseId);
            if (result) {
                return R.ok("文档上传成功");
            } else {
                return R.fail("文档上传失败");
            }
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 命中测试
     */
    @GetMapping("/testHit")
    public R testHit(@RequestParam("knowledgeBaseId") Long knowledgeBaseId, @RequestParam("question") String question) {
        try {
            List<? extends Object> results = oneChatKnowledgeBaseAttachmentService.testHit(knowledgeBaseId, question);
            return R.ok(results);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 统计知识库模块信息
     */
    @GetMapping("/statistics")
    public R statistics() {
        try {
            return R.ok(oneChatKnowledgeBaseService.getStatistics());
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
}