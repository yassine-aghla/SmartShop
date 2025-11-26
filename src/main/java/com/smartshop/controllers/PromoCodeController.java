package com.smartshop.controllers;

import com.smartshop.dtos.PromoCodeDto;
import com.smartshop.services.PromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
@Slf4j
public class PromoCodeController {

    private final PromoCodeService promoCodeService;


    @GetMapping
    public ResponseEntity<List<PromoCodeDto>> getAllPromoCodes() {
        log.info("GET /api/promo-codes - Récupération de tous les codes promo");
        List<PromoCodeDto> promoCodes = promoCodeService.getAllPromoCodes();
        return ResponseEntity.ok(promoCodes);
    }


    @GetMapping("/active")
    public ResponseEntity<List<PromoCodeDto>> getAllActivePromoCodes() {
        log.info("GET /api/promo-codes/active - Récupération des codes actifs");
        List<PromoCodeDto> promoCodes = promoCodeService.getAllActivePromoCodes();
        return ResponseEntity.ok(promoCodes);
    }


    @GetMapping("/inactive")
    public ResponseEntity<List<PromoCodeDto>> getAllInactivePromoCodes() {
        log.info("GET /api/promo-codes/inactive - Récupération des codes inactifs");
        List<PromoCodeDto> promoCodes = promoCodeService.getAllInactivePromoCodes();
        return ResponseEntity.ok(promoCodes);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeDto> getPromoCodeById(@PathVariable Long id) {
        log.info("GET /api/promo-codes/{} - Récupération du code", id);
        PromoCodeDto promoCode = promoCodeService.getPromoCodeById(id);
        return ResponseEntity.ok(promoCode);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PromoCodeDto> getPromoCodeByCode(@PathVariable String code) {
        log.info("GET /api/promo-codes/code/{} - Récupération du code", code);
        PromoCodeDto promoCode = promoCodeService.getPromoCodeByCode(code);
        return ResponseEntity.ok(promoCode);
    }


    @GetMapping("/percentage/{percentage}")
    public ResponseEntity<List<PromoCodeDto>> getPromoCodesByPercentage(@PathVariable Integer percentage) {
        log.info("GET /api/promo-codes/percentage/{} - Récupération des codes par %", percentage);
        List<PromoCodeDto> promoCodes = promoCodeService.getPromoCodesByPercentage(percentage);
        return ResponseEntity.ok(promoCodes);
    }


    @GetMapping("/exists/code/{code}")
    public ResponseEntity<Boolean> existsByCode(@PathVariable String code) {
        log.info("GET /api/promo-codes/exists/code/{} - Vérification de l'existence", code);
        boolean exists = promoCodeService.existsByCode(code);
        return ResponseEntity.ok(exists);
    }


    @GetMapping("/count/active")
    public ResponseEntity<Long> countActivePromoCodes() {
        log.info("GET /api/promo-codes/count/active - Comptage des codes actifs");
        long count = promoCodeService.countActivePromoCodes();
        return ResponseEntity.ok(count);
    }


    @GetMapping("/count/inactive")
    public ResponseEntity<Long> countInactivePromoCodes() {
        log.info("GET /api/promo-codes/count/inactive - Comptage des codes inactifs");
        long count = promoCodeService.countInactivePromoCodes();
        return ResponseEntity.ok(count);
    }


    @GetMapping("/validate/{code}")
    public ResponseEntity<Integer> validatePromoCode(@PathVariable String code) {
        log.info("GET /api/promo-codes/validate/{} - Validation du code", code);
        Integer discountPercentage = promoCodeService.validatePromoCodeAndGetDiscount(code);
        return ResponseEntity.ok(discountPercentage);
    }

    @PostMapping
    public ResponseEntity<PromoCodeDto> createPromoCode(@Valid @RequestBody PromoCodeDto promoCodeDTO) {
        log.info("POST /api/promo-codes - Création d'un nouveau code: {}", promoCodeDTO.getCode());
        PromoCodeDto createdPromoCode = promoCodeService.createPromoCode(promoCodeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPromoCode);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeDto> updatePromoCode(
            @PathVariable Long id,
            @Valid @RequestBody PromoCodeDto promoCodeDTO) {
        log.info("PUT /api/promo-codes/{} - Mise à jour du code", id);
        PromoCodeDto updatedPromoCode = promoCodeService.updatePromoCode(id, promoCodeDTO);
        return ResponseEntity.ok(updatedPromoCode);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activatePromoCode(@PathVariable Long id) {
        log.info("PUT /api/promo-codes/{}/activate - Activation du code", id);
        promoCodeService.activatePromoCode(id);
        return ResponseEntity.noContent().build();
    }



    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePromoCode(@PathVariable Long id) {
        log.info("PUT /api/promo-codes/{}/deactivate - Désactivation du code", id);
        promoCodeService.deactivatePromoCode(id);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromoCode(@PathVariable Long id) {
        log.info("DELETE /api/promo-codes/{} - Suppression du code", id);
        promoCodeService.deletePromoCode(id);
        return ResponseEntity.noContent().build();
    }
}