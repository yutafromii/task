package com.example.ecapp.controller.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecapp.common.ApiResponse;
import com.example.ecapp.dto.AdminOrderResponse;
import com.example.ecapp.service.AdminOrderService;

import lombok.Data;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {
  private final AdminOrderService adminOrderService;
  public AdminOrderController(AdminOrderService adminOrderService){ this.adminOrderService = adminOrderService; }

  @GetMapping(params = {"page"})
  public ApiResponse<Page<AdminOrderResponse>> page(
    @RequestParam(name="status", required=false) String status,
    @RequestParam(name="q", required=false) String q,
    @RequestParam(name="from", required=false) String from,
    @RequestParam(name="to", required=false) String to,
    Pageable pageable
  ){
    LocalDateTime fromDt = from == null || from.isBlank() ? null : LocalDate.parse(from).atStartOfDay();
    LocalDateTime toDt = to == null || to.isBlank() ? null : LocalDate.parse(to).plusDays(1).atStartOfDay();
    return ApiResponse.success(adminOrderService.page(status, q, fromDt, toDt, pageable));
  }

  @GetMapping
  public ApiResponse<List<AdminOrderResponse>> list(
    @RequestParam(name="status", required=false) String status,
    @RequestParam(name="q", required=false) String q,
    @RequestParam(name="from", required=false) String from,
    @RequestParam(name="to", required=false) String to
  ){
    LocalDateTime fromDt = from == null || from.isBlank() ? null : LocalDate.parse(from).atStartOfDay();
    LocalDateTime toDt = to == null || to.isBlank() ? null : LocalDate.parse(to).plusDays(1).atStartOfDay();
    return ApiResponse.success(adminOrderService.list(status, q, fromDt, toDt));
  }

  @GetMapping("/{id}")
  public ApiResponse<AdminOrderResponse> get(@PathVariable Long id){
    return ApiResponse.success(adminOrderService.get(id));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusBody body){
    try {
      AdminOrderResponse updated = adminOrderService.updateStatus(id, body.getStatus());
      return ResponseEntity.ok(ApiResponse.success(updated));
    } catch (IllegalArgumentException e){
      return ResponseEntity.status(400).body(java.util.Map.of("message", e.getMessage()));
    } catch (IllegalStateException e){
      return ResponseEntity.status(409).body(java.util.Map.of("message", e.getMessage()));
    }
  }

  @Data
  public static class UpdateStatusBody { private String status; }
}
