package com.loopins.core.controller;

import com.loopins.core.dto.request.CreateProductRequest;
import com.loopins.core.dto.request.StockAdjustmentRequest;
import com.loopins.core.dto.response.ApiResponse;
import com.loopins.core.dto.response.ProductResponse;
import com.loopins.core.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Tag(name = "Product Management (Admin)", description = "Admin APIs for managing products, prices and stock. Requires X-SERVICE-KEY header.")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Returns all products including inactive ones")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllProducts()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProduct(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created successfully"));
    }

    @PatchMapping("/{id}/stock")
    @Operation(
        summary = "Adjust product stock",
        description = "Use positive quantity to add stock, negative to reduce. Example: +50 = restock, -5 = damaged/removed"
    )
    public ResponseEntity<ApiResponse<ProductResponse>> adjustStock(
            @PathVariable String id,
            @Valid @RequestBody StockAdjustmentRequest request) {
        ProductResponse response = productService.adjustStock(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock adjusted successfully"));
    }

    @PatchMapping("/{id}/price")
    @Operation(summary = "Update product price")
    public ResponseEntity<ApiResponse<ProductResponse>> updatePrice(
            @PathVariable String id,
            @RequestParam BigDecimal price) {
        ProductResponse response = productService.updatePrice(id, price);
        return ResponseEntity.ok(ApiResponse.success(response, "Price updated successfully"));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Enable or disable a product", description = "Disabled products cannot be added to cart")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleActive(@PathVariable String id) {
        ProductResponse response = productService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Product status toggled"));
    }
}
