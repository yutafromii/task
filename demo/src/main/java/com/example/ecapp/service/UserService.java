package com.example.ecapp.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecapp.domain.AppUser;
import com.example.ecapp.dto.UserResponse;
import com.example.ecapp.dto.UserUpdateRequest;
import com.example.ecapp.repository.UserRepository;
import com.example.ecapp.repository.OrderRepository;            // ★追加
import com.example.ecapp.repository.CartRepository;             // ★追加（クラス名は実装に合わせて）
import com.example.ecapp.repository.DeliveryAddressRepository;  // ★追加
import com.example.ecapp.service.Base.AbstractBaseService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService extends AbstractBaseService<AppUser, UserUpdateRequest, UserResponse> {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // ★追加リポジトリ
  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final DeliveryAddressRepository deliveryAddressRepository;

  // ★コンストラクタに追加
  public UserService(UserRepository userRepository,
                     PasswordEncoder passwordEncoder,
                     OrderRepository orderRepository,
                     CartRepository cartRepository,
                     DeliveryAddressRepository deliveryAddressRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.orderRepository = orderRepository;
    this.cartRepository = cartRepository;
    this.deliveryAddressRepository = deliveryAddressRepository;
  }

  @Override
  protected UserRepository getRepository() {
    return userRepository;
  }

  @Override
  protected UserResponse toDto(AppUser entity) {
    // 後方互換: address が空なら分割住所から合成
    String legacyAddress = entity.getAddress();
    if (legacyAddress == null || legacyAddress.isBlank()) {
      StringBuilder sb = new StringBuilder();
      if (entity.getPrefecture() != null) sb.append(entity.getPrefecture());
      if (entity.getCity() != null) sb.append(sb.length()>0?" ":"").append(entity.getCity());
      if (entity.getAddressLine1() != null) sb.append(sb.length()>0?" ":"").append(entity.getAddressLine1());
      if (entity.getAddressLine2() != null) sb.append(sb.length()>0?" ":"").append(entity.getAddressLine2());
      legacyAddress = sb.length() == 0 ? null : sb.toString();
    }

    return UserResponse.builder()
        .id(entity.getId())
        .name(entity.getName())
        .email(entity.getEmail())
        .phoneNumber(entity.getPhoneNumber())
        .address(legacyAddress)
        .postalCode(entity.getPostalCode())
        .prefecture(entity.getPrefecture())
        .city(entity.getCity())
        .addressLine1(entity.getAddressLine1())
        .addressLine2(entity.getAddressLine2())
        // .password(entity.getPassword())  // ★ レスポンスに含めない
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
    user.setPostalCode(request.getPostalCode());
    user.setPrefecture(request.getPrefecture());
    user.setCity(request.getCity());
    user.setAddressLine1(request.getAddressLine1());
    user.setAddressLine2(request.getAddressLine2());
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }
    user.setRole(request.getRole());
    return user;
  }

  @Override
  protected void updateEntity(AppUser entity, UserUpdateRequest request) {
    if (request.getName() != null) entity.setName(request.getName());
    if (request.getEmail() != null) entity.setEmail(request.getEmail());
    if (request.getPhoneNumber() != null) entity.setPhoneNumber(request.getPhoneNumber());
    if (request.getAddress() != null) entity.setAddress(request.getAddress());
    if (request.getPostalCode() != null) entity.setPostalCode(request.getPostalCode());
    if (request.getPrefecture() != null) entity.setPrefecture(request.getPrefecture());
    if (request.getCity() != null) entity.setCity(request.getCity());
    if (request.getAddressLine1() != null) entity.setAddressLine1(request.getAddressLine1());
    if (request.getAddressLine2() != null) entity.setAddressLine2(request.getAddressLine2());
    if (request.getRole() != null) entity.setRole(request.getRole());
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      entity.setPassword(passwordEncoder.encode(request.getPassword()));
    }
  }

  /** ✅ 現在ログイン中のユーザーを取得 */
  public AppUser getLoginUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
      .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));
  }

  private AppUser getLoginUserOrNull() {
    try { return getLoginUser(); } catch (Exception e) { return null; }
  }

  /** ✅ /users/me 取得 */
  public UserResponse getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    AppUser user = userRepository.findByEmail(email)
      .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
    return toDto(user); // ★password 等は返さない
  }

  /** ✅ /users/me 更新 */
  public UserResponse updateCurrentUser(UserUpdateRequest request) {
    AppUser user = getLoginUser();
    if (request.getName() != null) user.setName(request.getName());
    if (request.getAddress() != null) user.setAddress(request.getAddress());
    if (request.getPostalCode() != null) user.setPostalCode(request.getPostalCode());
    if (request.getPrefecture() != null) user.setPrefecture(request.getPrefecture());
    if (request.getCity() != null) user.setCity(request.getCity());
    if (request.getAddressLine1() != null) user.setAddressLine1(request.getAddressLine1());
    if (request.getAddressLine2() != null) user.setAddressLine2(request.getAddressLine2());
    if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
    if (request.getEmail() != null) user.setEmail(request.getEmail());
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }
    AppUser updated = userRepository.save(user);
    return toDto(updated);
  }

  // ======== 管理者用メソッド ========
// ========= 管理者用 ここから =========

  /** 管理者: ユーザー一覧（新しい順にしたい場合は createdAt があれば置き換え） */
  public List<UserResponse> getAllUsersForAdmin() {
    var sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id");
    return userRepository.findAll(sort).stream().map(this::toDto).toList();
  }

  /** 管理者: ユーザー一覧（ページング） */
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public org.springframework.data.domain.Page<UserResponse> getUsersPageForAdmin(org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.domain.Pageable effective = pageable;
    if (effective == null) {
      effective = org.springframework.data.domain.PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
    }
    return userRepository.findAll(effective).map(this::toDto);
  }

  /** 管理者: ユーザー単体 */
  public UserResponse getByIdForAdmin(Long id) {
    AppUser user = userRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
    return toDto(user);
  }

  /** 管理者: ユーザー削除（参照チェック付き） */
  @Transactional
  public void deleteUserByAdmin(Long id) {
    // 自分自身の削除を禁止（不要なら消してください）
    AppUser current = getLoginUserOrNull();
    if (current != null && current.getId().equals(id)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "自身のアカウントは削除できません");
    }

    // 存在チェック
    AppUser target = userRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

    // 参照チェック：注文（履歴 or 現在）
    boolean hasOrderHistory = !orderRepository.findByUserIdOrderByOrderedAtDesc(id).isEmpty();
    boolean hasCurrentOrder = orderRepository.findByUserId(id).isPresent();
    if (hasOrderHistory || hasCurrentOrder) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "注文の参照があるため削除できません");
    }

    // 参照チェック：カート
    boolean hasCart = cartRepository.findByUserId(id).isPresent();
    if (hasCart) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "カートが存在するため削除できません");
    }

    // 住所を先に削除（外部キー制約対策）
    var addresses = deliveryAddressRepository.findByUserId(id);
    if (addresses != null && !addresses.isEmpty()) {
      try {
        deliveryAddressRepository.deleteAllInBatch(addresses);
      } catch (Exception ex) {
        // 環境によって deleteAllInBatch が無ければ deleteAll でOK
        deliveryAddressRepository.deleteAll(addresses);
      }
    }

    // ユーザー削除
    userRepository.delete(target);
  }
  public UserResponse updateByAdmin(Long id, UserUpdateRequest req) {
    AppUser u = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません: " + id));
  
    if (req.getName() != null) u.setName(req.getName());
    if (req.getEmail() != null) u.setEmail(req.getEmail());
    if (req.getPhoneNumber() != null) u.setPhoneNumber(req.getPhoneNumber());
    if (req.getPostalCode() != null) u.setPostalCode(req.getPostalCode());
    if (req.getPrefecture() != null) u.setPrefecture(req.getPrefecture());
    if (req.getCity() != null) u.setCity(req.getCity());
    if (req.getAddressLine1() != null) u.setAddressLine1(req.getAddressLine1());
    if (req.getAddressLine2() != null) u.setAddressLine2(req.getAddressLine2());
    if (req.getAddress() != null) u.setAddress(req.getAddress());
  
    // role 更新（ADMIN だけが到達できる前提）
    if (req.getRole() != null) u.setRole(req.getRole());
  
    // パスワードは空/空白の場合は変更しない
    if (req.getPassword() != null && !req.getPassword().isBlank()) {
      u.setPassword(passwordEncoder.encode(req.getPassword()));
    }
  
    AppUser saved = userRepository.save(u);
    return toDto(saved); // password を含めない DTO にしておく！
  }
  
}
