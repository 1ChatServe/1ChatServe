package chat.aikf.ai.service.impl;

import chat.aikf.common.core.utils.StringUtils;
import chat.aikf.ai.api.domain.OneChatAgent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import chat.aikf.ai.service.IOneChatAgentService;
import chat.aikf.ai.mapper.OneChatAgentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_agent(智能体管理)】的数据库操作Service实现
* @createDate 2026-03-25
*/
@Service
public class OneChatAgentServiceImpl extends ServiceImpl<OneChatAgentMapper, OneChatAgent>
    implements IOneChatAgentService {

    @Override
    public List<OneChatAgent> findList(OneChatAgent oneChatAgent) {
        List<OneChatAgent> oneChatAgents = this.list(new LambdaQueryWrapper<OneChatAgent>()
                        .like(StringUtils.isNotEmpty(oneChatAgent.getSearchValue()),OneChatAgent::getAgentName,oneChatAgent.getSearchValue())
                .orderByDesc(OneChatAgent::getCreateTime));
        return oneChatAgents;
    }
}