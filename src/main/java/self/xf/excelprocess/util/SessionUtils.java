package self.xf.excelprocess.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionUtils {
    private static Map<String, Long> sessionExpiryMap = new HashMap<>();

    // session 与 文件 的映射
    private static final Map<String, List<String>> processedFileMap = new ConcurrentHashMap<>();


    /**
     * 获取当前 sessionId 的过期时间，若过期，删除当前Session下的所有内容
     *
     * @param sessionId
     */
    public static boolean isSessionTimeout(String sessionId) {
        Long expiryTime = sessionExpiryMap.get(sessionId);
        if (expiryTime == null || System.currentTimeMillis() > expiryTime) {
            deleteAllFileForSession(sessionId);
            return true;
        }
        return false;
    }

    /**
     * 删除当前SessionId下的所有文件
     *
     * @param sessionId
     */
    private static void deleteAllFileForSession(String sessionId) {
        // 根据sessionId获取所有文件名
        List<String> nameList = processedFileMap.get(sessionId);
        if(!nameList.isEmpty()){
            for (String item : nameList) {
                // TODO:从配置文件中获取文件存储路径，拼接上文件名后删除
            }
        }
    }

}
