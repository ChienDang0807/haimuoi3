package vn.chiendt.haimuoi3.common.config;

import org.springframework.context.annotation.Configuration;

/**
 * WebSocket authentication và per-subscription authorization được xử lý bởi
 * {@link JwtChannelInterceptor} đăng ký trong {@link WebSocketConfig}.
 *
 * - CONNECT: JWT được validate, StompPrincipal được set trên session.
 * - SUBSCRIBE: destination được kiểm tra theo userId / shopId của principal.
 */
@Configuration
public class WebSocketSecurityConfig {
}

