package com.loopins.core.service;

import com.loopins.core.domain.entity.Cart;
import com.loopins.core.domain.entity.CartItem;
import com.loopins.core.domain.entity.User;
import com.loopins.core.domain.enums.CartStatus;
import com.loopins.core.domain.enums.UserRole;
import com.loopins.core.dto.request.AddCartItemRequest;
import com.loopins.core.dto.request.CreateCartRequest;
import com.loopins.core.dto.response.CartResponse;
import com.loopins.core.exception.BusinessException;
import com.loopins.core.exception.ResourceNotFoundException;
import com.loopins.core.mapper.CartMapper;
import com.loopins.core.repository.CartItemRepository;
import com.loopins.core.repository.CartRepository;
import com.loopins.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private CartResponse testCartResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.CUSTOMER)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        testCartResponse = CartResponse.builder()
                .id(1L)
                .userId(1L)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .subtotal(BigDecimal.ZERO)
                .totalItems(0)
                .build();
    }

    @Test
    void createCart_WhenUserExists_ShouldCreateNewCart() {
        CreateCartRequest request = CreateCartRequest.builder()
                .userId(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findActiveCartByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartMapper.toResponse(any(Cart.class))).thenReturn(testCartResponse);

        CartResponse result = cartService.createCart(request);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void createCart_WhenActiveCartExists_ShouldReturnExistingCart() {
        CreateCartRequest request = CreateCartRequest.builder()
                .userId(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findActiveCartByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartMapper.toResponse(testCart)).thenReturn(testCartResponse);

        CartResponse result = cartService.createCart(request);

        assertThat(result).isNotNull();
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void createCart_WhenUserNotFound_ShouldThrowException() {
        CreateCartRequest request = CreateCartRequest.builder()
                .userId(999L)
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.createCart(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addItem_WhenCartIsActive_ShouldAddItem() {
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId("PROD-001")
                .productName("Test Product")
                .unitPrice(new BigDecimal("100000"))
                .quantity(2)
                .build();

        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartMapper.toResponse(any(Cart.class))).thenReturn(testCartResponse);

        CartResponse result = cartService.addItem(1L, request);

        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItem_WhenCartNotActive_ShouldThrowException() {
        testCart.setStatus(CartStatus.CHECKED_OUT);

        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId("PROD-001")
                .productName("Test Product")
                .unitPrice(new BigDecimal("100000"))
                .quantity(2)
                .build();

        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("checked out");
    }

    @Test
    void addItem_WhenProductExists_ShouldIncreaseQuantity() {
        CartItem existingItem = CartItem.builder()
                .id(1L)
                .productId("PROD-001")
                .productName("Test Product")
                .unitPrice(new BigDecimal("100000"))
                .quantity(1)
                .cart(testCart)
                .build();
        testCart.getItems().add(existingItem);

        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId("PROD-001")
                .productName("Test Product")
                .unitPrice(new BigDecimal("100000"))
                .quantity(2)
                .build();

        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartMapper.toResponse(any(Cart.class))).thenReturn(testCartResponse);

        cartService.addItem(1L, request);

        assertThat(existingItem.getQuantity()).isEqualTo(3);
    }

    @Test
    void removeItem_WhenItemExists_ShouldRemoveItem() {
        CartItem item = CartItem.builder()
                .id(1L)
                .productId("PROD-001")
                .cart(testCart)
                .build();
        testCart.getItems().add(item);

        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartMapper.toResponse(any(Cart.class))).thenReturn(testCartResponse);

        CartResponse result = cartService.removeItem(1L, 1L);

        assertThat(result).isNotNull();
        verify(cartItemRepository).delete(item);
    }
}

