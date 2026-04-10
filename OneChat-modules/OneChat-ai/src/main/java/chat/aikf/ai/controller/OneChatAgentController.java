package chat.aikf.ai.controller;

import chat.aikf.ai.api.domain.OneChatKnowledgeBase;
import chat.aikf.ai.api.domain.OneChatModelConfig;
import chat.aikf.ai.service.IOneChatKnowledgeBaseService;
import chat.aikf.ai.service.IOneChatModelConfigService;
import chat.aikf.common.core.domain.R;
import chat.aikf.ai.api.domain.OneChatAgent;
import chat.aikf.ai.service.IOneChatAgentService;
import chat.aikf.common.core.utils.AgentAvatarUtils;
import chat.aikf.common.core.utils.StringUtils;
import chat.aikf.common.core.web.controller.BaseController;
import chat.aikf.common.core.web.page.TableDataInfo;
import chat.aikf.common.log.annotation.Log;
import chat.aikf.common.log.enums.BusinessType;
import cn.hutool.core.collection.CollectionUtil;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
* @author robin
* @description 针对表【one_chat_agent(智能体管理)】的控制器
* @createDate 2026-03-25
*/
@RestController
@RequestMapping("/oneChatAgent")
public class OneChatAgentController extends BaseController {

    @Resource
    private IOneChatAgentService oneChatAgentService;

    @Resource
    private IOneChatModelConfigService oneChatModelConfigService;


    @Resource
    private IOneChatKnowledgeBaseService oneChatKnowledgeBaseService;

    /**
     * 查询列表
     */
    @GetMapping("/list")
    public R list(OneChatAgent oneChatAgent) {
//         startPage();
        List<OneChatAgent> list = oneChatAgentService.findList(oneChatAgent);

        if(CollectionUtil.isNotEmpty(list)){
            list.stream().forEach(item->{

                if(CollectionUtil.isNotEmpty(item.getKnowledgeIds())){
                    item.setOneChatKnowledgeBases(
                            oneChatKnowledgeBaseService.listByIds(item.getKnowledgeIds())
                    );
                }
            });

        }
//        return getDataTable(list);

        return R.ok(list);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/get/{id}")
    public R get(@PathVariable Long id) {
        OneChatAgent oneChatAgent = oneChatAgentService.getById(id);
        if(null != oneChatAgent && CollectionUtil.isNotEmpty(oneChatAgent.getKnowledgeIds() )){
            oneChatAgent.setOneChatKnowledgeBases(
                    oneChatKnowledgeBaseService.listByIds(oneChatAgent.getKnowledgeIds())
            );
        }

        return R.ok(oneChatAgent);
    }

    /**
     * 创建
     */
    @Log(title = "创建智能体", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public R create(@Validated @RequestBody OneChatAgent oneChatAgent) {
        OneChatModelConfig modelConfig = oneChatModelConfigService.getById(oneChatAgent.getModeConfigId());
        if(null != modelConfig){
            oneChatAgent.setModelName(modelConfig.getModelName());
        }
        oneChatAgent.setAgentAvatar(AgentAvatarUtils.getRandomAgentAvatarUrl());
        oneChatAgentService.save(oneChatAgent);
        return R.ok();
    }

    /**
     * 更新
     */
    @Log(title = "更新智能体", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public R update(@RequestBody OneChatAgent oneChatAgent) {
        OneChatModelConfig modelConfig = oneChatModelConfigService.getById(oneChatAgent.getModeConfigId());
        if(null != modelConfig){
            oneChatAgent.setModelName(modelConfig.getModelName());
        }
        if(StringUtils.isEmpty(oneChatAgent.getAgentAvatar())){
            oneChatAgent.setAgentAvatar(AgentAvatarUtils.getRandomAgentAvatarUrl());
        }
        oneChatAgentService.updateById(oneChatAgent);
        return R.ok();
    }

    /**
     * 删除
     */
    @Log(title = "删除智能体", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R delete(@PathVariable Long[] ids) {
         oneChatAgentService.removeBatchByIds(Arrays.asList(ids));
        return R.ok();
    }
}