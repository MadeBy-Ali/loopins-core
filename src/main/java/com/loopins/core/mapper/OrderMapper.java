package com.loopins.core.mapper;

import com.loopins.core.domain.entity.Order;
import com.loopins.core.domain.entity.OrderItem;
import com.loopins.core.dto.response.OrderItemResponse;
import com.loopins.core.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    OrderResponse toResponse(Order order);

    @Mapping(target = "lineTotal", expression = "java(orderItem.getLineTotal())")
    OrderItemResponse toResponse(OrderItem orderItem);

    List<OrderItemResponse> toResponseList(List<OrderItem> items);
}

