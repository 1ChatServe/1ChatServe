package chat.aikf.ai.api.factory;

import chat.aikf.ai.api.RemoteAgentService;
import chat.aikf.ai.api.domain.OneChatAgent;
import chat.aikf.common.core.domain.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

/**
 * 智能体服务降级处理
 */
public class RemoteAgentFallbackFactory implements FallbackFactory<RemoteAgentService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteAgentFallbackFactory.class);

    @Override
    public RemoteAgentService create(Throwable throwable) {
        log.error("智能体服务调用失败: {}", throwable.getMessage());
        return new RemoteAgentService() {
            @Override
            public R<List<OneChatAgent>> list(OneChatAgent oneChatAgent, String source) {
                return R.fail("获取智能体列表失败");
            }

            @Override
            public R<OneChatAgent> get(Long id, String source) {
                return R.fail("获取智能体详情失败");
            }

            @Override
            public R<Void> create(OneChatAgent oneChatAgent, String source) {
                return R.fail("创建智能体失败");
            }

            @Override
            public R<Void> update(OneChatAgent oneChatAgent, String source) {
                return R.fail("更新智能体失败");
            }

            @Override
            public R<Void> delete(Long[] ids, String source) {
                return R.fail("删除智能体失败");
            }
        };
    }
}
