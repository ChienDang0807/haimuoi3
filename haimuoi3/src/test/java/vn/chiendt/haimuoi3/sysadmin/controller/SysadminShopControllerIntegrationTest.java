package vn.chiendt.haimuoi3.sysadmin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import vn.chiendt.haimuoi3.common.config.JwtAuthenticationFilter;
import vn.chiendt.haimuoi3.common.config.JwtTokenProvider;
import vn.chiendt.haimuoi3.common.config.SecurityConfig;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.sysadmin.dto.request.AssignOwnerRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopListResponse;
import vn.chiendt.haimuoi3.sysadmin.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.sysadmin.service.ShopManagementService;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SysadminShopController.class)
@Import({SecurityConfig.class, SysadminShopControllerIntegrationTest.TestSecurityBeans.class})
@TestPropertySource(properties = "spring.profiles.active=test")
class SysadminShopControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShopManagementService shopManagementService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
            return new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
        }
    }

    @Test
    void unauthenticatedRequests_areRejected() throws Exception {
        mockMvc.perform(get("/api/v1/sysadmin/shops"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerRequests_areForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/sysadmin/shops"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllShops_ShouldReturnPaginatedShops() throws Exception {
        // Given
        ShopResponse shopResponse = ShopResponse.builder()
                .id(1L)
                .shopName("Test Shop")
                .slug("test-shop")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        ShopListResponse listResponse = ShopListResponse.builder()
                .shops(Collections.singletonList(shopResponse))
                .currentPage(0)
                .totalPages(1)
                .totalElements(1L)
                .pageSize(20)
                .build();

        when(shopManagementService.findAllShops(any())).thenReturn(listResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/sysadmin/shops")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shops").isArray())
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(shopManagementService).findAllShops(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getShopById_ExistingShop_ShouldReturnShop() throws Exception {
        // Given
        ShopResponse shopResponse = ShopResponse.builder()
                .id(1L)
                .shopName("Test Shop")
                .slug("test-shop")
                .status("ACTIVE")
                .build();

        when(shopManagementService.getShopById(1L)).thenReturn(shopResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/sysadmin/shops/{shopId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.shopName").value("Test Shop"))
                .andExpect(jsonPath("$.data.slug").value("test-shop"));

        verify(shopManagementService).getShopById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getShopById_NonExistingShop_ShouldReturn404() throws Exception {
        // Given
        when(shopManagementService.getShopById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Shop not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/sysadmin/shops/{shopId}", 99999L))
                .andExpect(status().isNotFound());

        verify(shopManagementService).getShopById(99999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShop_ValidRequest_ShouldCreateShop() throws Exception {
        // Given
        CreateShopRequest request = CreateShopRequest.builder()
                .shopName("New Shop")
                .slug("new-shop")
                .description("New shop description")
                .email("newshop@test.com")
                .phone("0987654321")
                .build();

        ShopResponse shopResponse = ShopResponse.builder()
                .id(1L)
                .shopName("New Shop")
                .slug("new-shop")
                .status("ACTIVE")
                .build();

        when(shopManagementService.createShop(any(CreateShopRequest.class))).thenReturn(shopResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/sysadmin/shops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shopName").value("New Shop"))
                .andExpect(jsonPath("$.data.slug").value("new-shop"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(shopManagementService).createShop(any(CreateShopRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateShop_ValidRequest_ShouldUpdateShop() throws Exception {
        // Given
        UpdateShopRequest request = UpdateShopRequest.builder()
                .shopName("Updated Shop Name")
                .description("Updated description")
                .build();

        ShopResponse shopResponse = ShopResponse.builder()
                .id(1L)
                .shopName("Updated Shop Name")
                .slug("test-shop")
                .status("ACTIVE")
                .build();

        when(shopManagementService.updateShop(anyLong(), any(UpdateShopRequest.class)))
                .thenReturn(shopResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/sysadmin/shops/{shopId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shopName").value("Updated Shop Name"));

        verify(shopManagementService).updateShop(anyLong(), any(UpdateShopRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteShop_ShouldDeleteShop() throws Exception {
        // Given
        doNothing().when(shopManagementService).deleteShop(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/v1/sysadmin/shops/{shopId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Shop deleted successfully"));

        verify(shopManagementService).deleteShop(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignOwner_ValidRequest_ShouldAssignOwner() throws Exception {
        // Given
        AssignOwnerRequest request = AssignOwnerRequest.builder()
                .userId(1L)
                .build();

        ShopResponse shopResponse = ShopResponse.builder()
                .id(1L)
                .shopName("Test Shop")
                .ownerId(1L)
                .ownerName("Test Owner")
                .ownerEmail("owner@test.com")
                .build();

        when(shopManagementService.assignOwner(anyLong(), any(AssignOwnerRequest.class)))
                .thenReturn(shopResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/sysadmin/shops/{shopId}/owner", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ownerId").value(1))
                .andExpect(jsonPath("$.data.ownerName").value("Test Owner"));

        verify(shopManagementService).assignOwner(anyLong(), any(AssignOwnerRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeOwner_ValidRequest_ShouldChangeOwner() throws Exception {
        // Given
        AssignOwnerRequest request = AssignOwnerRequest.builder()
                .userId(2L)
                .build();

        ShopResponse shopResponse = ShopResponse.builder()
                .id(1L)
                .shopName("Test Shop")
                .ownerId(2L)
                .ownerName("New Owner")
                .build();

        when(shopManagementService.changeOwner(anyLong(), any(AssignOwnerRequest.class)))
                .thenReturn(shopResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/sysadmin/shops/{shopId}/owner", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ownerId").value(2))
                .andExpect(jsonPath("$.data.ownerName").value("New Owner"));

        verify(shopManagementService).changeOwner(anyLong(), any(AssignOwnerRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeOwner_ShouldRemoveOwner() throws Exception {
        // Given
        ShopResponse shopResponse = ShopResponse.builder()
                .id(1L)
                .shopName("Test Shop")
                .build();

        when(shopManagementService.removeOwner(anyLong())).thenReturn(shopResponse);

        // When & Then
        mockMvc.perform(delete("/api/v1/sysadmin/shops/{shopId}/owner", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(shopManagementService).removeOwner(1L);
    }
}

