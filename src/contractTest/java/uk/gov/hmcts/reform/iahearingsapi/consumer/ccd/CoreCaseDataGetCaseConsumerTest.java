package uk.gov.hmcts.reform.iahearingsapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.consumer.ccd.util.CcdConsumerTestBase;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.consumer.ccd.util.PactDslBuilderForCaseDetailsList.buildGetCaseDetailsPactDsl;
import static uk.gov.hmcts.reform.iahearingsapi.consumer.ccd.util.PactDslBuilderForCaseDetailsList.buildSubmittedCaseDetailsPactDsl;

public class CoreCaseDataGetCaseConsumerTest extends CcdConsumerTestBase {

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "ia_hearingsApi")
    public RequestResponsePact getCase(PactDslWithProvider builder) throws JSONException {
        return builder
            .given("A case has been submitted")
            .uponReceiving("A request to retrieve case data")
            .path("/cases/" + CASE_ID)
            .method("GET")
            .matchHeader(AUTHORIZATION, AUTHORIZATION_TOKEN)
            .matchHeader(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .matchHeader(EXPERIMENTAL, "true")
            .willRespondWith()
            .matchHeader(CONTENT_TYPE, CASE_CONTENT_TYPE_EXPRESSION, CASE_CONTENT_TYPE)
            .status(200)
            .body(buildGetCaseDetailsPactDsl(CASE_ID))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getCase", pactVersion = PactSpecVersion.V3)
    public void verifyGetCase() {

        final CaseDetails caseDetails = coreCaseDataApi.getCase(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            CASE_ID.toString()
        );

        assertThat(caseDetails.getId(), is(CASE_ID));
        assertThat(caseDetails.getJurisdiction(), is("IA"));
        assertCaseDetails(caseDetails);
    }

    private void assertCaseDetails(final CaseDetails caseDetails) {
        assertNotNull(caseDetails);

        Map<String, Object> caseDataMap = caseDetails.getData();

        assertThat(caseDataMap.get("appellantTitle"), is("Mr"));
        assertThat(caseDataMap.get("appellantGivenNames"), is("Bob"));
        assertThat(caseDataMap.get("appellantFamilyName"), is("Smith"));
        assertThat(caseDataMap.get("appellantDateOfBirth"), is("1990-12-07"));
        assertThat(caseDataMap.get("homeOfficeReferenceNumber"), is("000123456"));
        assertThat(caseDataMap.get("homeOfficeDecisionDate"), is("2019-08-01"));
        assertThat(caseDataMap.get("appealType"), is("protection"));
        assertThat(caseDataMap.get("submissionOutOfTime"), is("Yes"));
        assertThat(caseDataMap.get("applicationOutOfTimeExplanation"), is("test case"));

        //caseManagementLocation
        @SuppressWarnings("unchecked")
        Map<String, String> caseManagementLocation = (Map<String, String>) caseDataMap.get("caseManagementLocation");
        assertThat(caseManagementLocation.get("region"), is("1"));
        assertThat(caseManagementLocation.get("baseLocation"), is("765324"));

        assertThat(caseDataMap.get("staffLocation"), is("Taylor House"));

    }
}
