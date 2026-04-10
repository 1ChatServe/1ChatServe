package chat.aikf.ai.controller;

import chat.aikf.common.core.domain.R;
import chat.aikf.ai.api.domain.OneChatExclModelConfig;
import chat.aikf.ai.service.IOneChatExclModelConfigService;
import chat.aikf.common.core.web.controller.BaseController;
import chat.aikf.common.core.web.page.TableDataInfo;
import chat.aikf.common.log.annotation.Log;
import chat.aikf.common.log.enums.BusinessType;
import chat.aikf.common.security.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Arrays;

/**
* @author robin
* @description 针对表【one_chat_excl_model_config(员工专属模型，设置)】的控制器
* @createDate 2026-04-03
*/
@RestController
@RequestMapping("/exclModelConfig")
public class OneChatExclModelConfigController extends BaseController {

    @Resource
    private IOneChatExclModelConfigService oneChatExclModelConfigService;



    /**
     * 获取当前账号的配置
     */
    @GetMapping("/findOneChatExclModelConfig")
    public R findOneChatExclModelConfig() {

        OneChatExclModelConfig modelConfig = oneChatExclModelConfigService.getOne(new LambdaQueryWrapper<OneChatExclModelConfig>()
                .eq(OneChatExclModelConfig::getUserId, SecurityUtils.getUserId())
                .last("LIMIT 1"));

        return R.ok(modelConfig);
    }


    /**
     * 创建或更新
     */
    @Log(title = "创建或更新员工专属模型配置", businessType = BusinessType.INSERT)
    @PostMapping("/saveOrUpdate")
    public R saveOrUpdate(@RequestBody OneChatExclModelConfig oneChatExclModelConfig) {
        // 获取当前用户ID
        Long userId = SecurityUtils.getUserId();
        
        // 查询是否已存在配置
        OneChatExclModelConfig existingConfig = oneChatExclModelConfigService.getOne(
            new LambdaQueryWrapper<OneChatExclModelConfig>()
                .eq(OneChatExclModelConfig::getUserId, userId)
                .last("LIMIT 1")
        );
        
        if (existingConfig != null) {
            // 更新现有配置
            oneChatExclModelConfig.setId(existingConfig.getId());
            oneChatExclModelConfig.setUserId(userId);
            oneChatExclModelConfigService.updateById(oneChatExclModelConfig);
        } else {
            // 创建新配置
            oneChatExclModelConfig.setUserId(userId);
            oneChatExclModelConfigService.save(oneChatExclModelConfig);
        }
        
        return R.ok();
    }


}
