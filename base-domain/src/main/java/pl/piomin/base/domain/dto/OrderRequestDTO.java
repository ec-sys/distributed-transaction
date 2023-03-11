package pl.piomin.base.domain.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderRequestDTO {
    private String orderId;
    private Long customerId;
    List<OrderItemDTO> items;
    private int amount;
}