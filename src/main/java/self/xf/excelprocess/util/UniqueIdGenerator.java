package self.xf.excelprocess.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 生成id
 * @author XF
 * @date 2023/12/22
 */
public class UniqueIdGenerator {
    private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    public static String generateUniqueId(String prefix) {
        long timestamp = System.nanoTime();  // 使用纳秒级时间戳
        int randomNum = ThreadLocalRandom.current().nextInt(10000);  // 使用线程安全的随机数生成器
        String id = "";
        if (prefix.isEmpty()) {
            id = timestamp + ":" + randomNum;
        } else {
            id = prefix + ":" + timestamp + ":" + randomNum;
        }

        // 使用BASE64编码将ID长度控制为32-50位
        String encodedID = java.util.Base64.getEncoder().encodeToString(id.getBytes());
        if (encodedID.length() > 50) {
            encodedID = encodedID.substring(0, 50);
        }
        return encodedID;
    }
}
