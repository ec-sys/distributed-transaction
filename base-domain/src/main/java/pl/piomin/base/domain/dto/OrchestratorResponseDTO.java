package pl.piomin.base.domain.dto;
import lombok.Data;
import pl.piomin.base.domain.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

@Data
public class OrchestratorResponseDTO {

    private String orderId;
    private Long customerId;
    private OrderStatus status;
}