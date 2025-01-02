package self.xf.excelprocess.util;

/**
 * SnowflakeIdGenerator类用于生成全局唯一ID，使用Twitter的雪花算法。
 * 雪花算法生成的ID由时间戳、数据中心ID、机器ID和序列号组成，确保在同一时刻多次调用方法返回的值不一样。
 * 生成的ID去掉了特殊字符，并且字符串长度控制在20位。
 */
public class SnowflakeIdGenerator {

    // 雪花算法的起始时间戳（纪元）
    private static final long EPOCH = 1288834974657L;
    // 机器ID所占的位数
    private static final long WORKER_ID_BITS = 5L;
    // 数据中心ID所占的位数
    private static final long DATA_CENTER_ID_BITS = 5L;
    // 支持的最大机器ID，结果是31
    private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
    // 支持的最大数据中心ID，结果是31
    private static final long MAX_DATA_CENTER_ID = -1L ^ (-1L << DATA_CENTER_ID_BITS);
    // 序列在ID中占的位数
    private static final long SEQUENCE_BITS = 12L;
    // 机器ID向左移12位
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    // 数据中心ID向左移17位（12+5）
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    // 时间戳向左移22位（12+5+5）
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;
    // 生成序列的掩码，这里为4095
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);

    // 工作机器ID（0~31）
    private long workerId;
    // 数据中心ID（0~31）
    private long dataCenterId;
    // 毫秒内序列（0~4095）
    private long sequence = 0L;
    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     * @param workerId 工作机器ID
     * @param dataCenterId 数据中心ID
     */
    public SnowflakeIdGenerator(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("data center Id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 生成全局唯一ID
     * @return 生成的ID字符串
     */
    public synchronized String generateId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        long id = ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) |
                (dataCenterId << DATA_CENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;

        String idStr = Long.toString(id);

        // 去掉特殊字符并确保长度为20位
        idStr = idStr.replaceAll("[^a-zA-Z0-9]", "");
        if (idStr.length() > 20) {
            idStr = idStr.substring(0, 20);
        } else if (idStr.length() < 20) {
            StringBuilder sb = new StringBuilder(idStr);
            while (sb.length() < 20) {
                sb.append('0'); // 如果需要，使用0填充
            }
            idStr = sb.toString();
        }

        return idStr;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回当前时间，以毫秒为单位
     * @return 当前时间（毫秒）
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
}