package uk.gov.hmcts.reform.iahearingsapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iahearingsapi.util.AuthorizationHeadersProvider;

@SpringBootTest
@ActiveProfiles("functional")
public class WelcomeTest {

    @Value("${WelcomeTesttargetInstance}") private String targetInstance;

    @Autowired private AuthorizationHeadersProvider authorizationHeadersProvider;

    @Test
    public void should_display_welcome_message_on_root_request_200_SC() {

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        String response = SerenityRest.given()
            .when()
            .get("/")
            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .and()
            .extract().body().asString();

        assertThat(response).contains("Welcome to the Hearings API");
    }
}
