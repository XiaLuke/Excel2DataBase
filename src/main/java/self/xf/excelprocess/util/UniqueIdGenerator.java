package self.xf.excelprocess.util;

import java.util.concurrent.atomic.AtomicLong;

public class UniqueIdGenerator {
    private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    public static String generateUniqueId() {
        return String.valueOf(counter.getAndIncrement());
    }
}
