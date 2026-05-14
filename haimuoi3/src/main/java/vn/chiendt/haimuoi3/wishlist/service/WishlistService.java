package vn.chiendt.haimuoi3.wishlist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.wishlist.dto.request.WishlistContainsRequest;
import vn.chiendt.haimuoi3.wishlist.dto.response.WishlistContainsResponse;
import vn.chiendt.haimuoi3.wishlist.dto.response.WishlistItemResponse;

import java.util.List;

public interface WishlistService {

    List<WishlistItemResponse> listRecent(Long userId, int limit);

    Page<WishlistItemResponse> listPaged(Long userId, Pageable pageable);

    WishlistContainsResponse contains(Long userId, WishlistContainsRequest request);

    WishlistItemResponse add(Long userId, String productId);

    void remove(Long userId, String productId);
}
