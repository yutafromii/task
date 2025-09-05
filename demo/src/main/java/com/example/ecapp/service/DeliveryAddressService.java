package com.example.ecapp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ecapp.domain.DeliveryAddress;
import com.example.ecapp.dto.DeliveryAddressRequest;
import com.example.ecapp.dto.DeliveryAddressResponse;
import com.example.ecapp.repository.DeliveryAddressRepository;
import com.example.ecapp.service.Base.AbstractBaseService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DeliveryAddressService
    extends AbstractBaseService<DeliveryAddress, DeliveryAddressRequest, DeliveryAddressResponse> {

  private final DeliveryAddressRepository deliveryAddressRepository; // ✅ 追加
  private final UserService userService;

  public DeliveryAddressService(
      DeliveryAddressRepository deliveryAddressRepository,
      UserService userService) {
        
    this.deliveryAddressRepository = deliveryAddressRepository;
    this.userService = userService;
  }

  @Override
  protected DeliveryAddressRepository getRepository() {
    return deliveryAddressRepository;
  }

  @Override
  protected DeliveryAddressResponse toDto(DeliveryAddress entity) {
    // 旧addressが空なら分割住所から合成して返却
    String address = entity.getAddress();
    if (address == null || address.isBlank()) {
      StringBuilder sb = new StringBuilder();
      if (entity.getPrefecture() != null && !entity.getPrefecture().isBlank()) sb.append(entity.getPrefecture());
      if (entity.getCity() != null && !entity.getCity().isBlank()) sb.append(sb.length()>0?" ":"").append(entity.getCity());
      if (entity.getAddressLine1() != null && !entity.getAddressLine1().isBlank()) sb.append(sb.length()>0?" ":"").append(entity.getAddressLine1());
      if (entity.getAddressLine2() != null && !entity.getAddressLine2().isBlank()) sb.append(sb.length()>0?" ":"").append(entity.getAddressLine2());
      address = sb.length() == 0 ? null : sb.toString();
    }

    return DeliveryAddressResponse.builder()
        .id(entity.getId())
        .name(entity.getName())
        .furigana(entity.getFurigana())
        .postalCode(entity.getPostalCode())
        .address(address)
        .prefecture(entity.getPrefecture())
        .city(entity.getCity())
        .addressLine1(entity.getAddressLine1())
        .addressLine2(entity.getAddressLine2())
        .phone(entity.getPhone())
        .email(entity.getEmail())
        .build();
  }

  @Override
  protected DeliveryAddress toEntity(DeliveryAddressRequest request) {
    DeliveryAddress deliveryAddress = new DeliveryAddress();
    deliveryAddress.setName(request.getName());
    deliveryAddress.setFurigana(request.getFurigana());
    deliveryAddress.setPostalCode(request.getPostalCode());
    deliveryAddress.setAddress(request.getAddress());
    deliveryAddress.setPrefecture(request.getPrefecture());
    deliveryAddress.setCity(request.getCity());
    deliveryAddress.setAddressLine1(request.getAddressLine1());
    deliveryAddress.setAddressLine2(request.getAddressLine2());
    // address（旧）が未指定か空のときは分割から合成して埋める（NOT NULL 対応）
    if (deliveryAddress.getAddress() == null || deliveryAddress.getAddress().isBlank()) {
      String composed = composeAddress(request.getPrefecture(), request.getCity(), request.getAddressLine1(), request.getAddressLine2());
      if (composed == null) {
        throw new IllegalArgumentException("住所が未入力です");
      }
      deliveryAddress.setAddress(composed);
    }
    deliveryAddress.setPhone(request.getPhone());
    deliveryAddress.setEmail(request.getEmail());
    // ユーザーを紐づける
    deliveryAddress.setUser(userService.getLoginUser());

    return deliveryAddress;
  }

  @Override
  protected void updateEntity(DeliveryAddress entity, DeliveryAddressRequest request) {
    if (request.getName() != null) entity.setName(request.getName());
    if (request.getFurigana() != null) entity.setFurigana(request.getFurigana());
    if (request.getPostalCode() != null) entity.setPostalCode(request.getPostalCode());
    if (request.getPhone() != null) entity.setPhone(request.getPhone());
    if (request.getEmail() != null) entity.setEmail(request.getEmail());

    // 旧addressは、明示的に送られてきた場合のみ上書き
    boolean addressExplicitlyUpdated = false;
    if (request.getAddress() != null) {
      entity.setAddress(request.getAddress());
      addressExplicitlyUpdated = true;
    }
    // 分割の部分更新
    boolean anySplitUpdated = false;
    if (request.getPrefecture() != null) { entity.setPrefecture(request.getPrefecture()); anySplitUpdated = true; }
    if (request.getCity() != null) { entity.setCity(request.getCity()); anySplitUpdated = true; }
    if (request.getAddressLine1() != null) { entity.setAddressLine1(request.getAddressLine1()); anySplitUpdated = true; }
    if (request.getAddressLine2() != null) { entity.setAddressLine2(request.getAddressLine2()); anySplitUpdated = true; }

    // 分割が更新され、旧addressが今回明示されていない場合は合成して NOT NULL を満たす
    if (!addressExplicitlyUpdated && anySplitUpdated) {
      String composed = composeAddress(entity.getPrefecture(), entity.getCity(), entity.getAddressLine1(), entity.getAddressLine2());
      if (composed == null) {
        // すべて空になってしまう変更ならエラー
        throw new IllegalArgumentException("住所が未入力です");
      }
      entity.setAddress(composed);
    }
  }

  private String composeAddress(String prefecture, String city, String addressLine1, String addressLine2) {
    StringBuilder sb = new StringBuilder();
    if (prefecture != null && !prefecture.isBlank()) sb.append(prefecture.trim());
    if (city != null && !city.isBlank()) sb.append(sb.length()>0?" ":"").append(city.trim());
    if (addressLine1 != null && !addressLine1.isBlank()) sb.append(sb.length()>0?" ":"").append(addressLine1.trim());
    if (addressLine2 != null && !addressLine2.isBlank()) sb.append(sb.length()>0?" ":"").append(addressLine2.trim());
    return sb.length() == 0 ? null : sb.toString();
  }

  public List<DeliveryAddressResponse> getAllByLoginUser() {
    // ログインユーザーを取得
    var loginUser = userService.getLoginUser();

    // ユーザーに紐づく配送先を取得し、DTOに変換して返却
    return deliveryAddressRepository.findByUser(loginUser).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }
}
