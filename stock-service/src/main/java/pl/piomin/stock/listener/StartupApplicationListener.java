package pl.piomin.stock.listener;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pl.piomin.stock.domain.Product;
import pl.piomin.stock.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class StartupApplicationListener implements ApplicationRunner {

    @Autowired
    private ProductRepository repository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // generateData();
    }

    private void generateData() {
        Random r = new Random();
        Faker faker = new Faker();
        for (int j = 0; j < 1; j++) {
            List<Product> products = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                int count = r.nextInt(1000);
                int prize = r.nextInt(2000);
                Product p = new Product();
                p.setName(faker.commerce().productName());
                p.setAvailableItems(count);
                p.setReservedItems(0);
                p.setProductPrize(prize);
                products.add(p);
            }
            System.out.println("DONE: " + j);
            repository.saveAll(products);
        }
    }
}
