package chat.aikf.ai.service.impl;


import chat.aikf.ai.api.domain.OneChatExclModelConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import chat.aikf.ai.service.IOneChatExclModelConfigService;
import chat.aikf.ai.mapper.OneChatExclModelConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_excl_model_config(员工专属模型，设置)】的数据库操作Service实现
* @createDate 2026-04-03
*/
@Service
public class OneChatExclModelConfigServiceImpl extends ServiceImpl<OneChatExclModelConfigMapper, OneChatExclModelConfig>
    implements IOneChatExclModelConfigService {

    @Override
    public List<OneChatExclModelConfig> findList(OneChatExclModelConfig oneChatExclModelConfig) {
        List<OneChatExclModelConfig> oneChatExclModelConfigs = this.list(new LambdaQueryWrapper<OneChatExclModelConfig>()
                .orderByDesc(OneChatExclModelConfig::getCreateTime));
        return oneChatExclModelConfigs;
    }

    @Override
    public OneChatExclModelConfig findByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<OneChatExclModelConfig>()
                .eq(OneChatExclModelConfig::getUserId, userId)
                .orderByDesc(OneChatExclModelConfig::getUpdateTime)
                .last("LIMIT 1"));
    }

}