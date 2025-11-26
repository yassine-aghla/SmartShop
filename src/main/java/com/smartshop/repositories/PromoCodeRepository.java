package com.smartshop.repositories;

import com.smartshop.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCode(String code);
    boolean existsByCode(String code);
    List<PromoCode> findByActiveTrue();
    List<PromoCode> findByActiveFalse();
    long countByActiveTrue();
    long countByActiveFalse();
    List<PromoCode> findByDiscountPercentage(Integer discountPercentage);
    @Query("SELECT p FROM PromoCode p WHERE p.discountPercentage >= :percentage")
    List<PromoCode> findByDiscountPercentageGreaterThanOrEqual(@Param("percentage") Integer percentage);
    @Query("SELECT p FROM PromoCode p WHERE p.active = true AND p.discountPercentage = :percentage")
    List<PromoCode> findActiveByDiscountPercentage(@Param("percentage") Integer percentage);
}
