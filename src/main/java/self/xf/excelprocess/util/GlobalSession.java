package self.xf.excelprocess.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 全局session，用于存储一些全局变量，比如excel的数据，用于后续的操作
 *
 * @author XF
 * @date 2023/12/22
 */
public class GlobalSession {
    // 全局会话文件
    private static final ThreadLocal<MultipartFile> multipartFileMap = new ThreadLocal<>();

    // 保存每个sheet
    private static final ThreadLocal<Map<String, Object>> fileContentMap = new ThreadLocal<>();

    // 存储单个sheet整合后的list
    private static final ThreadLocal<ArrayList> contentListMap = new ThreadLocal<>();

    // 存储sql语句
    private static final ThreadLocal<StringBuilder> insertSql = new ThreadLocal<>();

    public static void setFile(MultipartFile file) {
        multipartFileMap.set(file);
    }

    public static MultipartFile getFile() {
        return multipartFileMap.get();
    }

    public static void removeFile() {
        multipartFileMap.remove();
    }

    public static void setFileContentMap(Map<String, Object> map) {
        fileContentMap.set(map);
    }

    public static Map<String, Object> getFileContentMap() {
        return fileContentMap.get();
    }

    public static void removeFileContentMap() {
        fileContentMap.remove();
    }

    public static void setListMap(ArrayList map) {
        contentListMap.set(map);
    }

    public static ArrayList getListMap() {
        return contentListMap.get();
    }

    public static void removeListMap() {
        contentListMap.remove();
    }

    public static void setInsertSql(StringBuilder map) {
        insertSql.set(map);
    }

    public static StringBuilder getInsertSql() {
        return insertSql.get();
    }

    public static void removeInsertSql() {
        insertSql.remove();
    }
}
