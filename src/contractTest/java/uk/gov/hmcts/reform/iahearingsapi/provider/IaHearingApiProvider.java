package uk.gov.hmcts.reform.iahearingsapi.provider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.IAC_PROVIDER;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.generateHearingLinkData;
import static uk.gov.hmcts.reform.iahearingsapi.DataProvider.generateServiceHearingValues;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLinkData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers.HearingsController;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Provider(IAC_PROVIDER)
@PactBroker(scheme = "https", host = "${PACT_BROKER_FULL_URL}")
public class IaHearingApiProvider {

    @Mock
    protected HearingService hearingService;

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
        final String validCaseRef = "9372710950276233";
        final ServiceHearingValuesModel serviceHearingValues = generateServiceHearingValues();
        final HearingRequestPayload payload = HearingRequestPayload.builder().caseReference(validCaseRef).build();

        when(hearingService.getServiceHearingValues(payload)).thenReturn(serviceHearingValues);

        HearingsController controller = new HearingsController(hearingService);
        ResponseEntity<ServiceHearingValuesModel> responseEntity = controller
            .getHearingsValues(payload);

        verify(hearingService, times(1))
            .getServiceHearingValues(any());
        Assert.isTrue(
            Objects.requireNonNull(responseEntity.getBody()).equals(serviceHearingValues),
            "Hearings values not as expected.");
    }

    @State(IAC_PROVIDER + " successfully returns hearings link data for a given case reference")
    public void getHearingsLinkData() {
        final String caseRef = "9372710950276233";
        HearingRequestPayload payload = HearingRequestPayload.builder().caseReference(caseRef).build();
        List<HearingLinkData> hearingLinkDataList = generateHearingLinkData(caseRef);

        when(hearingService.getHearingLinkData(payload)).thenReturn(hearingLinkDataList);

        HearingsController controller = new HearingsController(hearingService);
        ResponseEntity<List<HearingLinkData>> responseEntity = controller
            .getHearingsLinkData(payload);

        verify(hearingService, times(1))
            .getHearingLinkData(any());
        Assert.isTrue(
            Objects.requireNonNull(responseEntity.getBody()).equals(hearingLinkDataList),
            "Hearing link data not as expected.");
    }

}
