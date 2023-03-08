package pl.piomin.stock.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.piomin.base.domain.dto.InventoryRequestDTO;
import pl.piomin.base.domain.dto.InventoryResponseDTO;
import pl.piomin.stock.repository.ProductRepository;

@Service
public class InventoryService {

    @Autowired
    private ProductRepository productRepository;

    public InventoryResponseDTO deductInventory(final InventoryRequestDTO requestDTO){
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
