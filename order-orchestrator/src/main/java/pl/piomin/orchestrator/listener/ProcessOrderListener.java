package pl.piomin.orchestrator.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.piomin.base.domain.constants.KafkaTopicConstant;
import pl.piomin.base.domain.dto.OrchestratorRequestDTO;
import pl.piomin.orchestrator.service.OrchestratorService;

@Component
@Slf4j
public class ProcessOrderListener {

    @Autowired
    OrchestratorService orchestratorService;

    @KafkaListener(id = "orders", topics = KafkaTopicConstant.TOPIC_ORDER, groupId = KafkaTopicConstant.GROUP_PAYMENT)
    public void onEvent(OrchestratorRequestDTO requestDTO) {
        log.info("RECEIVE ORDER {}", requestDTO);
        orchestratorService.processOrder(requestDTO);
    }
}
