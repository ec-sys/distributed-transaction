package pl.piomin.order.domain;

public class PurchaseOrderException extends RuntimeException {

    public PurchaseOrderException(String message) {
        super(message);
    }
}
