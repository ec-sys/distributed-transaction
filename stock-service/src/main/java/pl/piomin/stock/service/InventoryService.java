package pl.piomin.stock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.piomin.base.domain.dto.InventoryRequestDTO;
import pl.piomin.base.domain.dto.InventoryResponseDTO;
import pl.piomin.base.domain.dto.OrderItemDTO;
import pl.piomin.base.domain.enums.InventoryStatus;
import pl.piomin.stock.domain.Product;
import pl.piomin.stock.repository.ProductRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InventoryService {

    @Autowired
    private ProductRepository productRepository;

    public InventoryResponseDTO deductInventory(final InventoryRequestDTO requestDTO) {
        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        Map<Long, OrderItemDTO> itemDTOMap = requestDTO.getItems()
                .stream()
                .collect(Collectors.toMap(OrderItemDTO::getProductId, Function.identity()));

        Iterable<Product> products = productRepository.findAllById(itemDTOMap.keySet());
        int productCount = 0;
        boolean isValid = true;
        for (Product item : products) {
            OrderItemDTO itemDTO = itemDTOMap.get(item.getId());
            if(itemDTO.getProductPrize() != item.getProductPrize()) {
                log.error("PRODUCT PRIZE IS NOT MATCHING, ID {}, NAME {}", item.getId(), item.getName());
                isValid = false;
            }
            if(itemDTO.getProductCount() > item.getAvailableItems()) {
                log.error("PRODUCT UNAVAILABLE, STOCK: ID {} - NAME {}, REQUEST: ID {}",
                        item.getId(), item.getName(), itemDTO.getProductId());
                isValid = false;
            }
            productCount++;
        }
        if(itemDTOMap.size() != productCount) {
            log.error("PRODUCT PRIZE IS NOT MATCHING, ID {}, NAME {}");
            isValid = false;
        }
        if(isValid) {
            responseDTO.setStatus(InventoryStatus.AVAILABLE);
        } else {
            responseDTO.setStatus(InventoryStatus.UNAVAILABLE);
        }
        products.forEach(item -> {
            OrderItemDTO itemDTO = itemDTOMap.get(item.getId());
            if(itemDTO.getProductPrize() != item.getProductPrize()) {
                log.error("PRODUCT PRIZE IS NOT MATCHING, ID {}, NAME {}", item.getId(), item.getName());
                isValid = true;
            }
        });

        int quantity = this.productInventoryMap.getOrDefault(requestDTO.getProductId(), 0);
        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setOrderId(requestDTO.getOrderId());
        responseDTO.setUserId(requestDTO.getUserId());
        responseDTO.setProductId(requestDTO.getProductId());
        responseDTO.setStatus(InventoryStatus.UNAVAILABLE);
        if(quantity > 0){
            responseDTO.setStatus(InventoryStatus.AVAILABLE);
            this.productInventoryMap.put(requestDTO.getProductId(), quantity - 1);
        }
        return responseDTO;
    }

    public void addInventory(final InventoryRequestDTO requestDTO){
        this.productInventoryMap
                .computeIfPresent(requestDTO.getProductId(), (k, v) -> v + 1);
    }
}
