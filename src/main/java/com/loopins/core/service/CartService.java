package com.loopins.core.service;

import com.loopins.core.domain.entity.Cart;
import com.loopins.core.domain.entity.CartItem;
import com.loopins.core.domain.entity.User;
import com.loopins.core.domain.enums.CartStatus;
import com.loopins.core.dto.request.AddCartItemRequest;
import com.loopins.core.dto.request.CreateCartRequest;
import com.loopins.core.dto.response.CartResponse;
import com.loopins.core.exception.BusinessException;
import com.loopins.core.exception.ResourceNotFoundException;
import com.loopins.core.mapper.CartMapper;
import com.loopins.core.repository.CartItemRepository;
import com.loopins.core.repository.CartRepository;
import com.loopins.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    /**
     * Creates a new cart for a user or guest, or returns existing active cart.
     */
    @Transactional
    public CartResponse createCart(CreateCartRequest request) {
        // Guest cart
        if (request.getSessionId() != null) {
            log.info("Creating guest cart for session: {}", request.getSessionId());
            return cartRepository.findActiveCartBySessionId(request.getSessionId())
                    .map(existingCart -> {
                        log.info("Returning existing guest cart: {}", existingCart.getId());
                        return cartMapper.toResponse(existingCart);
                    })
                    .orElseGet(() -> {
                        Cart newCart = Cart.builder()
                                .sessionId(request.getSessionId())
                                .status(CartStatus.ACTIVE)
                                .build();
                        Cart savedCart = cartRepository.save(newCart);
                        log.info("Created new guest cart: {}", savedCart.getId());
                        return cartMapper.toResponse(savedCart);
                    });
        }

        // User cart
        log.info("Creating cart for user: {}", request.getUserId());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        return cartRepository.findActiveCartByUserId(request.getUserId())
                .map(existingCart -> {
                    log.info("Returning existing active cart: {}", existingCart.getId());
                    return cartMapper.toResponse(existingCart);
                })
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .status(CartStatus.ACTIVE)
                            .build();
                    Cart savedCart = cartRepository.save(newCart);
                    log.info("Created new cart: {}", savedCart.getId());
                    return cartMapper.toResponse(savedCart);
                });
    }

    /**
     * Gets a cart by ID with all items.
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(Long cartId) {
        log.debug("Fetching cart: {}", cartId);
        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
        return cartMapper.toResponse(cart);
    }

    /**
     * Gets the active cart for a user.
     */
    @Transactional(readOnly = true)
    public CartResponse getActiveCartByUserId(Long userId) {
        log.debug("Fetching active cart for user: {}", userId);
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found for user: " + userId));
        return cartMapper.toResponse(cart);
    }

    /**
     * Gets the active cart for a guest session.
     */
    @Transactional(readOnly = true)
    public CartResponse getActiveCartBySessionId(String sessionId) {
        log.debug("Fetching active cart for session: {}", sessionId);
        Cart cart = cartRepository.findActiveCartBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found for session: " + sessionId));
        return cartMapper.toResponse(cart);
    }

    /**
     * Merges a guest cart into a user cart when user logs in.
     */
    @Transactional
    public CartResponse mergeGuestCartToUser(String sessionId, Long userId) {
        log.info("Merging guest cart (session: {}) to user: {}", sessionId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Get guest cart
        Cart guestCart = cartRepository.findActiveCartBySessionId(sessionId)
                .orElse(null);

        if (guestCart == null || guestCart.isEmpty()) {
            log.info("No guest cart to merge, returning or creating user cart");
            return createCart(CreateCartRequest.builder().userId(userId).build());
        }

        // Get or create user cart
        Cart userCart = cartRepository.findActiveCartByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .status(CartStatus.ACTIVE)
                            .build();
                    return cartRepository.save(newCart);
                });

        // Merge items from guest cart to user cart
        for (CartItem guestItem : guestCart.getItems()) {
            CartItem existingItem = userCart.getItems().stream()
                    .filter(item -> item.getProductId().equals(guestItem.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                existingItem.increaseQuantity(guestItem.getQuantity());
            } else {
                CartItem newItem = CartItem.builder()
                        .productId(guestItem.getProductId())
                        .productName(guestItem.getProductName())
                        .unitPrice(guestItem.getUnitPrice())
                        .quantity(guestItem.getQuantity())
                        .build();
                userCart.addItem(newItem);
            }
        }

        // Mark guest cart as checked out (or delete it)
        guestCart.markAsCheckedOut();
        cartRepository.save(guestCart);

        // Save merged user cart
        Cart savedCart = cartRepository.save(userCart);
        log.info("Guest cart merged successfully. User cart now has {} items", savedCart.getTotalItemCount());
        return cartMapper.toResponse(savedCart);
    }

    /**
     * Adds an item to the cart. If product already exists, increases quantity.
     */
    @Transactional
    public CartResponse addItem(Long cartId, AddCartItemRequest request) {
        log.info("Adding item to cart {}: productId={}, quantity={}",
                cartId, request.getProductId(), request.getQuantity());

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        if (!cart.isActive()) {
            throw new BusinessException("Cannot modify a cart that has been checked out");
        }

        // Check if item already exists in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity if product already in cart
            existingItem.increaseQuantity(request.getQuantity());
            log.info("Updated existing item quantity: {}", existingItem.getQuantity());
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .unitPrice(request.getUnitPrice())
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
            log.info("Added new item to cart");
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    /**
     * Removes an item from the cart.
     */
    @Transactional
    public CartResponse removeItem(Long cartId, Long itemId) {
        log.info("Removing item {} from cart {}", itemId, cartId);

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        if (!cart.isActive()) {
            throw new BusinessException("Cannot modify a cart that has been checked out");
        }

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));

        cart.removeItem(itemToRemove);
        cartItemRepository.delete(itemToRemove);

        Cart savedCart = cartRepository.save(cart);
        log.info("Item removed from cart");
        return cartMapper.toResponse(savedCart);
    }

    /**
     * Updates item quantity in cart.
     */
    @Transactional
    public CartResponse updateItemQuantity(Long cartId, Long itemId, Integer quantity) {
        log.info("Updating item {} quantity to {} in cart {}", itemId, quantity, cartId);

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        if (!cart.isActive()) {
            throw new BusinessException("Cannot modify a cart that has been checked out");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));

        if (quantity <= 0) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        } else {
            item.updateQuantity(quantity);
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    /**
     * Clears all items from the cart.
     */
    @Transactional
    public CartResponse clearCart(Long cartId) {
        log.info("Clearing cart: {}", cartId);

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        if (!cart.isActive()) {
            throw new BusinessException("Cannot modify a cart that has been checked out");
        }

        cart.getItems().clear();
        Cart savedCart = cartRepository.save(cart);
        log.info("Cart cleared");
        return cartMapper.toResponse(savedCart);
    }

    /**
     * Gets cart entity for checkout process.
     */
    @Transactional(readOnly = true)
    public Cart getCartEntity(Long cartId) {
        return cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
    }

    /**
     * Marks cart as checked out.
     */
    @Transactional
    public void markCartAsCheckedOut(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
        cart.markAsCheckedOut();
        cartRepository.save(cart);
        log.info("Cart {} marked as checked out", cartId);
    }
}

