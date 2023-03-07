package pl.piomin.order.service;

import org.springframework.stereotype.Service;
import pl.piomin.base.domain.Order;
import pl.piomin.base.domain.enums.OrderSource;
import pl.piomin.base.domain.enums.OrderStatus;

@Service
public class OrderManageService {

    public Order confirm(Order orderPayment, Order orderStock) {
        Order o = new Order(orderPayment.getId(),
                orderPayment.getCustomerId(),
                orderPayment.getProductId(),
                orderPayment.getProductCount(),
                orderPayment.getPrice());

        if (orderPayment.getStatus().equals(OrderStatus.ACCEPT) && orderStock.getStatus().equals(OrderStatus.ACCEPT)) {
            o.setStatus(OrderStatus.CONFIRMED);
        } else if (orderPayment.getStatus().equals(OrderStatus.REJECT) && orderStock.getStatus().equals(OrderStatus.REJECT)) {
            o.setStatus(OrderStatus.REJECTED);
        } else if (orderPayment.getStatus().equals(OrderStatus.REJECT) || orderStock.getStatus().equals(OrderStatus.REJECT)) {
            OrderSource source = orderPayment.getStatus().equals(OrderStatus.REJECT) ? OrderSource.PAYMENT : OrderSource.STOCK;
            o.setStatus(OrderStatus.ROLLBACK);
            o.setSource(source);
        }
        return o;
    }
}
