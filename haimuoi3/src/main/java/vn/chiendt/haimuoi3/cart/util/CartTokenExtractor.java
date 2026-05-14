package vn.chiendt.haimuoi3.cart.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public final class CartTokenExtractor {

    private static final String HEADER_NAME = "X-Cart-Token";
    private static final String COOKIE_NAME = "cart_token";

    private CartTokenExtractor() {}

    public static String extract(HttpServletRequest request) {
        String headerToken = request.getHeader(HEADER_NAME);
        if (headerToken != null && !headerToken.isBlank()) {
            return headerToken;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
