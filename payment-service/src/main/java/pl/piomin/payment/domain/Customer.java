package pl.piomin.payment.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "customers")
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int amountTotal;
    private int amountAvailable;
    private int amountReserved;

    public Customer() {
    }

    public Customer(Long id, String name, int amountAvailable, int amountReserved) {
        this.id = id;
        this.name = name;
        this.amountAvailable = amountAvailable;
        this.amountReserved = amountReserved;
    }
}
