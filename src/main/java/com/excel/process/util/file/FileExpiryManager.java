package com.excel.process.util.file;

import com.excel.process.util.StaticMethod;
import com.excel.process.util.websocket.FileExpiryWebSocketHandler;

import java.io.File;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class FileExpiryManager {
    private static final long FILE_EXPIRY_TIME = 2 * 60 * 1000; // 2 分钟删除
    private static final PriorityQueue<FileExpiryInfo> fileExpiryQueue = new PriorityQueue<>();
    private static final ReentrantLock lock = new ReentrantLock();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // 文件过期信息类
    private static class FileExpiryInfo implements Comparable<FileExpiryInfo> {
        String filePath;
        long expiryTime;
        String sessionId;

        FileExpiryInfo(String filePath, long expiryTime, String sessionId) {
            this.filePath = filePath;
            this.expiryTime = expiryTime;
            this.sessionId = sessionId;
        }

        @Override
        public int compareTo(FileExpiryInfo other) {
            return Long.compare(this.expiryTime, other.expiryTime);
        }
    }

    // **获取文件路径**
    public static String getFilePath(String sessionId, String fileName) {
        fileName = StaticMethod.getCurrentProjectDirectory() + fileName;
        for (FileExpiryInfo expiryInfo : fileExpiryQueue) {
            if (expiryInfo.sessionId.equals(sessionId) && expiryInfo.filePath.equals(fileName)) {
                return expiryInfo.filePath;
            }
        }
        return null;
    }

    // 添加文件及其过期时间
    public static void addFileWithExpiry(String filePath, String sessionId) {
        long expiryTime = System.currentTimeMillis() + FILE_EXPIRY_TIME;
        lock.lock();
        try {
            fileExpiryQueue.add(new FileExpiryInfo(filePath, expiryTime, sessionId));
        } finally {
            lock.unlock();
        }
    }

    public static void shutDown() {
        lock.lock();
        try {
            while (!fileExpiryQueue.isEmpty()) {
                FileExpiryInfo info = fileExpiryQueue.poll();
                File file = new File(info.filePath);
                if (file.exists() && file.delete()) {
                    System.out.println("Deleted file during shutdown: " + info.filePath);
                } else {
                    System.out.println("Failed to delete file during shutdown: " + info.filePath);
                }
            }
            System.out.println("Cleared fileExpiryQueue during shutdown.");
        } finally {
            lock.unlock();
        }
    }

    // 定期检查并删除过期文件
    static {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            lock.lock();
            try {
                while (!fileExpiryQueue.isEmpty()) {
                    FileExpiryInfo info = fileExpiryQueue.peek();
                    if (currentTime > info.expiryTime) {
                        // 文件过期，删除文件并移除队列头部元素
                        fileExpiryQueue.poll();
                        File file = new File(info.filePath);
                        if (file.exists() && file.delete()) {
                            System.out.println("Deleted expired file: " + info.filePath);
                            // 通过 WebSocket 通知前端
                            FileExpiryWebSocketHandler.sendMessageToSession(info.sessionId, file.getName());
                        } else {
                            System.out.println("Failed to delete file: " + info.filePath);
                        }
                    } else {
                        break; // 队列头部文件未过期，后续文件也不会过期
                    }
                }
            } finally {
                lock.unlock();
            }
        }, 0, 1, TimeUnit.MINUTES); // 每分钟检查一次
    }

}