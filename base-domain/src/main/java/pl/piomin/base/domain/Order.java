package pl.piomin.base.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.piomin.base.domain.enums.OrderSource;
import pl.piomin.base.domain.enums.OrderStatus;

@Data
public class Order {
    private Long id;
    private Long customerId;
    private Long productId;
    private int productCount;
    private int price;
    private OrderStatus status;
    private OrderSource source;

    public Order() {
    }

    public Order(Long id, Long customerId, Long productId, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.productId = productId;
        this.status = status;
    }

    public Order(Long id, Long customerId, Long productId, int productCount, int price) {
        this.id = id;
        this.customerId = customerId;
        this.productId = productId;
        this.productCount = productCount;
        this.price = price;
    }
}
