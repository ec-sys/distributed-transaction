package pl.piomin.base.domain.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentRequestDTO {
    private String orderId;
    private Long customerId;
    private int amount;
}

