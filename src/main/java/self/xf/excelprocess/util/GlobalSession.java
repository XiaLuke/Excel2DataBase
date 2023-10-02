package self.xf.excelprocess.util;

import java.util.Map;

public class GlobalSession {
    private static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    public static void set(Map<String, Object> value) {
        threadLocal.set(value);
    }

    public static Map<String, Object> get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

}
