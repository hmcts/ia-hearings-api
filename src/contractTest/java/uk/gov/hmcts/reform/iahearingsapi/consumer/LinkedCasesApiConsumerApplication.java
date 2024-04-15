package uk.gov.hmcts.reform.iahearingsapi.consumer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.LinkedCasesApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    LinkedCasesApi.class
})
@PropertySource("classpath:application.properties")
public class LinkedCasesApiConsumerApplication {

}
