package chat.aikf.im.tio.model;


import chat.aikf.ops.api.domain.OneChatKfVisitorMsg;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 *访客端消息响应实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GuestReplyMsgDto {

    /**
     * 接受内容
     */
    private String content;


    /**
     * 消息发送人
     */
    private String fromObj;


    /**
     * 客服组规则id
     */
    private Long kfRuleId;


    /**
     * 访客id(对应数据库记录id)
     */
    private Long kfVisitorId;



    /**
     * 消息类型 text文字  emotion表情  image图片
     */
    private String msgType;



    /**
     * 发送时间
     */
    private Date sendTime;



    /**
     * 展示头像
     */
    private String showAvatar;


    /**
     * 展示名称
     */
    private String showName;



    /**
     * 消息接受人
     */
    private String toObj;


    /**
     * 消息来源 0:访客 1:员工客服 3:ai回复
     */
    private Integer msgSource;



    /**
     *  1：转人工transferUser 2:正常回话:chatting 3:结束会话:endChat
     */
    private String status;





    /**
     * 构建访客端数据响应体
     * @param aiVisitorMsg
     * @return
     */
    public static GuestReplyMsgDto buildGuestReplyMsgDto( OneChatKfVisitorMsg aiVisitorMsg,Integer msgSource,String status){

        GuestReplyMsgDto replyMsgDto=new GuestReplyMsgDto();
        replyMsgDto.setContent(aiVisitorMsg.getContent());
        replyMsgDto.setFromObj(aiVisitorMsg.getFromObj());
        replyMsgDto.setKfRuleId(aiVisitorMsg.getKfRuleId());
        replyMsgDto.setKfVisitorId(aiVisitorMsg.getKfVisitorId());
        replyMsgDto.setMsgType(aiVisitorMsg.getMsgType());
        replyMsgDto.setSendTime(aiVisitorMsg.getSendTime());
        replyMsgDto.setShowName(aiVisitorMsg.getShowName());
        replyMsgDto.setToObj(aiVisitorMsg.getToObj());
        replyMsgDto.setMsgSource(msgSource);
        replyMsgDto.setStatus(status);
        return replyMsgDto;
    }


//    /**
//     * 构造人工消息相应体
//     * @param aiVisitorMsg
//     * @return
//     */
//    public static GuestReplyMsgDto buildUserReplyMsg( OneChatKfVisitorMsg aiVisitorMsg){
//
//        GuestReplyMsgDto replyMsgDto=new GuestReplyMsgDto();
//        replyMsgDto.setContent(aiVisitorMsg.getContent());
//        replyMsgDto.setFromObj(aiVisitorMsg.getFromObj());
//        replyMsgDto.setKfRuleId(aiVisitorMsg.getKfRuleId());
//        replyMsgDto.setKfVisitorId(aiVisitorMsg.getKfVisitorId());
//        replyMsgDto.setMsgType(aiVisitorMsg.getMsgType());
//        replyMsgDto.setSendTime(aiVisitorMsg.getSendTime());
//        replyMsgDto.setShowName(aiVisitorMsg.getShowName());
//        replyMsgDto.setToObj(aiVisitorMsg.getToObj());
//        replyMsgDto.setStatus("userReplying");
//
//        return replyMsgDto;
//    }
//
//
//
//    /**
//     * 构造客户端转人工消息实体
//     * @param aiVisitorMsg
//     * @return
//     */
//    public static  GuestReplyMsgDto transferUser( OneChatKfVisitorMsg aiVisitorMsg){
//
//        GuestReplyMsgDto replyMsgDto=new GuestReplyMsgDto();
//        replyMsgDto.setContent(aiVisitorMsg.getContent());
//        replyMsgDto.setFromObj(aiVisitorMsg.getFromObj());
//        replyMsgDto.setKfRuleId(aiVisitorMsg.getKfRuleId());
//        replyMsgDto.setKfVisitorId(aiVisitorMsg.getKfVisitorId());
//        replyMsgDto.setMsgType(aiVisitorMsg.getMsgType());
//        replyMsgDto.setSendTime(aiVisitorMsg.getSendTime());
//        replyMsgDto.setShowName(aiVisitorMsg.getShowName());
//        replyMsgDto.setToObj(aiVisitorMsg.getToObj());
//        replyMsgDto.setStatus("transferUser");
//
//        return replyMsgDto;
//    }

}
