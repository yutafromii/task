package com.example.ecapp.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;

  public static <T> ApiResponse<T> success(T data){
    return new ApiResponse<>(true, "OK", data);
  }
  public static <T> ApiResponse<T> error(String message){
    return new ApiResponse<>(false, message, null);
  }
}
