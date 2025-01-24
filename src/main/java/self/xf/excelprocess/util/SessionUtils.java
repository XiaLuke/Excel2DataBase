package self.xf.excelprocess.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class SessionUtils {
    // 设置文件与过期时间用
    private static Map<String, Long> fileExpiryMap = new HashMap<>();
    // 记录当前session 过期时间
    private static Map<String,Long> sessionExpiryMap = new HashMap<>();
    // session 与 文件 的映射
    private static final Map<String, List<String>> fileListWithSession = new ConcurrentHashMap<>();
    // 做文件清理用
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static {
        // 定期检查过期文件的任务
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            List<Map<String, Long>> expiredFiles = new ArrayList<>();

            synchronized (sessionExpiryMap) {
//                for (Map<String, Long> info : sessionExpiryMap) {
//                    logger.warn("当前时间: " + currentTime + ", 文件过期时间: " + info.expiryTime);
//                    if (currentTime > info.expiryTime) {
//                        logger.info("File expired: " + info.filePath);
//                        expiredFiles.add(info);
//                    }
//                }
//                // 从原始列表中删除已过期的文件
//                fileExpiryList.removeAll(expiredFiles);
            }

            // 异步删除文件并通知 WebSocket 客户端
//            for (GlobalStore.FileExpiryInfo info : expiredFiles) {
//                CompletableFuture.runAsync(() -> {
//                    File file = new File(info.filePath);
//                    if (file.exists() && file.delete()) {
//                        logger.info("Deleted expired file: " + info.filePath);
//                        notifyFileExpiry(file.getName());
//                    }
//                });
//            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * 一个session 下的所有文件
     *
     * @param sessionId
     * @return {@link List}<{@link String}>
     */
    public static List<String> getProcessFileMaps(String sessionId){
        return fileListWithSession.get(sessionId);
    }

    /**
     * 将此次文件再保存到对应session下
     * @param sessionId
     * @param filePath
     */
    public static void setProcessFileMaps(String sessionId, String filePath) {
        List<String> strings = fileListWithSession.get(sessionId);
        fileListWithSession.put(sessionId,strings);
    }

    /**
     * 设置上传文件的过期时间
     *
     * @param filePath 文件路径
     */
    private static void setFileExpiryMap(String filePath) {
        // TODO: 增加默认文件过期时间
        long expiryTime = System.currentTimeMillis() + 0;
        setSessionExpiryMap(filePath, expiryTime);
    }

    private static void setSessionExpiryMap(String filePath, Long time) {
        fileExpiryMap.put(filePath, time);
    }

}
