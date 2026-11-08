package com.loopins.core.controller;

import com.loopins.core.dto.response.ApiResponse;
import com.loopins.core.dto.response.ProductResponse;
import com.loopins.core.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product Catalog (Public)", description = "Public read-only product lookup for the storefront")
public class PublicProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a single product by ID, for storefront display (price, stock, availability)")
    public ApiResponse<ProductResponse> getProduct(@PathVariable String id) {
        return ApiResponse.success(productService.getProduct(id));
    }
}
