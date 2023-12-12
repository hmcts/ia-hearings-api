package uk.gov.hmcts.reform.iahearingsapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_DECISION_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.DECIDE_AN_APPLICATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;

import io.restassured.http.Header;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;

@ActiveProfiles("functional")
@Slf4j
public class DecideAnApplicationHandlerFunctionalTest  extends CcdCaseCreationTest {

    private static final String HEARING_ID = "12345";

    @BeforeEach
    void getAuthentications() {
        fetchTokensAndUserIds();
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_handle_decide_an_application_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);
        createHearing(result);

        AsylumCase asylumCase = result.getCaseData();
        asylumCase.write(MAKE_AN_APPLICATION_DECISION_REASON, "Some reason");

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails), DECIDE_AN_APPLICATION);
        given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("data.manualCanHearingRequired", notNullValue());
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_fail_to_handle_decide_an_application_due_to_invalid_authentication(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails), DECIDE_AN_APPLICATION);

        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, "invalidToken"))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .log().all(true)
            .extract().response();

        assertEquals(401, response.getStatusCode());
    }

    private void createHearing(Case result) {
        listCaseWithRequiredFields();
        HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference(Long.toString(result.getCaseId()))
            .hearingId(HEARING_ID)
            .build();
        given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, legalRepToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("asylum/test")
            .then()
            .log().all(true)
            .extract().response();
    }
}
