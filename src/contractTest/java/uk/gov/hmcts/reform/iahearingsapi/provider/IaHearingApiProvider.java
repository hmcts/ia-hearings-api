package uk.gov.hmcts.reform.iahearingsapi.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.CASE_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.IAC_PROVIDER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.generateServiceHearingValues;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.generateHearingLinkData;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLinkData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers.HearingsController;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@Provider(IAC_PROVIDER)
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:80}")
public class IaHearingApiProvider {

    @Mock
    protected HearingService hearingService;

    private MockMvc mockMvc;
    private HearingsController controller;

    @BeforeEach
    void before(PactVerificationContext context) {

        controller =
            new HearingsController(hearingService);

        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .addFilter((request, response, chain) -> {
                HttpServletResponse httpResp = (HttpServletResponse) response;
                httpResp.addHeader("Connection", "close");
                chain.doFilter(request, response);
            })
            .build();

        context.setTarget(
            new MockMvcTestTarget(mockMvc)
        );
    }

    @AfterEach
    public void teardown(PactVerificationContext context) {
        reset(hearingService);
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    public void pactVerificationTestTemplate(PactVerificationContext context) {
        if (null != context) {
            context.verifyInteraction();
        }
    }

    @State(IAC_PROVIDER + " successfully returns serviceHearingValue for a given case reference")
    public void getHearingsValues() {
        final ServiceHearingValuesModel serviceHearingValues = generateServiceHearingValues();
        when(hearingService.getServiceHearingValues(any()))
            .thenReturn(serviceHearingValues);

        final HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference(CASE_REFERENCE).build();
        ResponseEntity<ServiceHearingValuesModel> responseEntity = controller
            .getHearingsValues(payload);

        verify(hearingService, times(1))
            .getServiceHearingValues(any());
        assertNotNull(responseEntity.getBody());
        assertEquals(serviceHearingValues, responseEntity.getBody(), "Hearings values not as expected.");
    }

    @State(IAC_PROVIDER + " successfully returns hearings link data for a given case reference")
    public void getHearingsLinkData() {
        HearingRequestPayload payload = HearingRequestPayload.builder().caseReference(CASE_REFERENCE).build();
        List<HearingLinkData> hearingLinkDataList = generateHearingLinkData(CASE_REFERENCE);

        when(hearingService.getHearingLinkData(payload)).thenReturn(hearingLinkDataList);

        HearingsController controller = new HearingsController(hearingService);
        ResponseEntity<List<HearingLinkData>> responseEntity = controller
            .getHearingsLinkData(payload);

        verify(hearingService, times(1))
            .getHearingLinkData(any());
        assertNotNull(responseEntity.getBody());
        assertEquals(hearingLinkDataList, responseEntity.getBody(), "Hearing link data not as expected.");
    }
}
