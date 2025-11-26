package com.smartshop.controllers;

import com.smartshop.dtos.ProductDTO;
import com.smartshop.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;


    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("GET /api/products - Récupération de tous les produits");
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ProductDTO>> getAllProductsPaginated(Pageable pageable) {
        log.info("GET /api/products/paginated - Récupération avec pagination");
        Page<ProductDTO> products = productService.getAllProductsPaginated(pageable);
        return ResponseEntity.ok(products);
    }


    @GetMapping("/deleted")
    public ResponseEntity<List<ProductDTO>> getAllDeletedProducts() {
        log.info("GET /api/products/deleted - Récupération des produits supprimés");
        List<ProductDTO> products = productService.getAllDeletedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - Récupération du produit", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }


    @GetMapping("/nom/{nom}")
    public ResponseEntity<ProductDTO> getProductByNom(@PathVariable String nom) {
        log.info("GET /api/products/nom/{} - Récupération du produit", nom);
        ProductDTO product = productService.getProductByNom(nom);
        return ResponseEntity.ok(product);
    }


    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchByNom(@RequestParam String terme) {
        log.info("GET /api/products/search - Recherche avec le terme: {}", terme);
        List<ProductDTO> products = productService.searchByNom(terme);
        return ResponseEntity.ok(products);
    }


    @GetMapping("/search/paginated")
    public ResponseEntity<Page<ProductDTO>> searchByNomPaginated(
            @RequestParam String terme,
            Pageable pageable) {
        log.info("GET /api/products/search/paginated - Recherche: {}", terme);
        Page<ProductDTO> products = productService.searchByNomPaginated(terme, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrix,
            @RequestParam BigDecimal maxPrix) {
        log.info("GET /api/products/price-range - Recherche entre {} et {}", minPrix, maxPrix);
        List<ProductDTO> products = productService.getProductsByPriceRange(minPrix, maxPrix);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts(@RequestParam Integer seuil) {
        log.info("GET /api/products/low-stock - Seuil: {}", seuil);
        List<ProductDTO> products = productService.getLowStockProducts(seuil);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<ProductDTO>> getOutOfStockProducts() {
        log.info("GET /api/products/out-of-stock - Produits en rupture");
        List<ProductDTO> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/check-stock/{id}")
    public ResponseEntity<Boolean> checkStock(
            @PathVariable Long id,
            @RequestParam Integer quantite) {
        log.info("GET /api/products/check-stock/{} - Quantité: {}", id, quantite);
        boolean hasStock = productService.hasEnoughStock(id, quantite);
        return ResponseEntity.ok(hasStock);
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveProducts() {
        log.info("GET /api/products/count/active - Comptage");
        long count = productService.countActiveProducts();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/deleted")
    public ResponseEntity<Long> countDeletedProducts() {
        log.info("GET /api/products/count/deleted - Comptage");
        long count = productService.countDeletedProducts();
        return ResponseEntity.ok(count);
    }


    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("POST /api/products - Création d'un nouveau produit: {}", productDTO.getNom());
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        try {
            log.info("PUT /api/products/{} - Mise à jour du produit", id);
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(updatedProduct);
        }
      catch (Exception e){
          Map<String,String>error=new HashMap<>();
          error.put("error",e.getMessage());
          return ResponseEntity.ok(error);
      }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{} - Suppression du produit", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreProduct(@PathVariable Long id) {
        log.info("PUT /api/products/{}/restore - Restauration du produit", id);
        productService.restoreProduct(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}/decrement-stock")
    public ResponseEntity<Void> decrementStock(
            @PathVariable Long id,
            @RequestParam Integer quantite) {
        log.info("PUT /api/products/{}/decrement-stock - Quantité: {}", id, quantite);
        productService.decrementStock(id, quantite);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}/increment-stock")
    public ResponseEntity<Void> incrementStock(
            @PathVariable Long id,
            @RequestParam Integer quantite) {
        log.info("PUT /api/products/{}/increment-stock - Quantité: {}", id, quantite);
        productService.incrementStock(id, quantite);
        return ResponseEntity.noContent().build();
    }
}
