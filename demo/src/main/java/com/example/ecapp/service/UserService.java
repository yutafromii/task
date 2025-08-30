package com.example.ecapp.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import com.example.ecapp.domain.AppUser;
import com.example.ecapp.dto.UserResponse;
import com.example.ecapp.dto.UserUpdateRequest;
import com.example.ecapp.repository.UserRepository;
import com.example.ecapp.service.Base.AbstractBaseService;

@Service
public class UserService extends AbstractBaseService<AppUser, UserUpdateRequest, UserResponse> {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  protected UserRepository getRepository() {
    return userRepository;
  }

  @Override
  protected UserResponse toDto(AppUser entity) {
    return UserResponse.builder()
        .id(entity.getId())
        .name(entity.getName())
        .email(entity.getEmail())
        .phoneNumber(entity.getAddress())
        .address(entity.getAddress())
        .password(entity.getPassword())
        .role(entity.getRole())
        .build();
  }

  @Override
  protected AppUser toEntity(UserUpdateRequest request) {
    AppUser user = new AppUser();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPhoneNumber(request.getPhoneNumber());
    user.setAddress(request.getAddress());
    user.setPassword(request.getPassword());
    user.setRole(request.getRole());
    return user;
  }

  @Override
  protected void updateEntity(AppUser entity, UserUpdateRequest request) {
    entity.setName(request.getName());
    entity.setEmail(request.getEmail());
    entity.setPhoneNumber(request.getPhoneNumber());
    entity.setAddress(request.getAddress());
    entity.setPassword(request.getPassword());
    entity.setRole(request.getRole());
  }

  // 必要があれば、以下に独自メソッド

// ✅ 現在ログイン中のユーザーを取得
public AppUser getLoginUser() {
  String email = SecurityContextHolder.getContext().getAuthentication().getName();
  return userRepository.findByEmail(email)
    .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));
}

  public UserResponse getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName(); // Principalからemail取得
    AppUser user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

    // DTOに変換して返却
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setName(user.getName());
    response.setEmail(user.getEmail());
    response.setPhoneNumber(user.getPhoneNumber());
    response.setAddress(user.getAddress());
    response.setPassword(user.getPassword()); // 不要なら省略

    return response;
  }

  // ✅ /users/me 更新用
  public UserResponse updateCurrentUser(UserUpdateRequest request) {
    AppUser user = getLoginUser();
    user.setName(request.getName());
    user.setAddress(request.getAddress());
    user.setPhoneNumber(request.getPhoneNumber());
    user.setEmail(request.getEmail());

    AppUser updated = userRepository.save(user);
    return toDto(updated);
  }

}
