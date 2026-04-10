package chat.aikf.ai.service.impl;

import chat.aikf.ai.domain.OneChatSessionKeyStep;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import chat.aikf.ai.service.IOneChatSessionKeyStepService;
import chat.aikf.ai.mapper.OneChatSessionKeyStepMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_session_key_step(会话分析关键步骤表)】的数据库操作Service实现
* @createDate 2026-04-02
*/
@Service
public class OneChatSessionKeyStepServiceImpl extends ServiceImpl<OneChatSessionKeyStepMapper, OneChatSessionKeyStep>
    implements IOneChatSessionKeyStepService {

    @Override
    public List<OneChatSessionKeyStep> findList(OneChatSessionKeyStep oneChatSessionKeyStep) {
        List<OneChatSessionKeyStep> oneChatSessionKeySteps = this.list(new LambdaQueryWrapper<OneChatSessionKeyStep>()
                .orderByDesc(OneChatSessionKeyStep::getCreateTime));
        return oneChatSessionKeySteps;
    }

    @Override
    public List<OneChatSessionKeyStep> findBySessionAnalysisId(Long sessionAnalysisId) {
        return this.list(new LambdaQueryWrapper<OneChatSessionKeyStep>()
                .eq(OneChatSessionKeyStep::getSessionAnalysisId, sessionAnalysisId)
                .orderByAsc(OneChatSessionKeyStep::getCreateTime));
    }

}
