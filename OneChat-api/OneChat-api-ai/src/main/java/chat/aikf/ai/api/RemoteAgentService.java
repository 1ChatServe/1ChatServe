package chat.aikf.ai.api;

import chat.aikf.ai.api.domain.OneChatAgent;
import chat.aikf.common.core.constant.SecurityConstants;
import chat.aikf.common.core.constant.ServiceNameConstants;
import chat.aikf.common.core.domain.R;
import chat.aikf.ai.api.factory.RemoteAgentFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能体接口调用
 */
@FeignClient(contextId = "remoteAgentService", value = ServiceNameConstants.OneChatAi, fallbackFactory = RemoteAgentFallbackFactory.class)
public interface RemoteAgentService {

    /**
     * 查询智能体列表
     * @param oneChatAgent 查询条件
     * @param source 来源
     * @return 智能体列表
     */
    @GetMapping("/oneChatAgent/list")
    public R<List<OneChatAgent>> list(OneChatAgent oneChatAgent, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据ID查询智能体
     * @param id 智能体ID
     * @param source 来源
     * @return 智能体信息
     */
    @GetMapping("/oneChatAgent/get/{id}")
    public R<OneChatAgent> get(@PathVariable("id") Long id, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 创建智能体
     * @param oneChatAgent 智能体信息
     * @param source 来源
     * @return 操作结果
     */
    @PostMapping("/oneChatAgent/create")
    public R<Void> create(@RequestBody OneChatAgent oneChatAgent, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 更新智能体
     * @param oneChatAgent 智能体信息
     * @param source 来源
     * @return 操作结果
     */
    @PutMapping("/oneChatAgent/update")
    public R<Void> update(@RequestBody OneChatAgent oneChatAgent, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 删除智能体
     * @param ids 智能体ID列表
     * @param source 来源
     * @return 操作结果
     */
    @DeleteMapping("/oneChatAgent/{ids}")
    public R<Void> delete(@PathVariable("ids") Long[] ids, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
