package pl.piomin.payment.service;

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
import pl.piomin.payment.domain.Reservation;
import pl.piomin.payment.repository.CustomerRepository;

import java.util.Random;

@Service
public class OrderManageService {

    private static final OrderSource SOURCE = OrderSource.PAYMENT;
    private static final Logger LOG = LoggerFactory.getLogger(OrderManageService.class);
    private CustomerRepository repository;
    private KafkaTemplate<Long, Order> template;

    public OrderManageService(CustomerRepository repository, KafkaTemplate<Long, Order> template) {
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

        KeyValueBytesStoreSupplier customerOrderStoreSupplier = Stores.persistentKeyValueStore(KafkaTopicConstant.STORED_CUSTOMER_ORDER);

        stream.selectKey((k, v) -> v.getCustomerId())
                .groupByKey(Grouped.with(Serdes.Long(), orderSerde))
                .aggregate(
                        () -> new Reservation(random.nextInt(1000)),
                        (id, order, rsv) -> {
                            String orderStatus = order.getStatus().toString();
                            switch (orderStatus) {
                                case "CONFIRMED":
                                    rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
                                    break;
                                case "ROLLBACK":
                                    if (!order.getSource().equals(OrderSource.PAYMENT)) {
                                        rsv.setAmountAvailable(rsv.getAmountAvailable() + order.getPrice());
                                        rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
                                    }
                                    break;
                                case "NEW":
                                    if (order.getPrice() <= rsv.getAmountAvailable()) {
                                        rsv.setAmountAvailable(rsv.getAmountAvailable() - order.getPrice());
                                        rsv.setAmountReserved(rsv.getAmountReserved() + order.getPrice());
                                        order.setStatus(OrderStatus.ACCEPT);
                                    } else {
                                        order.setStatus(OrderStatus.REJECT);
                                    }
                                    template.send(KafkaTopicConstant.TOPIC_PAYMENT, order.getId(), order);
                                    break;
                            }
                            return rsv;
                        },
                        Materialized.<Long, Reservation>as(customerOrderStoreSupplier)
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(rsvSerde))
                .toStream()
                .peek((k, trx) -> LOG.info("Commit: {}", trx));

        return stream;
    }
}
