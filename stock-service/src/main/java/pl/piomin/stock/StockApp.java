package pl.piomin.stock;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import pl.piomin.stock.domain.Product;
import pl.piomin.stock.repository.ProductRepository;

import javax.annotation.PostConstruct;
import java.util.Random;

@SpringBootApplication
@EnableKafka
public class StockApp {

    private static final Logger LOG = LoggerFactory.getLogger(StockApp.class);
    @Autowired
    private ProductRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(StockApp.class, args);
    }

//    @PostConstruct
    public void generateData() {
        Random r = new Random();
        Faker faker = new Faker();
        for (int i = 0; i < 1000; i++) {
            int count = r.nextInt(1000);
            Product p = new Product(null, faker.commerce().productName(), count, 0);
            repository.save(p);
        }
    }
}
