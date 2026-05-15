package vn.chiendt.haimuoi3.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    configurePublicEndpoints(auth);
                    configureAdminEndpoints(auth);
                    configureCustomerEndpoints(auth);
                    configureShopOwnerEndpoints(auth);
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void configurePublicEndpoints(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/payments/webhook").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/shops/by-id/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/shops/{slug}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/global").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/suggest").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/products/cart/batch").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/global-categories/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/carts/session").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/carts/session/items").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/carts/session/**").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/api/v1/carts/session/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/carts/session/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/carts/merge").permitAll();
    }

    private void configureAdminEndpoints(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/sysadmin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/global-categories").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/global-categories/*/image-url").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/media/global-category/*/upload/multiple").hasRole("ADMIN");
    }

    private void configureCustomerEndpoints(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/api/v1/products/*/reviews").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/v1/orders/checkout").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/v1/orders").hasRole("CUSTOMER")
                .requestMatchers("/api/v1/carts/me", "/api/v1/carts/me/**").hasRole("CUSTOMER")
                .requestMatchers("/api/v1/customers/me/**").hasRole("CUSTOMER");
    }

    private void configureShopOwnerEndpoints(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/api/v1/media/product/*/upload", "/api/v1/media/product/*/upload/multiple")
                .hasRole("SHOP_OWNER")
                .requestMatchers("/api/v1/shops/my-shop/**").hasRole("SHOP_OWNER");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
