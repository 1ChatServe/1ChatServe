package chat.aikf.ai.api;

import chat.aikf.ai.api.domain.OneAiReplyDto;
import chat.aikf.ai.api.domain.OneChatAgent;
import chat.aikf.ai.api.domain.SessionAnalysisResult;
import chat.aikf.ai.api.factory.RemoteAgentFallbackFactory;
import chat.aikf.ai.api.factory.RemoteAstFallbackFactory;
import chat.aikf.common.core.constant.SecurityConstants;
import chat.aikf.common.core.constant.ServiceNameConstants;
import chat.aikf.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI会话接口调用
 */
@FeignClient(contextId = "remoteAstService", value = ServiceNameConstants.OneChatAi, fallbackFactory = RemoteAstFallbackFactory.class)
public interface RemoteAstService {


    /**
     * ai回复
     * @param replyDto
     * @return
     */
    @PostMapping("/ast/aiReply")
    public R<String> aiReply(@RequestBody OneAiReplyDto replyDto,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);




    /**
     * 根据访客ID查询最新的会话分析结果
     * @param kfVisitorId 访客ID
     * @return 会话分析结果
     */
    @GetMapping("/ast/getSessionAnalysis/{kfVisitorId}")
    public R<SessionAnalysisResult> getSessionAnalysis(@PathVariable Long kfVisitorId,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
