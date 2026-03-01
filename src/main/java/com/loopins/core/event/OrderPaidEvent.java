package com.loopins.core.event;

import com.loopins.core.domain.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderPaidEvent extends ApplicationEvent {

    private final Order order;

    public OrderPaidEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
