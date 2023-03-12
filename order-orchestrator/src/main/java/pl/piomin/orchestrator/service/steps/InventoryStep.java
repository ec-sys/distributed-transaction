package pl.piomin.orchestrator.service.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import pl.piomin.base.domain.dto.InventoryRequestDTO;
import pl.piomin.base.domain.dto.InventoryResponseDTO;
import pl.piomin.base.domain.enums.InventoryStatus;
import pl.piomin.orchestrator.service.BaseStep;
import pl.piomin.orchestrator.service.WorkflowStep;
import pl.piomin.orchestrator.service.WorkflowStepStatus;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class InventoryStep extends BaseStep implements WorkflowStep {

    private final InventoryRequestDTO requestDTO;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String endpoint;
    private WorkflowStepStatus stepStatus = WorkflowStepStatus.PENDING;

    public InventoryStep(RestTemplate restTemplate, ObjectMapper objectMapper, String endpoint, InventoryRequestDTO requestDTO) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.endpoint = endpoint;
        this.requestDTO = requestDTO;
    }

    @Override
    public WorkflowStepStatus getStatus() {
        return this.stepStatus;
    }

    @Override
    public boolean process() {
        boolean isSuccessful = false;
        InventoryResponseDTO responseDTO = callAPI("/inventory/deduct");
        if (Objects.nonNull(responseDTO)) {
            log.info("MAKE INVENTORY STATUS: {}, ORDER ID: {}", responseDTO.getStatus(), requestDTO.getOrderId());
            if (InventoryStatus.AVAILABLE.equals(responseDTO.getStatus())) isSuccessful = true;
        } else {
            log.error("MAKE INVENTORY FAILED - NULL RETURN, ORDER ID: {}", requestDTO.getOrderId());
        }
        this.stepStatus = isSuccessful ? WorkflowStepStatus.COMPLETE : WorkflowStepStatus.PENDING;
        return isSuccessful;
    }

    @Override
    public boolean revert() {
        boolean isSuccessful = false;
        InventoryResponseDTO responseDTO = callAPI("/inventory/add");
        if (Objects.nonNull(responseDTO)) {
            log.info("REVERT INVENTORY STATUS: {}, ORDER ID: {}", responseDTO.getStatus(), responseDTO.getOrderId());
            if (InventoryStatus.AVAILABLE.equals(responseDTO.getStatus())) isSuccessful = true;
        } else {
            log.error("REVERT INVENTORY FAILED - NULL RETURN, ORDER ID: {}", requestDTO.getOrderId());
        }
        return isSuccessful;
    }

    private InventoryResponseDTO callAPI(String path) {
        String url = endpoint + path;
        return restTemplate
                .postForEntity(url, objectMapper.convertValue(requestDTO, Map.class), InventoryResponseDTO.class)
                .getBody();
    }
}
