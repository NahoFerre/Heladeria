package com.heladeria.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/usuarios/yo").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/productos", "/api/productos/*").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/api/productos/**").hasRole("ADMIN")
                        .requestMatchers("/api/insumos/**").hasRole("ADMIN")
                        .requestMatchers("/api/compras/**").hasRole("ADMIN")
                        .requestMatchers("/api/mermas/**").hasRole("ADMIN")
                        .requestMatchers("/api/gastos/**").hasRole("ADMIN")
                        .requestMatchers("/api/promociones/vigentes").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/api/promociones/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/ventas/*/anular").hasRole("ADMIN")
                        .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/api/cajas/actual", "/api/cajas/abrir", "/api/cajas/cerrar",
                                "/api/cajas/movimientos", "/api/cajas/movimientos/actual").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/api/cajas/**").hasRole("ADMIN")
                        .requestMatchers("/api/reportes/resumen-hoy", "/api/reportes/stock-bajo").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/api/reportes/**").hasRole("ADMIN")
                        .requestMatchers("/api/auditoria/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                // Sin este entryPoint, Spring Security manda "WWW-Authenticate: Basic" en cada 401,
                // y eso hace que el navegador muestre su propia ventana nativa de usuario/contraseña
                // en vez de dejar que el login de Angular (o el logout automático ante una sesión
                // inválida) maneje la respuesta — nunca llegaba a verse el mensaje de la app.
                .httpBasic(basic -> basic.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"error\":\"No autenticado\"}");
                }));
        return http.build();
    }

    // Angular corre en :4200 (local o vía Tailscale) y el backend en :8080 -> son orígenes distintos para el navegador
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4200", "http://*.*.*.*:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
