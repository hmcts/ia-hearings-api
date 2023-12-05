package uk.gov.hmcts.reform.iahearingsapi;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_RELISTED_UPDATE_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;

import io.cucumber.cienvironment.internal.com.eclipsesource.json.Json;
import io.restassured.http.Header;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;

@Slf4j
@ActiveProfiles("functional")
public class RecordAdjournmentUpdateRequestHandlerFunctionalTest extends CcdCaseCreationTest {

    private static final int BOUND = 99999999;
    Random rand;

    @BeforeEach
    void getAuthentications() {
        fetchTokensAndUserIds();
        rand = new Random();
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_handle_record_adjournment_details_cancellation_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);
        String hearingId = createHearingAndGetHearing(result);

        AsylumCase asylumCase = result.getCaseData();
        asylumCase.write(RELIST_CASE_IMMEDIATELY, "No");
        asylumCase.write(ADJOURNMENT_DETAILS_HEARING, new DynamicList(hearingId));
        asylumCase.write(HEARING_CANCELLATION_REASON, "reclassified");
        asylumCase.write(NEXT_HEARING_DATE, "2023-11-28T09:45:00.000");

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), RECORD_ADJOURNMENT_DETAILS);
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
    void should_handle_record_adjournment_details_update_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);
        String hearingId = createHearingAndGetHearing(result);

        AsylumCase asylumCase = result.getCaseData();
        asylumCase.write(RELIST_CASE_IMMEDIATELY, "Yes");
        asylumCase.write(ADJOURNMENT_DETAILS_HEARING, new DynamicList(hearingId));
        asylumCase.write(HEARING_RELISTED_UPDATE_REASON, "reclassified");
        asylumCase.write(NEXT_HEARING_DATE, "2023-11-28T09:45:00.000");

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), RECORD_ADJOURNMENT_DETAILS);
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
            .assertThat().body("data.updateHmcRequestSuccess", notNullValue());
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_fail_to_handle_record_adjournment_details_due_to_invalid_authentication(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), RECORD_ADJOURNMENT_DETAILS);

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

    private String createHearingAndGetHearing(Case result) {
        listCaseWithRequiredFields();
        String hearingId = rand.nextInt(BOUND) + "";
        HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference(Long.toString(result.getCaseId()))
            .hearingId(hearingId)
            .build();
        Response hearingCreationResponse = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, legalRepToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("asylum/test")
            .then()
            .log().all(true)
            .extract().response();
        Json.parse(hearingCreationResponse.getBody().asString());

        return hearingId;
    }
}
