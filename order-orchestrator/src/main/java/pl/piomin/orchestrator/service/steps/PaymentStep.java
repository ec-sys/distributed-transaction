package pl.piomin.orchestrator.service.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import pl.piomin.base.domain.dto.PaymentRequestDTO;
import pl.piomin.base.domain.dto.PaymentResponseDTO;
import pl.piomin.base.domain.enums.PaymentStatus;
import pl.piomin.orchestrator.service.BaseStep;
import pl.piomin.orchestrator.service.WorkflowStep;
import pl.piomin.orchestrator.service.WorkflowStepStatus;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class PaymentStep extends BaseStep implements WorkflowStep {

    private final PaymentRequestDTO requestDTO;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String endpoint;
    private WorkflowStepStatus stepStatus = WorkflowStepStatus.PENDING;

    public PaymentStep(RestTemplate restTemplate, ObjectMapper objectMapper, String endpoint, PaymentRequestDTO requestDTO) {
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
        PaymentResponseDTO responseDTO = callAPI("/payment/debit");
        if (Objects.nonNull(responseDTO)) {
            log.info("MAKE PAYMENT STATUS: {}, ORDER ID: {}", responseDTO.getStatus(), requestDTO.getOrderId());
            if (PaymentStatus.PAYMENT_APPROVED.equals(responseDTO.getStatus())) isSuccessful = true;
        } else {
            log.error("MAKE PAYMENT FAILED - NULL RETURN, ORDER ID: {}", requestDTO.getOrderId());
        }
        this.stepStatus = isSuccessful ? WorkflowStepStatus.COMPLETE : WorkflowStepStatus.PENDING;
        return isSuccessful;
    }

    @Override
    public boolean revert() {
        boolean isSuccessful = false;
        PaymentResponseDTO responseDTO = callAPI("/payment/credit");
        if (Objects.nonNull(responseDTO)) {
            log.info("REVERT PAYMENT STATUS: {}, ORDER ID: {}", responseDTO.getStatus(), responseDTO.getOrderId());
            if (PaymentStatus.PAYMENT_APPROVED.equals(responseDTO.getStatus())) isSuccessful = true;
        } else {
            log.error("REVERT PAYMENT FAILED - NULL RETURN, ORDER ID: {}", requestDTO.getOrderId());
        }
        return isSuccessful;
    }

    private PaymentResponseDTO callAPI(String path) {
        String url = endpoint + path;
        return restTemplate
                .postForEntity(url, objectMapper.convertValue(requestDTO, Map.class), PaymentResponseDTO.class)
                .getBody();
    }
}
