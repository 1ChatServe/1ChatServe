package chat.aikf.im.controller;

import chat.aikf.common.core.constant.OneChatCacheKeyConstants;
import chat.aikf.common.core.domain.R;
import chat.aikf.common.core.utils.DeviceUtils;
import chat.aikf.common.core.utils.RequestContextHelper;
import chat.aikf.common.redis.service.RedisService;
import chat.aikf.im.api.domain.dto.GuestIdentityMsgDto;
import chat.aikf.im.api.domain.dto.VisitorStateDto;
import chat.aikf.im.tio.constant.OneChatImConstant;
import chat.aikf.im.tio.conversation.service.VisitorStateService;
import chat.aikf.im.tio.service.ChatSessionMsgService;
import chat.aikf.ops.api.constant.OneChatVisitorSate;
import cn.hutool.core.collection.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/im")
public class OneChatImController {


    @Autowired
    private VisitorStateService visitorStateService;


    @Autowired
    private ChatSessionMsgService chatSessionMsgService;




    @Autowired
    private RedisService redisService;

//    @Autowired
//    private RequestContextUtils requestContextUtils;



    /**
     * 结束会话
     * @param visitorStateDto
     * @return
     */
    @PostMapping("/endChat")
    public R endChat(@RequestBody VisitorStateDto visitorStateDto){
        visitorStateService
                .processByStateToUser(visitorStateDto.getWebStyleId(),visitorStateDto.getKfRuleId(),visitorStateDto.getVisitorId(),visitorStateDto.getUserAccount(), OneChatVisitorSate.END_STATE);
        return R.ok();
    }




    /**
     * 转人工(客户端手动转接)
     * @param visitorStateDto
     * @return
     */
    @PostMapping("/transferUser")
    public R transferUser(@RequestBody VisitorStateDto visitorStateDto){
        visitorStateService.handleAiToHumanTransfer(
                visitorStateDto.getWebStyleId(),
                visitorStateDto.getKfRuleId(),
                visitorStateDto.getVisitorId(),
                visitorStateDto.getUserAccount()
        );
        return R.ok();
    }


    /**
     * 获取客户初始化数据(分配客服等逻辑都在这)
     * @param webStyleId
     * @param visitorId
     * @return
     */
    @GetMapping("/findInitSession")
    public R<GuestIdentityMsgDto> findInitSession(String webStyleId, String visitorId, HttpServletRequest request){
        String initKey = OneChatCacheKeyConstants.ImKeyGenerator.getInitVisitorSessionKey(visitorId,webStyleId);




        GuestIdentityMsgDto msgDto = redisService.getCacheObject(initKey);



        if(null == msgDto){
            msgDto
                    = chatSessionMsgService.findInitSession(webStyleId, visitorId, request);
        }


        return R.ok(msgDto);
    }


}
