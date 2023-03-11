package pl.piomin.orchestrator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.piomin.base.domain.dto.OrchestratorRequestDTO;
import pl.piomin.base.domain.dto.OrchestratorResponseDTO;
import pl.piomin.orchestrator.service.OrchestratorService;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
public class OrchestratorConfig {

    @Autowired
    private OrchestratorService orchestratorService;

    @Bean
    public Function<Flux<OrchestratorRequestDTO>, Flux<OrchestratorResponseDTO>> processor() {
        return flux -> flux
                .flatMap(dto -> this.orchestratorService.orderProduct(dto))
                .doOnNext(dto -> System.out.println("Status : " + dto.getStatus()));
    }
}
