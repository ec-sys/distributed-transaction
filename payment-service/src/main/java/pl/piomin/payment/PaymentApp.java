package pl.piomin.payment;

import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import pl.piomin.payment.domain.Customer;
import pl.piomin.payment.repository.CustomerRepository;

import javax.annotation.PostConstruct;
import java.util.Random;

@SpringBootApplication
@EnableKafka
public class PaymentApp {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentApp.class);
    @Autowired
    private CustomerRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(PaymentApp.class, args);
    }

//    @PostConstruct
    public void generateData() {
        Random r = new Random();
        Faker faker = new Faker();
        for (int i = 0; i < 100; i++) {
            int count = r.nextInt(1000);
            Customer c = new Customer(null, faker.name().fullName(), count, 0);
            repository.save(c);
        }
    }
}
