package self.xf.excelprocess.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import self.xf.excelprocess.util.GlobalSession.FileExpiryWebSocketHandler;

/**
 * WebSocketConfig 是一个配置类，用于启用 WebSocket 支持
 * 并注册 WebSocket 处理程序。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * 注册 WebSocket 处理程序。
     *
     * @param registry 用于注册处理程序的 WebSocketHandlerRegistry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册 FileExpiryWebSocketHandler 以处理 /fileExpiry 端点的 WebSocket 连接
        registry.addHandler(new FileExpiryWebSocketHandler(), "/fileExpiry").setAllowedOrigins("*");
    }
}