package com.siger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Spring WebSocket + STOMP configuration.
 *
 * <p>The handshake endpoint is {@code /ws-notificaciones} (public HTTP upgrade).
 * Authentication is performed at the STOMP {@code CONNECT} frame by
 * {@link com.siger.security.JwtChannelInterceptor}, which sets the user
 * principal (email) so messages can be routed with
 * {@code convertAndSendToUser(email, "/queue/notificaciones", ...)}.</p>
 *
 * <p>Clients subscribe to {@code /user/queue/notificaciones} (the
 * {@code /user} prefix is added automatically by Spring's user-destination
 * resolver because of {@link #setUserDestinationPrefix(String)}).</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple in-memory broker for user-specific queues and broadcast topics.
        registry.enableSimpleBroker("/queue", "/topic");
        // Prefix for messages bound for @MessageMapping methods.
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix used by Spring to resolve "user destinations" — convertAndSendToUser
        // automatically prepends this to the destination passed by the application.
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS-capable endpoint. SockJS connects over HTTP (it handles the
        // upgrade negotiation), so the URL the client uses is http://.../ws-notificaciones.
        registry.addEndpoint("/ws-notificaciones")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}
