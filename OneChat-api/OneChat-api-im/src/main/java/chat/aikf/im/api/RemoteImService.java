package chat.aikf.im.api;


import chat.aikf.common.core.constant.SecurityConstants;
import chat.aikf.common.core.constant.ServiceNameConstants;
import chat.aikf.common.core.domain.R;
import chat.aikf.im.api.domain.dto.GuestIdentityMsgDto;
import chat.aikf.im.api.domain.dto.VisitorStateDto;
import chat.aikf.im.api.factory.RemoteImFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(contextId = "remoteImService", value = ServiceNameConstants.OneChatIm, fallbackFactory = RemoteImFallbackFactory.class)
public interface RemoteImService {


    /**
     * 结束会话
     * @param visitorStateDto
     * @param source
     * @return
     */
    @PostMapping("/im/endChat")
    public R endChat(@RequestBody VisitorStateDto visitorStateDto, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);


//    /**
//     * 接入中
//     * @param visitorStateDto
//     * @param source
//     * @return
//     */
//    @PostMapping("/im/accessChat")
//    public R accessChat(@RequestBody  VisitorStateDto visitorStateDto, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);


    /**
     *  获取客户初始化数据(分配客服等逻辑都在这)
     * @param webStyleId
     * @param visitorId
     * @return
     */
    @GetMapping("/im/findInitSession")
    public  R<GuestIdentityMsgDto> findInitSession(  @RequestParam("webStyleId") String webStyleId,
                                                     @RequestParam("visitorId") String visitorId);


    /**
     * 转人工(客户端手动转接)
     * @param visitorStateDto
     * @return
     */
    @PostMapping("/im/transferUser")
    public R transferUser(@RequestBody VisitorStateDto visitorStateDto, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);



}
