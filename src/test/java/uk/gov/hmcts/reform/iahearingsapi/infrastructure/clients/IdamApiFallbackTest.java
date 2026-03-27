package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class IdamApiFallbackTest {

    private IdamApiFallback idamApiFallback;

    @BeforeEach
    void setUp() {
        idamApiFallback = new IdamApiFallback();
    }

    @Test
    void userInfo_shouldThrowRuntimeException() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                                                  () -> idamApiFallback.userInfo("someToken"));

        assertEquals("IDAM service: userInfo unavailable, call failed after retries",
                     exception.getMessage());
    }

    @Test
    void token_shouldThrowRuntimeException() {
        Map<String, String> form = Map.of("grant_type", "password");

        RuntimeException exception = assertThrows(RuntimeException.class,
                                                  () -> idamApiFallback.token(form));

        assertEquals("IDAM service: token unavailable, call failed after retries",
                     exception.getMessage());
    }
}
