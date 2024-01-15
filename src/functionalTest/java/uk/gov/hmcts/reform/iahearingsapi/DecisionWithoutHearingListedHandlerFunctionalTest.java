package uk.gov.hmcts.reform.iahearingsapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.DECISION_WITHOUT_HEARING_LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.DECISION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;

import io.restassured.http.Header;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;

@ActiveProfiles("functional")
@Disabled
@Slf4j
public class DecisionWithoutHearingListedHandlerFunctionalTest  extends CcdCaseCreationTest {

    @BeforeEach
    void getAuthentications() {
        fetchTokensAndUserIds();
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_handle_decision_without_hearing_listed(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        log.info("caseId: " + result.getCaseId());
        log.info("s2sToken: " + s2sToken);

        AsylumCase asylumCase = new AsylumCase();
        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            DECISION,
            asylumCase,
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), DECISION_WITHOUT_HEARING_LISTED);

        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, systemUserToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(callback)
            .post("/asylum/ccdAboutToSubmit")
            .then()
            .log().all(true)
            .extract().response();

        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_fail_to_handle_decision_without_hearing_listed(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        AsylumCase asylumCase = new AsylumCase();
        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
            "IA",
            LISTING,
            asylumCase,
            LocalDateTime.now(),
            "securityClassification"
        );

        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), DECISION_WITHOUT_HEARING_LISTED);

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
}
