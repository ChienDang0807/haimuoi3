package vn.chiendt.haimuoi3.shop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.chiendt.haimuoi3.common.config.JwtAuthenticationFilter;
import vn.chiendt.haimuoi3.common.config.JwtTokenProvider;
import vn.chiendt.haimuoi3.common.config.SecurityConfig;
import vn.chiendt.haimuoi3.inventory.dto.response.InventoryItemResponse;
import vn.chiendt.haimuoi3.inventory.service.InventoryService;
import vn.chiendt.haimuoi3.order.service.OrderService;
import vn.chiendt.haimuoi3.product.service.ProductService;
import vn.chiendt.haimuoi3.shop.service.ShopCategoryService;
import vn.chiendt.haimuoi3.shop.service.ShopDashboardService;
import vn.chiendt.haimuoi3.shop.service.ShopService;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ShopController.class)
@Import({SecurityConfig.class, ShopControllerIntegrationTest.TestSecurityBeans.class})
@TestPropertySource(properties = "spring.profiles.active=test")
class ShopControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShopService shopService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ShopCategoryService shopCategoryService;

    @MockitoBean
    private ShopDashboardService shopDashboardService;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
            return new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
        }
    }

    @Test
    void adjustMyShopInventory_withoutAuthentication_isForbidden() throws Exception {
        mockMvc.perform(patch("/api/v1/shops/my-shop/inventory/p1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantityOnHand\":12}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adjustMyShopInventory_returnsInventoryItemWithoutPersistenceFields() throws Exception {
        UserEntity owner = UserEntity.builder().id(7L).build();
        InventoryItemResponse item = InventoryItemResponse.builder()
                .productId("p1")
                .displayName("Product 1")
                .sku("SKU-1")
                .quantityOnHand(12)
                .lowStock(false)
                .build();
        when(inventoryService.adjustStockForShopOwner(eq(7L), eq("p1"), any())).thenReturn(item);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                owner,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_SHOP_OWNER"))
        );

        mockMvc.perform(patch("/api/v1/shops/my-shop/inventory/p1")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantityOnHand\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.productId").value("p1"))
                .andExpect(jsonPath("$.result.quantityOnHand").value(12))
                .andExpect(jsonPath("$.result.version").doesNotExist());
    }
}
