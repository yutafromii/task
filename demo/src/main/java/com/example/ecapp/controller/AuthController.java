package com.example.ecapp.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.ecapp.domain.AppUser;
import com.example.ecapp.dto.LoginRequest;
import com.example.ecapp.dto.RegisterRequest;
import com.example.ecapp.repository.UserRepository;
import com.example.ecapp.security.JwtUtil;

import java.time.Duration;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class AuthController {

  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;
  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.auth.cookie.name:token}")
  private String cookieName;

  @Value("${app.auth.cookie.secure:false}")
  private boolean cookieSecure;

  @Value("${app.auth.cookie.same-site:Strict}")
  private String cookieSameSite;

  @Value("${app.auth.cookie.max-age-seconds:3600}")
  private long cookieMaxAgeSeconds;

  public AuthController(JwtUtil jwtUtil, AuthenticationManager authenticationManager,
      UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.jwtUtil = jwtUtil;
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getEmail(), loginRequest.getPassword()));

      String token = jwtUtil.generateToken(authentication.getName());

      ResponseCookie cookie = ResponseCookie.from(cookieName, token)
          .httpOnly(true)
          .secure(cookieSecure)
          .path("/")
          .maxAge(Duration.ofSeconds(cookieMaxAgeSeconds))
          .sameSite(cookieSameSite)
          .build();

      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, cookie.toString())
          .body(Collections.singletonMap("token", token));
    } catch (Exception e) {
      logger.warn("認証失敗: {}", loginRequest.getEmail());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ログインに失敗しました");
    }
  }

  @GetMapping("/mypage")
  public ResponseEntity<?> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()
        || authentication.getPrincipal().equals("anonymousUser")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未認証です");
    }

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String email = userDetails.getUsername(); // ここは email が返る

    return ResponseEntity.ok(Collections.singletonMap("email", email));
  }

  @PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletResponse response) {
  // セッション/コンテキストも明示的にクリア
  org.springframework.security.core.context.SecurityContextHolder.clearContext();

  ResponseCookie cookie = ResponseCookie.from(cookieName, "")
      .httpOnly(true)
      .secure(cookieSecure)
      .path("/")
      .maxAge(0)                 // ← これで即時失効
      .sameSite(cookieSameSite)
      .build();

  return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, cookie.toString())
      .body("ログアウトしました");
}

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      return ResponseEntity
          .status(HttpStatus.CONFLICT)
          .body(Collections.singletonMap("error", "すでに同じメールアドレスが存在します"));
    }

    AppUser newUser = new AppUser();
    // newUser.setUsername(request.getUsername()); // 不要
    newUser.setPassword(passwordEncoder.encode(request.getPassword()));
    newUser.setName(request.getName());
    newUser.setEmail(request.getEmail());
    newUser.setRole("USER");

    userRepository.save(newUser);

    return ResponseEntity.ok(Collections.singletonMap("message", "ユーザー登録に成功しました"));
  }
}
