package pl.piomin.base.domain.dto;
import lombok.Data;
import pl.piomin.base.domain.enums.OrderStatus;

import java.util.UUID;

@Data
public class OrchestratorResponseDTO {

    private Integer userId;
    private Integer productId;
    private UUID orderId;
    private Double amount;
    private OrderStatus status;
}