package com.example.ecapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecapp.controller.Base.BaseController;
import com.example.ecapp.common.ApiResponse;
import com.example.ecapp.domain.AppUser;
import com.example.ecapp.dto.UserResponse;
import com.example.ecapp.dto.UserUpdateRequest;
import com.example.ecapp.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController extends BaseController<AppUser, UserUpdateRequest, UserResponse, UserService> {

  private final UserService userService;

  public UserController(UserService userService) {
    super(userService);
    this.userService = userService;
  }

  @GetMapping("/me")
  public ApiResponse<UserResponse> getCurrentUser() {
    return ApiResponse.success(userService.getCurrentUser());
  }

  @PutMapping("/me")
  public ApiResponse<UserResponse> updateCurrentUser(@RequestBody UserUpdateRequest request) {
    return ApiResponse.success(userService.updateCurrentUser(request));
  }

  // 独自処理が必要であれば以下に記述。

}
