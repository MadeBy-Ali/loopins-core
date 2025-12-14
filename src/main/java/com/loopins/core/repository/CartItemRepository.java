package com.loopins.core.repository;

import com.loopins.core.domain.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, String productId);

    void deleteByCartId(Long cartId);

    boolean existsByCartIdAndProductId(Long cartId, String productId);
}

