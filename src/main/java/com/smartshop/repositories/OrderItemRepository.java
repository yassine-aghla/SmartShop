package com.smartshop.repositories;

import com.smartshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);
    List<OrderItem> findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    long countByProductId(Long productId);

    @Query("SELECT COALESCE(SUM(oi.quantite), 0) FROM OrderItem oi " +
            "JOIN oi.order o WHERE oi.product.id = :productId AND o.statut = 'CONFIRMED'")
    Integer getTotalQuantitySoldByProduct(@Param("productId") Long productId);
    @Query("SELECT COALESCE(SUM(oi.totalLigne), 0) FROM OrderItem oi " +
            "JOIN oi.order o WHERE oi.product.id = :productId AND o.statut = 'CONFIRMED'")
    BigDecimal getTotalRevenueByProduct(@Param("productId") Long productId);
    @Query("SELECT oi.product.id, oi.product.nom, SUM(oi.quantite) as totalQty " +
            "FROM OrderItem oi JOIN oi.order o WHERE o.statut = 'CONFIRMED' " +
            "GROUP BY oi.product.id, oi.product.nom ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts();
}