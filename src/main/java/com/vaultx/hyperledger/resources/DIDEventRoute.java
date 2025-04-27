package com.vaultx.hyperledger.resources;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import com.vaultx.hyperledger.service.DIDEventProcessor;

@Component
@RequiredArgsConstructor
public class DIDEventRoute extends RouteBuilder {

    private final DIDEventProcessor DIDEventProcessor;

    @Override
    public void configure() throws Exception {
        from("direct:processDocumentMetadata").process(DIDEventProcessor);
        //                .to("log:received-message");
    }
}
