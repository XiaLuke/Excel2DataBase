package self.xf.excelprocess.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 全局session，用于存储一些全局变量，比如excel的数据，用于后续的操作
 *
 * @author XF
 * @date 2023/12/22
 */
public class GlobalSession {
    private static final ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    public static void set(Object value) {
        threadLocal.set(value);
    }

    public static Object get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

    public static StringBuilder getStringBuilder() {

        return (StringBuilder) get();
    }

    public static MultipartFile getFile() {
        return (MultipartFile) get();
    }

    public static Map<String, Object> getObjectMap() {
        return (Map<String, Object>) get();
    }

    public static List<Map<String, Object>> getObjectMapList() {
        Map<String, Object> objectMap = getObjectMap();
        return (List<Map<String, Object>>) objectMap.get("list");
    }
}
