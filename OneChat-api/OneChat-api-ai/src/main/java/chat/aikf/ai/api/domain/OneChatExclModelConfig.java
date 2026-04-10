package chat.aikf.ai.api.domain;

import chat.aikf.common.core.web.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName one_chat_excl_model_config
 */
@TableName(value ="one_chat_excl_model_config")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OneChatExclModelConfig extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 员工id
     */
    private Long userId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 核采样参数
     */
    private Double topP;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;

}