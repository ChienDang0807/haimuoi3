package vn.chiendt.haimuoi3.notification.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.chiendt.haimuoi3.common.config.JwtAuthenticationFilter;
import vn.chiendt.haimuoi3.common.config.JwtTokenProvider;
import vn.chiendt.haimuoi3.common.config.SecurityConfig;
import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;
import vn.chiendt.haimuoi3.notification.model.NotificationType;
import vn.chiendt.haimuoi3.notification.service.NotificationInboxService;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import({SecurityConfig.class, NotificationControllerIntegrationTest.TestSecurityBeans.class})
@TestPropertySource(properties = "spring.profiles.active=test")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationInboxService notificationInboxService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter(
                JwtTokenProvider jwtTokenProvider,
                UserRepository userRepository) {
            return new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
        }
    }

    @Test
    void listMine_returnsNotificationsForShopOwner() throws Exception {
        UserEntity owner = UserEntity.builder()
                .id(1L)
                .email("owner@test.com")
                .role(UserRole.SHOP_OWNER)
                .build();
        NotificationDTO notification = NotificationDTO.builder()
                .id("n1")
                .type(NotificationType.ORDER_CREATED)
                .read(false)
                .timestamp(LocalDateTime.now())
                .recipientRole("SHOP_OWNER")
                .payload(Map.of())
                .build();
        when(notificationInboxService.listForCurrentUser(eq(owner), eq(50)))
                .thenReturn(List.of(notification));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                owner,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_SHOP_OWNER")));

        mockMvc.perform(get("/api/v1/notifications/me")
                        .param("limit", "50")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value("n1"));
    }
}
