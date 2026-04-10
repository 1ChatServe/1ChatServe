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
 * @TableName one_chat_model_config
 */
@TableName(value ="one_chat_model_config")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OneChatModelConfig extends BaseEntity {

    // 模型类型常量
    public static final String MODEL_TYPE_CHAT = "1"; // 对话
    public static final String MODEL_TYPE_EMBEDDING = "2"; // 嵌入(向量)
    public static final String MODEL_TYPE_IMAGE = "3"; // 图像


    //启动
    public static final Integer MODEL_STATUS_START=1;
    //关闭
    public static final Integer MODEL_STATUS_END=0;


    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 供应商
     */
    private String provider;

    /**
     * 模型类型（1:对话；2:嵌入(向量)；3:图像）
     */
    private String modelType;

    /**
     * 排序
     */
    private Integer modelSort;

    /**
     * API KEY
     */
    private String apiKey;

    /**
     * 接入地址
     */
    private String baseUrl;

    /**
     * 状态(0:停用, 1:启用)
     */
    private Integer modelStatus;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;



}