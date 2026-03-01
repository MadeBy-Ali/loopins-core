package com.loopins.core.service;

import com.loopins.core.domain.entity.Product;
import com.loopins.core.dto.request.CreateProductRequest;
import com.loopins.core.dto.request.StockAdjustmentRequest;
import com.loopins.core.dto.response.ProductResponse;
import com.loopins.core.exception.BusinessException;
import com.loopins.core.exception.ResourceNotFoundException;
import com.loopins.core.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsById(request.getId())) {
            throw new BusinessException("Product with ID '" + request.getId() + "' already exists");
        }
        Product product = Product.builder()
                .id(request.getId())
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Product saved = productRepository.save(product);
        log.info("Created product: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse adjustStock(String id, StockAdjustmentRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        int newStock = product.getStock() + request.getQuantity();
        if (newStock < 0) {
            throw new BusinessException("Cannot reduce stock below 0. Current stock: "
                    + product.getStock() + ", adjustment: " + request.getQuantity());
        }

        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        log.info("Adjusted stock for product {}: {} -> {} (reason: {})",
                id, product.getStock(), newStock, request.getReason());
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse updatePrice(String id, java.math.BigDecimal newPrice) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setPrice(newPrice);
        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        log.info("Updated price for product {}: {}", id, newPrice);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse toggleActive(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(!product.getActive());
        product.setUpdatedAt(LocalDateTime.now());
        Product saved = productRepository.save(product);
        log.info("Toggled active for product {}: {}", id, saved.getActive());
        return toResponse(saved);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
