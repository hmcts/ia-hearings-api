package uk.gov.hmcts.reform.iahearingsapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ADJOURNMENT_WHEN;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.SUBMIT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;

import io.restassured.http.Header;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;

@Slf4j
@ActiveProfiles("functional")
public class AutoRequestHearingFunctionalTest extends CcdCaseCreationTest {

    @BeforeEach
    void getAuthentications() {
        fetchTokensAndUserIds();
    }

    @ParameterizedTest
    @CsvSource({"LIST_CASE_WITHOUT_HEARING_REQUIREMENTS, SUBMIT_HEARING_REQUIREMENTS, true",
        "DECISION_AND_REASONS_STARTED, PRE_HEARING, true",
        "REVIEW_HEARING_REQUIREMENTS, LISTING, true",
        "RECORD_ADJOURNMENT_DETAILS, PRE_HEARING, true",
        "RESTORE_STATE_FROM_ADJOURN, ADJOURNED, true",
        "LIST_CASE_WITHOUT_HEARING_REQUIREMENTS, SUBMIT_HEARING_REQUIREMENTS, false",
        "DECISION_AND_REASONS_STARTED, PRE_HEARING, false",
        "REVIEW_HEARING_REQUIREMENTS, LISTING, false",
        "RECORD_ADJOURNMENT_DETAILS, PRE_HEARING, false",
        "RESTORE_STATE_FROM_ADJOURN, ADJOURNED, false"})
    void should_submit_hearing_creation_request_successfully(Event event, State state, boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            state,
            buildAsylumCase(result, event, isAipJourney),
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails),
                                           event);

        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, caseOfficerToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .log().all(true)
            .extract().response();

        assertEquals(200, response.getStatusCode());
    }

    @Test
    void should_fail_to_submit_create_hearing_request_due_to_invalid_authentication() {
        AsylumCase asylumCase = new AsylumCase();
        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            00000111,
            "IA",
            SUBMIT_HEARING_REQUIREMENTS,
            asylumCase,
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails),
                                           LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);

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

    private AsylumCase buildAsylumCase(Case result, Event event, boolean isAipJourney) {

        AsylumCase asylumCase = (AsylumCase) result.getCaseData();
        asylumCase.write(HEARING_CHANNEL, new DynamicList("INTER"));
        asylumCase.write(RELIST_CASE_IMMEDIATELY, YES);
        asylumCase.write(HEARING_ADJOURNMENT_WHEN, ON_HEARING_DATE);
        asylumCase.write(LISTING_LENGTH, "120");

        if (isAipJourney) {
            asylumCase.write(HMCTS_CASE_NAME_INTERNAL, "Talha Awan");
        }

        return asylumCase;
    }
}
