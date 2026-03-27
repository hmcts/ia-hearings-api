package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DeleteHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HmcHearingApiFallbackTest {

    private HmcHearingApiFallback hmcHearingApiFallback;
    @BeforeEach
    void setUp() {
        hmcHearingApiFallback = new HmcHearingApiFallback();
    }

    @Test
    void createHearingRequest_throws_runtimeException() {
        CreateHearingRequest someRequest = new CreateHearingRequest();
        RuntimeException e = assertThrows(RuntimeException.class, () ->hmcHearingApiFallback.createHearingRequest(
            "auth", "serviceAuth", "ID",
            "URL","role", someRequest)
        );
        assertEquals("HMC Hearing service: createHearingRequest call failed after retries", e.getMessage());
    }

    @Test
    void getHearingRequest_throws_runtimeException() {
        RuntimeException e = assertThrows(RuntimeException.class, () ->hmcHearingApiFallback.getHearingRequest(
            "auth","serviceAuth", "ID", "URL", "roleUrl","ID", true
        ));
        assertEquals("HMC Hearing service: getHearingRequest call failed after retries", e.getMessage());


    }

    @Test
    void getHearingsRequest_throws_runtimeException() {
        RuntimeException e = assertThrows(RuntimeException.class, () ->hmcHearingApiFallback.getHearingsRequest(
            "auth", "serviceAuth","URL","role","depID", "ID"));
        assertEquals("HMC Hearing service: getHearingsRequest call failed after retries", e.getMessage());
    }

    @Test
    void updateHearingRequest_throws_runtimeException() {
        UpdateHearingRequest someRequest = new UpdateHearingRequest();
        RuntimeException e = assertThrows(RuntimeException.class, () ->hmcHearingApiFallback.updateHearingRequest(
            "auth", "serviceAuth","URL","role","depID", someRequest, "id"));
        assertEquals("HMC Hearing service: updateHearingRequest call failed after retries", e.getMessage());
    }

    @Test
    void getPartiesNotifiedRequest_throws_runtimeException() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> hmcHearingApiFallback.getPartiesNotifiedRequest(
            "auth", "serviceAuth","URL","role","depID", "id"));
        assertEquals("HMC Hearing service: getPartiesNotifiedRequest call failed after retries", e.getMessage());
    }

    @Test
    void updatePartiesNotifiedRequest_throws_runtimeException() {
        PartiesNotified parties =  new PartiesNotified();
        LocalDateTime now = LocalDateTime.now();
        RuntimeException e = assertThrows(RuntimeException.class, () -> hmcHearingApiFallback.updatePartiesNotifiedRequest(
            "auth", "serviceAuth","ID","URL","URL", parties, "ID",
            1, now));
        assertEquals("HMC Hearing service: updatePartiesNotifiedRequest call failed after retries", e.getMessage());
    }

    @Test
    void deleteHearing_throws_503_status() {
        DeleteHearingRequest someRequest = new DeleteHearingRequest();
        ResponseEntity<HmcHearingResponse> response = hmcHearingApiFallback.deleteHearing(
            "auth", "serviceAuth", "ID", "URL",
            "URL", 9L, someRequest
        );
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void getUnNotifiedHearings_throws_runtimeException() {
        LocalDateTime now = LocalDateTime.now();
        List<String> status = List.of("status", "status2", "status3");
        RuntimeException e = assertThrows(RuntimeException.class, () ->hmcHearingApiFallback.getUnNotifiedHearings(
            "auth","service","id","url","url",
            now,now,status,"id"));
        assertEquals("HMC Hearing service: getUnNotifiedHearings call failed after retries", e.getMessage());
    }
}
