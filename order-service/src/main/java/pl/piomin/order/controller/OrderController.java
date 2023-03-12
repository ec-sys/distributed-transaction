package pl.piomin.order.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.piomin.base.domain.dto.OrderRequestDTO;
import pl.piomin.base.domain.dto.OrderResponseDTO;
import pl.piomin.order.service.OrderGeneratorService;
import pl.piomin.order.service.OrderManageService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    OrderManageService orderManageService;

    @Autowired
    OrderGeneratorService orderGeneratorService;

    @PostMapping
    public OrderResponseDTO create(@RequestBody OrderRequestDTO requestDTO) {
        return orderManageService.createOrder(requestDTO);
    }

    @PostMapping("/generate")
    public boolean create() {
        orderGeneratorService.generate();
        return true;
    }
}
