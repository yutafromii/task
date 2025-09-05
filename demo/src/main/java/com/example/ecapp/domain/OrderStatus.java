package com.example.ecapp.domain;

public enum OrderStatus {
  PENDING("受注"),
  PAID("支払い確認"),
  PREPARING("配送準備中"),
  SHIPPED("発送済み"),
  CANCELED("キャンセル");

  private final String labelJa;
  OrderStatus(String labelJa){ this.labelJa = labelJa; }
  public String label(){ return labelJa; }

  public static OrderStatus fromLabel(String label){
    if (label == null) return null;
    String trimmed = label.trim();
    for (OrderStatus s : values()){
      if (s.labelJa.equals(trimmed)) return s;
    }
    // allow English code as well
    try { return OrderStatus.valueOf(trimmed.toUpperCase()); } catch(Exception ignore){}
    throw new IllegalArgumentException("invalid status: " + label);
  }
}

