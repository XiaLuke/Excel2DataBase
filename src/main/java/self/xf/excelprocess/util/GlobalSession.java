package self.xf.excelprocess.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

/**
 * 全局session，用于存储一些全局变量，比如excel的数据，用于后续的操作
 *
 * @author XF
 * @date 2023/12/22
 */
public class GlobalSession {
    // 全局会话文件
    private static final ThreadLocal<MultipartFile> multipartFileMap = new ThreadLocal<>();

    // 保存每个sheet的原始数据
    private static final ThreadLocal<Map<String, Object>> originalDataMap = new ThreadLocal<>();

    // 存储单个sheet整合后的list
    private static final ThreadLocal<Map<String, Object>> afterProcessSheetMap = new ThreadLocal<>();

    // 使用Map存储用户的文件信息，key为sessionId
    private static final Map<String, List<String>> processedFileMap = new ConcurrentHashMap<>();

    // 过期时间
    private static Map<String, MultipartFile> fileMap = new HashMap<>();
    private static Map<String, Long> sessionExpiryMap = new HashMap<>();
    private static Map<String, String> sessionFileMap = new HashMap<>();
    private static final long SESSION_TIMEOUT = 1 * 60 * 1000; // 1 minutes
    private static final long FILE_EXPIRY_TIME = 2 * 60 * 1000; // 2 分钟删除

    // 添加文件过期管理
    private static class FileExpiryInfo {
        String filePath;
        long expiryTime;

        FileExpiryInfo(String filePath, long expiryTime) {
            this.filePath = filePath;
            this.expiryTime = expiryTime;
        }
    }

    // 保存设置了过期时间的文件
    private static final List<FileExpiryInfo> fileExpiryList = new CopyOnWriteArrayList<>();
    // 定期检查并删除过期文件
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        // 启动定期检查过期文件的任务
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            Iterator<FileExpiryInfo> iterator = fileExpiryList.iterator();

            while (iterator.hasNext()) {
                FileExpiryInfo info = iterator.next();
                long timeRemaining = info.expiryTime - currentTime;
                System.out.println("File: " + info.filePath + " will expire in: " + timeRemaining + " ms");

                if (currentTime > info.expiryTime) {
                    File file = new File(info.filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                    iterator.remove();
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static void addFileWithExpiry(String filePath) {
        long expiryTime = System.currentTimeMillis() + FILE_EXPIRY_TIME;
        fileExpiryList.add(new FileExpiryInfo(filePath, expiryTime));
    }

    // 在应用关闭时清理资源
    public static void shutdown() {
        scheduler.shutdown();
        // 删除所有临时文件
        for (FileExpiryInfo info : fileExpiryList) {
            File file = new File(info.filePath);
            if (file.exists()) {
                file.delete();
            }
        }
        fileExpiryList.clear();
    }

    public static void setFile(String sessionId, MultipartFile file) {
        fileMap.put(sessionId, file);
        setFile(file);
        sessionExpiryMap.put(sessionId, System.currentTimeMillis() + SESSION_TIMEOUT);
    }

    public static MultipartFile getFile(String sessionId) {
        return fileMap.get(sessionId);
    }

    public static boolean isSessionExpired(String sessionId) {
        Long expiryTime = sessionExpiryMap.get(sessionId);
        if (expiryTime == null) {
            deleteAllSqlFilesForSession(sessionId);

        }else if(System.currentTimeMillis() > expiryTime){

        }
        return false;
    }

    public static void removeSession(String sessionId) {
        fileMap.remove(sessionId);
        sessionExpiryMap.remove(sessionId);
    }

    public static void deleteAllSqlFilesForSession(String sessionId) {
        List<String> fileName = getLastProcessedFileName(sessionId);
        if (fileName != null) {
            String path = "E:\\File\\JavaProject\\XF\\Excel2DataBase\\src\\main\\resources\\" + fileName;
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            removeProcessedFile(sessionId);
        }
    }

    public static List<String> getLastProcessedFileNames(String sessionId) {
        return processedFileMap.get(sessionId) == null ? new ArrayList<>() : processedFileMap.get(sessionId);
    }

    public static void setLastProcessedFileName(String sessionId, String fileName) {
        List<String> names = getLastProcessedFileName(sessionId);
        names.add(fileName);
        processedFileMap.put(sessionId, names);
    }

    public static List<String> getLastProcessedFileName(String sessionId) {
        return processedFileMap.get(sessionId) == null ? new ArrayList<>() : processedFileMap.get(sessionId);
    }
    public static String getLastProcessedFileName(String sessionId, String fileName) {
        List<String> fileNames = processedFileMap.get(sessionId);
        if (fileNames != null) {
            for (String name : fileNames) {
                if (name.contains(fileName)) {
                    return name;
                }
            }
        }
        return null;
    }

    public static void removeProcessedFile(String sessionId) {
        processedFileMap.remove(sessionId);
    }

    public static void setFile(MultipartFile file) {
        multipartFileMap.set(file);
    }

    public static MultipartFile getFile() {
        return multipartFileMap.get() == null ? null : multipartFileMap.get();
    }

    public static void removeFile() {
        multipartFileMap.remove();
    }

    public static void setOriginalDataMap(Map<String, Object> map) {
        originalDataMap.set(map);
    }

    public static Map<String, Object> getOriginalDataMap() {
        return originalDataMap.get() == null ? new HashMap<>() : originalDataMap.get();
    }

    public static void removeFileContentMap() {
        originalDataMap.remove();
    }


    // 处理完成后的数据
    public static void setListMap(Map<String, Object> map) {
        afterProcessSheetMap.set(map);
    }

    public static Map<String, Object> getListMap() {
        return afterProcessSheetMap.get() == null ? new HashMap<>() : afterProcessSheetMap.get();
    }

    public static void removeListMap() {
        afterProcessSheetMap.remove();
    }

}
