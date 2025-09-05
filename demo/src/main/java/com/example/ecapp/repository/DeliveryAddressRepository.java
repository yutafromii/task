// src/main/java/com/example/ecapp/repository/DeliveryAddressRepository.java
package com.example.ecapp.repository;

import com.example.ecapp.domain.DeliveryAddress;
import com.example.ecapp.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    // 既存
    List<DeliveryAddress> findByUser(AppUser user);

    // 追加（使い勝手向上）
    List<DeliveryAddress> findByUserId(Long userId);
    boolean existsByUser(AppUser user);
    boolean existsByUserId(Long userId);

    // 「本人の住所か」を検証したいときに使える（将来 shippingAddressId を受け取る設計用）
    Optional<DeliveryAddress> findByIdAndUserId(Long id, Long userId);
}
