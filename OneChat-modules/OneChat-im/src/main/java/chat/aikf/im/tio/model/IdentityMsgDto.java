package chat.aikf.im.tio.model;

import chat.aikf.common.core.utils.*;
import chat.aikf.common.core.utils.ip.IpUtils;
import chat.aikf.im.tio.constant.OneChatChannelTypes;
import chat.aikf.im.api.constant.OneChatMsgTypes;
import chat.aikf.im.tio.constant.OneChatImConstant;
import chat.aikf.ops.api.constant.OneChatVisitorSate;
import chat.aikf.ops.api.domain.OneChatKfRule;
import chat.aikf.ops.api.domain.OneChatKfVisitorMsg;
import chat.aikf.ops.api.domain.OneChatkfVisitor;
import cn.hutool.core.collection.ListUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tio.http.common.HttpRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdentityMsgDto {
    /**
     * 当前状态 OneChatVisitorSate
     */
    private Integer receptionState;

    /**
     * 消息发送人
     */
    private String fromObj;


    /**
     * 消息接受人
     */
    private String toObj;


    /**
     * 消息接受人名称
     */
    private String toObjName;


    /**
     * 消息接受人头像
     */
    private String toObjavatar;



    /**
     * 访客id
     */
    private String kfVisitorId;


    /**
     * 客服规则id
     */
    private String kfRuleId;


    /**
     * 对应样式id
     */
    private String webStyleId;


    /**
     * 消息提示
     */
    private String msgTip;


    /**
     * 指导问题，多个使用逗号隔开
     */
    private List<String> guideMessage;



    private OneChatkfVisitor visitor;


    /**
     * 当前客服规则
     */
    private OneChatKfRule oneChatKfRule;


//    /**
//     * 构建访客信息
//     * @param httpRequest
//     * @param restored
//     * @param identityMsgDto
//     * @param deviceInfo
//     * @return
//     */
//    public static OneChatkfVisitor buildOneChatkfVisitorInfo(HttpRequest httpRequest, VisitorSessionKey restored, IdentityMsgDto identityMsgDto, DeviceUtils.DeviceInfo deviceInfo){

//        String ip = "";
//        if (httpRequest != null) {
//            // 尝试从 X-Real-IP 头获取
//            ip = httpRequest.getHeaders().get(OneChatImConstant.X_REAL_IP);
//            if (StringUtils.isEmpty(ip)) {
//                // 尝试从 X-Forwarded-For 头获取
//                ip = httpRequest.getHeaders().get("X-Forwarded-For");
//                if (StringUtils.isNotEmpty(ip)) {
//                    // X-Forwarded-For 可能包含多个 IP，取第一个
//                    ip = ip.split(",")[0].trim();
//                } else {
//                    // 直接从请求获取
//                    ip = httpRequest.getClientIp();
//                }
//            }
//        }
//
//        OneChatkfVisitor oneChatkfVisitor = OneChatkfVisitor.builder()
//                .name(IpLocationUtils.getCityByIp(ip) + restored.getVisitorIdLast4())
//                .visitorId(restored.visitorId())
//                .webStyleId(Long.parseLong(identityMsgDto.getWebStyleId()))
//                .kfRuleId(Long.parseLong(identityMsgDto.getKfRuleId()))
//                .userAccount(identityMsgDto.getToObj())
//                .ipaddr(ip)
//                .ipRealAddr(IpLocationUtils.getCityByIp(ip))
//                .currentViewTime(new Date())
//                .firstViewTime(new Date())
//                .viewNumber(1)
//                .channelType(OneChatChannelTypes.CHANNEL_TYPE_WEB)
//                .viewDevice(deviceInfo.getDeviceType())
//                .viewOs(deviceInfo.getOs())
//                .viewLanguage(deviceInfo.getLanguage())
//                .viewBrowser(deviceInfo.getBrowser())
//                .currentState(identityMsgDto.getReceptionState())
//                .build();
//
//
//        oneChatkfVisitor.setVisitorMsgs(ListUtil.toList(
//                OneChatKfVisitorMsg.builder()
//                        .id(SnowFlakeUtils.nextId())
//                        .showAvatar(identityMsgDto.getToObjavatar())
//                        .showName(identityMsgDto.getToObjName())
//                        .fromObj(identityMsgDto.getFromObj())
//                        .toObj(identityMsgDto.getToObj())
//                        .kfRuleId(Long.parseLong(identityMsgDto.getKfRuleId()))
//                        .msgType(OneChatMsgTypes.MSG_TYPE_TEXT)
//                        .content(identityMsgDto.getMsgTip())
//                        .sendTime(new Date())
//                        .build()
//        ));
//
//
//        return oneChatkfVisitor;
//
//    }


    public static OneChatkfVisitor buildObj(String visitorId,HttpServletRequest httpRequest, IdentityMsgDto identityMsgDto, DeviceUtils.DeviceInfo deviceInfo){

        OneChatkfVisitor oneChatkfVisitor = OneChatkfVisitor.builder()

                .name(IpLocationUtils.getCityByIp( RequestContextHelper.getOriginalIp(httpRequest)) + OneChatkfVisitor.getVisitorIdLast4(visitorId))
                .visitorId(visitorId)
                .userAccount(identityMsgDto.getToObj())
                .webStyleId(Long.parseLong(identityMsgDto.getWebStyleId()))
                .kfRuleId(Long.parseLong(identityMsgDto.getKfRuleId()))
                .userAccount(identityMsgDto.getToObj())
                .ipaddr(RequestContextHelper.getOriginalIp(httpRequest))
                .ipRealAddr(IpLocationUtils.getCityByIp(RequestContextHelper.getOriginalIp(httpRequest)))
                .currentViewTime(new Date())
                .firstViewTime(new Date())
                .viewNumber(1)
                .channelType(OneChatChannelTypes.CHANNEL_TYPE_WEB)
                .viewDevice(deviceInfo.getDeviceType())
                .viewOs(deviceInfo.getOs())
                .viewLanguage(deviceInfo.getLanguage())
                .viewBrowser(deviceInfo.getBrowser())
                .currentState(OneChatVisitorSate.getVisitorState(identityMsgDto.getReceptionState()))
                .build();
        return oneChatkfVisitor;
    }



}
