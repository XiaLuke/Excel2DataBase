package self.xf.excelprocess.util.websocket;

import org.springframework.web.socket.WebSocketSession;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketSessionManager {
    private static final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    // 注册 session
    public static void registerSession(String sessionId, WebSocketSession session) {
        sessionMap.put(sessionId, session);
    }

    // 获取 session
    public static WebSocketSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    // 移除 session
    public static void removeSession(String sessionId) {
        sessionMap.remove(sessionId);
    }
}
