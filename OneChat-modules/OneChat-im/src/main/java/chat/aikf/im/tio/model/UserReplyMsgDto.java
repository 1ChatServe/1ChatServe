package chat.aikf.im.tio.model;


import chat.aikf.ops.api.domain.OneChatKfVisitorMsg;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 *员工端消息响应实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserReplyMsgDto {
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
    private String kfRuleId;


    /**
     * 访客id(对应数据库记录id)
     */
    private String kfVisitorId;



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
     * 构建员工消息响应体
     * @param visitorMsg
     * @return
     */
    public  static UserReplyMsgDto   buildUserReplyMsgDto( OneChatKfVisitorMsg visitorMsg,Integer msgSource){
        UserReplyMsgDto userReplyMsgDto=new UserReplyMsgDto();
        userReplyMsgDto.setContent(visitorMsg.getContent());
        userReplyMsgDto.setFromObj(visitorMsg.getFromObj());
        userReplyMsgDto.setKfRuleId(visitorMsg.getKfRuleId().toString());
        userReplyMsgDto.setKfVisitorId(visitorMsg.getKfVisitorId().toString());
        userReplyMsgDto.setMsgType(visitorMsg.getMsgType());
        userReplyMsgDto.setSendTime(visitorMsg.getSendTime());
        userReplyMsgDto.setShowAvatar(visitorMsg.getShowAvatar());
        userReplyMsgDto.setShowName(visitorMsg.getShowName());
        userReplyMsgDto.setToObj(visitorMsg.getToObj());
        userReplyMsgDto.setMsgSource(msgSource);

        return userReplyMsgDto;
    }
}
