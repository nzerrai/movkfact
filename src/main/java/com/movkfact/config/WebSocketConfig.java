package com.movkfact.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Spring WebSocket configuration with STOMP protocol (S3.3).
 *
 * - Endpoint: /ws/batch-notifications (SockJS fallback enabled)
 * - Application prefix: /app
 * - Broker prefix: /topic (simple in-memory broker)
 * - Heartbeat: 30s (keep-alive, requires TaskScheduler)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Bean
    public TaskScheduler webSocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic")
              .setHeartbeatValue(new long[]{30000, 30000})
              .setTaskScheduler(webSocketTaskScheduler());
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Aligned with SecurityConfig.corsConfigurationSource() allowed origins (no wildcard)
        registry.addEndpoint("/ws/batch-notifications")
                .setAllowedOrigins(
                    "http://localhost:3000",
                    "http://localhost:3001",
                    "http://127.0.0.1:3000",
                    "http://127.0.0.1:3001"
                )
                .withSockJS();
    }
}
