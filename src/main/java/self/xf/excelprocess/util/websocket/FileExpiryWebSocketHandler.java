package self.xf.excelprocess.util.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileExpiryWebSocketHandler extends TextWebSocketHandler {

    // 存储 sessionId 到 WebSocketSession 的映射
    private static final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 获取 sessionId 并存储到映射中
        String sessionId = session.getId();
        sessionMap.put(sessionId, session);
        System.out.println("WebSocket connected: " + sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        // 移除关闭的会话
        String sessionId = session.getId();
        sessionMap.remove(sessionId);
        System.out.println("WebSocket disconnected: " + sessionId);
    }

    // 发送消息给指定的 sessionId
    public static void sendMessageToSession(String sessionId, String message) {
        WebSocketSession session = sessionMap.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                System.out.println("Sent message to session " + sessionId + ": " + message);
            } catch (IOException e) {
                System.err.println("Failed to send message to session " + sessionId + ": " + e.getMessage());
            }
        } else {
            System.err.println("Session " + sessionId + " is not open or does not exist.");
        }
    }
}