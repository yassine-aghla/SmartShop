package com.smartshop.services;


import com.smartshop.dtos.PromoCodeDto;
import com.smartshop.entity.PromoCode;
import com.smartshop.exceptions.PromocodeNotFoundException;
import com.smartshop.exceptions.PromocodeAlreadyExistsException;
import com.smartshop.exceptions.PromocodeBusinessException;
import com.smartshop.exceptions.PromocodeInvalidException;
import com.smartshop.mappers.PromoCodeMapper;
import com.smartshop.repositories.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;

    @Transactional
    public PromoCodeDto createPromoCode(PromoCodeDto promoCodeDTO) {
        log.info("Création d'un nouveau code promo: {}", promoCodeDTO.getCode());

        validatePromoCodeData(promoCodeDTO);


        if (promoCodeRepository.existsByCode(promoCodeDTO.getCode())) {
            log.warn("Tentative de création avec un code qui existe déjà: {}", promoCodeDTO.getCode());
            throw new PromocodeAlreadyExistsException(
                    "Un code promo '" + promoCodeDTO.getCode() + "' existe déjà"
            );
        }

        PromoCode promoCode = promoCodeMapper.toEntity(promoCodeDTO);
        promoCode.setActive(true);

        PromoCode savedPromoCode = promoCodeRepository.save(promoCode);
        log.info("Code promo créé avec succès: ID={}, code={}", savedPromoCode.getId(), savedPromoCode.getCode());

        return promoCodeMapper.toDTO(savedPromoCode);
    }


    @Transactional(readOnly = true)
    public PromoCodeDto getPromoCodeById(Long id) {
        log.info("Récupération du code promo avec l'ID: {}", id);

        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Code promo non trouvé avec l'ID: {}", id);
                    return new PromocodeNotFoundException("Code promo non trouvé avec l'ID: " + id);
                });

        return promoCodeMapper.toDTO(promoCode);
    }


    @Transactional(readOnly = true)
    public PromoCodeDto getPromoCodeByCode(String code) {
        log.info("Récupération du code promo: {}", code);

        PromoCode promoCode = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("Code promo non trouvé: {}", code);
                    return new PromocodeNotFoundException("Code promo non trouvé: " + code);
                });

        return promoCodeMapper.toDTO(promoCode);
    }


    @Transactional(readOnly = true)
    public List<PromoCodeDto> getAllPromoCodes() {
        log.info("Récupération de tous les codes promo");

        return promoCodeRepository.findAll()
                .stream()
                .map(promoCodeMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PromoCodeDto> getAllActivePromoCodes() {
        log.info("Récupération de tous les codes promo actifs");

        return promoCodeRepository.findByActiveTrue()
                .stream()
                .map(promoCodeMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PromoCodeDto> getAllInactivePromoCodes() {
        log.info("Récupération de tous les codes promo inactifs");

        return promoCodeRepository.findByActiveFalse()
                .stream()
                .map(promoCodeMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PromoCodeDto> getPromoCodesByPercentage(Integer percentage) {
        log.info("Récupération des codes promo avec {}% de remise", percentage);

        return promoCodeRepository.findByDiscountPercentage(percentage)
                .stream()
                .map(promoCodeMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public PromoCodeDto updatePromoCode(Long id, PromoCodeDto promoCodeDTO) {
        log.info("Mise à jour du code promo avec l'ID: {}", id);

        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Code promo non trouvé pour la mise à jour: ID={}", id);
                    return new PromocodeNotFoundException("Code promo non trouvé avec l'ID: " + id);
                });

        if (!promoCode.getCode().equals(promoCodeDTO.getCode()) &&
                promoCodeRepository.existsByCode(promoCodeDTO.getCode())) {
            log.warn("Tentative de mise à jour avec un code qui existe déjà: {}", promoCodeDTO.getCode());
            throw new PromocodeAlreadyExistsException(
                    "Un code promo '" + promoCodeDTO.getCode() + "' existe déjà"
            );
        }

        promoCode.setCode(promoCodeDTO.getCode());
        promoCode.setActive(promoCodeDTO.getActive());
        promoCode.setDiscountPercentage(promoCodeDTO.getDiscountPercentage());

        PromoCode updatedPromoCode = promoCodeRepository.save(promoCode);
        log.info("Code promo mis à jour avec succès: ID={}", updatedPromoCode.getId());

        return promoCodeMapper.toDTO(updatedPromoCode);
    }


    @Transactional
    public void deletePromoCode(Long id) {
        log.info("Suppression du code promo avec l'ID: {}", id);

        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Code promo non trouvé pour la suppression: ID={}", id);
                    return new PromocodeNotFoundException("Code promo non trouvé avec l'ID: " + id);
                });

        promoCodeRepository.delete(promoCode);
        log.info("Code promo supprimé avec succès: ID={}", id);
    }

    @Transactional
    public void activatePromoCode(Long id) {
        log.info("Activation du code promo avec l'ID: {}", id);

        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Code promo non trouvé: ID={}", id);
                    return new PromocodeNotFoundException("Code promo non trouvé avec l'ID: " + id);
                });

        if (promoCode.getActive()) {
            log.warn("Le code promo est déjà actif: ID={}", id);
            throw new PromocodeBusinessException("Le code promo est déjà actif");
        }

        promoCode.setActive(true);
        promoCodeRepository.save(promoCode);
        log.info("Code promo activé avec succès: ID={}", id);
    }

    @Transactional
    public void deactivatePromoCode(Long id) {
        log.info("Désactivation du code promo avec l'ID: {}", id);

        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Code promo non trouvé: ID={}", id);
                    return new PromocodeNotFoundException("Code promo non trouvé avec l'ID: " + id);
                });

        if (!promoCode.getActive()) {
            log.warn("Le code promo est déjà inactif: ID={}", id);
            throw new PromocodeBusinessException("Le code promo est déjà inactif");
        }

        promoCode.setActive(false);
        promoCodeRepository.save(promoCode);
        log.info("Code promo désactivé avec succès: ID={}", id);
    }

    @Transactional(readOnly = true)
    public Integer validatePromoCodeAndGetDiscount(String code) {
        log.info("Validation du code promo: {}", code);

        PromoCode promoCode = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("Code promo non trouvé: {}", code);
                    return new PromocodeNotFoundException("Code promo non trouvé: " + code);
                });

        if (!promoCode.isValid()) {
            log.warn("Code promo invalide ou inactif: {}", code);
            throw new PromocodeInvalidException("Le code promo '" + code + "' est invalide ou inactif");
        }

        log.info("Code promo validé: {} - Remise: {}%", code, promoCode.getDiscountPercentage());
        return promoCode.getDiscountPercentage();
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return promoCodeRepository.existsByCode(code);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return promoCodeRepository.existsById(id);
    }


    @Transactional(readOnly = true)
    public long countActivePromoCodes() {
        return promoCodeRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public long countInactivePromoCodes() {
        return promoCodeRepository.countByActiveFalse();
    }


    public void validatePromoCodeData(PromoCodeDto promoCodeDTO) {
        log.debug("Validation des données du code promo: {}", promoCodeDTO.getCode());


        if (promoCodeDTO.getCode() == null || promoCodeDTO.getCode().trim().isEmpty()) {
            throw new PromocodeBusinessException("Le code promo ne peut pas être vide");
        }

        if (!promoCodeDTO.getCode().matches("^PROMO-[A-Z0-9]{4}$")) {
            throw new PromocodeBusinessException(
                    "Le code doit avoir le format PROMO-XXXX (X = lettres/chiffres majuscules)"
            );
        }

        if (promoCodeDTO.getDiscountPercentage() == null) {
            throw new PromocodeBusinessException("Le pourcentage de remise ne peut pas être null");
        }

        if (promoCodeDTO.getDiscountPercentage() < 1 || promoCodeDTO.getDiscountPercentage() > 100) {
            throw new PromocodeBusinessException("Le pourcentage doit être entre 1% et 100%");
        }

        log.debug("Validation des données du code promo réussie");
    }
}
