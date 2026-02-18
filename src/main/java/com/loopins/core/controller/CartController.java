package com.loopins.core.controller;

import com.loopins.core.dto.request.AddCartItemRequest;
import com.loopins.core.dto.request.CreateCartRequest;
import com.loopins.core.dto.response.ApiResponse;
import com.loopins.core.dto.response.CartResponse;
import com.loopins.core.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "APIs for managing shopping carts and cart items")
public class CartController {

    private final CartService cartService;

    /**
     * Creates a new cart for a user or returns existing active cart.
     */
    @PostMapping
    @Operation(
        summary = "Create a new cart",
        description = "Creates a new cart for a user. If the user already has an active cart, returns the existing cart."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Cart created successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    public ResponseEntity<ApiResponse<CartResponse>> createCart(
            @Valid @RequestBody CreateCartRequest request) {
        log.info("POST /carts - Creating cart for user: {}", request.getUserId());
        CartResponse cart = cartService.createCart(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(cart, "Cart created successfully"));
    }

    /**
     * Gets a cart by ID with all items.
     */
    @GetMapping("/{cartId}")
    @Operation(
        summary = "Get cart by ID",
        description = "Retrieves a cart with all its items by cart ID"
    )
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @Parameter(description = "Cart ID") @PathVariable Long cartId) {
        log.info("GET /carts/{}", cartId);
        CartResponse cart = cartService.getCart(cartId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    /**
     * Gets active cart for a user.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get active cart by user ID", description = "Retrieves the active cart for a logged-in user")
    public ResponseEntity<ApiResponse<CartResponse>> getActiveCartByUser(@PathVariable Long userId) {
        log.info("GET /carts/user/{}", userId);
        CartResponse cart = cartService.getActiveCartByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    /**
     * Gets active cart for a guest session.
     */
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get active cart by session ID", description = "Retrieves the active cart for a guest/anonymous user")
    public ResponseEntity<ApiResponse<CartResponse>> getActiveCartBySession(@PathVariable String sessionId) {
        log.info("GET /carts/session/{}", sessionId);
        CartResponse cart = cartService.getActiveCartBySessionId(sessionId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    /**
     * Merges guest cart to user cart when user logs in.
     */
    @PostMapping("/merge")
    @Operation(summary = "Merge guest cart to user cart", description = "Merges items from a guest cart into user cart when user logs in")
    public ResponseEntity<ApiResponse<CartResponse>> mergeCart(
            @RequestParam String sessionId,
            @RequestParam Long userId) {
        log.info("POST /carts/merge - session: {}, user: {}", sessionId, userId);
        CartResponse cart = cartService.mergeGuestCartToUser(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Guest cart merged successfully"));
    }

    /**
     * Adds an item to the cart.
     */
    @PostMapping("/{cartId}/items")
    @Operation(
        summary = "Add item to cart",
        description = "Adds a product to the cart. If the product already exists, increases the quantity."
    )
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Parameter(description = "Cart ID") @PathVariable Long cartId,
            @Valid @RequestBody AddCartItemRequest request) {
        log.info("POST /carts/{}/items - Adding product: {}", cartId, request.getProductId());
        CartResponse cart = cartService.addItem(cartId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart"));
    }

    /**
     * Removes an item from the cart.
     */
    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId) {
        log.info("DELETE /carts/{}/items/{}", cartId, itemId);
        CartResponse cart = cartService.removeItem(cartId, itemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart"));
    }

    /**
     * Updates item quantity in cart.
     */
    @PatchMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        log.info("PATCH /carts/{}/items/{} - quantity: {}", cartId, itemId, quantity);
        CartResponse cart = cartService.updateItemQuantity(cartId, itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item quantity updated"));
    }

    /**
     * Clears all items from the cart.
     */
    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@PathVariable Long cartId) {
        log.info("DELETE /carts/{}/items - Clearing cart", cartId);
        CartResponse cart = cartService.clearCart(cartId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart cleared"));
    }
}

