package chat.aikf.ai.api.domain;

import chat.aikf.common.core.web.domain.BaseEntity;
import chat.aikf.common.core.typehandler.ListTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @TableName one_chat_agent
 */
@TableName(value ="one_chat_agent", autoResultMap = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OneChatAgent extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    private Long id;


    /**
     * 智能体头像
     */
    private  String agentAvatar;


    /**
     * 智能体名称
     */
    @NotBlank(message = "智能体名称不能为空")
    private  String agentName;

    /**
     * 智能体描述
     */
    private String agentDesc;

    /**
     * 角色设定
     */
    @NotBlank(message = "智能体角色设定不能为空")
    private String roleSetting;




    /**
     * 模型配置id
     */
    @NotNull(message = "模型名称不能为空")
    private Long modeConfigId;


    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 开场欢迎语
     */
    private String welcomeMessage;


    /**
     * 开场引导语，最多支持5个问题，使用字段隔开
     */
    @TableField(typeHandler = ListTypeHandler.class)
    private List<String> guideMessage;

    /**
     * 知识能力，最多支持5个，多个知识库id使用逗号隔开
     */
    @TableField(typeHandler = ListTypeHandler.class)
    private List<String> knowledgeIds;


    @TableField(exist = false)
    private   List<OneChatKnowledgeBase> oneChatKnowledgeBases;

    /**
     * 高级配置，问题推荐开关(1开启，0关闭)
     */
    private Integer advancedConfig;

    /**
     * 高级配置，问题推荐开关(1开启，0关闭)
     */
     private Integer modelStatus;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;

}