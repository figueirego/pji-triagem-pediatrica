package com.pji.triagem.base.config.security;

import com.pji.triagem.base.filter.SecurityFilter;
import com.pji.triagem.service.impl.auth.CustomAuthProvider;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final CustomAuthProvider customAuthProvider;
    private final SecurityFilter securityFilter;


    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint)) //tratar erros de autenticação
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .requestMatchers("/auth/login").permitAll()  // Permitir rotas de autenticação sem segurança
                                .requestMatchers("/auth/refresh").permitAll()
                                .requestMatchers("/health").permitAll()
                                .requestMatchers("/actuator/**").permitAll() // Liberar actuator
                                .requestMatchers("/admin/**").permitAll() // Liberar Spring Boot Admin
                                .requestMatchers("/auth/teste").hasRole("USER") // Exigir autenticação para todas as outras rotas
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html","/swagger-resources/**").permitAll()
                                .requestMatchers("/api-docs/**").permitAll()
                                .requestMatchers("/auth/register").permitAll()
                                .requestMatchers("/auth/register/user").permitAll()
                                .anyRequest().authenticated()  // Exigir autenticação para todas as outras rotas
                )
                // é responsável por autenticar as credenciais iniciais do usuário
                .authenticationProvider(customAuthProvider) //validar acessos do usuario
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)  ; //O filtro é responsável por validar tokens JWT em requisições subsequentes, onde o usuário já está autenticado.

        return http.build();
    }


}
