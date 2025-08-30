package com.example.ecapp.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.example.ecapp.controller.Base.BaseController;
import com.example.ecapp.common.ApiResponse;
import com.example.ecapp.domain.DeliveryAddress;
import com.example.ecapp.dto.DeliveryAddressRequest;
import com.example.ecapp.dto.DeliveryAddressResponse;
import com.example.ecapp.service.DeliveryAddressService;

@RestController
@RequestMapping("/delivery-addresses") // ✅ 複数形に変更
public class DeliveryAddressController extends BaseController<DeliveryAddress, DeliveryAddressRequest, DeliveryAddressResponse, DeliveryAddressService> {

  private final DeliveryAddressService deliveryAddressService;

  public DeliveryAddressController(DeliveryAddressService deliveryAddressService) {
    super(deliveryAddressService);
    this.deliveryAddressService = deliveryAddressService;
  }

  // ✅ ログインユーザーの配送先一覧（任意で追加）
  @GetMapping("/me")
  public ApiResponse<List<DeliveryAddressResponse>> getMyDeliveryAddresses() {
    return ApiResponse.success(deliveryAddressService.getAllByLoginUser());
  }
}
