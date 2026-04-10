package chat.aikf.im.tio.service;

import chat.aikf.ai.api.RemoteAstService;
import chat.aikf.ai.api.domain.OneAiReplyDto;
import chat.aikf.common.core.config.OneChatConfig;
import chat.aikf.common.core.constant.Constants;
import chat.aikf.common.core.constant.OneChatCacheKeyConstants;
import chat.aikf.common.core.constant.SecurityConstants;
import chat.aikf.common.core.domain.R;
import chat.aikf.common.core.exception.ServiceException;
import chat.aikf.common.core.utils.StringUtils;
import chat.aikf.common.redis.service.RedisService;
import chat.aikf.im.api.domain.dto.GuestIdentityMsgDto;
import chat.aikf.im.tio.constant.OneChatImConstant;
import chat.aikf.im.tio.conversation.service.VisitorStateService;
import chat.aikf.im.tio.model.*;
import chat.aikf.im.tio.starter.OneChatImStarter;
import chat.aikf.im.tio.utils.KfCacheRelUtils;
import chat.aikf.ops.api.RemoteKfRuleService;
import chat.aikf.ops.api.RemoteKfVisitorService;
import chat.aikf.ops.api.constant.OneChatVisitorSate;
import chat.aikf.ops.api.domain.OneChatKfRule;
import chat.aikf.ops.api.domain.OneChatKfVisitorMsg;
import chat.aikf.ops.api.domain.OneChatkfVisitor;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;
import java.util.*;


@Service
@Slf4j
public class ChatMessageService {



    @Autowired
    private RemoteKfVisitorService remoteKfVisitorService;


    @Autowired
    @Lazy
    private OneChatImStarter oneChatImStarter;



//    @Autowired
//    private OneChatConfig oneChatConfig;
//
//
//    @Autowired
//    private KfCacheRelUtils kfCacheRelUtils;


    @Autowired
    private RedisService redisService;


    @Autowired
    private RemoteAstService remoteAstService;


    @Autowired
    private RemoteKfRuleService remoteKfRuleService;


//    @Autowired
//    @Lazy
//    private VisitorStateService visitorStateService;







    //访客消息入库，更新。并提示通知访客
    public void savePendingMessage(GuestResqMsgDto chatMsgDto,boolean isGuestClient){

        String initKey = OneChatCacheKeyConstants.ImKeyGenerator.getInitVisitorSessionKey( chatMsgDto.getVisitorId(),chatMsgDto.getWebStyleId().toString());
        GuestIdentityMsgDto msgDto = redisService.getCacheObject(initKey);


        if(null != msgDto){//校验当前会话凭证是否存在

            if(isGuestClient){ //来自客服客户端
                chatMsgDto.setFromObj(msgDto.getKfVisitorId());
                chatMsgDto.setKfRuleId(Long.parseLong(msgDto.getKfRuleId()));
                chatMsgDto.setKfVisitorId(Long.parseLong(msgDto.getKfVisitorId()));
                chatMsgDto.setShowAvatar(msgDto.getKfVisitoravatar());
                chatMsgDto.setShowName(msgDto.getKfVisitorName());
                chatMsgDto.setToObj(msgDto.getReceptObjId());
            }else{//来自客服员工端


            }


            //客户会话入库
            OneChatKfVisitorMsg visitorMsg = OneChatKfVisitorMsg.builder()
                    .kfVisitorId(chatMsgDto.getKfVisitorId())
                    .fromObj(chatMsgDto.getFromObj())
                    .showAvatar(chatMsgDto.getShowAvatar())
                    .showName(chatMsgDto.getShowName())
                    .toObj(chatMsgDto.getToObj())
                    .kfRuleId(chatMsgDto.getKfRuleId())
                    .msgType(chatMsgDto.getMsgType())
                    .content(chatMsgDto.getContent())
                    .msgSource(chatMsgDto.getMsgSource())
                    .readReceipt(chatMsgDto.getReadReceipt())
                    .sendTime(new Date())
                    .build();

            R<OneChatKfVisitorMsg> r = remoteKfVisitorService.addMsgVisitor(visitorMsg, SecurityConstants.INNER);

            if (R.FAIL == r.getCode()) {
                log.error("会话消息入库失败:"+r.getMsg());
                throw new ServiceException(r.getMsg());
            }


            if(OneChatVisitorSate.AI_RECEIVE_STATE==msgDto.getInitState()){//ai回复

                // 推送给员工端
                WsResponse responseToadmin1 = WsResponse.fromText(JSONUtil.toJsonStr(UserReplyMsgDto.buildUserReplyMsgDto(visitorMsg,0)), Constants.UTF8);

                Tio.sendToUser(oneChatImStarter.getServerTioConfig(),chatMsgDto.getToObj(), responseToadmin1);

                //客服客户端原始消息推送给
                R<OneChatKfRule> oneChatKfRule = remoteKfRuleService.findOneChatKfRule(chatMsgDto.getKfRuleId(), SecurityConstants.INNER);

                    R<String> aiReply = remoteAstService.aiReply(OneAiReplyDto.builder()
                            .agentId(oneChatKfRule.getData().getAgentId())
                            .question(chatMsgDto.getContent())
                            .build(), SecurityConstants.INNER);
                    // AI回复消息入库
                    OneChatKfVisitorMsg aiVisitorMsg = OneChatKfVisitorMsg.builder()
                            .kfVisitorId(chatMsgDto.getKfVisitorId())
                            .fromObj(chatMsgDto.getToObj())
                            .showAvatar(msgDto.getReceptObjavatar())
                            .showName(msgDto.getReceptObjName())
                            .toObj(chatMsgDto.getFromObj())
                            .kfRuleId(chatMsgDto.getKfRuleId())
                            .msgType(chatMsgDto.getMsgType())
                            .msgSource(3)
                            .readReceipt(chatMsgDto.getReadReceipt())
                            .sendTime(new Date())
                            .build();

                    if(null != oneChatKfRule && oneChatKfRule.getData() !=null && oneChatKfRule.getData().getAgentId() != null){


                        if(aiReply !=null && StringUtils.isNotEmpty(aiReply.getData()) && !aiReply.getData().equals("当前智能客服无法为你提供需要的服务,请转接人工")){//ai回复，并且消息推送给客户与员工

                            aiVisitorMsg.setContent(aiReply.getData());
                            R<OneChatKfVisitorMsg> aiMsgR = remoteKfVisitorService.addMsgVisitor(aiVisitorMsg, SecurityConstants.INNER);
                            if (R.FAIL == aiMsgR.getCode()) {
                                log.error("AI回复消息入库失败:"+aiMsgR.getMsg());
                            } else {


                                // 推送给员工端
                                WsResponse responseToadmin = WsResponse.fromText(JSONUtil.toJsonStr(UserReplyMsgDto.buildUserReplyMsgDto(aiVisitorMsg,3)), Constants.UTF8);
                                String toObj = chatMsgDto.getToObj();
                                Tio.sendToUser(oneChatImStarter.getServerTioConfig(),toObj, responseToadmin);

                                // 推送给客户端
                                WsResponse responseToGuest = WsResponse.fromText(JSONUtil.toJsonStr(GuestReplyMsgDto.buildGuestReplyMsgDto(aiVisitorMsg,3,"chatting")), Constants.UTF8);
                                String toUserId = new VisitorSessionKey(chatMsgDto.getVisitorId(), chatMsgDto.getWebStyleId().toString()).toString();
                                Tio.sendToUser(oneChatImStarter.getServerTioConfig(),toUserId, responseToGuest);
                            }
                        }else{//客户端提示客户转人工
                            aiVisitorMsg.setContent("当前智能客服无法为你提供需要的服务,请转接人工");
                            WsResponse response = WsResponse.fromText(JSONUtil.toJsonStr(GuestReplyMsgDto.buildGuestReplyMsgDto(aiVisitorMsg,3,"transferUser")), Constants.UTF8);
                            String toUserId = new VisitorSessionKey(chatMsgDto.getVisitorId(), chatMsgDto.getWebStyleId().toString()).toString();
                            Tio.sendToUser(oneChatImStarter.getServerTioConfig(),toUserId, response);
                        }
                    }else{
                        //客户端提示客户转人工
                        aiVisitorMsg.setContent("当前智能客服无法为你提供需要的服务,请转接人工");
                        WsResponse response = WsResponse.fromText(JSONUtil.toJsonStr(GuestReplyMsgDto.buildGuestReplyMsgDto(aiVisitorMsg,3,"transferUser")), Constants.UTF8);
                        String toUserId = new VisitorSessionKey(chatMsgDto.getVisitorId(), chatMsgDto.getWebStyleId().toString()).toString();
                        Tio.sendToUser(oneChatImStarter.getServerTioConfig(),toUserId, response);

                    }


            }else if(OneChatVisitorSate.RECEIVE_STATE==msgDto.getInitState()){ //人工会话
                chatMsgDto.setMsgId(r.getData().getId().toString());
                chatMsgDto.setSendTime(visitorMsg.getSendTime());

                if(OneChatImConstant.CLIENT_TYPE_GUEST.equals(chatMsgDto.getClientType())){//来自访客的信息，发送给员工
                    WsResponse response = WsResponse.fromText(JSONUtil.toJsonStr(UserReplyMsgDto.buildUserReplyMsgDto(visitorMsg,0)), Constants.UTF8);
                    String toUserId = chatMsgDto.getToObj();
                    Tio.sendToUser(oneChatImStarter.getServerTioConfig(),toUserId, response);
                }else{ //来自员工的消息，发送给访客

                  WsResponse response = WsResponse.fromText(JSONUtil.toJsonStr(GuestReplyMsgDto.buildGuestReplyMsgDto(r.getData(),1,"chatting")), Constants.UTF8);
                  String toUserId = new VisitorSessionKey(chatMsgDto.getToObj(), chatMsgDto.getWebStyleId().toString()).toString();
                  Tio.sendToUser(oneChatImStarter.getServerTioConfig(),toUserId, response);
                }

            }

        }




    }


    //处理访客信息(主动接入)
    public  void handleVisitorInfo(IdentityMsgDto msgDto){
        OneChatkfVisitor visitor = msgDto.getVisitor();
        if(null != visitor){
            //访客消息入库
            R<OneChatkfVisitor> r = remoteKfVisitorService.
                    addOrUpdate(visitor, SecurityConstants.INNER);
            if (R.FAIL == r.getCode()) {
                log.error("更新访客信息失败:"+r.getMsg());
                Tio.closeUser(oneChatImStarter.getServerTioConfig(),new VisitorSessionKey(visitor.getVisitorId(),visitor.getKfRuleId().toString()).toString(),"更新访客信息失败,请稍后重试");
                throw new ServiceException(r.getMsg());
            }
            if(null != r.getData().getId()){
                msgDto.setKfVisitorId(r.getData().getId().toString());
            }


            //通知访客初始化状态(以及提示语)
//            WsResponse responseToVisitor = WsResponse.fromText(JSONUtil.toJsonStr(GuestIdentityMsgDto.buildObj(msgDto, visitor.getVisitorMsgs().stream().findAny().get(),r.getData(),oneChatConfig.sessionTime)), Constants.UTF8);
//            Tio.sendToUser(oneChatImStarter.getServerTioConfig(),new VisitorSessionKey(visitor.getVisitorId(),visitor.getWebStyleId().toString()).toString(),responseToVisitor);


            if(msgDto.getReceptionState() != OneChatVisitorSate.END_STATE){ //离线不做通知与缓存
                //给员工通知消息
                WsResponse responseToUser = WsResponse.fromText(JSONUtil.toJsonStr(UserIdentityMsgDto.builder().initState(
                        msgDto.getReceptionState()
                ).build()), Constants.UTF8);
                Tio.sendToUser(oneChatImStarter.getServerTioConfig(), msgDto.getToObj(),responseToUser);


                //构建访客连接初始化数据
//                kfCacheRelUtils.linkInitCache(GuestIdentityMsgDto.buildObj(msgDto, visitor.getVisitorMsgs().stream().findAny().get(),r.getData(),oneChatConfig.sessionTime),visitor.getWebStyleId().toString());
            }



        }

    }
}