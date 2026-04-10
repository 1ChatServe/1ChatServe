package chat.aikf.ops.api.constant;


/**
 * 访客端状态
 */
public class OneChatVisitorSate {
    //排队中
    public static final Integer IDLE_STATE=0;

    //人工接待中
    public static final Integer RECEIVE_STATE=1;

    //会话结束
    public static final Integer END_STATE=2;





    //客服离线会话结束(客服不在线)
    public static final Integer OFFLINE_STATE=4;


    //ai接待中
    public static final Integer AI_RECEIVE_STATE=5;

    /**
     * 获取访客状态的映射
     * @param state 传入的状态码
     * @return 访客状态 (0:排队中; 1:对话中; 2:已结束; 3:AI会话)
     */
    public static Integer getVisitorState(Integer state) {
        if (state == null) {
            return 0; // 默认排队中
        }

        switch (state) {
            case 0: // IDLE_STATE
                return 0; // 排队中

            case 1: // RECEIVE_STATE
                return 1; // 对话中

            case 2: // END_STATE
            case 4: // OFFLINE_STATE
                return 2; // 已结束

            case 3: // PING_STATE
                return 0; // 排队中

            case 5: // AI_RECEIVE_STATE
                return 3; // AI会话

            default:
                return 0; // 默认排队中
        }
    }

}
