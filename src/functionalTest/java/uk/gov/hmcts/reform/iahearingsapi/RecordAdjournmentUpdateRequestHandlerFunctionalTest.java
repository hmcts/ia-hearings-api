package uk.gov.hmcts.reform.iahearingsapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ADJOURNMENT_WHEN;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_CANCEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_UPDATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_FORMAT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;

import java.time.LocalDateTime;
import java.util.Optional;
import io.restassured.http.Header;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;

@Slf4j
@Disabled
@ActiveProfiles("functional")
public class RecordAdjournmentUpdateRequestHandlerFunctionalTest extends CcdCaseCreationTest {

    @BeforeEach
    void getAuthentications() {
        fetchTokensAndUserIds();
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_handle_record_adjournment_details_cancellation_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);
        createHearing(result);

        AsylumCase asylumCase = result.getCaseData();
        asylumCase.write(RELIST_CASE_IMMEDIATELY, "No");
        asylumCase.write(HEARING_ADJOURNMENT_WHEN, BEFORE_HEARING_DATE);
        asylumCase.write(ADJOURNMENT_DETAILS_HEARING, new DynamicList(getHearingId(result.getCaseId())));
        asylumCase.write(HEARING_REASON_TO_CANCEL, new DynamicList("reclassified"));
        asylumCase.write(NEXT_HEARING_DATE, "2023-11-28T09:45:00.000");
        asylumCase.write(NEXT_HEARING_FORMAT, new DynamicList("INTER"));

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails), RECORD_ADJOURNMENT_DETAILS);
        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .extract().response();

        assertEquals(200, response.statusCode());
    }


    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_handle_record_adjournment_details_update_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);
        createHearing(result);

        AsylumCase asylumCase = result.getCaseData();
        asylumCase.write(RELIST_CASE_IMMEDIATELY, "Yes");
        asylumCase.write(HEARING_ADJOURNMENT_WHEN, BEFORE_HEARING_DATE);
        asylumCase.write(ADJOURNMENT_DETAILS_HEARING, new DynamicList(getHearingId(result.getCaseId())));
        asylumCase.write(HEARING_REASON_TO_UPDATE, new DynamicList("reclassified"));
        asylumCase.write(NEXT_HEARING_DATE, "2023-11-28T09:45:00.000");
        asylumCase.write(NEXT_HEARING_FORMAT, new DynamicList("INTER"));

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            result.getCaseData(),
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails),
                                                                RECORD_ADJOURNMENT_DETAILS);
        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .extract().response();

        assertEquals(200, response.statusCode());
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

        Callback<CaseData> callback = new Callback<>(caseDetails, Optional.of(caseDetails), RECORD_ADJOURNMENT_DETAILS);

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
            .build();
        given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, legalRepToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("/test")
            .then()
            .log().all(true)
            .extract().response();
    }

    private String getHearingId(Long caseId) {
        HearingsGetResponse hearingsResponse = getHearingForCase(Long.toString(caseId));

        return hearingsResponse.getCaseHearings().stream().findFirst()
            .map(CaseHearing::getHearingRequestId).orElse(null);
    }
}
