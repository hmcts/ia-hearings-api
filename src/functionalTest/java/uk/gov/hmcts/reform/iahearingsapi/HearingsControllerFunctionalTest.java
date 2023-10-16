package uk.gov.hmcts.reform.iahearingsapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingRequestPayload;

@ActiveProfiles("functional")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HearingsControllerFunctionalTest extends CcdCaseCreationTest {

    private int hearingId;

    @Test
    @Order(1)
    void should_create_hearing_successfully() {
        HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference(getCaseId())
            .build();

        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, legalRepToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("/test")
            .then()
            .extract().response();

        assertEquals(200, response.getStatusCode());
    }

    @Test
    @Order(2)
    void should_get_hearing_id_successfully() {
        systemUserToken = idamAuthProvider.getSystemUserToken();
        s2sToken = s2sAuthTokenGenerator.generate();

        hearingId = given(hmcApiSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, systemUserToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .get("/hearings/" + getCaseId())
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .body()
            .jsonPath()
            .get("caseHearings[0].hearingID");

        assertNotNull(hearingId);
    }
    @Test
    @Order(3)
    void should_fail_on_create_hearing_if_case_id_doesnt_match() {
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
            .extract().response();

        assertEquals(400, response.getStatusCode());
    }

    @Test
    @Order(4)
    void should_get_hearings_values_successfully() {
        HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference(getCaseId())
            .hearingId("hearingId")
            .build();

        given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, systemUserToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("/serviceHearingValues")
            .then()
            .statusCode(HttpStatus.SC_OK)
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
            .assertThat().body("caseflags", notNullValue())
            .assertThat().body("vocabulary", notNullValue())
            .assertThat().body("hearingChannels", notNullValue())
            .assertThat().body("hearingLevelParticipantAttendance", notNullValue());
    }

    @Test
    @Order(5)
    void should_fail_on_hearings_values_if_case_id_invalid() {
        HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference("invalidCaseId")
            .hearingId("hearingId")
            .build();

        given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, systemUserToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("/serviceHearingValues")
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);

    }
}
