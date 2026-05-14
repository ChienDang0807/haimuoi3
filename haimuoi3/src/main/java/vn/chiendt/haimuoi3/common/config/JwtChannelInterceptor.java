package vn.chiendt.haimuoi3.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.shop.repository.ShopRepository;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            StompPrincipal principal = authenticate(accessor);
            accessor.setUser(principal);
            log.debug("WebSocket CONNECT authenticated: userId={} role={}", principal.name(), principal.role());
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            enforceSubscribeAuthz(accessor);
        }

        return message;
    }

    private StompPrincipal authenticate(StompHeaderAccessor accessor) {
        String auth = accessor.getFirstNativeHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new MessageDeliveryException("Missing or invalid Authorization header on CONNECT");
        }
        String token = auth.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            throw new MessageDeliveryException("Invalid or expired JWT token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        UserEntity user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new MessageDeliveryException("User not found or inactive"));

        Long shopId = null;
        if (user.getRole() == UserRole.SHOP_OWNER) {
            shopId = shopRepository.findByOwnerId(userId)
                    .map(shop -> shop.getId())
                    .orElse(null);
        }

        return new StompPrincipal(userId.toString(), user.getRole().name(), shopId);
    }

    private void enforceSubscribeAuthz(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        if (!(user instanceof StompPrincipal principal)) {
            throw new MessageDeliveryException("Not authenticated");
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        checkCustomerQueue(destination, principal);
        checkShopTopic(destination, principal);
    }

    private void checkCustomerQueue(String destination, StompPrincipal principal) {
        final String prefix = "/queue/user/";
        if (!destination.startsWith(prefix)) {
            return;
        }
        String remainder = destination.substring(prefix.length());
        int slashIdx = remainder.indexOf('/');
        String destUserId = slashIdx >= 0 ? remainder.substring(0, slashIdx) : remainder;
        if (!principal.name().equals(destUserId)) {
            log.warn("Forbidden subscribe: userId={} tried to access queue of userId={}", principal.name(), destUserId);
            throw new MessageDeliveryException("Forbidden: cannot subscribe to another user's queue");
        }
        if (!destination.endsWith("/notifications")) {
            throw new MessageDeliveryException("Forbidden: unsupported queue destination");
        }
    }

    private void checkShopTopic(String destination, StompPrincipal principal) {
        final String prefix = "/topic/shop/";
        if (!destination.startsWith(prefix)) {
            return;
        }
        // destination pattern: /topic/shop/{shopId}/orders
        String remainder = destination.substring(prefix.length());
        int slashIdx = remainder.indexOf('/');
        String destShopId = slashIdx >= 0 ? remainder.substring(0, slashIdx) : remainder;

        if (principal.shopId() == null || !principal.shopId().toString().equals(destShopId)) {
            log.warn("Forbidden subscribe: userId={} (shopId={}) tried to access shop topic shopId={}",
                    principal.name(), principal.shopId(), destShopId);
            throw new MessageDeliveryException("Forbidden: cannot subscribe to another shop's topic");
        }
        if (!destination.endsWith("/notifications")) {
            throw new MessageDeliveryException("Forbidden: unsupported shop topic destination");
        }
    }
}
