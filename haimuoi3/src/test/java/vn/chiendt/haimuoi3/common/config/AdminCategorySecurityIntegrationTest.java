package vn.chiendt.haimuoi3.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import vn.chiendt.haimuoi3.media.dto.response.MediaUploadResponse;
import vn.chiendt.haimuoi3.media.model.MediaTargetType;
import vn.chiendt.haimuoi3.media.service.MediaService;
import vn.chiendt.haimuoi3.product.controller.GlobalCategoryController;
import vn.chiendt.haimuoi3.media.controller.MediaController;
import vn.chiendt.haimuoi3.product.dto.request.CreateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryResponse;
import vn.chiendt.haimuoi3.product.service.GlobalCategoryService;
import vn.chiendt.haimuoi3.product.service.ProductService;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {GlobalCategoryController.class, MediaController.class})
@Import({SecurityConfig.class, AdminCategorySecurityIntegrationTest.TestSecurityBeans.class})
@TestPropertySource(properties = "spring.profiles.active=test")
class AdminCategorySecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GlobalCategoryService globalCategoryService;

    @MockBean
    private MediaService mediaService;

    @MockBean
    private ProductService productService;

    @Test
    void unauthenticatedRequests_areRejected() throws Exception {
        mockMvc.perform(post("/api/v1/global-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Beverages",
                                  "slug":"beverages",
                                  "displayOrder":1,
                                  "active":true
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/global-categories/gc1/image-url")
                        .queryParam("imageUrl", "https://cdn.example.com/gc1.jpg"))
                .andExpect(status().isForbidden());

        MockMultipartFile file = new MockMultipartFile("files", "a.jpg", "image/jpeg", "x".getBytes());
        mockMvc.perform(multipart("/api/v1/media/global-category/gc1/upload/multiple")
                        .file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerRequests_areForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/global-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Beverages",
                                  "slug":"beverages",
                                  "displayOrder":1,
                                  "active":true
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/global-categories/gc1/image-url")
                        .queryParam("imageUrl", "https://cdn.example.com/gc1.jpg"))
                .andExpect(status().isForbidden());

        MockMultipartFile file = new MockMultipartFile("files", "a.jpg", "image/jpeg", "x".getBytes());
        mockMvc.perform(multipart("/api/v1/media/global-category/gc1/upload/multiple")
                        .file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SHOP_OWNER")
    void shopOwnerRequests_areForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/global-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Beverages",
                                  "slug":"beverages",
                                  "displayOrder":1,
                                  "active":true
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/global-categories/gc1/image-url")
                        .queryParam("imageUrl", "https://cdn.example.com/gc1.jpg"))
                .andExpect(status().isForbidden());

        MockMultipartFile file = new MockMultipartFile("files", "a.jpg", "image/jpeg", "x".getBytes());
        mockMvc.perform(multipart("/api/v1/media/global-category/gc1/upload/multiple")
                        .file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRequests_areAllowed() throws Exception {
        when(globalCategoryService.save(any(CreateGlobalCategoryRequest.class))).thenReturn(sampleCategory());
        when(globalCategoryService.updateImageUrl(anyString(), anyString())).thenReturn(Optional.of(sampleCategory()));
        when(mediaService.uploadImage(any(), eq(MediaTargetType.GLOBAL_CATEGORY))).thenReturn(sampleUpload());
        when(globalCategoryService.updateImages(anyString(), anyList())).thenReturn(sampleCategory());

        mockMvc.perform(post("/api/v1/global-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Beverages",
                                  "slug":"beverages",
                                  "displayOrder":1,
                                  "active":true
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/v1/global-categories/gc1/image-url")
                        .queryParam("imageUrl", "https://cdn.example.com/gc1.jpg"))
                .andExpect(status().isOk());

        MockMultipartFile file = new MockMultipartFile("files", "a.jpg", "image/jpeg", "x".getBytes());
        mockMvc.perform(multipart("/api/v1/media/global-category/gc1/upload/multiple")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    private GlobalCategoryResponse sampleCategory() {
        return GlobalCategoryResponse.builder()
                .globalCategoryId("gc1")
                .name("Beverages")
                .slug("beverages")
                .imageUrl("https://cdn.example.com/gc1.jpg")
                .displayOrder(1)
                .isActive(true)
                .metaData(java.util.Map.of("images", List.of("https://cdn.example.com/gc1.jpg")))
                .build();
    }

    private MediaUploadResponse sampleUpload() {
        return MediaUploadResponse.builder()
                .url("https://cdn.example.com/gc1.jpg")
                .build();
    }

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                                        UserRepository userRepository) {
            return new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
        }
    }

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;
}
