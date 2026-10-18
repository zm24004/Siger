package com.siger.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Channel interceptor that authenticates the STOMP {@code CONNECT} frame
 * using the same JWT used for the REST API.
 *
 * <p>Spring's {@code clientInboundChannel} auto-detects any
 * {@link ChannelInterceptor} bean, so annotating this class with
 * {@code @Component} is enough to plug it in.</p>
 *
 * <p>On {@code CONNECT}, the {@code Authorization} native header is read,
 * the JWT is verified with {@link JwtService#leer(String)}, and the email
 * subject is set as the user principal on the accessor. This principal
 * name is later used by {@code SimpMessagingTemplate#convertAndSendToUser}
 * to route per-user messages.</p>
 *
 * <p>Any failure (missing header, malformed value, JWT signature/expiry)
 * rejects the CONNECT by throwing {@link MessagingException}; the client
 * sees a closed socket rather than an unauthenticated session.</p>
 */
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public JwtChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() != null
                && StompCommand.CONNECT.equals(accessor.getCommand())) {

            String auth = accessor.getFirstNativeHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new MessagingException("Falta token de autenticación");
            }

            String token = auth.substring("Bearer ".length());
            Claims claims;
            try {
                claims = jwtService.leer(token);
            } catch (JwtException ex) {
                throw new MessagingException("Token JWT inválido");
            }

            String email = claims.getSubject();
            // Principal is a functional interface — only getName() is abstract.
            // The lambda provides that method, returning the email subject.
            accessor.setUser(() -> email);
        }

        return message;
    }
}
