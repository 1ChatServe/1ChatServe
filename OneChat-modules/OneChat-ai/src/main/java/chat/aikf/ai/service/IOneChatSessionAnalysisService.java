package chat.aikf.ai.service;

import chat.aikf.ai.domain.OneChatSessionAnalysis;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_session_analysis(会话分析结果表)】的数据库操作Service
* @createDate 2026-04-02
*/
public interface IOneChatSessionAnalysisService extends IService<OneChatSessionAnalysis> {

    /**
     * 查询列表
     * @param oneChatSessionAnalysis
     * @return
     */
    List<OneChatSessionAnalysis> findList(OneChatSessionAnalysis oneChatSessionAnalysis);

    /**
     * 根据访客ID查询最新的会话分析结果
     * @param kfVisitorId 访客ID
     * @return 会话分析结果
     */
    OneChatSessionAnalysis findLatestByKfVisitorId(Long kfVisitorId);

    /**
     * 根据访客ID删除所有会话分析记录
     * @param kfVisitorId 访客ID
     * @return 删除成功返回true，否则返回false
     */
    boolean deleteByKfVisitorId(Long kfVisitorId);

}
