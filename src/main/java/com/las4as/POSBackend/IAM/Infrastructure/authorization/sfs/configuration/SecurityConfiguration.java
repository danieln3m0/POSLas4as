package com.las4as.POSBackend.IAM.Infrastructure.authorization.sfs.configuration;

import com.las4as.POSBackend.IAM.Infrastructure.authorization.CustomUserDetailsService;
import com.las4as.POSBackend.IAM.Infrastructure.authorization.sfs.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/data-rights/request").permitAll() // Solicitudes ARCO públicas
                .requestMatchers("/api/v1/data-rights/customer/**").permitAll() // Consulta de solicitudes por cliente
                .requestMatchers("/api/v1/consent/register").permitAll() // Registro de consentimientos público
                .requestMatchers("/api/v1/consent/check/**").permitAll() // Verificación de consentimientos público

                // Swagger/OpenAPI - Allow all Swagger and OpenAPI documentation paths
                .requestMatchers("/api-docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html").permitAll()
                .requestMatchers("/webjars/**", "/swagger-resources/**", "/configuration/**").permitAll()
                
                // Permitir acceso a la raíz y endpoints de salud
                .requestMatchers("/", "/health", "/actuator/**").permitAll()
                // Permitir acceso a endpoints de verificación
                .requestMatchers("/api/v1/users/check-username/**", "/api/v1/users/check-email/**").permitAll()
                // Permitir acceso a creación de usuarios (registro público)
                .requestMatchers("/api/v1/users").permitAll()

                // Endpoints de administración de usuarios - solo ADMIN
                .requestMatchers("/api/v1/admin/users/**").hasRole("ADMIN")

                // Endpoints de derechos ARCO - ADMIN y DATA_PROTECTION_OFFICER
                .requestMatchers("/api/v1/data-rights/requests/**").hasAnyRole("ADMIN", "DATA_PROTECTION_OFFICER")

                // Endpoints de consentimientos - administración
                .requestMatchers("/api/v1/consent/admin/**").hasAnyRole("ADMIN", "DATA_PROTECTION_OFFICER")

                // Endpoints de inventario - roles específicos
                .requestMatchers("/api/v1/inventory/**").hasAnyRole("ADMIN", "INVENTORY_MANAGER")

                // Endpoints de ventas - roles específicos
                .requestMatchers("/api/v1/sales/**").hasAnyRole("ADMIN", "CASHIER")

                // Todos los demás endpoints requieren autenticación
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            // Agregar el filtro JWT antes del filtro de autenticación de usuario/contraseña
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 