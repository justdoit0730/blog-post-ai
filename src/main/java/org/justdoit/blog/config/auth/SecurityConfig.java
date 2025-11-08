package org.justdoit.blog.config.auth;

import lombok.RequiredArgsConstructor;
import org.justdoit.blog.service.user.CustomLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))

                // 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/h2-console/**",
                                "/template/**",
                                "/resources/**",
                                "/naverLogin",
                                "/user/**",
                                "/error/**",
                                "/js/**",
                                "/",
                                "/oauth/callback/**",
                                "/test",
                                "/csrf",
                                "/etc/**"

                        ).permitAll() // 로그인 없이 접근 허용
                        .requestMatchers("/api/v1/**").hasAnyRole("MANAGER", "USER")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/user/login")
                        .loginProcessingUrl("/user/doLogin")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/user/login?error=true")
                        .successHandler(customLoginSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/user/logout")         // POST 요청 URL
                        .logoutSuccessUrl("/")             // 로그아웃 후 리다이렉트
                        .invalidateHttpSession(true)       // 세션 종료
                        .deleteCookies("JSESSIONID")       // 쿠키 삭제
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .invalidSessionUrl("/")
                        .maximumSessions(1)
                        .expiredUrl("/")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
