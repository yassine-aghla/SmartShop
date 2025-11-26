package com.smartshop.services;



import com.smartshop.dtos.ProductDTO;
import com.smartshop.entity.Product;
import com.smartshop.exceptions.ProductNotFoundException;
import com.smartshop.exceptions.ProductAlreadyExistsException;
import com.smartshop.exceptions.ProductBusinessException;
import com.smartshop.exceptions.InsufficientStockException;
import com.smartshop.mappers.ProductMapper;
import com.smartshop.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;


    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Création d'un nouveau produit: {}", productDTO.getNom());


        validateProductData(productDTO);


        if (productRepository.existsByNomAndActive(productDTO.getNom())) {
            log.warn("Tentative de création avec un produit qui existe déjà: {}", productDTO.getNom());
            throw new ProductAlreadyExistsException(
                    "Un produit '" + productDTO.getNom() + "' existe déjà"
            );
        }

        Product product = productMapper.toEntity(productDTO);
        product.setDeleted(false);

        Product savedProduct = productRepository.save(product);
        log.info("Produit créé avec succès: ID={}, nom={}", savedProduct.getId(), savedProduct.getNom());

        return productMapper.toDTO(savedProduct);
    }


    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Récupération du produit avec l'ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Produit non trouvé avec l'ID: {}", id);
                    return new ProductNotFoundException("Produit non trouvé avec l'ID: " + id);
                });

        return productMapper.toDTO(product);
    }


    @Transactional(readOnly = true)
    public ProductDTO getProductByNom(String nom) {
        log.info("Récupération du produit: {}", nom);

        Product product = productRepository.findByNomAndActive(nom)
                .orElseThrow(() -> {
                    log.warn("Produit non trouvé: {}", nom);
                    return new ProductNotFoundException("Produit non trouvé: " + nom);
                });

        return productMapper.toDTO(product);
    }


    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Récupération de tous les produits actifs");

        return productRepository.findAllActive()
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProductsPaginated(Pageable pageable) {
        log.info("Récupération de tous les produits avec pagination");

        return productRepository.findAllActivePaginated(pageable)
                .map(productMapper::toDTO);
    }


    @Transactional(readOnly = true)
    public List<ProductDTO> getAllDeletedProducts() {
        log.info("Récupération de tous les produits supprimés (soft delete)");

        return productRepository.findAllDeleted()
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Mise à jour du produit avec l'ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Produit non trouvé pour la mise à jour: ID={}", id);
                    return new ProductNotFoundException("Produit non trouvé avec l'ID: " + id);
                });

        if (!product.getNom().equals(productDTO.getNom()) &&
                productRepository.existsByNomAndActive(productDTO.getNom())) {
            log.warn("Tentative de mise à jour avec un nom qui existe déjà: {}", productDTO.getNom());
            throw new ProductAlreadyExistsException(
                    "Un produit '" + productDTO.getNom() + "' existe déjà"
            );
        }

        productMapper.updateEntityFromDTO(productDTO, product);

        Product updatedProduct = productRepository.save(product);
        log.info("Produit mis à jour avec succès: ID={}", updatedProduct.getId());

        return productMapper.toDTO(updatedProduct);
    }


    @Transactional
    public void deleteProduct(Long id) {
        log.info("Suppression du produit avec l'ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Produit non trouvé pour la suppression: ID={}", id);
                    return new ProductNotFoundException("Produit non trouvé avec l'ID: " + id);
                });

        product.setDeleted(true);
        productRepository.save(product);
        log.info("Produit supprimé (soft delete) avec succès: ID={}", id);
    }


    @Transactional
    public void restoreProduct(Long id) {
        log.info("Restauration du produit avec l'ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Produit non trouvé: ID={}", id);
                    return new ProductNotFoundException("Produit non trouvé avec l'ID: " + id);
                });

        if (!product.getDeleted()) {
            log.warn("Le produit n'est pas supprimé: ID={}", id);
            throw new ProductBusinessException("Le produit n'est pas marqué comme supprimé");
        }

        product.setDeleted(false);
        productRepository.save(product);
        log.info("Produit restauré avec succès: ID={}", id);
    }


    @Transactional(readOnly = true)
    public List<ProductDTO> searchByNom(String terme) {
        log.info("Recherche de produits avec le terme: {}", terme);

        return productRepository.searchByNom(terme)
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Page<ProductDTO> searchByNomPaginated(String terme, Pageable pageable) {
        log.info("Recherche de produits avec le terme: {} (paginé)", terme);

        return productRepository.searchByNomPaginated(terme, pageable)
                .map(productMapper::toDTO);
    }


    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(BigDecimal minPrix, BigDecimal maxPrix) {
        log.info("Recherche de produits entre {} et {}", minPrix, maxPrix);

        return productRepository.findByPrixBetweenAndActive(minPrix, maxPrix)
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts(Integer seuil) {
        log.info("Recherche de produits avec stock < {}", seuil);

        return productRepository.findLowStockProducts(seuil)
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ProductDTO> getOutOfStockProducts() {
        log.info("Recherche de produits en rupture de stock");

        return productRepository.findOutOfStockProducts()
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public boolean hasEnoughStock(Long productId, Integer quantite) {
        log.debug("Vérification du stock: productId={}, quantite={}", productId, quantite);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé avec l'ID: " + productId));

        return product.hasEnoughStock(quantite);
    }


    @Transactional
    public void decrementStock(Long productId, Integer quantite) {
        log.info("Décrémentation du stock: productId={}, quantite={}", productId, quantite);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé avec l'ID: " + productId));

        if (!product.hasEnoughStock(quantite)) {
            log.error("Stock insuffisant: productId={}, stock={}, demande={}",
                    productId, product.getStock(), quantite);
            throw new InsufficientStockException(
                    "Stock insuffisant pour le produit '" + product.getNom() +
                            "': " + product.getStock() + " disponibles, " + quantite + " demandé"
            );
        }

        product.decrementStock(quantite);
        productRepository.save(product);
        log.info("Stock décrémenté avec succès: productId={}, nouveau stock={}", productId, product.getStock());
    }


    @Transactional
    public void incrementStock(Long productId, Integer quantite) {
        log.info("Incrémentation du stock: productId={}, quantite={}", productId, quantite);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé avec l'ID: " + productId));

        product.incrementStock(quantite);
        productRepository.save(product);
        log.info("Stock incrémenté avec succès: productId={}, nouveau stock={}", productId, product.getStock());
    }


    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }


    @Transactional(readOnly = true)
    public long countActiveProducts() {
        return productRepository.countActive();
    }

    @Transactional(readOnly = true)
    public long countDeletedProducts() {
        return productRepository.countDeleted();
    }


    public void validateProductData(ProductDTO productDTO) {
        log.debug("Validation des données du produit: {}", productDTO.getNom());

        if (productDTO.getNom() == null || productDTO.getNom().trim().isEmpty()) {
            throw new ProductBusinessException("Le nom du produit ne peut pas être vide");
        }

        if (productDTO.getNom().length() < 3 || productDTO.getNom().length() > 150) {
            throw new ProductBusinessException("Le nom doit faire entre 3 et 150 caractères");
        }


        if (productDTO.getPrix() == null) {
            throw new ProductBusinessException("Le prix ne peut pas être null");
        }

        if (productDTO.getPrix().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductBusinessException("Le prix doit être supérieur à 0");
        }

        if (productDTO.getStock() == null) {
            throw new ProductBusinessException("Le stock ne peut pas être null");
        }

        if (productDTO.getStock() < 0) {
            throw new ProductBusinessException("Le stock ne peut pas être négatif");
        }

        log.debug("Validation des données du produit réussie");
    }
}