package com.smartshop.repositories;


import com.smartshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.nom = :nom AND p.deleted = false")
    Optional<Product> findByNomAndActive(@Param("nom") String nom);


    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.nom = :nom AND p.deleted = false")
    boolean existsByNomAndActive(@Param("nom") String nom);


    @Query("SELECT p FROM Product p WHERE p.deleted = false ORDER BY p.nom")
    List<Product> findAllActive();


    @Query("SELECT p FROM Product p WHERE p.deleted = false ORDER BY p.nom")
    Page<Product> findAllActivePaginated(Pageable pageable);


    @Query("SELECT p FROM Product p WHERE p.deleted = true ORDER BY p.nom")
    List<Product> findAllDeleted();


    @Query("SELECT COUNT(p) FROM Product p WHERE p.deleted = false")
    long countActive();


    @Query("SELECT COUNT(p) FROM Product p WHERE p.deleted = true")
    long countDeleted();

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.prix BETWEEN :minPrix AND :maxPrix ORDER BY p.prix")
    List<Product> findByPrixBetweenAndActive(@Param("minPrix") BigDecimal minPrix, @Param("maxPrix") BigDecimal maxPrix);


    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.stock < :seuil ORDER BY p.stock")
    List<Product> findLowStockProducts(@Param("seuil") Integer seuil);


    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.stock <= :seuil ORDER BY p.stock")
    List<Product> findProductsNearOutOfStock(@Param("seuil") Integer seuil);


    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.stock = 0 ORDER BY p.nom")
    List<Product> findOutOfStockProducts();


    @Query("SELECT p FROM Product p WHERE p.deleted = false AND LOWER(p.nom) LIKE LOWER(CONCAT('%', :terme, '%')) ORDER BY p.nom")
    List<Product> searchByNom(@Param("terme") String terme);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND LOWER(p.nom) LIKE LOWER(CONCAT('%', :terme, '%')) ORDER BY p.nom")
    Page<Product> searchByNomPaginated(@Param("terme") String terme, Pageable pageable);


    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.prix BETWEEN :minPrix AND :maxPrix ORDER BY p.prix")
    Page<Product> findByPrixBetweenAndActivePaginated(@Param("minPrix") BigDecimal minPrix, @Param("maxPrix") BigDecimal maxPrix, Pageable pageable);
}
