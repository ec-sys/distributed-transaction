package pl.piomin.order.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;
import pl.piomin.base.domain.Order;
import pl.piomin.base.domain.constants.KafkaTopicConstant;
import pl.piomin.order.service.OrderManageService;

import java.time.Duration;

@Component
@Slf4j
public class OrderProcessStream {

    private static final Serde<Long> LONG_SERDE = Serdes.Long();

    @Autowired
    OrderManageService orderManageService;

    @Autowired
    public KStream<Long, Order> processOrderPipeline(StreamsBuilder builder) {
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);
        KStream<Long, Order> paymentStream = builder
                .stream(KafkaTopicConstant.TOPIC_PAYMENT, Consumed.with(Serdes.Long(), orderSerde));
        // join
        paymentStream.join(
                builder.stream(KafkaTopicConstant.TOPIC_STOCK),
                orderManageService::confirm,
                JoinWindows.of(Duration.ofSeconds(10)),
                StreamJoined.with(Serdes.Long(), orderSerde, orderSerde))
                .peek((k, o) -> log.info("Output: {}", o))
                .to(KafkaTopicConstant.TOPIC_ORDER);
        paymentStream.print(Printed.toSysOut());
        return paymentStream;
    }

    @Autowired
    public KTable<Long, Order> storeOrder(StreamsBuilder builder) {
        KeyValueBytesStoreSupplier store = Stores.persistentKeyValueStore(KafkaTopicConstant.STORED_ORDER);
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);
        KStream<Long, Order> streamOrder = builder
                .stream(KafkaTopicConstant.TOPIC_ORDER, Consumed.with(Serdes.Long(), orderSerde));
        return streamOrder.toTable(Materialized.<Long, Order>as(store)
                .withKeySerde(Serdes.Long())
                .withValueSerde(orderSerde));
    }
}
