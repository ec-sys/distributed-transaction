package pl.piomin.stock.service;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;
import pl.piomin.base.domain.Order;
import pl.piomin.base.domain.constants.KafkaTopicConstant;
import pl.piomin.base.domain.enums.OrderSource;
import pl.piomin.base.domain.enums.OrderStatus;
import pl.piomin.stock.domain.Reservation;
import pl.piomin.stock.repository.ProductRepository;

import java.util.Random;

@Service
public class OrderManageService {

    private static final OrderSource SOURCE = OrderSource.STOCK;
    private static final Logger LOG = LoggerFactory.getLogger(OrderManageService.class);
    private ProductRepository repository;
    private KafkaTemplate<Long, Order> template;

    public OrderManageService(ProductRepository repository, KafkaTemplate<Long, Order> template) {
        this.repository = repository;
        this.template = template;
    }

    @Autowired
    public KStream<Long, Order> processOrderPipeline(StreamsBuilder builder) {
        Random random = new Random();
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);
        JsonSerde<Reservation> rsvSerde = new JsonSerde<>(Reservation.class);
        KStream<Long, Order> stream = builder
                .stream(KafkaTopicConstant.TOPIC_ORDER, Consumed.with(Serdes.Long(), orderSerde))
                .peek((k, order) -> LOG.info("New: {}", order));

        KeyValueBytesStoreSupplier stockOrderStoreSupplier = Stores.persistentKeyValueStore(KafkaTopicConstant.STORED_PAYMENT_ORDER);

        stream.selectKey((k, v) -> v.getProductId())
                .groupByKey(Grouped.with(Serdes.Long(), orderSerde))
                .aggregate(() -> new Reservation(random.nextInt(100)),
                        (id, order, rsv) -> {
                            String orderStatus = order.getStatus().toString();
                            switch (orderStatus) {
                                case "CONFIRMED":
                                    rsv.setItemsReserved(rsv.getItemsReserved() - order.getProductCount());
                                    break;
                                case "ROLLBACK":
                                    if (!order.getSource().equals(OrderSource.STOCK)) {
                                        rsv.setItemsAvailable(rsv.getItemsAvailable() + order.getProductCount());
                                        rsv.setItemsReserved(rsv.getItemsReserved() - order.getProductCount());
                                    }
                                    break;
                                case "NEW":
                                    if (order.getProductCount() <= rsv.getItemsAvailable()) {
                                        rsv.setItemsAvailable(rsv.getItemsAvailable() - order.getProductCount());
                                        rsv.setItemsReserved(rsv.getItemsReserved() + order.getProductCount());
                                        order.setStatus(OrderStatus.ACCEPT);
                                    } else {
                                        order.setStatus(OrderStatus.REJECT);
                                    }
                                    template.send(KafkaTopicConstant.TOPIC_STOCK, order.getId(), order)
                                            .addCallback(result -> LOG.info("Sent: {}",
                                                    result != null ? result.getProducerRecord().value() : null),
                                                    ex -> { });
                                    break;
                            }
                            LOG.info("{}", rsv);
                            return rsv;
                        },
                        Materialized.<Long, Reservation>as(stockOrderStoreSupplier)
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(rsvSerde))
                .toStream()
                .peek((k, trx) -> LOG.info("Commit: {}", trx));

        return stream;
    }

}
