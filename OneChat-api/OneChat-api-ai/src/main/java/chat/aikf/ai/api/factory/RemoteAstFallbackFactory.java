package chat.aikf.ai.api.factory;

import chat.aikf.ai.api.RemoteAstService;
import chat.aikf.ai.api.domain.OneAiReplyDto;
import chat.aikf.ai.api.domain.SessionAnalysisResult;
import chat.aikf.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;


/**
 * AI会话服务降级处理
 */
public class RemoteAstFallbackFactory implements FallbackFactory<RemoteAstService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteAstFallbackFactory.class);

    @Override
    public RemoteAstService create(Throwable throwable) {
        log.error("AI会话服务调用失败: {}", throwable.getMessage());
        return new RemoteAstService() {

            @Override
            public R<String> aiReply(OneAiReplyDto replyDto,String source){

                return R.fail("AI会话服务知识库获取内容失败");
            }

            @Override
            public R<SessionAnalysisResult> getSessionAnalysis(Long kfVisitorId, String source) {
                return R.fail("获取指定访客AI数据分析失败");
            }

        };
    }
}
