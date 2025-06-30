package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    HmcHearingApi.class
})
@PropertySource("classpath:application.properties")
public class HmcHearingApiConsumerApplication {

}
