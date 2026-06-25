package com.apitesting.config;

import com.apitesting.security.JwtAuthenticationFilter;
import com.apitesting.security.Permission;
import com.apitesting.security.RestAccessDeniedHandler;
import com.apitesting.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Central säkerhetskonfiguration: stateless JWT, CORS för React-devservern,
 * och behörighetsregler per endpoint. Behörigheter uttrycks som authorities
 * (Permission-enumets namn), inte ROLE_-prefix.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    // Filter och handlers tas som metod-parametrar (inte konstruktor-injektion)
    // för att undvika en cirkulär beroendecykel via PasswordEncoder-beanen nedan.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter,
                                           RestAuthenticationEntryPoint authenticationEntryPoint,
                                           RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- Öppna endpoints ---
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/register",
                                "/api/auth/basic", "/api/auth/api-key").permitAll()
                        .requestMatchers("/api/status/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

                        // --- Inloggad användare ---
                        .requestMatchers("/api/auth/me").authenticated()

                        // --- Böcker ---
                        .requestMatchers(HttpMethod.GET, "/api/books/**").hasAuthority(Permission.BOOK_READ.name())
                        .requestMatchers(HttpMethod.POST, "/api/books").hasAuthority(Permission.BOOK_CREATE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAuthority(Permission.BOOK_UPDATE.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/books/**").hasAuthority(Permission.BOOK_UPDATE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAuthority(Permission.BOOK_DELETE.name())

                        // --- Användare (CRUD-resurs) ---
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAuthority(Permission.USER_READ.name())
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority(Permission.USER_MANAGE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority(Permission.USER_MANAGE.name())

                        // --- Lån ---
                        .requestMatchers(HttpMethod.GET, "/api/loans/me").hasAuthority(Permission.LOAN_VIEW_OWN.name())
                        .requestMatchers(HttpMethod.GET, "/api/loans").hasAuthority(Permission.LOAN_VIEW_ALL.name())
                        .requestMatchers(HttpMethod.POST, "/api/loans/borrow-for").hasAuthority(Permission.LOAN_MANAGE.name())
                        .requestMatchers(HttpMethod.POST, "/api/loans/*/return")
                                .hasAnyAuthority(Permission.LOAN_RETURN.name(), Permission.LOAN_MANAGE.name())
                        .requestMatchers(HttpMethod.POST, "/api/loans").hasAuthority(Permission.LOAN_BORROW.name())

                        // --- Admin: roll-/behörighetshantering (mest specifika först) ---
                        .requestMatchers(HttpMethod.POST, "/api/admin/accounts/*/permissions/*").hasAuthority(Permission.PERMISSION_MANAGE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/accounts/*/permissions/*").hasAuthority(Permission.PERMISSION_MANAGE.name())
                        .requestMatchers(HttpMethod.PUT, "/api/admin/accounts/*/role").hasAuthority(Permission.PERMISSION_MANAGE.name())

                        // --- Admin: kontohantering ---
                        .requestMatchers(HttpMethod.GET, "/api/admin/accounts")
                                .hasAnyAuthority(Permission.ACCOUNT_MANAGE.name(), Permission.PERMISSION_MANAGE.name())
                        .requestMatchers(HttpMethod.POST, "/api/admin/accounts").hasAuthority(Permission.ACCOUNT_MANAGE.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/accounts/*").hasAuthority(Permission.ACCOUNT_MANAGE.name())

                        // --- Admin: övrigt (roller/permissions-listor) ---
                        .requestMatchers("/api/admin/**").hasAuthority(Permission.PERMISSION_MANAGE.name())

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Location"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
