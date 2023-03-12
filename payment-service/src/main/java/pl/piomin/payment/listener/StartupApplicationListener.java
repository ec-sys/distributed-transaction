package pl.piomin.payment.listener;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pl.piomin.payment.domain.Customer;
import pl.piomin.payment.repository.CustomerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class StartupApplicationListener implements ApplicationRunner {

    @Autowired
    private CustomerRepository repository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        generateData();
    }

    public void generateData() {
        Random r = new Random();
        Faker faker = new Faker();
        for (int j = 0; j < 2; j++) {
            List<Customer> customers = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                int count = r.nextInt(100000);
                Customer c = new Customer(null, faker.name().fullName(), count, 0);
                c.setAmountTotal(count);
                customers.add(c);
            }
            repository.saveAll(customers);
            log.info("DONE : {}", j);
        }
    }
}
