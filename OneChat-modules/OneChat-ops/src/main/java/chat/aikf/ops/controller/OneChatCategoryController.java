package chat.aikf.ops.controller;


import chat.aikf.common.core.domain.R;
import chat.aikf.common.core.web.controller.BaseController;
import chat.aikf.ops.api.domain.OneChatCategory;
import chat.aikf.ops.service.IOneChatCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 分类管理
 */
@RestController
@RequestMapping("/category")
public class OneChatCategoryController extends BaseController {

    @Autowired
    private IOneChatCategoryService oneChatCategoryService;


    /**
     * 获取分类
     * @param oneChatCategory
     * @return
     */
    @GetMapping("/findList")
    public R<List<OneChatCategory>> findList(OneChatCategory oneChatCategory){
        List<OneChatCategory> list = oneChatCategoryService.list(new LambdaQueryWrapper<OneChatCategory>()
                .eq(oneChatCategory.getType() != null, OneChatCategory::getType, oneChatCategory.getType()));
        return R.ok(list);
    }


}
