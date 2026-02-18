package com.loopins.core.mapper;

import com.loopins.core.domain.entity.Cart;
import com.loopins.core.domain.entity.CartItem;
import com.loopins.core.dto.response.CartItemResponse;
import com.loopins.core.dto.response.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "subtotal", expression = "java(cart.calculateSubtotal())")
    @Mapping(target = "totalItems", expression = "java(cart.getTotalItemCount())")
    CartResponse toResponse(Cart cart);

    @Mapping(target = "lineTotal", expression = "java(cartItem.getLineTotal())")
    CartItemResponse toResponse(CartItem cartItem);

    List<CartItemResponse> toResponseList(List<CartItem> items);
}

