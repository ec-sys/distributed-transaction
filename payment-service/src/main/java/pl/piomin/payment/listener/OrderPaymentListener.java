package pl.piomin.payment.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.piomin.base.domain.Order;
import pl.piomin.base.domain.constants.KafkaTopicConstant;
import pl.piomin.base.domain.enums.OrderStatus;
import pl.piomin.payment.service.OrderManageService;

@Component
public class OrderPaymentListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrderPaymentListener.class);

    @Autowired
    OrderManageService orderManageService;

    @KafkaListener(id = "orders", topics = KafkaTopicConstant.TOPIC_ORDER, groupId = KafkaTopicConstant.GROUP_PAYMENT)
    public void onEvent(Order o) {
        LOG.info("Received: {}", o);
        if (o.getStatus().equals(OrderStatus.NEW))
            orderManageService.reserve(o);
        else
            orderManageService.confirm(o);
    }
}
