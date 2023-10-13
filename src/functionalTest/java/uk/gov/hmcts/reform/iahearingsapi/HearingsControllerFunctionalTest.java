package uk.gov.hmcts.reform.iahearingsapi;

import static io.restassured.RestAssured.given;
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
    public void should_create_hearing_successfully() {
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
    public void should_get_hearing_id_successfully() {
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
    public void should_fail_on_create_hearing_if_caseId_doesnt_match() {
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
    public void should_get_hearings_values_successfully() {
        HearingRequestPayload payload = HearingRequestPayload.builder()
            .caseReference(getCaseId())
            .hearingId(String.valueOf(hearingId))
            .build();

        Response response = given(hearingsSpecification)
            .when()
            .contentType("application/json")
            .header(new Header(AUTHORIZATION, systemUserToken))
            .header(new Header(SERVICE_AUTHORIZATION, s2sToken))
            .body(payload)
            .post("/serviceHearingValues")
            .then()
            .extract().response();

        assertEquals(200, response.getStatusCode());
    }
}
