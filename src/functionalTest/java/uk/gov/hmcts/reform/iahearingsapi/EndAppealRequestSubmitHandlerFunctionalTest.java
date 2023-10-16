package uk.gov.hmcts.reform.iahearingsapi;

import java.time.LocalDateTime;
import java.util.Optional;
import static io.restassured.RestAssured.given;
import static java.lang.Long.parseLong;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;

@ActiveProfiles("functional")
public class EndAppealRequestSubmitHandlerFunctionalTest extends CcdCaseCreationTest {

    @Test
    void should_submit_end_appeal_successfully() {
        AsylumCase asylumCase = new AsylumCase();
        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            parseLong(getCaseId()),
            "IA",
            LISTING,
            asylumCase,
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), END_APPEAL);

        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .extract().response();

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void should_fail_on_submit_end_appeal_if_case_id_cant_be_found() {
        AsylumCase asylumCase = new AsylumCase();
        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            00000111,
            "IA",
            LISTING,
            asylumCase,
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), END_APPEAL);

        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .extract().response();

        assertEquals(500, response.getStatusCode());
    }
}
