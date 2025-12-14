package com.loopins.core.mapper;

import com.loopins.core.domain.entity.Cart;
import com.loopins.core.domain.entity.CartItem;
import com.loopins.core.domain.entity.User;
import com.loopins.core.dto.response.CartItemResponse;
import com.loopins.core.dto.response.CartResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-14T12:48:08+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class CartMapperImpl implements CartMapper {

    @Override
    public CartResponse toResponse(Cart cart) {
        if ( cart == null ) {
            return null;
        }

        CartResponse.CartResponseBuilder cartResponse = CartResponse.builder();

        cartResponse.userId( cartUserId( cart ) );
        cartResponse.id( cart.getId() );
        cartResponse.status( cart.getStatus() );
        cartResponse.items( toResponseList( cart.getItems() ) );
        cartResponse.createdAt( cart.getCreatedAt() );
        cartResponse.updatedAt( cart.getUpdatedAt() );

        cartResponse.subtotal( cart.calculateSubtotal() );
        cartResponse.totalItems( cart.getTotalItemCount() );

        return cartResponse.build();
    }

    @Override
    public CartItemResponse toResponse(CartItem cartItem) {
        if ( cartItem == null ) {
            return null;
        }

        CartItemResponse.CartItemResponseBuilder cartItemResponse = CartItemResponse.builder();

        cartItemResponse.id( cartItem.getId() );
        cartItemResponse.productId( cartItem.getProductId() );
        cartItemResponse.productName( cartItem.getProductName() );
        cartItemResponse.unitPrice( cartItem.getUnitPrice() );
        cartItemResponse.quantity( cartItem.getQuantity() );

        cartItemResponse.lineTotal( cartItem.getLineTotal() );

        return cartItemResponse.build();
    }

    @Override
    public List<CartItemResponse> toResponseList(List<CartItem> items) {
        if ( items == null ) {
            return null;
        }

        List<CartItemResponse> list = new ArrayList<CartItemResponse>( items.size() );
        for ( CartItem cartItem : items ) {
            list.add( toResponse( cartItem ) );
        }

        return list;
    }

    private Long cartUserId(Cart cart) {
        if ( cart == null ) {
            return null;
        }
        User user = cart.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
