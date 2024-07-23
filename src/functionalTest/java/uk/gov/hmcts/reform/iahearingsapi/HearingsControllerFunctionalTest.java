package uk.gov.hmcts.reform.iahearingsapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import io.restassured.http.Header;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled
@ActiveProfiles("functional")
class HearingsControllerFunctionalTest extends CcdCaseCreationTest {
    @BeforeEach
    void getAuthentications() {
        fetchTokensAndUserIds();
    }

    @Order(1)
    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_create_hearing_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);

        HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference(Long.toString(result.getCaseId()))
            .build();

        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, legalRepToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("/test")
            .then()
            .log().all(true)
            .extract().response();

        assertEquals(200, response.getStatusCode());
        log.info("aipCaseId: " + getAipCaseId());
        log.info("legalRepCaseId: " + getLegalRepCaseId());
        log.info("test: should_create_hearing_successfully");
    }

    @Test
    @Order(2)
    void should_fail_to_create_hearing_if_case_id_doesnt_match() {
        HearingRequestPayload payloadWithInvalidId = HearingRequestPayload.builder()
            .caseReference("invalidId")
            .build();

        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, legalRepToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payloadWithInvalidId)
            .post("/test")
            .then()
            .log().all(true)
            .extract().response();

        assertEquals(400, response.getStatusCode());
    }

    @Order(3)
    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_get_hearings_values_successfully(boolean isAipJourney) {
        Case result = createAndGetCase(isAipJourney);
        listCaseWithRequiredFields();

        HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference(Long.toString(result.getCaseId()))
            .hearingId("hearingId")
            .build();

        given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, legalRepToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("/serviceHearingValues")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("hmctsServiceID", notNullValue())
            .assertThat().body("hmctsInternalCaseName", notNullValue())
            .assertThat().body("publicCaseName", notNullValue())
            .assertThat().body("caseCategories", notNullValue())
            .assertThat().body("caseDeepLink", notNullValue())
            .assertThat().body("hearingPriorityType", notNullValue())
            .assertThat().body("hearingLocations", notNullValue())
            .assertThat().body("facilitiesRequired", notNullValue())
            .assertThat().body("listingComments", notNullValue())
            .assertThat().body("hearingRequester", notNullValue())
            .assertThat().body("leadJudgeContractType", notNullValue())
            .assertThat().body("judiciary", notNullValue())
            .assertThat().body("parties", notNullValue())
            .assertThat().body("caseFlags", notNullValue())
            .assertThat().body("vocabulary", notNullValue())
            .assertThat().body("hearingChannels", notNullValue())
            .assertThat().body("hearingLevelParticipantAttendance", notNullValue());

        log.info("aipCaseId: " + getAipCaseId());
        log.info("legalRepCaseId: " + getLegalRepCaseId());
        log.info("test: should_get_hearings_values_successfully");
    }

    @Test
    @Order(4)
    void should_fail_on_hearings_values_if_case_id_invalid() {
        HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference("invalidCaseId")
            .hearingId("hearingId")
            .build();

        given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, legalRepToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("/serviceHearingValues")
            .then()
            .log().all(true)
            .statusCode(HttpStatus.SC_BAD_REQUEST);
    }


    @Test
    @Order(5)
    void should_get_hearings_values_successfully_for_bail() {
        Case result = createAndGetBailCase();

        HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference(Long.toString(result.getCaseId()))
            .hearingId("hearingId")
            .build();

        given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, legalRepToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("/serviceHearingValues")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("hmctsServiceID", notNullValue())
            .assertThat().body("hmctsInternalCaseName", notNullValue())
            .assertThat().body("publicCaseName", notNullValue())
            .assertThat().body("caseCategories", notNullValue())
            .assertThat().body("caseDeepLink", notNullValue())
            .assertThat().body("hearingPriorityType", notNullValue())
            .assertThat().body("hearingLocations", notNullValue())
            .assertThat().body("facilitiesRequired", notNullValue())
            .assertThat().body("listingComments", notNullValue())
            .assertThat().body("hearingRequester", notNullValue())
            .assertThat().body("leadJudgeContractType", notNullValue())
            .assertThat().body("judiciary", notNullValue())
            .assertThat().body("parties", notNullValue())
            .assertThat().body("caseFlags", notNullValue())
            .assertThat().body("vocabulary", notNullValue())
            .assertThat().body("hearingChannels", notNullValue())
            .assertThat().body("hearingLevelParticipantAttendance", notNullValue());
    }



}
