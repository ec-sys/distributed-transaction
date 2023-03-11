package pl.piomin.payment.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.piomin.base.domain.dto.PaymentRequestDTO;
import pl.piomin.base.domain.dto.PaymentResponseDTO;
import pl.piomin.payment.service.PaymentService;

@RestController
@RequestMapping("payment")
public class PaymentOrderController {

    @Autowired
    private PaymentService service;

    @PostMapping("/debit")
    public PaymentResponseDTO debit(@RequestBody PaymentRequestDTO requestDTO) {
        return this.service.debit(requestDTO);
    }

    @PostMapping("/credit")
    public void credit(@RequestBody PaymentRequestDTO requestDTO) {
        this.service.credit(requestDTO);
    }
}
