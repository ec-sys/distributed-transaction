package pl.piomin.order.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.piomin.base.domain.Order;
import pl.piomin.base.domain.constants.KafkaTopicConstant;
import pl.piomin.base.domain.enums.OrderStatus;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderGeneratorService {

    private static Random RAND = new Random();
    private AtomicLong id = new AtomicLong();
    private Executor executor;
    private KafkaTemplate<Long, Order> template;

    public OrderGeneratorService(Executor executor, KafkaTemplate<Long, Order> template) {
        this.executor = executor;
        this.template = template;
    }

    @Async
    public void generate() {
        for (int i = 0; i < 10000; i++) {
            int x = RAND.nextInt(5) + 1;
            Order o = new Order(id.incrementAndGet(), randomId() + 1, randomId(), OrderStatus.NEW);
            o.setPrice(100 * x);
            o.setProductCount(x);
            template.send(KafkaTopicConstant.TOPIC_ORDER, o.getId(), o);
        }
    }

    private long randomId() {
        long leftLimit = 1L;
        long rightLimit = 100L;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
    }
}
