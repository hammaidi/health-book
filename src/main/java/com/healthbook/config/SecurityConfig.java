package com.healthbook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 🔒 PROTECTION CSRF
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            // 🛡️ HEADERS DE SÉCURITÉ (AUTORISE BOOTSTRAP CDN)
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self' https://cdn.jsdelivr.net; " +
                    "style-src 'self' https://cdn.jsdelivr.net 'unsafe-inline'; " +
                    "script-src 'self' 'unsafe-inline'"
                ))
                .frameOptions(frame -> frame.sameOrigin())
            )
            // 🔐 AUTORISATIONS PAR RÔLE
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/patients", "/patients/**").hasRole("ADMIN")
                .requestMatchers("/medecins", "/medecins/**").hasAnyRole("ADMIN", "MEDECIN")
                .requestMatchers("/rendezvous/**").hasAnyRole("ADMIN", "MEDECIN", "PATIENT")
                .requestMatchers("/dashboard/**").authenticated()
                .anyRequest().authenticated()
            )
            // 🔑 AUTHENTIFICATION
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // 🚪 DÉCONNEXION SÉCURISÉE
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}