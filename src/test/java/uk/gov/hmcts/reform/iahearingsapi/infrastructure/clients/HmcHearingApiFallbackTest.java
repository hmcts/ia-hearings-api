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
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HmcHearingApiFallbackTest {

    private HmcHearingApiFallback hmcHearingApiFallback;

    @BeforeEach
    void setUp() {
        hmcHearingApiFallback = new HmcHearingApiFallback();
    }

    @Test
    void createHearingRequest_throws_HmcException() {
        CreateHearingRequest someRequest = new CreateHearingRequest();
        HmcException e = assertThrows(
            HmcException.class, () -> hmcHearingApiFallback.createHearingRequest(
            "auth", "serviceAuth", "ID",
            "URL","role", someRequest)
        );
        assertEquals(HmcException.MESSAGE_TEMPLATE + "createHearingRequest call failed "
                         + "after retries", e.getMessage());
    }

    @Test
    void getHearingRequest_throws_HmcException() {
        HmcException e = assertThrows(HmcException.class, () -> hmcHearingApiFallback.getHearingRequest(
            "auth","serviceAuth", "ID", "URL", "roleUrl","ID", true
        ));
        assertEquals(HmcException.MESSAGE_TEMPLATE + "getHearingRequest call failed after retries", e.getMessage());


    }

    @Test
    void getHearingsRequest_throws_HmcException() {
        HmcException e = assertThrows(HmcException.class, () -> hmcHearingApiFallback.getHearingsRequest(
            "auth", "serviceAuth","URL","role","depID", "ID"));
        assertEquals(HmcException.MESSAGE_TEMPLATE + "getHearingsRequest call failed after retries", e.getMessage());
    }

    @Test
    void updateHearingRequest_throws_HmcException() {
        UpdateHearingRequest someRequest = new UpdateHearingRequest();
        HmcException e = assertThrows(HmcException.class, () -> hmcHearingApiFallback.updateHearingRequest(
            "auth", "serviceAuth","URL","role","depID", someRequest, "id"));
        assertEquals(HmcException.MESSAGE_TEMPLATE + "updateHearingRequest call failed after retries", e.getMessage());
    }

    @Test
    void getPartiesNotifiedRequest_throws_HmcException() {
        HmcException e = assertThrows(HmcException.class, () -> hmcHearingApiFallback.getPartiesNotifiedRequest(
            "auth", "serviceAuth",
            "URL","role","depID", "id"));
        assertEquals(HmcException.MESSAGE_TEMPLATE + "getPartiesNotifiedRequest call "
                         + "failed after retries", e.getMessage());
    }

    @Test
    void updatePartiesNotifiedRequest_throws_HmcException() {
        PartiesNotified parties =  new PartiesNotified();
        LocalDateTime now = LocalDateTime.now();
        HmcException e = assertThrows(HmcException.class,
                                          () -> hmcHearingApiFallback.updatePartiesNotifiedRequest(
            "auth", "serviceAuth","ID","URL","URL", parties, "ID",
            1, now));
        assertEquals(HmcException.MESSAGE_TEMPLATE + "updatePartiesNotifiedRequest "
                         + "call failed after retries", e.getMessage());
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
    void getUnNotifiedHearings_throws_HmcException() {
        LocalDateTime now = LocalDateTime.now();
        List<String> status = List.of("status", "status2", "status3");
        HmcException e = assertThrows(HmcException.class, () -> hmcHearingApiFallback.getUnNotifiedHearings(
            "auth","service","id","url","url",
            now,now,status,"id"));
        assertEquals(HmcException.MESSAGE_TEMPLATE + "getUnNotifiedHearings call failed after retries", e.getMessage());
    }
}
