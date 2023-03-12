package pl.piomin.order.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pl.piomin.base.domain.constants.KafkaTopicConstant;
import pl.piomin.base.domain.dto.OrchestratorRequestDTO;
import pl.piomin.base.domain.dto.OrderItemDTO;
import pl.piomin.base.domain.dto.OrderRequestDTO;
import pl.piomin.base.domain.dto.OrderResponseDTO;
import pl.piomin.base.domain.enums.OrderStatus;
import pl.piomin.order.domain.PurchaseOrderException;

import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class OrderManageService {

    private KafkaTemplate<String, OrchestratorRequestDTO> orderTemplate;

    public OrderManageService(KafkaTemplate<String, OrchestratorRequestDTO> orderTemplate) {
        this.orderTemplate = orderTemplate;
    }

    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        validPurchaseOrder(requestDTO);

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

    private boolean validPurchaseOrder(OrderRequestDTO requestDTO) {
        if(Objects.isNull(requestDTO.getCustomerId())) {
            throw new PurchaseOrderException("INVALID ORDER, INVALID CUSTOMER INFO");
        }
        if(requestDTO.getAmount() <= 0) {
            throw new PurchaseOrderException("INVALID ORDER, INVALID PURCHASE AMOUNT");
        }
        if(CollectionUtils.isEmpty(requestDTO.getItems())) {
            throw new PurchaseOrderException("INVALID ORDER, EMPTY PURCHASE ITEMS");
        }
        int totalAmount = 0;
        for (OrderItemDTO item : requestDTO.getItems()) {
            totalAmount = totalAmount + (item.getProductCount() * item.getProductPrize());
        }
        if(totalAmount != requestDTO.getAmount()) {
            throw new PurchaseOrderException("INVALID ORDER, PURCHASED AMOUNT IS NOT MATCHING");
        }
        return true;
    }
}
