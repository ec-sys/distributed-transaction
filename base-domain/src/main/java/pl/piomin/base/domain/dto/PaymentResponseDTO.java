package pl.piomin.base.domain.dto;

import lombok.Data;
import pl.piomin.base.domain.enums.PaymentStatus;

import java.util.UUID;

@Data
public class PaymentResponseDTO {
    private UUID orderId;
    private Long customerId;
    private int amount;
    private PaymentStatus status;
}