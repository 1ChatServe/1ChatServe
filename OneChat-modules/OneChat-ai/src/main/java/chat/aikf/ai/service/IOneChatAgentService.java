package chat.aikf.ai.service;

import chat.aikf.ai.api.domain.OneChatAgent;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_agent(智能体管理)】的数据库操作Service
* @createDate 2026-03-25
*/
public interface IOneChatAgentService extends IService<OneChatAgent> {

    /**
     * 查询列表
     * @param oneChatAgent
     * @return
     */
    List<OneChatAgent> findList(OneChatAgent oneChatAgent);

}