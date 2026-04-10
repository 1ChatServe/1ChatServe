package chat.aikf.ai.service;

import chat.aikf.ai.domain.OneChatSessionKeyStep;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_session_key_step(会话分析关键步骤表)】的数据库操作Service
* @createDate 2026-04-02
*/
public interface IOneChatSessionKeyStepService extends IService<OneChatSessionKeyStep> {

    /**
     * 查询列表
     * @param oneChatSessionKeyStep
     * @return
     */
    List<OneChatSessionKeyStep> findList(OneChatSessionKeyStep oneChatSessionKeyStep);

    /**
     * 根据会话分析ID查询关键步骤
     * @param sessionAnalysisId
     * @return
     */
    List<OneChatSessionKeyStep> findBySessionAnalysisId(Long sessionAnalysisId);

}
