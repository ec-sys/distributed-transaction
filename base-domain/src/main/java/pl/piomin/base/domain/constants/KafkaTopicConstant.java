package pl.piomin.base.domain.constants;

public class KafkaTopicConstant {
    public static final String TOPIC_ORDER = "orders";
    public static final String TOPIC_PAYMENT = "payment-orders";
    public static final String TOPIC_STOCK = "stock-orders";

    public static final String GROUP_PAYMENT = "payment";
    public static final String GROUP_STOCK = "stock";

    public static final String STORED_ORDER = "stored-orders";
    public static final String STORED_CUSTOMER_ORDER = "stored-customer-orders";
    public static final String STORED_PAYMENT_ORDER = "stored-payment-orders";
}
