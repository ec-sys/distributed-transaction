package pl.piomin.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pl.piomin.base.domain.constants.KafkaTopicConstant;
import pl.piomin.base.domain.dto.OrchestratorRequestDTO;
import pl.piomin.base.domain.dto.OrderRequestDTO;
import pl.piomin.base.domain.dto.OrderResponseDTO;
import pl.piomin.base.domain.enums.OrderStatus;

import java.util.UUID;

@Service
@Slf4j
public class OrderManageService {

    private KafkaTemplate<String, OrchestratorRequestDTO> orderTemplate;

    public OrderManageService(KafkaTemplate<String, OrchestratorRequestDTO> orderTemplate) {
        this.orderTemplate = orderTemplate;
    }

    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        OrderResponseDTO responseDTO = new OrderResponseDTO();
        String orderId = UUID.randomUUID().toString();
        requestDTO.setOrderId(orderId);

        responseDTO.setOrderId(orderId);
        responseDTO.setCustomerId(requestDTO.getCustomerId());

        OrchestratorRequestDTO orchestrator = new OrchestratorRequestDTO();
        BeanUtils.copyProperties(requestDTO, orchestrator);
        orderTemplate.send(KafkaTopicConstant.TOPIC_ORDER, orderId, orchestrator);
        log.info("SEND ORDER: {}", orchestrator);
        responseDTO.setStatus(OrderStatus.ORDER_CREATED);
        return responseDTO;
    }
}
