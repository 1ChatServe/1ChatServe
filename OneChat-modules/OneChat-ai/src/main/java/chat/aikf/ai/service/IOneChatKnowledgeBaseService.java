package chat.aikf.ai.service;

import chat.aikf.ai.api.domain.OneChatKnowledgeBase;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_knowledge_base(企业知识库)】的数据库操作Service
* @createDate 2026-03-24
*/
public interface IOneChatKnowledgeBaseService extends IService<OneChatKnowledgeBase> {

    /**
     * 查询列表
     * @param oneChatKnowledgeBase
     * @return
     */
    List<OneChatKnowledgeBase> findList(OneChatKnowledgeBase oneChatKnowledgeBase);

    /**
     * 统计知识库模块信息
     * @return 统计结果
     */
    java.util.Map<String, Object> getStatistics();


    /**
     * 根据知识库id删除知识库
     * @param ids
     */
    void removeKnowledgeBase(Long[] ids);

}