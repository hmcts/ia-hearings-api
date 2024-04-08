package uk.gov.hmcts.reform.iahearingsapi;

import java.time.LocalDateTime;
import java.util.Optional;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;
import io.restassured.http.Header;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;

@ActiveProfiles("functional")
@Slf4j
public class EndAppealRequestSubmitHandlerFunctionalTest extends CcdCaseCreationTest {

    @BeforeEach
    void getAuthentications() {
        fetchTokensAndUserIds();
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_submit_end_appeal_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        log.info("caseId: " + result.getCaseId());
        log.info("caseOfficerToken: " + legalRepToken);
        log.info("s2sToken: " + s2sToken);


        AsylumCase asylumCase = new AsylumCase();
        CaseDetails<CaseData> caseDetails = new CaseDetails<>(
            result.getCaseId(),
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
            .log().all(true)
            .extract().response();

        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_fail_to_submit_end_appeal_due_to_invalid_token(boolean isAipJourney) {
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
        Callback callback = new Callback<>(caseDetails, Optional.of(caseDetails), END_APPEAL);

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
