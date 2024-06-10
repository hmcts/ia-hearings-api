package uk.gov.hmcts.reform.iahearingsapi.provider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers.HearingsController;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Provider("iaServiceHearingProvider")
@PactBroker
public class GetServiceHearingValuesProvider {

    @LocalServerPort
    private int port;

    @MockBean
    protected HearingService mockService;

    @Value("${pact.verifier.publishResults:false}")
    private String publishResults;

    @BeforeEach
    public void setup(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", publishResults);
        if (null != context) {
            context.setTarget(new HttpTestTarget("localhost", port, "/"));
        }
    }

    @AfterEach
    public void teardown(PactVerificationContext context) {
        reset(mockService);
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    public void pactVerificationTestTemplate(PactVerificationContext context) {
        if (null != context) {
            context.verifyInteraction();
        }
    }

    @State("iac hearingService successfully returns serviceHearingValue for valid case ref")
    public void getHearingsValues() {
        final String validCaseRef = "9372710950276233";
        ServiceHearingValuesModel serviceHearingValues = generateServiceHearingValues();
        doReturn(serviceHearingValues).when(mockService)
            .getServiceHearingValues(any());
        HearingsController controller = new HearingsController(mockService);
        ResponseEntity<ServiceHearingValuesModel> responseEntity = controller
            .getHearingsValues(HearingRequestPayload.builder().caseReference(validCaseRef).build());
        verify(mockService, times(1))
            .getServiceHearingValues(any());
        Assert.isTrue(
            Objects.requireNonNull(responseEntity.getBody()).toString().equals(serviceHearingValues.toString()),
            "Case ref value is not as expected.");
    }

    private ServiceHearingValuesModel generateServiceHearingValues() {
        return ServiceHearingValuesModel.builder()
            .hmctsServiceId("hmctsServiceId")
            .hmctsInternalCaseName("internalCaseName")
            .publicCaseName("publicName")
            .caseAdditionalSecurityFlag(false)
            .caseCategories(Collections.emptyList())
            .caseDeepLink("caseDeepLink")
            .caserestrictedFlag(false)
            .externalCaseReference("externalCaseReference")
            .caseSlaStartDate(LocalDate.now().toString())
            .caseManagementLocationCode("caseManagementLocationCode")
            .autoListFlag(false)
            .hearingType("hearingType")
            .hearingWindow(null)
            .duration(60)
            .hearingPriorityType(PriorityType.STANDARD)
            .numberOfPhysicalAttendees(3)
            .hearingInWelshFlag(false)
            .hearingLocations(Collections.emptyList())
            .facilitiesRequired(Collections.emptyList())
            .listingComments("listingComments")
            .hearingRequester("hearingRequester")
            .privateHearingRequiredFlag(false)
            .caseInterpreterRequiredFlag(false)
            .panelRequirements(null)
            .leadJudgeContractType("leadJudgeContractType")
            .judiciary(null)
            .hearingIsLinkedFlag(false)
            .parties(Collections.emptyList())
            .caseFlags(Caseflags.builder()
                           .flags(Collections.emptyList())
                           .flagAmendUrl("flagAmendUrl")
                           .build())
            .screenFlow(null)
            .vocabulary(Collections.emptyList())
            .hearingChannels(Collections.emptyList())
            .hearingLevelParticipantAttendance(Collections.emptyList())
            .build();
    }
}
