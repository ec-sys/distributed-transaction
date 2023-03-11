package pl.piomin.order.controller;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import pl.piomin.base.domain.Order;
import pl.piomin.base.domain.constants.KafkaTopicConstant;
import pl.piomin.base.domain.dto.OrderRequestDTO;
import pl.piomin.base.domain.dto.OrderResponseDTO;
import pl.piomin.base.domain.enums.OrderSource;
import pl.piomin.base.domain.enums.OrderStatus;
import pl.piomin.order.service.OrderGeneratorService;
import pl.piomin.order.service.OrderManageService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    OrderManageService orderManageService;

    @Autowired
    OrderGeneratorService orderGeneratorService;

    @PostMapping
    public OrderResponseDTO create(@RequestBody OrderRequestDTO requestDTO) {
        return orderManageService.createOrder(requestDTO);
    }

    @PostMapping("/generate")
    public boolean create() {
        orderGeneratorService.generate();
        return true;
    }
}
