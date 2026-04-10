package chat.aikf.ai.service.impl;

import chat.aifk.common.datascope.annotation.DataScope;
import chat.aikf.common.core.utils.StringUtils;
import chat.aikf.ai.api.domain.OneChatModelConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import chat.aikf.ai.service.IOneChatModelConfigService;
import chat.aikf.ai.mapper.OneChatModelConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_model_config(模型配置表)】的数据库操作Service实现
* @createDate 2026-03-24
*/
@Service
public class OneChatModelConfigServiceImpl extends ServiceImpl<OneChatModelConfigMapper, OneChatModelConfig>
    implements IOneChatModelConfigService {

    @Override
    @DataScope
    public List<OneChatModelConfig> findList(OneChatModelConfig oneChatModelConfig) {
        LambdaQueryWrapper<OneChatModelConfig> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.isNotEmpty(oneChatModelConfig.getSearchValue()),OneChatModelConfig::getModelName,oneChatModelConfig.getSearchValue());
        queryWrapper.eq(oneChatModelConfig.getModelType() !=null,OneChatModelConfig::getModelType,oneChatModelConfig.getModelType());
        queryWrapper.eq(oneChatModelConfig.getModelStatus() != null,OneChatModelConfig::getModelStatus,oneChatModelConfig.getModelStatus());
        // 按排序字段升序排列
        queryWrapper.orderByAsc(OneChatModelConfig::getModelSort);
        
        return this.list(queryWrapper);
    }
}