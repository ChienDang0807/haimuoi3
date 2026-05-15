package vn.chiendt.haimuoi3.order.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.cart.repository.CartRepository;
import vn.chiendt.haimuoi3.cart.service.CartService;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.inventory.service.InventoryService;
import vn.chiendt.haimuoi3.notification.service.NotificationService;
import vn.chiendt.haimuoi3.order.mapper.OrderMapper;
import vn.chiendt.haimuoi3.order.repository.OrderRepository;
import vn.chiendt.haimuoi3.order.validator.CheckoutOrderRequestValidator;
import vn.chiendt.haimuoi3.order.validator.CreateOrderRequestValidator;
import vn.chiendt.haimuoi3.order.validator.CustomerOrderCancelValidator;
import vn.chiendt.haimuoi3.order.validator.CustomerOrderConfirmDeliveredValidator;
import vn.chiendt.haimuoi3.order.validator.OrderPageableValidator;
import vn.chiendt.haimuoi3.order.validator.ShopOrderStatusTransitionValidator;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.shop.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.shop.service.ShopService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplShopOwnerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CreateOrderRequestValidator createOrderRequestValidator;

    @Mock
    private OrderPageableValidator orderPageableValidator;

    @Mock
    private CustomerOrderCancelValidator customerOrderCancelValidator;

    @Mock
    private CustomerOrderConfirmDeliveredValidator customerOrderConfirmDeliveredValidator;

    @Mock
    private ShopService shopService;

    @Mock
    private ShopOrderStatusTransitionValidator shopOrderStatusTransitionValidator;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CheckoutOrderRequestValidator checkoutOrderRequestValidator;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getOrderForShopOwner_whenOrderNotInOwnerShop_throwsNotFound() {
        when(shopService.getShopByOwnerId(5L)).thenReturn(ShopResponse.builder().id(9L).build());
        when(orderRepository.findByIdAndShopId(3L, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderForShopOwner(5L, 3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with id: 3");

        verify(shopService).getShopByOwnerId(5L);
        verify(createOrderRequestValidator).validatePositiveOrderId(3L);
    }

    @Test
    void getOrdersForShopOwner_resolvesOwnerShopBeforeListing() {
        when(shopService.getShopByOwnerId(5L)).thenReturn(ShopResponse.builder().id(9L).build());
        when(orderRepository.findByShopId(9L, Pageable.unpaged())).thenReturn(org.springframework.data.domain.Page.empty());

        orderService.getOrdersForShopOwner(5L, Pageable.unpaged());

        verify(shopService).getShopByOwnerId(5L);
        verify(orderPageableValidator).validate(Pageable.unpaged());
    }
}
