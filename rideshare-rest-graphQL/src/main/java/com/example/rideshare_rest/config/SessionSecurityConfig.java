package com.example.rideshare_rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;


@Profile("session-auth")
@Configuration
@EnableMethodSecurity // Включает поддержку @PreAuthorize
public class SessionSecurityConfig {

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain sessionSecurityFilterChain(HttpSecurity http) throws Exception {

        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/login", "/csrf", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()


                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("READER", "EDITOR", "OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("EDITOR", "OPERATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("EDITOR", "OPERATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("EDITOR", "OPERATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("OPERATOR")

                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .ignoringRequestMatchers("/login")
                )
                .formLogin(form -> form
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .invalidateHttpSession(true)
                        .logoutSuccessHandler((req, resp, auth) -> resp.setStatus(HttpStatus.NO_CONTENT.value()))
                )
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.migrateSession())
                        .maximumSessions(1)
                );

        return http.build();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("passenger")
                        .password(encoder.encode("password"))
                        .roles("READER")
                        .build(),

                User.withUsername("driver")
                        .password(encoder.encode("password"))
                        .roles("READER", "EDITOR")
                        .build(),

                User.withUsername("operator")
                        .password(encoder.encode("password"))
                        .roles("READER", "EDITOR", "OPERATOR")
                        .build()
        );
    }
}