package com.design.order_management_system.config;

import com.design.order_management_system.security.JwtAuthenticationFilter;
import com.design.order_management_system.security.JwtLogoutHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtLogoutHandler jwtLogoutHandler;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
        return httpSecurity.formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                                    "/auth/login",
                                    "/v3/api-docs/**",
                                    "/swagger-ui/**",
                                    "/swagger-ui.html"
                            )
                            .permitAll();
                    auth.anyRequest()
                            .authenticated();
                })
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                ((_, response, _) -> response.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                        ).accessDeniedHandler(
                                ((_, response, _) -> response.setStatus(HttpServletResponse.SC_FORBIDDEN))
                        )
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        LogoutFilter.class
                )
                .logout(logout -> logout.logoutUrl("/auth/logout")
                        .addLogoutHandler(jwtLogoutHandler)
                        .logoutSuccessHandler((_, response, _) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Logout successful!");
                        }))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
