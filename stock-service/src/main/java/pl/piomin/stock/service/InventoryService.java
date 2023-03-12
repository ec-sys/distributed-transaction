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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        Set<Long> dbProductIds = new HashSet<>();
        boolean isValid = true;
        for (Product item : products) {
            OrderItemDTO itemDTO = itemDTOMap.get(item.getId());
            // check product is out of date
            if (itemDTO.getProductPrize() != item.getProductPrize()) {
                log.error("PRODUCT PRIZE IS NOT MATCHING, ID {}, NAME {}, DB PRIZE: {}, ORDER PRIZE: {}",
                        item.getId(), item.getName(), item.getProductPrize(), itemDTO.getProductPrize());
                isValid = false;
            }
            // check product quantity
            if (itemDTO.getProductCount() > item.getAvailableItems()) {
                log.error("PRODUCT UNAVAILABLE, STOCK: ID {} - NAME {}, REQUEST: ID {}",
                        item.getId(), item.getName(), itemDTO.getProductId());
                isValid = false;
            }
            dbProductIds.add(item.getId());
            item.setAvailableItems(item.getAvailableItems() - itemDTO.getProductCount());
            item.setReservedItems(item.getReservedItems() + itemDTO.getProductCount());
        }

        // check have not exist in database
        Set<Long> remainIds = itemDTOMap.keySet();
        remainIds.removeAll(dbProductIds);
        if (remainIds.size() > 0) {
            log.error("PRODUCT PRIZE IS NOT MATCHING, ID {}, NAME {}", remainIds);
            isValid = false;
        }

        // return
        if (isValid) {
            productRepository.saveAll(products);
            responseDTO.setStatus(InventoryStatus.AVAILABLE);
        } else {
            responseDTO.setStatus(InventoryStatus.UNAVAILABLE);
        }
        return responseDTO;
    }

    public void addInventory(final InventoryRequestDTO requestDTO) {
        log.info("START REVERTING INVENTORY: {}", requestDTO);

        Map<Long, OrderItemDTO> itemDTOMap = requestDTO.getItems()
                .stream()
                .collect(Collectors.toMap(OrderItemDTO::getProductId, Function.identity()));
        Iterable<Product> products = productRepository.findAllById(itemDTOMap.keySet());
        for (Product item : products) {
            OrderItemDTO itemDTO = itemDTOMap.get(item.getId());
            item.setAvailableItems(item.getAvailableItems() + itemDTO.getProductCount());
            item.setReservedItems(item.getReservedItems() - itemDTO.getProductCount());
        }
        productRepository.saveAll(products);

        log.info("END REVERTING INVENTORY: {}", requestDTO);
    }
}
