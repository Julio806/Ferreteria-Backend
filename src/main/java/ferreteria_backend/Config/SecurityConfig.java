package ferreteria_backend.Config;

import ferreteria_backend.Security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // ==========================================
    // PASSWORD ENCODER
    // ==========================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ==========================================
    // SECURITY
    // ==========================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // ==========================================
                // CSRF
                // ==========================================

                .csrf(csrf -> csrf.disable())

                // ==========================================
                // CORS
                // ==========================================

                .cors(cors -> {})

                // ==========================================
                // SESIONES
                // ==========================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // ==========================================
                // AUTORIZACIONES
                // ==========================================

                .authorizeHttpRequests(auth -> auth

                        // ------------------------------------------
                        // LOGIN PÚBLICO
                        // ------------------------------------------

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // ------------------------------------------
                        // ADMINISTRADOR
                        // ------------------------------------------

                        .requestMatchers(
                                "/api/usuarios/**",
                                "/api/roles/**"
                        ).hasRole("ADMIN")

                        // ------------------------------------------
                        // ADMINISTRADOR Y VENDEDOR
                        // ------------------------------------------

                        .requestMatchers(
                                "/api/productos/**",
                                "/api/categorias/**",
                                "/api/ventas/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "VENDEDOR"
                        )

                        // ------------------------------------------
                        // COMPRAS SOLO ADMIN
                        // ------------------------------------------

                        .requestMatchers(
                                "/api/compras/**"
                        ).hasRole("ADMIN")

                        // ------------------------------------------
                        // DASHBOARD
                        // ------------------------------------------

                        .requestMatchers(
                                "/api/dashboard/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "VENDEDOR"
                        )

                        // ------------------------------------------
                        // TODO LO DEMÁS
                        // ------------------------------------------

                        .anyRequest().authenticated()
                )

                // ==========================================
                // DESACTIVAR LOGIN HTML
                // ==========================================

                .formLogin(form -> form.disable())

                // ==========================================
                // DESACTIVAR BASIC AUTH
                // ==========================================

                .httpBasic(basic -> basic.disable())

                // ==========================================
                // JWT FILTER
                // ==========================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
