package chat.aikf.ai.controller;

import chat.aikf.common.core.domain.R;
import chat.aikf.ai.api.domain.OneChatModelConfig;
import chat.aikf.ai.factory.ModelFactory;
import chat.aikf.ai.service.IOneChatModelConfigService;
import chat.aikf.common.core.web.controller.BaseController;
import chat.aikf.common.core.web.page.TableDataInfo;
import chat.aikf.common.log.annotation.Log;
import chat.aikf.common.log.enums.BusinessType;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author robin
* @description 针对表【one_chat_model_config(模型配置表)】的控制器
* @createDate 2026-03-24
*/
@RestController
@RequestMapping("/modelConfig")
public class OneChatModelConfigController extends BaseController {

    @Resource
    private IOneChatModelConfigService oneChatModelConfigService;

    @Resource
    private ModelFactory modelFactory;

    /**
     * 查询列表
     */
    @GetMapping("/list")
    public TableDataInfo list(OneChatModelConfig oneChatModelConfig) {
        startPage();
        List<OneChatModelConfig> list = oneChatModelConfigService.findList(oneChatModelConfig);
        return getDataTable(list);
    }


    /**
     * 根据模型类型查询列表
     * @param oneChatModelConfig
     * @return
     */
    @GetMapping("/listAll")
    public R listAll(OneChatModelConfig oneChatModelConfig){
        oneChatModelConfig.setModelStatus(OneChatModelConfig.MODEL_STATUS_START);
        List<OneChatModelConfig> list = oneChatModelConfigService.findList(oneChatModelConfig);

        return R.ok(list);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/get/{id}")
    public R get(@PathVariable Long id) {
        return R.ok(oneChatModelConfigService.getById(id));
    }

    /**
     * 创建
     */
    @Log(title = "创建模型配置", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public R create(@RequestBody OneChatModelConfig oneChatModelConfig) {
        oneChatModelConfigService.save(oneChatModelConfig);
        // 刷新模型工厂配置
        modelFactory.refreshModels();
        return R.ok();
    }

    /**
     * 更新
     */
    @Log(title = "更新模型配置", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public R update(@RequestBody OneChatModelConfig oneChatModelConfig) {
        oneChatModelConfigService.updateById(oneChatModelConfig);
        // 刷新模型工厂配置
        modelFactory.refreshModels();
        return R.ok();
    }

    /**
     * 删除
     */
    @Log(title = "删除模型配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R delete(@PathVariable Long[] ids) {
        oneChatModelConfigService.removeBatchByIds(Arrays.asList(ids));
        // 刷新模型工厂配置
        modelFactory.refreshModels();
        return R.ok();
    }

    /**
     * 获取模型类型列表
     */
    @GetMapping("/modelTypes")
    public R getModelTypes() {
        Map<String, String> modelTypes = new HashMap<>();
        modelTypes.put(OneChatModelConfig.MODEL_TYPE_CHAT, "对话");
        modelTypes.put(OneChatModelConfig.MODEL_TYPE_EMBEDDING, "嵌入(向量)");
        modelTypes.put(OneChatModelConfig.MODEL_TYPE_IMAGE, "图像");
        return R.ok(modelTypes);
    }
}