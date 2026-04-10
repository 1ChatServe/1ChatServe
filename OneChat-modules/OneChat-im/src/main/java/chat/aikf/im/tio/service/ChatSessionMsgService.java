package chat.aikf.im.tio.service;

import chat.aikf.common.core.constant.SecurityConstants;
import chat.aikf.common.core.domain.R;
import chat.aikf.common.core.exception.ServiceException;
import chat.aikf.common.core.utils.RequestContextHelper;
import chat.aikf.im.allocation.service.CustomerAssignService;
import chat.aikf.im.api.domain.dto.GuestIdentityMsgDto;
import chat.aikf.im.tio.constant.OneChatImConstant;
import chat.aikf.im.tio.model.IdentityMsgDto;
import chat.aikf.im.tio.utils.KfCacheRelUtils;
import chat.aikf.ops.api.RemoteKfVisitorService;
import chat.aikf.ops.api.domain.OneChatkfVisitor;
import chat.aikf.common.core.utils.DeviceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;



@Service
@Slf4j
public class ChatSessionMsgService {

    @Autowired
    private CustomerAssignService customerAssignService;


    @Autowired
    private RemoteKfVisitorService remoteKfVisitorService;

    @Autowired
    private KfCacheRelUtils kfCacheRelUtils;


    //获取初始化信息
    public GuestIdentityMsgDto findInitSession(String webStyleId, String visitorId, HttpServletRequest request){
        GuestIdentityMsgDto guestIdentityMsgDto = new GuestIdentityMsgDto();
        //分配接待员工给客户(分配逻辑)
        IdentityMsgDto identityMsgDto = customerAssignService.
                getOnlineUserId( webStyleId, visitorId);
        log.info("接待人:"+identityMsgDto);
        if(null != identityMsgDto){
            //构建访客信息(如果或更新+构建缓存链接关系)
            OneChatkfVisitor oneChatkfVisitor = IdentityMsgDto.buildObj(visitorId,request,  identityMsgDto,  DeviceUtils.parseDeviceWithLanguage(RequestContextHelper.getOriginalRequestInfo(request)
                    .get(OneChatImConstant.TIO_USER_AGENT)
                    , RequestContextHelper.getOriginalRequestInfo(request)
                            .get(OneChatImConstant.TIO_ACCEPT_LANGUAGE)));
            //访客消息入库
            R<OneChatkfVisitor> r = remoteKfVisitorService.
                    addOrUpdate(oneChatkfVisitor, SecurityConstants.INNER);

            if (R.FAIL == r.getCode()) {
                log.error("更新访客信息失败:"+r.getMsg());
                throw new ServiceException(r.getMsg());
            }
            guestIdentityMsgDto.setInitState(identityMsgDto.getReceptionState());
            guestIdentityMsgDto.setReceptObjId(identityMsgDto.getToObj());
            guestIdentityMsgDto.setReceptObjName(identityMsgDto.getToObjName());
            guestIdentityMsgDto.setReceptObjavatar(identityMsgDto.getToObjavatar());
            guestIdentityMsgDto.setKfVisitorName(r.getData().getName());
            guestIdentityMsgDto.setKfVisitoravatar(r.getData().getAvatar());
            guestIdentityMsgDto.setKfVisitorId(r.getData().getId().toString());
            guestIdentityMsgDto.setVisitorId(r.getData().getVisitorId());
            guestIdentityMsgDto.setKfRuleId(identityMsgDto.getKfRuleId());
            guestIdentityMsgDto.setMsgTip(identityMsgDto.getMsgTip());
            guestIdentityMsgDto.setGuideMessage(identityMsgDto.getGuideMessage());

            //构建访客连接初始化数据(构建会话缓存，可以理解成类似session)
            kfCacheRelUtils.linkInitCache(guestIdentityMsgDto,webStyleId);
        }

        return  guestIdentityMsgDto;
    }



}
