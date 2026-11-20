package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/csrf").permitAll()

                .requestMatchers(HttpMethod.GET,
                    "/api/products/**",
                    "/api/categories/**"
                ).permitAll()
                .requestMatchers(HttpMethod.HEAD,
                    "/api/products/**",
                    "/api/categories/**"
                ).permitAll()
                .requestMatchers(
                    "/api/products/**",
                    "/api/categories/**"
                ).hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .logout(Customizer.withDefaults())
            .build();
    }
}
