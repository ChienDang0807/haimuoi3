package vn.chiendt.haimuoi3.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Chuoi shopId trong Mongo product/document la so (PK Postgres cua shop).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShopIdUtils {

    /**
     * @return null neu null/blank hoac khong parse duoc thanh Long
     */
    public static Long parseLongOrNull(String shopId) {
        if (!StringUtils.hasText(shopId)) {
            return null;
        }
        try {
            return Long.parseLong(shopId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * @throws IllegalArgumentException neu null/blank hoac khong phai so
     */
    public static long requireLongShopId(String shopId, String message) {
        Long v = parseLongOrNull(shopId);
        if (v == null) {
            throw new IllegalArgumentException(message);
        }
        return v;
    }
}
