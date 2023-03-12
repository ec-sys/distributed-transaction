package pl.piomin.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import pl.piomin.base.domain.constants.KafkaTopicConstant;

@Configuration
@EnableKafka
public class KafkaTopicConfig {
    @Bean
    public NewTopic orders() {
        return TopicBuilder.name(KafkaTopicConstant.TOPIC_ORDER)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    public NewTopic paymentTopic() {
        return TopicBuilder.name(KafkaTopicConstant.TOPIC_PAYMENT)
                .partitions(3)
                .compact()
                .build();
    }

    @Bean
    public NewTopic stockTopic() {
        return TopicBuilder.name(KafkaTopicConstant.TOPIC_STOCK)
                .partitions(3)
                .compact()
                .build();
    }
}
