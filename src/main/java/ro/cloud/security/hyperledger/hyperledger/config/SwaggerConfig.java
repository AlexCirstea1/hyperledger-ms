package ro.cloud.security.hyperledger.hyperledger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hyperledger Microservice API")
                        .version("1.0")
                        .description(
                                "Spring Boot application for managing user events on Hyperledger Fabric blockchain")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
