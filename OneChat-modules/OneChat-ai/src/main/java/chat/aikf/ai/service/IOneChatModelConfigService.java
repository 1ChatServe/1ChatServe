package chat.aikf.ai.service;

import chat.aikf.ai.api.domain.OneChatModelConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_model_config(模型配置表)】的数据库操作Service
* @createDate 2026-03-24
*/
public interface IOneChatModelConfigService extends IService<OneChatModelConfig> {

    /**
     * 查询列表
     * @param oneChatModelConfig
     * @return
     */
    List<OneChatModelConfig> findList(OneChatModelConfig oneChatModelConfig);

}