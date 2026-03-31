package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration")
@Disabled("Disabled since pipeline causes issues opening/closing many endpoints at once - DIAC-1427")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FeignResilienceIntegrationTest {

    private WireMockServer wireMockServer;

    @Autowired
    private IdamApi idamApi;

    @Autowired
    private HmcHearingApi hmcHearingApi;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        com.github.tomakehurst.wiremock.client.WireMock.configureFor("localhost", wireMockServer.port());
        System.setProperty("idam.baseUrl", "http://localhost:" + wireMockServer.port());
        System.setProperty("hmc.baseUrl", "http://localhost:" + wireMockServer.port());

    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
        System.clearProperty("idam.baseUrl");
        System.clearProperty("hmc.baseUrl");
    }

    @Test
    void idamApi_shouldTriggerFallback_whenServiceReturns500() {
        stubFor(get(urlEqualTo("/o/userinfo"))
                    .willReturn(aResponse()
                                    .withStatus(500)));

        assertThrows(Exception.class,
                     () -> idamApi.userInfo("Bearer someToken"));
    }

    @Test
    void idamApi_shouldTriggerFallback_whenTokenEndpointReturns500() {
        stubFor(post(urlEqualTo("/o/token"))
                    .willReturn(aResponse()
                                    .withStatus(500)));

        assertThrows(Exception.class,
                     () -> idamApi.token(java.util.Map.of("grant_type", "password")));
    }

    @Test
    void hmcHearingApi_shouldTriggerFallback_whenServiceReturns500() {
        stubFor(get(urlEqualTo("/hearing/12345"))
                    .willReturn(aResponse()
                                    .withStatus(500)));

        assertThrows(Exception.class,
                     () -> hmcHearingApi.getHearingRequest(
                         "Bearer token", "serviceAuth",
                         null, null, null,
                         "12345", null));
    }

    @Test
    void idamApi_shouldTriggerFallback_whenServiceTimesOut() {
        stubFor(get(urlEqualTo("/o/userinfo"))
                    .willReturn(aResponse()
                                    .withFixedDelay(5000)
                                    .withStatus(200)));

        assertThrows(Exception.class,
                     () -> idamApi.userInfo("Bearer someToken"));
    }

    @Test
    void idamApi_circuitBreaker_shouldOpenAfterRepeatedFailures() {
        // stub to always return 500
        stubFor(get(urlEqualTo("/o/userinfo"))
                    .willReturn(aResponse().withStatus(500)));

        // fire enough requests to trip the circuit breaker
        // minimumNumberOfCalls is 3 in integration yaml so we need at least 3
        for (int i = 0; i < 3; i++) {
            assertThrows(Exception.class,
                         () -> idamApi.userInfo("Bearer someToken"));
        }

        //change wiremock to return 200 but circuit should still be open
        stubFor(get(urlEqualTo("/o/userinfo"))
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("{}")));

        assertThrows(Exception.class,
                     () -> idamApi.userInfo("Bearer someToken"));
    }

    @Test
    void hmcHearingApi_circuitBreaker_shouldOpenAfterRepeatedFailures() {
        stubFor(get(urlEqualTo("/hearing/12345"))
                    .willReturn(aResponse()
                                    .withStatus(500)));

        for (int i = 0; i < 3; i++) {
            assertThrows(Exception.class,
                         () -> hmcHearingApi.getHearingRequest(
                             "Bearer token", "serviceAuth",
                             null, null, null,
                             "12345", null));
        }

        stubFor(get(urlEqualTo("/hearing/12345"))
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("{}")));

        assertThrows(Exception.class,
                     () -> hmcHearingApi.getHearingRequest(
                         "Bearer token", "serviceAuth",
                         null, null, null,
                         "12345", null));
    }
}
