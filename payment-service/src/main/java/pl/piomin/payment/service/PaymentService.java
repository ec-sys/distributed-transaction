package pl.piomin.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.piomin.base.domain.dto.PaymentRequestDTO;
import pl.piomin.base.domain.dto.PaymentResponseDTO;
import pl.piomin.base.domain.enums.PaymentStatus;
import pl.piomin.payment.domain.Customer;
import pl.piomin.payment.repository.CustomerRepository;

@Service
@Slf4j
public class PaymentService {
    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public PaymentResponseDTO debit(final PaymentRequestDTO requestDTO) {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO();
        responseDTO.setAmount(requestDTO.getAmount());
        responseDTO.setCustomerId(requestDTO.getCustomerId());
        responseDTO.setOrderId(requestDTO.getOrderId());

        Customer customer = customerRepository.findById(requestDTO.getCustomerId()).orElseThrow();
        log.info("Found: {}", customer);
        int balance = customer.getAmountAvailable();
        int amount = requestDTO.getAmount();
        if(balance >= amount) {
            // accepted, save to db
            customer.setAmountAvailable(balance - amount);
            customer.setAmountReserved(customer.getAmountReserved() + amount);
            responseDTO.setStatus(PaymentStatus.PAYMENT_APPROVED);
            customerRepository.save(customer);
        } else {
            // rejected
            responseDTO.setStatus(PaymentStatus.PAYMENT_REJECTED);
        }
        return responseDTO;
    }

    @Transactional
    public void credit(final PaymentRequestDTO requestDTO){
        Customer customer = customerRepository.findById(requestDTO.getCustomerId()).orElseThrow();
        log.info("Found: {}", customer);
        // reverted, save to db
        customer.setAmountAvailable(customer.getAmountAvailable() + requestDTO.getAmount());
        customer.setAmountReserved(customer.getAmountReserved() - requestDTO.getAmount());
        customerRepository.save(customer);
    }
}
