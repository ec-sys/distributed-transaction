package pl.piomin.order;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pl.piomin.base.domain.Order;
import pl.piomin.order.controller.OrderController;
import pl.piomin.order.service.OrderGeneratorService;
import pl.piomin.order.service.OrderManageService;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
@EnableKafkaStreams
@EnableAsync
public class OrderApp {

    private static final Logger LOG = LoggerFactory.getLogger(OrderApp.class);

    public static void main(String[] args) {
        SpringApplication.run(OrderApp.class, args);
    }
}
