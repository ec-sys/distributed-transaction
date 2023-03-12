package pl.piomin.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.piomin.base.domain.dto.InventoryRequestDTO;
import pl.piomin.base.domain.dto.OrchestratorRequestDTO;
import pl.piomin.base.domain.dto.OrchestratorResponseDTO;
import pl.piomin.base.domain.dto.PaymentRequestDTO;
import pl.piomin.base.domain.enums.OrderStatus;
import pl.piomin.orchestrator.service.steps.InventoryStep;
import pl.piomin.orchestrator.service.steps.PaymentStep;

import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class OrchestratorService {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    ObjectMapper objectMapper;
    @Value("${service.endpoints.payment}")
    private String endpointPayment;
    @Value("${service.endpoints.inventory}")
    private String endpointInventory;

    public OrchestratorResponseDTO processOrder(final OrchestratorRequestDTO requestDTO) {
        Workflow orderWorkflow = this.getOrderWorkflow(requestDTO);
        Map<Integer, WorkflowStep> stepMap = orderWorkflow.getSteps();
        boolean isCompleted = true;
        try {
            stepMap.forEach((stepNumber, step) -> {
                if (!step.process()) {
                    throw new WorkflowException("CREATE ORDER FAILED AT STEP " + step.getClass().getName());
                }
            });
        } catch (Exception ex) {
            isCompleted = false;
            ex.printStackTrace();
            log.error(ex.getMessage());
        }

        if (!isCompleted) {
            revertOrder(orderWorkflow, requestDTO);
            return getResponseDTO(requestDTO, OrderStatus.ORDER_CANCELLED);
        } else {
            return getResponseDTO(requestDTO, OrderStatus.ORDER_COMPLETED);
        }
    }

    private void revertOrder(final Workflow workflow, final OrchestratorRequestDTO requestDTO) {
        Map<Integer, WorkflowStep> stepMap = workflow.getSteps();
        stepMap.forEach((stepNumber, step) -> {
            if (WorkflowStepStatus.COMPLETE.equals(step.getStatus())) step.revert();
        });
    }

    private OrchestratorResponseDTO getResponseDTO(OrchestratorRequestDTO requestDTO, OrderStatus status) {
        OrchestratorResponseDTO responseDTO = new OrchestratorResponseDTO();
        responseDTO.setOrderId(requestDTO.getOrderId());
        responseDTO.setCustomerId(requestDTO.getCustomerId());
        responseDTO.setStatus(status);
        return responseDTO;
    }

    private Workflow getOrderWorkflow(OrchestratorRequestDTO requestDTO) {
        WorkflowStep paymentStep = new PaymentStep(
                restTemplate, objectMapper, endpointPayment, getPaymentRequestDTO(requestDTO));
        WorkflowStep inventoryStep = new InventoryStep(
                restTemplate, objectMapper, endpointInventory, getInventoryRequestDTO(requestDTO));
        Map<Integer, WorkflowStep> stepMap = new TreeMap<>();
        stepMap.put(1, inventoryStep);
        stepMap.put(2, paymentStep);
        return new OrderWorkflow(stepMap);
    }

    private PaymentRequestDTO getPaymentRequestDTO(OrchestratorRequestDTO requestDTO) {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO();
        BeanUtils.copyProperties(requestDTO, paymentRequestDTO);
        return paymentRequestDTO;
    }

    private InventoryRequestDTO getInventoryRequestDTO(OrchestratorRequestDTO requestDTO) {
        InventoryRequestDTO inventoryRequestDTO = new InventoryRequestDTO();
        BeanUtils.copyProperties(requestDTO, inventoryRequestDTO);
        return inventoryRequestDTO;
    }
}
