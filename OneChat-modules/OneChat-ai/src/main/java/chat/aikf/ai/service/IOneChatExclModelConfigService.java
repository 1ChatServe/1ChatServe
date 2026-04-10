package chat.aikf.ai.service;

import chat.aikf.ai.api.domain.OneChatExclModelConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_excl_model_config(员工专属模型，设置)】的数据库操作Service
* @createDate 2026-04-03
*/
public interface IOneChatExclModelConfigService extends IService<OneChatExclModelConfig> {

    /**
     * 查询列表
     * @param oneChatExclModelConfig
     * @return
     */
    List<OneChatExclModelConfig> findList(OneChatExclModelConfig oneChatExclModelConfig);

    /**
     * 根据员工ID查询模型配置
     * @param userId 员工ID
     * @return
     */
    OneChatExclModelConfig findByUserId(Long userId);

}