package pl.piomin.base.domain.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class InventoryRequestDTO {
    private UUID orderId;
    private Long productId;
    private int productCount;
    private int amount;
}