package pl.piomin.orchestrator.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.piomin.base.domain.dto.InventoryRequestDTO;
import pl.piomin.base.domain.dto.OrchestratorRequestDTO;
import pl.piomin.base.domain.dto.OrchestratorResponseDTO;
import pl.piomin.base.domain.dto.PaymentRequestDTO;
import pl.piomin.base.domain.enums.OrderStatus;
import pl.piomin.orchestrator.service.steps.InventoryStep;
import pl.piomin.orchestrator.service.steps.PaymentStep;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrchestratorService {

    @Autowired
    @Qualifier("payment")
    private WebClient paymentClient;

    @Autowired
    @Qualifier("inventory")
    private WebClient inventoryClient;

    public Mono<OrchestratorResponseDTO> orderProduct(final OrchestratorRequestDTO requestDTO) {
        Workflow orderWorkflow = this.getOrderWorkflow(requestDTO);
        return Flux.fromStream(() -> orderWorkflow.getSteps().stream())
                .flatMap(WorkflowStep::process)
                .handle(((isDone, synchronousSink) -> {
                    if (isDone)
                        synchronousSink.next(true);
                    else
                        synchronousSink.error(new WorkflowException("create order failed!"));
                }))
                .then(Mono.fromCallable(() -> getResponseDTO(requestDTO, OrderStatus.ORDER_COMPLETED)))
                .onErrorResume(ex -> this.revertOrder(orderWorkflow, requestDTO));
    }

    private Mono<OrchestratorResponseDTO> revertOrder(final Workflow workflow, final OrchestratorRequestDTO requestDTO) {
        return Flux.fromStream(() -> workflow.getSteps().stream())
                .filter(wf -> wf.getStatus().equals(WorkflowStepStatus.COMPLETE))
                .flatMap(WorkflowStep::revert)
                .retry(3)
                .then(Mono.just(this.getResponseDTO(requestDTO, OrderStatus.ORDER_CANCELLED)));
    }

    private OrchestratorResponseDTO getResponseDTO(OrchestratorRequestDTO requestDTO, OrderStatus status) {
        OrchestratorResponseDTO responseDTO = new OrchestratorResponseDTO();
        responseDTO.setOrderId(requestDTO.getOrderId());
        responseDTO.setCustomerId(requestDTO.getCustomerId());
        responseDTO.setStatus(status);
        return responseDTO;
    }

    private Workflow getOrderWorkflow(OrchestratorRequestDTO requestDTO) {
        WorkflowStep paymentStep = new PaymentStep(this.paymentClient, this.getPaymentRequestDTO(requestDTO));
        WorkflowStep inventoryStep = new InventoryStep(this.inventoryClient, this.getInventoryRequestDTO(requestDTO));
        return new OrderWorkflow(List.of(paymentStep, inventoryStep));
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
