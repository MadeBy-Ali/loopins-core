package com.loopins.core.mapper;

import com.loopins.core.domain.entity.Order;
import com.loopins.core.domain.entity.OrderItem;
import com.loopins.core.domain.entity.User;
import com.loopins.core.dto.response.OrderItemResponse;
import com.loopins.core.dto.response.OrderResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-14T12:48:07+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderResponse toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse.OrderResponseBuilder orderResponse = OrderResponse.builder();

        orderResponse.userId( orderUserId( order ) );
        orderResponse.id( order.getId() );
        orderResponse.status( order.getStatus() );
        orderResponse.subtotal( order.getSubtotal() );
        orderResponse.shippingFee( order.getShippingFee() );
        orderResponse.totalAmount( order.getTotalAmount() );
        orderResponse.shippingAddress( order.getShippingAddress() );
        orderResponse.paymentUrl( order.getPaymentUrl() );
        orderResponse.paymentReference( order.getPaymentReference() );
        orderResponse.items( toResponseList( order.getItems() ) );
        orderResponse.createdAt( order.getCreatedAt() );
        orderResponse.updatedAt( order.getUpdatedAt() );
        orderResponse.paidAt( order.getPaidAt() );
        orderResponse.shippedAt( order.getShippedAt() );
        orderResponse.completedAt( order.getCompletedAt() );

        return orderResponse.build();
    }

    @Override
    public OrderItemResponse toResponse(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderItemResponse.OrderItemResponseBuilder orderItemResponse = OrderItemResponse.builder();

        orderItemResponse.id( orderItem.getId() );
        orderItemResponse.productId( orderItem.getProductId() );
        orderItemResponse.productName( orderItem.getProductName() );
        orderItemResponse.unitPrice( orderItem.getUnitPrice() );
        orderItemResponse.quantity( orderItem.getQuantity() );

        orderItemResponse.lineTotal( orderItem.getLineTotal() );

        return orderItemResponse.build();
    }

    @Override
    public List<OrderItemResponse> toResponseList(List<OrderItem> items) {
        if ( items == null ) {
            return null;
        }

        List<OrderItemResponse> list = new ArrayList<OrderItemResponse>( items.size() );
        for ( OrderItem orderItem : items ) {
            list.add( toResponse( orderItem ) );
        }

        return list;
    }

    private Long orderUserId(Order order) {
        if ( order == null ) {
            return null;
        }
        User user = order.getUser();
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
