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
    return DeliveryAddressResponse.builder()
        .id(entity.getId())
        .name(entity.getName())
        .furigana(entity.getFurigana())
        .postalCode(entity.getPostalCode())
        .address(entity.getAddress())
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
    deliveryAddress.setPhone(request.getPhone());
    deliveryAddress.setEmail(request.getEmail());
    // ユーザーを紐づける
    deliveryAddress.setUser(userService.getLoginUser());

    return deliveryAddress;
  }

  @Override
  protected void updateEntity(DeliveryAddress entity, DeliveryAddressRequest request) {
    entity.setName(request.getName());
    entity.setFurigana(request.getFurigana());
    entity.setPostalCode(request.getPostalCode());
    entity.setAddress(request.getAddress());
    entity.setPhone(request.getPhone());
    entity.setEmail(request.getEmail());
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