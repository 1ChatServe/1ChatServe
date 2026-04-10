package chat.aikf.ai.controller;

import chat.aikf.ai.api.domain.OneAiReplyDto;
import chat.aikf.ai.api.domain.OneChatAgent;
import chat.aikf.ai.api.domain.SessionAnalysisResult;
import chat.aikf.ai.domain.OneChatSessionAnalysis;
import chat.aikf.ai.domain.OneChatSessionKeyStep;
import chat.aikf.ai.service.IOneChatAstService;
import chat.aikf.ai.service.IOneChatKnowledgeBaseAttachmentService;
import chat.aikf.ai.service.IOneChatSessionAnalysisService;
import chat.aikf.ai.service.IOneChatSessionKeyStepService;
import chat.aikf.common.core.domain.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.List;


/**
 * AI会话
 */
@RestController
@RequestMapping("/ast")
@Slf4j
public class OneChatAstController {

    @Autowired
    private IOneChatAstService IOneChatAstService;



    @Autowired
    private IOneChatSessionAnalysisService oneChatSessionAnalysisService;

    @Autowired
    private IOneChatSessionKeyStepService oneChatSessionKeyStepService;


    /**
     * 推荐回复（SSE方式）
     * @param visitorMessage 访客消息
     * @return SSE流
     */
    @GetMapping("/recommendReply")
    public Flux<ServerSentEvent<String>> recommendReply(@RequestParam String visitorMessage) {
        return Flux.create(sink -> {
            try {
                // 发送开始事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("start")
                        .data("开始生成推荐回复...")
                        .build());
                
                // 执行回复推荐生成
                String result = IOneChatAstService.recommendReply(visitorMessage);
                
                // 发送推荐结果
                sink.next(ServerSentEvent.<String>builder()
                        .event("result")
                        .data(result)
                        .build());
                
                // 发送完成事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("complete")
                        .data("推荐回复生成完成")
                        .build());
                
                // 完成流
                sink.complete();
            } catch (Exception e) {
                log.error("生成推荐回复失败", e);
                sink.next(ServerSentEvent.<String>builder()
                        .event("error")
                        .data(e.getMessage())
                        .build());
                sink.complete();
            }
        });
    }


//    /**
//     * 角色设定
//     * @param request
//     * @return
//     */
//    @PostMapping("/roleSetInfo")
//    public R<String> roleSetInfo(@RequestBody OneChatAgent oneChatAgent) {
//
//        try {
//            String reply = IOneChatAstService.recommendReply(request.getVisitorMessage());
//            return R.ok(reply);
//        } catch (RuntimeException e) {
//            log.error("生成失败", e);
//            return R.fail(e.getMessage());
//        }
//    }


    /**
     * ai回复
     * @param replyDto
     * @return
     */
    @PostMapping("/aiReply")
    public R<String> aiReply(@RequestBody OneAiReplyDto replyDto){

        String knowledgeSummaryByKfRule = IOneChatAstService
                .getKnowledgeSummaryByKfRule(replyDto.getAgentId(), replyDto.getQuestion());

        return R.ok(knowledgeSummaryByKfRule);
    }

    /**
     * 会话分析（SSE方式）
     * @param kfVisitorId 访客ID
     * @return SSE流
     */
    @GetMapping("/analyzeSession/{kfVisitorId}")
    public Flux<ServerSentEvent<String>> analyzeSession(@PathVariable Long kfVisitorId) {
        return Flux.create(sink -> {
            try {
                // 发送开始事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("start")
                        .data("开始分析会话...")
                        .build());
                
                // 执行会话分析
                SessionAnalysisResult result = IOneChatAstService.analyzeSession(kfVisitorId);
                
                // 发送分析结果
                ObjectMapper mapper = new ObjectMapper();
                String resultJson = mapper.writeValueAsString(result);
                sink.next(ServerSentEvent.<String>builder()
                        .event("result")
                        .data(resultJson)
                        .build());
                
                // 发送完成事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("complete")
                        .data("会话分析完成")
                        .build());
                
                // 完成流
                sink.complete();
            } catch (Exception e) {
                log.error("会话分析失败", e);
                sink.next(ServerSentEvent.<String>builder()
                        .event("error")
                        .data(e.getMessage())
                        .build());
                sink.complete();
            }
        });
    }

    /**
     * 根据访客ID查询最新的会话分析结果
     * @param kfVisitorId 访客ID
     * @return 会话分析结果
     */
    @GetMapping("/getSessionAnalysis/{kfVisitorId}")
    public R<SessionAnalysisResult> getSessionAnalysis(@PathVariable Long kfVisitorId) {
        try {
            // 查询最新的会话分析结果
            OneChatSessionAnalysis sessionAnalysis = oneChatSessionAnalysisService.findLatestByKfVisitorId(kfVisitorId);
            
            if (sessionAnalysis == null) {
                return R.ok(null);
            }
            
            // 查询相关的关键步骤
            List<OneChatSessionKeyStep> keySteps = oneChatSessionKeyStepService.findBySessionAnalysisId(sessionAnalysis.getId());
            
            // 转换为SessionAnalysisResult格式
            SessionAnalysisResult result = new SessionAnalysisResult();
            
            // 综合汇总模块
            SessionAnalysisResult.ComprehensiveSummary comprehensiveSummary = new SessionAnalysisResult.ComprehensiveSummary();
            comprehensiveSummary.setOverallScore(sessionAnalysis.getOverallScore());
            comprehensiveSummary.setIntentionLevel(sessionAnalysis.getIntentionLevel());
            comprehensiveSummary.setSessionSummary(sessionAnalysis.getSessionSummary());
            result.setComprehensiveSummary(comprehensiveSummary);
            
            // 客户意向模块
            SessionAnalysisResult.CustomerIntention customerIntention = new SessionAnalysisResult.CustomerIntention();
            customerIntention.setIntentionPercentage(sessionAnalysis.getIntentionPercentage());
            customerIntention.setIntentionLevel(sessionAnalysis.getIntentionLevelText());
            customerIntention.setIntentionSummary(sessionAnalysis.getIntentionSummary());
            result.setCustomerIntention(customerIntention);
            
            // 情感分析模块
            SessionAnalysisResult.SentimentAnalysis sentimentAnalysis = new SessionAnalysisResult.SentimentAnalysis();
            sentimentAnalysis.setSatisfactionPercentage(sessionAnalysis.getSatisfactionPercentage());
            sentimentAnalysis.setSatisfactionLevel(sessionAnalysis.getSatisfactionLevel());
            sentimentAnalysis.setSatisfactionSummary(sessionAnalysis.getSatisfactionSummary());
            result.setSentimentAnalysis(sentimentAnalysis);
            
            // 关键步骤
            if (keySteps != null && !keySteps.isEmpty()) {
                List<SessionAnalysisResult.KeyStep> steps = new ArrayList<>();
                for (OneChatSessionKeyStep step : keySteps) {
                    SessionAnalysisResult.KeyStep keyStep = new SessionAnalysisResult.KeyStep();
                    keyStep.setStepDescription(step.getStepDescription());
                    keyStep.setTimestamp(step.getTimestamp());
                    steps.add(keyStep);
                }
                result.setKeySteps(steps);
            }
            
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询会话分析结果失败", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 生成回复推荐（SSE方式）
     * @param kfVisitorId 访客ID
     * @param token 认证token
     * @return SSE流
     */
    @GetMapping("/generateReplyRecommendations/{kfVisitorId}")
    public Flux<ServerSentEvent<String>> generateReplyRecommendations(@PathVariable Long kfVisitorId, @RequestParam(required = false) String token) {
        return Flux.create(sink -> {
            try {
                // 发送开始事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("start")
                        .data("开始生成回复推荐...")
                        .build());
                
                // 执行回复推荐生成
                String result = IOneChatAstService.generateReplyRecommendations(kfVisitorId);
                
                // 发送推荐结果
                sink.next(ServerSentEvent.<String>builder()
                        .event("result")
                        .data(result)
                        .build());
                
                // 发送完成事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("complete")
                        .data("回复推荐生成完成")
                        .build());
                
                // 完成流
                sink.complete();
            } catch (Exception e) {
                log.error("生成回复推荐失败", e);
                sink.next(ServerSentEvent.<String>builder()
                        .event("error")
                        .data(e.getMessage())
                        .build());
                sink.complete();
            }
        });
    }

    /**
     * 优化角色设定
     * @param agent 模型名称
     * @return 优化后的角色设定
     */
    @PostMapping("/optimizeRoleSetting")
    public Flux<ServerSentEvent<String>> optimizeRoleSetting(@RequestBody OneChatAgent agent) {
        return Flux.create(sink -> {
            try {
                // 发送开始事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("start")
                        .data("开始优化角色设定...")
                        .build());
                
                // 执行角色设定优化
                String result = IOneChatAstService.optimizeRoleSetting(agent.getRoleSetting(), agent.getModelName());
                
                // 发送优化结果
                sink.next(ServerSentEvent.<String>builder()
                        .event("result")
                        .data(result)
                        .build());
                
                // 发送完成事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("complete")
                        .data("角色设定优化完成")
                        .build());
                
                // 完成流
                sink.complete();
            } catch (Exception e) {
                log.error("优化角色设定失败", e);
                sink.next(ServerSentEvent.<String>builder()
                        .event("error")
                        .data(e.getMessage())
                        .build());
                sink.complete();
            }
        });
    }

    /**
     * 会话测试 - 根据智能体绑定的知识库检索并生成回复（SSE方式）
     * @param agentId 智能体ID
     * @param question 用户问题
     * @return SSE流
     */
    @GetMapping("/chatTest/{agentId}")
    public Flux<ServerSentEvent<String>> chatTest(@PathVariable Long agentId, @RequestParam String question) {
        return Flux.create(sink -> {
            try {
                // 发送开始事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("start")
                        .data("开始检索知识库...")
                        .build());

                // 执行知识库检索和大模型回复
                String result = IOneChatAstService.getKnowledgeSummaryByKfRule(agentId, question);

                // 发送结果事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("result")
                        .data(result)
                        .build());

                // 发送完成事件
                sink.next(ServerSentEvent.<String>builder()
                        .event("complete")
                        .data("回复生成完成")
                        .build());

                // 完成流
                sink.complete();
            } catch (Exception e) {
                log.error("会话测试失败", e);
                sink.next(ServerSentEvent.<String>builder()
                        .event("error")
                        .data(e.getMessage())
                        .build());
                sink.complete();
            }
        });
    }

}
