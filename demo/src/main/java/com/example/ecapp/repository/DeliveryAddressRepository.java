package com.example.ecapp.repository;


import com.example.ecapp.domain.DeliveryAddress;
import com.example.ecapp.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    // 特定ユーザーのお届け先一覧を取得
    List<DeliveryAddress> findByUser(AppUser user);
}