package chat.aikf.common.core.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Agent默认头像工具类
 */
public class AgentAvatarUtils {

    // 预定义所有Agent头像文件名（相对路径）
    private static final String[] AGENT_AVATAR_FILES = {
            "/file/agentImg/bottts-1.png",
            "/file/agentImg/bottts-2.png",
            "/file/agentImg/bottts-3.png",
            "/file/agentImg/bottts-4.png",
            "/file/agentImg/bottts-5.png",
            "/file/agentImg/bottts-6.png",
            "/file/agentImg/bottts-7.png",
            "/file/agentImg/bottts-8.png",
            "/file/agentImg/bottts-9.png",
            "/file/agentImg/bottts-10.png",
            "/file/agentImg/bottts-11.png",
            "/file/agentImg/bottts-12.png",
            "/file/agentImg/bottts-13.png",
            "/file/agentImg/bottts-14.png",
            "/file/agentImg/bottts-15.png",
            "/file/agentImg/bottts-16.png",
            "/file/agentImg/bottts-17.png",
            "/file/agentImg/bottts-18.png",
            "/file/agentImg/bottts-19.png",
            "/file/agentImg/bottts-default.png"
    };

     //默认头像
    private static final String DEFAULT_AVATRA="/agentImg/bottts-default.png";

    private static final Random RANDOM = new Random();

      // 为了快速查找，构建一个 Set（只存文件名部分，如 "avatar-1.png"）
    private static final Set<String> AVATAR_FILE_NAMES = new HashSet<>();

    static {
        for (String path : AGENT_AVATAR_FILES) {
            // 提取最后一段：avatar-1.png
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            AVATAR_FILE_NAMES.add(fileName);
        }
    }

     /**
     * 判断给定的文件名（如 "avatar-1.png"）是否是合法的默认头像
     */
    public static boolean isValidAvatarFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        return AVATAR_FILE_NAMES.contains(fileName);
    }

    /**
     * 获取随机Agent头像的相对路径
     */
    public static String getRandomAgentAvatarPath() {
        return AGENT_AVATAR_FILES[RANDOM.nextInt(AGENT_AVATAR_FILES.length)];
    }

    /**
     * 获取完整的Agent头像URL路径
     * @return 完整的头像URL路径
     */
    public static String getRandomAgentAvatarUrl() {
        // 这里返回相对路径，实际使用时会根据系统配置拼接完整URL
        return getRandomAgentAvatarPath();
    }

     /**
     * 获取完整路径（如果文件名合法）
     * @return 合法则返回 "/file/avatars/avatar-x.png"，否则返回 null
     */
    public static String getAvatarPathByName(String fileName) {
        if (isValidAvatarFileName(fileName)) {
            return "/agentImg/" + fileName;
        }
        return DEFAULT_AVATRA;
    }
}