package uk.gov.hmcts.reform.iahearingsapi.consumer;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.consumer.HmcHearingApiConsumerTestBase.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.iahearingsapi.consumer.HmcHearingApiConsumerTestBase.AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.reform.iahearingsapi.consumer.HmcHearingApiConsumerTestBase.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.iahearingsapi.consumer.HmcHearingApiConsumerTestBase.SERVICE_AUTH_TOKEN;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.pactfoundation.consumer.dsl.LambdaDsl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.CaseLinkDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.CaseLinkInfo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.GetLinkedCasesResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking.Reason;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.LinkedCasesApi;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@TestPropertySource(properties = {"core_case_data.api.url=http://localhost:4452", "idam.api.url=http://localhost:5000"})
@ContextConfiguration(classes = { LinkedCasesApiConsumerApplication.class })
@PactTestFor(providerName = "linked-cases-api", port = "4452")
public class LinkedCasesApiConsumerTest {

    @Autowired
    LinkedCasesApi linkedCasesApi;

    @Pact(provider = "linked-cases-api", consumer = "ia_hearingsApi")
    public RequestResponsePact generatePactFragmentForLinkedCases(PactDslWithProvider builder)
        throws JsonProcessingException {

        // @formatter:off
        Map<String, String> headers = Map.of(CONTENT_TYPE, APPLICATION_JSON_VALUE,
                                             AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                                             SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN);
        return builder
            .given("a case initiated a link to this case")
            .uponReceiving("a request to retrieve linked parent cases")
            .path("/getLinkedCases/1111222233334444")
            .query("startRecordNumber=1&maxReturnRecordCount=1")
            .method("GET")
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(createGetLinkedCasesResponseResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForLinkedCases")
    public void verifyGetLinkedCases() {

        GetLinkedCasesResponse getLinkedCasesResponse =
            linkedCasesApi.getLinkedCases(AUTHORIZATION_TOKEN,
                                          SERVICE_AUTH_TOKEN,
                                          "1111222233334444",
                                          "1",
                                          "1");

        assertNotNull(getLinkedCasesResponse);

        final CaseLinkInfo caseLinkInfo = getLinkedCasesResponse.getLinkedCases().get(0);
        final CaseLinkDetails caseLinkDetails = caseLinkInfo.getLinkDetails().get(0);
        final LocalDateTime createdDateTime = caseLinkDetails.getCreatedDateTime();
        final Reason reason = caseLinkDetails.getReasons().get(0);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        assertFalse(getLinkedCasesResponse.isHasMoreRecords());
        assertEquals("1111222233334444", caseLinkInfo.getCaseReference());
        assertEquals("Asylum", caseLinkInfo.getCcdCaseType());
        assertEquals("appealSubmitted", caseLinkInfo.getState());
        assertEquals("Name LastName", caseLinkInfo.getCaseNameHmctsInternal());
        assertEquals("IA", caseLinkInfo.getCcdJurisdiction());
        assertEquals("2024-04-10T12:03:21", createdDateTime.format(formatter));
        assertEquals("CLRC015", reason.getReasonCode());
    }

    private DslPart createGetLinkedCasesResponseResponse() {
        return LambdaDsl
            .newJsonBody(
                (getLinkedCasesResponse) -> getLinkedCasesResponse
                    .booleanType("hasMoreRecords", false)
                    .array("linkedCases", (linkedCases) -> {
                        linkedCases
                            .object(linkedCase -> {
                                linkedCase.stringType("caseNameHmctsInternal", "Name LastName");
                                linkedCase.stringType("caseReference", "1111222233334444");
                                linkedCase.stringType("ccdCaseType", "Asylum");
                                linkedCase.stringType("ccdJurisdiction", "IA");
                                linkedCase.stringType("state", "appealSubmitted");
                                linkedCase
                                    .array("linkDetails", (linkDetails) -> {
                                        linkDetails
                                            .object(linkDetail -> {
                                                linkDetail
                                                    .stringType("createdDateTime", "2024-04-10T12:03:21.00000");
                                                linkDetail
                                                    .array("reasons", (reasons) -> {
                                                        reasons.object(reason -> {
                                                            reason.stringType("reasonCode", "CLRC015");
                                                            reason.stringType("otherDescription", "");
                                                        });
                                                    });
                                            });
                                    });
                            });
                    })
            ).build();
    }
}
