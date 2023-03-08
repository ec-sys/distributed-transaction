package pl.piomin.base.domain.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Long productId;
    private int productCount;
    private int productPrize;
}
