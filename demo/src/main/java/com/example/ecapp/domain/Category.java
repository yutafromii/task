package com.example.ecapp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
  JACKET,
  PANTS,
  SHIRT;

  @JsonCreator
  public static Category from(String value) {
    if (value == null) return null;
    return Category.valueOf(value.trim().toUpperCase());
  }

  @JsonValue
  public String toValue() {
    return name().toLowerCase();
  }
}

