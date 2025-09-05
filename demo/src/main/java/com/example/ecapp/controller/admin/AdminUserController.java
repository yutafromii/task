// src/main/java/com/example/ecapp/controller/admin/AdminUserController.java
package com.example.ecapp.controller.admin;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.ecapp.common.ApiResponse;
import com.example.ecapp.dto.UserResponse;
import com.example.ecapp.dto.UserUpdateRequest;
import com.example.ecapp.service.UserService;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')") // ★ ADMIN 限定
public class AdminUserController {

  private final UserService userService;

  public AdminUserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ApiResponse<List<UserResponse>> list() {
    return ApiResponse.success(userService.getAllUsersForAdmin());
  }

  @GetMapping(params = {"page"})
  public ApiResponse<Page<UserResponse>> page(Pageable pageable) {
    return ApiResponse.success(userService.getUsersPageForAdmin(pageable));
  }

  @GetMapping("/{id}")
  public ApiResponse<UserResponse> get(@PathVariable Long id) {
    return ApiResponse.success(userService.getByIdForAdmin(id));
  }

  @PutMapping("/{id}")
  public ApiResponse<UserResponse> update(@PathVariable Long id, @RequestBody UserUpdateRequest req) {
    return ApiResponse.success(userService.updateByAdmin(id, req));
  }
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    userService.deleteUserByAdmin(id); // 参照チェック込み
  }
}
