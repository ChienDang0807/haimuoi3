package vn.chiendt.haimuoi3.common.config;

import java.security.Principal;

/**
 * Principal được set trên STOMP session sau khi xác thực JWT tại CONNECT.
 * name = userId (dạng String). shopId chỉ có giá trị khi role == SHOP_OWNER.
 */
public record StompPrincipal(String name, String role, Long shopId) implements Principal {

    @Override
    public String getName() {
        return name;
    }
}
