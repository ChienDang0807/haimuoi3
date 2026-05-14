package vn.chiendt.haimuoi3.wishlist.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.wishlist.dto.request.WishlistContainsRequest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WishlistContainsRequestValidator {

    public static List<String> normalizeProductIds(WishlistContainsRequest request) {
        if (request == null || request.getProductIds() == null || request.getProductIds().isEmpty()) {
            throw new IllegalArgumentException("productIds must not be empty");
        }
        List<String> raw = request.getProductIds();
        if (raw.size() > Constants.Wishlist.MAX_CONTAINS_IDS) {
            throw new IllegalArgumentException("productIds must not exceed " + Constants.Wishlist.MAX_CONTAINS_IDS);
        }
        List<String> out = new ArrayList<>();
        for (String id : new LinkedHashSet<>(raw)) {
            if (!StringUtils.hasText(id)) {
                continue;
            }
            out.add(id.trim());
        }
        if (out.isEmpty()) {
            throw new IllegalArgumentException("productIds must not be empty");
        }
        return out;
    }
}
