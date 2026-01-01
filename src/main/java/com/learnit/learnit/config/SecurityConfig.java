package com.learnit.learnit.config;

import com.learnit.learnit.user.service.OAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuthService oAuthService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    /**
     * ✅ 1) /api/** 전용: 인증 실패 시 redirect 금지(무조건 401)
     *    ✅ IMPORTANT: 공개 API도 여기에서 permitAll 해야 함 (웹체인 permitAll은 무시됨)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .requestCache(cache -> cache.disable())

                .authorizeHttpRequests(auth -> auth
                        // 🔥 리뷰 관련 API 전부 허용 (메서드 무관)
                        .requestMatchers("/api/reviews/**").permitAll()

                        // 기타 공개 API
                        .requestMatchers(
                                "/api/courses",
                                "/api/search/**",
                                "/api/user/check-email",
                                "/api/mypage/github/**",
                                "/api/me"
                        ).permitAll()

                        // 그 외 api는 로그인 필요
                        .anyRequest().authenticated()
                )

                // ✅ API는 절대 redirect 하지 않고 401만 반환
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"Unauthorized\"}");
                }))

                // 세션 로그인 유지
                .securityContext(Customizer.withDefaults());

        return http.build();
    }

    /**
     * ✅ 2) 웹(페이지) 전용: 기존처럼 redirect 동작 유지
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/login", "/signup", "/user/additional-info", "/user/find-password",
                                "/css/**", "/js/**", "/images/**", "/files/**", "/uploads/**",
                                "/CourseList", "/CourseDetail", "/course/**", "/search", "/error/**",
                                "/oauth2/authorization/**", "/login/oauth2/code/**",
                                "/cart/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureUrl("/login?error=true")
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuthService))
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}
