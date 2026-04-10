package chat.aikf.ai.service.impl;

import chat.aikf.ai.domain.OneChatSessionAnalysis;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import chat.aikf.ai.service.IOneChatSessionAnalysisService;
import chat.aikf.ai.mapper.OneChatSessionAnalysisMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_session_analysis(会话分析结果表)】的数据库操作Service实现
* @createDate 2026-04-02
*/
@Service
public class OneChatSessionAnalysisServiceImpl extends ServiceImpl<OneChatSessionAnalysisMapper, OneChatSessionAnalysis>
    implements IOneChatSessionAnalysisService {

    @Override
    public List<OneChatSessionAnalysis> findList(OneChatSessionAnalysis oneChatSessionAnalysis) {
        List<OneChatSessionAnalysis> oneChatSessionAnalyses = this.list(new LambdaQueryWrapper<OneChatSessionAnalysis>()
                .orderByDesc(OneChatSessionAnalysis::getCreateTime));
        return oneChatSessionAnalyses;
    }

    @Override
    public OneChatSessionAnalysis findLatestByKfVisitorId(Long kfVisitorId) {
        return this.getOne(new LambdaQueryWrapper<OneChatSessionAnalysis>()
                .eq(OneChatSessionAnalysis::getKfVisitorId, kfVisitorId)
                .orderByDesc(OneChatSessionAnalysis::getAnalysisTime)
                .last("LIMIT 1"));
    }

    @Override
    public boolean deleteByKfVisitorId(Long kfVisitorId) {
        return this.remove(new LambdaQueryWrapper<OneChatSessionAnalysis>()
                .eq(OneChatSessionAnalysis::getKfVisitorId, kfVisitorId));
    }

}
