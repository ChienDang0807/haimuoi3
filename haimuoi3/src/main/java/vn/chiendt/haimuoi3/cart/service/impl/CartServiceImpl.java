package vn.chiendt.haimuoi3.cart.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.cart.dto.request.AddCartItemRequest;
import vn.chiendt.haimuoi3.cart.dto.request.AddUserCartItemRequest;
import vn.chiendt.haimuoi3.cart.dto.request.MergeCartRequest;
import vn.chiendt.haimuoi3.cart.dto.response.CartResponse;
import vn.chiendt.haimuoi3.cart.dto.response.MergeCartResponse;
import vn.chiendt.haimuoi3.cart.mapper.CartMapper;
import vn.chiendt.haimuoi3.cart.model.mongo.CartEntity;
import vn.chiendt.haimuoi3.cart.model.mongo.CartItemEntity;
import vn.chiendt.haimuoi3.cart.model.mongo.CartState;
import vn.chiendt.haimuoi3.cart.repository.CartRepository;
import vn.chiendt.haimuoi3.cart.service.CartService;
import vn.chiendt.haimuoi3.cart.validator.CartBusinessValidator;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CART-SERVICE")
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CartBusinessValidator cartBusinessValidator;
    private final ProductRepository productRepository;

    @Override
    public CartResponse createOrGetGuestCart(String cartToken) {
        cartBusinessValidator.validateCartToken(cartToken);

        CartEntity cart = cartRepository.findByCartTokenAndState(cartToken, CartState.ACTIVE)
                .orElseGet(() -> createNewGuestCart(cartToken));

        return cartMapper.toCartResponse(cart);
    }

    @Override
    public CartResponse addGuestItem(AddCartItemRequest request) {
        cartBusinessValidator.validateAddItemRequest(request);

        CartEntity cart = findActiveGuestCartOrThrow(request.getCartToken());

        addOrMergeItem(cart, request.getProductId(), request.getQuantity());

        cart.setUpdatedAt(Instant.now());
        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse getGuestCart(String cartToken) {
        cartBusinessValidator.validateCartToken(cartToken);
        CartEntity cart = findActiveGuestCartOrThrow(cartToken);
        return cartMapper.toCartResponse(cart);
    }

    @Override
    public CartResponse getUserCart(String userId) {
        cartBusinessValidator.validateUserId(userId);
        CartEntity cart = cartRepository.findByUserIdAndState(userId, CartState.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User cart not found for userId: " + userId));
        return cartMapper.toCartResponse(cart);
    }

    @Override
    public CartResponse getOrCreateUserCart(Long userId) {
        cartBusinessValidator.validatePositiveUserId(userId);
        CartEntity cart = findActiveUserCartOrCreate(userId);
        return cartMapper.toCartResponse(cart);
    }

    @Override
    public CartResponse addUserCartItem(Long userId, AddUserCartItemRequest request) {
        cartBusinessValidator.validatePositiveUserId(userId);
        cartBusinessValidator.validateAddUserCartItemRequest(request);

        CartEntity cart = findActiveUserCartOrCreate(userId);
        addOrMergeItem(cart, request.getProductId(), request.getQuantity());

        cart.setUpdatedAt(Instant.now());
        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateUserCartItemQuantity(Long userId, String productId, Integer newQuantity) {
        cartBusinessValidator.validatePositiveUserId(userId);
        cartBusinessValidator.validateProductId(productId);
        cartBusinessValidator.validateQuantity(newQuantity);

        CartEntity cart = cartRepository.findByUserIdAndState(String.valueOf(userId), CartState.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User cart not found"));
        CartItemEntity item = findItemInCart(cart, productId);

        item.setQuantity(newQuantity);
        cart.setUpdatedAt(Instant.now());
        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse removeUserCartItem(Long userId, String productId) {
        cartBusinessValidator.validatePositiveUserId(userId);
        cartBusinessValidator.validateProductId(productId);

        CartEntity cart = cartRepository.findByUserIdAndState(String.valueOf(userId), CartState.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User cart not found"));

        boolean removed = cart.getItems().removeIf(item -> productId.equals(item.getProductId()));
        if (!removed) {
            throw new ResourceNotFoundException("Item not found in cart for productId: " + productId);
        }

        cart.setUpdatedAt(Instant.now());
        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Override
    public void clearUserCart(Long userId) {
        cartBusinessValidator.validatePositiveUserId(userId);
        String uid = String.valueOf(userId);
        cartRepository.findByUserIdAndState(uid, CartState.ACTIVE).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setUpdatedAt(Instant.now());
            cartRepository.save(cart);
        });
    }

    @Override
    @Transactional
    public MergeCartResponse mergeGuestCartToUser(MergeCartRequest request) {
        cartBusinessValidator.validateMergeRequest(request);

        Optional<CartEntity> guestCartOpt = cartRepository.findByCartTokenAndState(
                request.getGuestCartToken(), CartState.ACTIVE);

        if (guestCartOpt.isEmpty()) {
            log.info("Guest cart not found or already merged for token: {}", request.getGuestCartToken());
            return MergeCartResponse.builder()
                    .merged(false)
                    .mergedItemsCount(0)
                    .notifications(List.of("Guest cart not found or already merged"))
                    .build();
        }

        CartEntity guestCart = guestCartOpt.get();
        if (guestCart.getItems().isEmpty()) {
            guestCart.setState(CartState.MERGED);
            guestCart.setUpdatedAt(Instant.now());
            cartRepository.save(guestCart);
            return MergeCartResponse.builder()
                    .merged(false)
                    .mergedItemsCount(0)
                    .notifications(List.of("Guest cart was empty, nothing to merge"))
                    .build();
        }

        CartEntity userCart = cartRepository.findByUserIdAndState(request.getUserId(), CartState.ACTIVE)
                .orElseGet(() -> createNewUserCart(request.getUserId()));

        List<String> notifications = new ArrayList<>();
        int mergedCount = 0;

        for (CartItemEntity guestItem : guestCart.getItems()) {
            int clamped = mergeItemIntoCart(userCart, guestItem, notifications);
            mergedCount += clamped;
        }

        guestCart.setState(CartState.MERGED);
        guestCart.setUpdatedAt(Instant.now());
        userCart.setUpdatedAt(Instant.now());

        cartRepository.save(guestCart);
        cartRepository.save(userCart);

        return MergeCartResponse.builder()
                .merged(true)
                .mergedItemsCount(mergedCount)
                .notifications(notifications)
                .build();
    }

    @Override
    public CartResponse removeItem(String cartToken, String productId) {
        cartBusinessValidator.validateCartToken(cartToken);

        CartEntity cart = findActiveGuestCartOrThrow(cartToken);
        boolean removed = cart.getItems().removeIf(item -> productId.equals(item.getProductId()));

        if (!removed) {
            throw new ResourceNotFoundException("Item not found in cart for productId: " + productId);
        }

        cart.setUpdatedAt(Instant.now());
        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateItemQuantity(String cartToken, String productId, int newQuantity) {
        cartBusinessValidator.validateCartToken(cartToken);
        cartBusinessValidator.validateQuantity(newQuantity);

        CartEntity cart = findActiveGuestCartOrThrow(cartToken);
        CartItemEntity item = findItemInCart(cart, productId);

        item.setQuantity(newQuantity);
        cart.setUpdatedAt(Instant.now());
        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Override
    public void clearCart(String cartToken) {
        cartBusinessValidator.validateCartToken(cartToken);

        CartEntity cart = findActiveGuestCartOrThrow(cartToken);
        cart.getItems().clear();
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }

    private CartEntity createNewGuestCart(String cartToken) {
        CartEntity cart = CartEntity.builder()
                .cartToken(cartToken)
                .state(CartState.ACTIVE)
                .expiresAt(Instant.now().plus(Constants.Cart.GUEST_CART_TTL_DAYS, ChronoUnit.DAYS))
                .build();
        return cartRepository.save(cart);
    }

    private CartEntity createNewUserCart(String userId) {
        CartEntity cart = CartEntity.builder()
                .userId(userId)
                .state(CartState.ACTIVE)
                .build();
        return cartRepository.save(cart);
    }

    private CartEntity findActiveGuestCartOrThrow(String cartToken) {
        return cartRepository.findByCartTokenAndState(cartToken, CartState.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active guest cart not found for token: " + cartToken));
    }

    private CartEntity findActiveUserCartOrCreate(Long userId) {
        String uid = String.valueOf(userId);
        return cartRepository.findByUserIdAndState(uid, CartState.ACTIVE)
                .orElseGet(() -> createNewUserCart(uid));
    }

    private CartItemEntity findItemInCart(CartEntity cart, String productId) {
        return cart.getItems().stream()
                .filter(item -> productId.equals(item.getProductId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart for productId: " + productId));
    }

    private void addOrMergeItem(CartEntity cart, String productId, int quantity) {
        ProductEntity product = loadActiveProductForCart(productId);
        Optional<CartItemEntity> existingOpt = cart.getItems().stream()
                .filter(item -> productId.equals(item.getProductId()))
                .findFirst();

        if (existingOpt.isPresent()) {
            CartItemEntity existing = existingOpt.get();
            int newQty = Math.min(existing.getQuantity() + quantity, Constants.Cart.MAX_QUANTITY_PER_ITEM);
            existing.setQuantity(newQty);
        } else {
            cartBusinessValidator.validateMaxDistinctItems(cart.getItems().size());
            cart.getItems().add(buildCartLineFromProduct(product, quantity));
        }
    }

    private ProductEntity loadActiveProductForCart(String productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        String status = product.getStatus();
        if (status == null || !"ACTIVE".equalsIgnoreCase(status.trim())) {
            throw new IllegalArgumentException("Product is not available for cart: " + productId);
        }
        if (product.getShopId() == null || product.getShopId().isBlank()) {
            throw new IllegalArgumentException("Product has no shop: " + productId);
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Product has invalid price: " + productId);
        }
        if (ProductKind.resolve(product) == ProductKind.PARENT) {
            throw new IllegalArgumentException(Constants.Product.PARENT_NOT_SELLABLE);
        }
        return product;
    }

    private CartItemEntity buildCartLineFromProduct(ProductEntity product, int quantity) {
        return CartItemEntity.builder()
                .id(UUID.randomUUID().toString())
                .productId(product.getId())
                .quantity(quantity)
                .unitPriceSnapshot(product.getPrice())
                .shopId(product.getShopId())
                .productNameSnapshot(product.getName())
                .build();
    }

    private int mergeItemIntoCart(CartEntity userCart, CartItemEntity guestItem, List<String> notifications) {
        Optional<CartItemEntity> existingOpt = userCart.getItems().stream()
                .filter(item -> guestItem.getProductId().equals(item.getProductId()))
                .findFirst();

        if (existingOpt.isPresent()) {
            CartItemEntity existing = existingOpt.get();
            int combined = existing.getQuantity() + guestItem.getQuantity();
            int clamped = Math.min(combined, Constants.Cart.MAX_QUANTITY_PER_ITEM);
            if (clamped < combined) {
                notifications.add("Quantity for product " + guestItem.getProductId()
                        + " was clamped to max " + Constants.Cart.MAX_QUANTITY_PER_ITEM);
            }
            existing.setQuantity(clamped);
            return clamped;
        } else {
            ProductEntity product = loadActiveProductForCart(guestItem.getProductId());
            cartBusinessValidator.validateMaxDistinctItems(userCart.getItems().size());
            userCart.getItems().add(buildCartLineFromProduct(product, guestItem.getQuantity()));
            return guestItem.getQuantity();
        }
    }
}
