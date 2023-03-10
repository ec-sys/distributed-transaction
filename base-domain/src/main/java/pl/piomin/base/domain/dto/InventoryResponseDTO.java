package pl.piomin.base.domain.dto;

import lombok.Data;
import pl.piomin.base.domain.enums.InventoryStatus;

import java.util.UUID;

@Data
public class InventoryResponseDTO {
    private UUID orderId;
    private InventoryStatus status;
}