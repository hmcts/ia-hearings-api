package uk.gov.hmcts.reform.iahearingsapi.consumer.ccd;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.LinkedCasesApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    CoreCaseDataApi.class,
    LinkedCasesApi.class
})
public class CoreCaseDataConsumerApplication {

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @MockBean
    RestTemplate restTemplate;
}
