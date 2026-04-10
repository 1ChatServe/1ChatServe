package chat.aikf.im.tio.utils;


import chat.aikf.common.core.config.OneChatConfig;
import chat.aikf.common.core.constant.Constants;
import chat.aikf.common.core.constant.OneChatCacheKeyConstants;
import chat.aikf.common.core.constant.SecurityConstants;
import chat.aikf.common.core.domain.R;
import chat.aikf.common.core.utils.SpringUtils;
import chat.aikf.common.redis.service.RedisService;
import chat.aikf.im.api.constant.OneChatMsgTypes;
import chat.aikf.im.api.domain.dto.GuestIdentityMsgDto;
import chat.aikf.im.tio.model.GuestReplyMsgDto;
import chat.aikf.im.tio.model.VisitorSessionKey;
import chat.aikf.im.tio.starter.OneChatImStarter;
import chat.aikf.ops.api.RemoteKfRuleService;
import chat.aikf.ops.api.RemoteKfVisitorService;
import chat.aikf.ops.api.constant.OneChatVisitorSate;
import chat.aikf.ops.api.domain.OneChatKfRule;
import chat.aikf.ops.api.domain.OneChatKfRuleScope;
import chat.aikf.ops.api.domain.OneChatKfVisitorMsg;
import chat.aikf.ops.api.domain.OneChatkfVisitor;
import chat.aikf.ops.api.utils.RuleFfServingUtils;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 客服业务缓存工具类
 */
@Component
public class KfCacheRelUtils {


    @Autowired
    private RedisService redisService;

    @Autowired
    private OneChatConfig oneChatConfig;


    @Autowired
    private RuleFfServingUtils ruleFfServingUtils;


    @Autowired
    private RemoteKfRuleService remoteKfRuleService;



    @Autowired
    @Lazy
    private OneChatImStarter oneChatImStarter;



    @Autowired
    private RemoteKfVisitorService remoteKfVisitorService;





    /**
     * 访客连接成功后设置的初始化缓存
     * @param msgDto
     * @param webStyleId
     */
    public void linkInitCache(GuestIdentityMsgDto msgDto,String webStyleId){

        //连接kfSession数据
        String initKey = OneChatCacheKeyConstants.ImKeyGenerator.getInitVisitorSessionKey(msgDto.getVisitorId(),webStyleId);
        redisService.setCacheObject(initKey,msgDto, oneChatConfig.sessionTime.longValue() , TimeUnit.MINUTES);


        //构建指定样式规则下,客服与访客的连接关系
        ruleFfServingUtils.bindVisitorToKf(webStyleId,msgDto.getKfRuleId(),msgDto.getReceptObjId(),msgDto.getVisitorId(),oneChatConfig.sessionTime.longValue());


        //设置指定样式下的访客规则已经分配到了哪个员工
        String allocateKfKey = OneChatCacheKeyConstants.ImKeyGenerator.getCurrentRuleAllocateKfKey(webStyleId,msgDto.getKfRuleId());
        redisService.setCacheObject(allocateKfKey,msgDto.getReceptObjId());

    }


    /**
     * 访客连接成功后设置的初始化缓存(手动对话结束)
     * @param visitorId
     * @param webStyleId
     */
    public void removeLinkInitCache(String kfRuleId,String userAccount,String visitorId,String webStyleId){

        //删除指定访客的kfsession
        String initKey = OneChatCacheKeyConstants.ImKeyGenerator.getInitVisitorSessionKey(visitorId,webStyleId);
        GuestIdentityMsgDto msgDto = SpringUtils.getBean(RedisService.class).getCacheObject(initKey);

        if(null != msgDto){
            redisService.deleteObject(initKey);

            //接触指定样式-客服规则下访客与客服的连接关系
            ruleFfServingUtils.unbindVisitorFromKf(webStyleId,kfRuleId,userAccount,visitorId);



            R<OneChatKfRule> data = remoteKfRuleService.findOneChatKfRule(Long.parseLong(kfRuleId), SecurityConstants.INNER);
            if(null != data){
                OneChatKfRule oneChatKfRule = data.getData();
                if(null != oneChatKfRule){
                    WsResponse response = WsResponse.fromText(JSONUtil.toJsonStr(GuestReplyMsgDto.buildGuestReplyMsgDto(OneChatKfVisitorMsg.builder()
                            .kfVisitorId(Long.parseLong(msgDto.getKfVisitorId()))
                            .fromObj(msgDto.getReceptObjId())
                            .showAvatar(msgDto.getReceptObjavatar())
                            .showName(msgDto.getReceptObjName())
                            .toObj(msgDto.getVisitorId())
                            .content(oneChatKfRule.getEndMsg())
                            .kfRuleId(Long.parseLong(msgDto.getKfRuleId()))
                            .msgType(OneChatMsgTypes.MSG_TYPE_TEXT)
                            .sendTime(new Date())
                            .build(),1,"endChat")), Constants.UTF8);
                    String toUserId = new VisitorSessionKey(visitorId,webStyleId).toString();
                    Tio.sendToUser(oneChatImStarter.getServerTioConfig(),toUserId, response);

                }



            }


        }









    }


    /**
     * 更新初始化连接数据（转人工）
     * @param visitorId
     * @param webStyleId
     */
    public void updateLinkInitCache(String visitorId,String webStyleId){

        String initKey =OneChatCacheKeyConstants.ImKeyGenerator.getInitVisitorSessionKey(visitorId,webStyleId);

        GuestIdentityMsgDto msgDto = SpringUtils.getBean(RedisService.class).getCacheObject(initKey);
        if(null != msgDto){
            R<OneChatKfRule> data = remoteKfRuleService
                    .findOneChatKfRule(Long.parseLong(msgDto.getKfRuleId()), SecurityConstants.INNER);
            OneChatKfRule oneChatKfRule = data.getData();

            if(null != oneChatKfRule){

                List<OneChatKfRuleScope> ruleScopeList = oneChatKfRule.getRuleScopeList();

                OneChatKfRuleScope oneChatKfRuleScope = ruleScopeList.stream()
                        .filter(item -> item.getUserAccount().equals(msgDto.getReceptObjId())).findAny().orElse(null);

                if(null != oneChatKfRuleScope){
                    msgDto.setInitState(OneChatVisitorSate.RECEIVE_STATE);
                    msgDto.setReceptObjName(oneChatKfRuleScope.getNickName());
                    msgDto.setReceptObjavatar(oneChatKfRuleScope.getAvatar());
                    msgDto.setReceptObjId(oneChatKfRuleScope.getUserAccount());
                    msgDto.setMsgTip(oneChatKfRule.getReceiveMsg());
                    redisService.setCacheObject(initKey,msgDto, oneChatConfig.sessionTime.longValue() , TimeUnit.MINUTES);

                    OneChatkfVisitor oneChatkfVisitor = remoteKfVisitorService.getOneChatkfVisitorById(Long.parseLong(msgDto.getKfVisitorId()), SecurityConstants.INNER).getData();

                    if(oneChatkfVisitor != null){
                        oneChatkfVisitor.setCurrentState(OneChatVisitorSate.getVisitorState(OneChatVisitorSate.RECEIVE_STATE));

                        //更新访客状态
                        remoteKfVisitorService.addOrUpdate(oneChatkfVisitor,SecurityConstants.INNER);

                        WsResponse response = WsResponse.fromText(JSONUtil.toJsonStr(GuestReplyMsgDto.buildGuestReplyMsgDto(OneChatKfVisitorMsg.builder()
                                .kfVisitorId(Long.parseLong(msgDto.getKfVisitorId()))
                                .fromObj(msgDto.getReceptObjId())
                                .showAvatar(oneChatKfRuleScope.getAvatar())
                                .showName(oneChatKfRuleScope.getNickName())
                                .toObj(msgDto.getVisitorId())
                                .content(oneChatKfRule.getReceiveMsg())
                                .kfRuleId(Long.parseLong(msgDto.getKfRuleId()))
                                .msgType(OneChatMsgTypes.MSG_TYPE_TEXT)
                                .sendTime(new Date())
                                .build(),1,"chatting")), Constants.UTF8);
                        String toUserId = new VisitorSessionKey(visitorId,webStyleId).toString();
                        Tio.sendToUser(oneChatImStarter.getServerTioConfig(),toUserId, response);

                    }



                }

            }
        }

    }


    /**
     * 获取指定样式客服规则已经分配到了哪个员工账号
     * @param webStyleId
     * @param kfRuleId
     * @return
     */
    public String getCurrentRuleAllocateKf(String webStyleId,String kfRuleId){
        String allocateKfKey = OneChatCacheKeyConstants.ImKeyGenerator.getCurrentRuleAllocateKfKey(webStyleId,kfRuleId);

        return  (String)redisService.getCacheObject(allocateKfKey);
    }


    /**
     * 获取指定样式下的客服规则-员工接待了多少人
     * @param webStyleId
     * @param kfRuleId
     * @param userAccount
     * @return
     */
    public long getServingCountByKf(String webStyleId,String kfRuleId, String userAccount){

       return ruleFfServingUtils.getServingCountByKf(webStyleId,kfRuleId,userAccount);
    }


}