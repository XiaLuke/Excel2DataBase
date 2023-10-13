package self.xf.excelprocess.util;

import java.util.List;
import java.util.Map;

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

    public static Map<String,Object> getObjectMap(){
        return (Map<String, Object>) GlobalSession.get();
    }

    public static List<Map<String, Object>> getObjectMapList(){
        Map<String, Object> objectMap = getObjectMap();
        return (List<Map<String, Object>>) objectMap.get("list");
    }
}
