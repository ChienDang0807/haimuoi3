package vn.chiendt.haimuoi3.sysadmin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.chiendt.haimuoi3.common.config.JwtAuthenticationFilter;
import vn.chiendt.haimuoi3.common.config.JwtTokenProvider;
import vn.chiendt.haimuoi3.common.config.SecurityConfig;
import vn.chiendt.haimuoi3.product.dto.request.CreateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.request.UpdateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryAdminResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalProductResponse;
import vn.chiendt.haimuoi3.product.service.GlobalCategoryService;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SysadminCategoryController.class)
@Import({SecurityConfig.class, SysadminCategoryControllerIntegrationTest.TestSecurityBeans.class})
@TestPropertySource(properties = "spring.profiles.active=test")
class SysadminCategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GlobalCategoryService globalCategoryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    private GlobalCategoryAdminResponse categoryResponse;
    private CreateGlobalCategoryRequest createRequest;
    private UpdateGlobalCategoryRequest updateRequest;

    @BeforeEach
    void setUp() {
        categoryResponse = GlobalCategoryAdminResponse.builder()
                .globalCategoryId("cat-123")
                .name("Test Category")
                .slug("test-category")
                .description("Test description")
                .imageUrl("http://example.com/image.jpg")
                .displayOrder(1)
                .isActive(true)
                .productCount(0L)
                .build();

        createRequest = CreateGlobalCategoryRequest.builder()
                .name("New Category")
                .slug("new-category")
                .description("New description")
                .displayOrder(2)
                .build();

        updateRequest = UpdateGlobalCategoryRequest.builder()
                .name("Updated Category")
                .description("Updated description")
                .displayOrder(3)
                .build();
    }

    @Test
    void unauthenticatedRequests_areRejected() throws Exception {
        mockMvc.perform(get("/api/v1/sysadmin/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void nonAdminRequests_areForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/sysadmin/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllCategories_Success() throws Exception {
        Page<GlobalCategoryAdminResponse> page = new PageImpl<>(List.of(categoryResponse), PageRequest.of(0, 20), 1);
        when(globalCategoryService.findAllForAdmin(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/sysadmin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].globalCategoryId").value("cat-123"))
                .andExpect(jsonPath("$.result.content[0].name").value("Test Category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetCategoryById_Success() throws Exception {
        when(globalCategoryService.getForAdmin("cat-123")).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/sysadmin/categories/cat-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.globalCategoryId").value("cat-123"))
                .andExpect(jsonPath("$.result.slug").value("test-category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategory_Success() throws Exception {
        when(globalCategoryService.create(any(CreateGlobalCategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/sysadmin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.globalCategoryId").value("cat-123"))
                .andExpect(jsonPath("$.message").value("Category created successfully"));

        verify(globalCategoryService).create(any(CreateGlobalCategoryRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateCategory_Success() throws Exception {
        when(globalCategoryService.update(eq("cat-123"), any(UpdateGlobalCategoryRequest.class)))
                .thenReturn(categoryResponse);

        mockMvc.perform(put("/api/v1/sysadmin/categories/cat-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.globalCategoryId").value("cat-123"))
                .andExpect(jsonPath("$.message").value("Category updated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testToggleCategoryStatus_Success() throws Exception {
        when(globalCategoryService.toggleStatus("cat-123")).thenReturn(categoryResponse);

        mockMvc.perform(put("/api/v1/sysadmin/categories/cat-123/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.globalCategoryId").value("cat-123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteCategory_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/sysadmin/categories/cat-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        verify(globalCategoryService).delete("cat-123");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetProductsByCategory_Success() throws Exception {
        Page<GlobalProductResponse> page = Page.empty(PageRequest.of(0, 20));
        when(globalCategoryService.findProductsByCategory(eq("cat-123"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/sysadmin/categories/cat-123/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content").isArray())
                .andExpect(jsonPath("$.message").value("Products retrieved successfully"));
    }

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                                        UserRepository userRepository) {
            return new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
        }
    }
}
