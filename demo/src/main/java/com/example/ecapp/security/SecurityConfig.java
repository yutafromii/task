package com.example.ecapp.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// Removed unused WebMvc imports; CORS configured via CorsConfigurationSource

import com.example.ecapp.security.JwtAuthenticationFilter;
import com.example.ecapp.security.JwtUtil;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  @Value("${app.cors.allowed-origins:http://localhost:3000}")
  private String allowedOrigins;

  public SecurityConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ 明示的に設定
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/login", "/logout", "/register", "/error").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/users/me").authenticated()
            .requestMatchers("/products/**").permitAll()
            .requestMatchers("/delivery-addresses/**").permitAll()
            .requestMatchers("/carts/**").permitAll()
            .requestMatchers("/cart-items/**").permitAll()
            .requestMatchers("/orders/**").permitAll()
            .requestMatchers("/admin/**").permitAll()
            .requestMatchers("/users/**").permitAll()
            .anyRequest().authenticated())
        .logout(logout -> logout.disable())
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtUtil, userDetailsService),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  // ✅ CorsConfigurationSource Bean（Spring Security 用）
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    // 環境変数/プロパティから許可オリジン（カンマ区切り）。ワイルドカード(https://*.example.com)対応
    List<String> entries = Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();

    List<String> exact = entries.stream().filter(s -> !s.contains("*")).toList();
    List<String> patterns = entries.stream().filter(s -> s.contains("*")).toList();

    if (!exact.isEmpty()) config.setAllowedOrigins(exact);
    if (!patterns.isEmpty()) config.setAllowedOriginPatterns(patterns);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true); // ✅ Cookie を許可
    config.setMaxAge(3600L); // キャッシュ時間（任意）

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
