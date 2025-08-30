package com.example.ecapp.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.*;
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

      ResponseCookie cookie = ResponseCookie.from("token", token)
          .httpOnly(true)
          .secure(false)
          .path("/")
          .maxAge(Duration.ofHours(1))
          .sameSite("Strict")
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
    Cookie cookie = new Cookie("token", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0);

    response.addCookie(cookie);
    return ResponseEntity.ok().body("ログアウトしました");
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
