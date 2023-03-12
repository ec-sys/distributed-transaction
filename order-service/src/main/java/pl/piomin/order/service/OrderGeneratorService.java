package pl.piomin.order.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.piomin.base.domain.constants.KafkaTopicConstant;
import pl.piomin.base.domain.dto.OrderItemDTO;
import pl.piomin.base.domain.dto.OrderRequestDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderGeneratorService {

    private static Random RAND = new Random();
    private AtomicLong id = new AtomicLong();
    private Executor executor;
    private KafkaTemplate<String, OrderRequestDTO> template;

    public OrderGeneratorService(Executor executor, KafkaTemplate<String, OrderRequestDTO> template) {
        this.executor = executor;
        this.template = template;
    }

    @Async
    public void generate() {
        for (int i = 0; i < 10000; i++) {
            int x = RAND.nextInt(20) + 1;
            OrderRequestDTO requestDTO = new OrderRequestDTO();
            requestDTO.setOrderId(UUID.randomUUID().toString());
            requestDTO.setAmount(100 * x);
            requestDTO.setCustomerId(new Long(RAND.nextInt(100)));

            // order items
            List<OrderItemDTO> itemDTOList = new ArrayList<>();
            for (int j = 0; j < x; j++) {
                OrderItemDTO orderItem = new OrderItemDTO();
                orderItem.setProductId(new Long(RAND.nextInt(100)));
                orderItem.setProductPrize(100 * (RAND.nextInt(5) + 1));
                orderItem.setProductCount(RAND.nextInt(5) + 1);
                itemDTOList.add(orderItem);
            }
            requestDTO.setItems(itemDTOList);
            template.send(KafkaTopicConstant.TOPIC_ORDER, requestDTO.getOrderId(), requestDTO);
        }
    }

    private long randomId() {
        long leftLimit = 1L;
        long rightLimit = 100L;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
    }
}
