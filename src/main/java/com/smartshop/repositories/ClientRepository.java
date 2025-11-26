package com.smartshop.repositories;

import com.smartshop.entity.Client;
import com.smartshop.entity.CustomerTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {


    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Client> findByIsActiveTrue();


    Page<Client> findByIsActiveTrue(Pageable pageable);


    List<Client> findByCustomerTier(CustomerTier customerTier);

    Page<Client> findByCustomerTier(CustomerTier customerTier, Pageable pageable);


    List<Client> findByCustomerTierAndIsActiveTrue(CustomerTier customerTier);


    long countByCustomerTier(CustomerTier customerTier);

    Optional<Client> findByUserId(Long userId);


    boolean existsByUserId(Long userId);

    @Query("SELECT c FROM Client c WHERE c.totalSpent >= :amount AND c.isActive = true")
    List<Client> findClientsWithMinSpent(@Param("amount") java.math.BigDecimal amount);

    @Query("SELECT c FROM Client c WHERE c.totalOrders >= :orders AND c.isActive = true")
    List<Client> findClientsWithMinOrders(@Param("orders") Integer orders);


    long countByIsActiveTrue();
}