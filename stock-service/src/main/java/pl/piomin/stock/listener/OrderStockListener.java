package pl.piomin.stock.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderStockListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrderStockListener.class);

//    @Autowired
//    OrderManageService orderManageService;
//
//    @KafkaListener(id = "orders", topics = KafkaTopicConstant.TOPIC_ORDER, groupId = KafkaTopicConstant.GROUP_STOCK)
//    public void onEvent(Order o) {
//        LOG.info("Received: {}", o);
//        if (o.getStatus().equals(OrderStatus.NEW))
//            orderManageService.reserve(o);
//        else
//            orderManageService.confirm(o);
//    }
}
