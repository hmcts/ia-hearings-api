package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iahearingsapi.TestUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.HmcMessageProcessor;

@ExtendWith(MockitoExtension.class)
class HmcHearingsEventTopicListenerTest {

    private static final String SERVICE_CODE = "BFA1";

    private HmcHearingsEventTopicListener hmcHearingsEventTopicListener;

    @Mock
    private HmcMessageProcessor hmcMessageProcessor;

    @Mock
    private ObjectMapper mockObjectMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        hmcHearingsEventTopicListener = new HmcHearingsEventTopicListener(SERVICE_CODE, hmcMessageProcessor);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "objectMapper", mockObjectMapper);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "hmctsServiceId", SERVICE_CODE);
    }

    @Test
    public void testOnMessageWithRelevantMessage() throws Exception {
        ServiceData serviceData = TestUtils.createServiceData(SERVICE_CODE);

        String stringMessage = OBJECT_MAPPER.writeValueAsString(serviceData);
        byte[] message = StandardCharsets.UTF_8.encode(stringMessage).array();

        given(mockObjectMapper.readValue(any(String.class), eq(ServiceData.class))).willReturn(serviceData);

        hmcHearingsEventTopicListener.onMessage(message);

        verify(hmcMessageProcessor).processMessage(any(ServiceData.class));
    }

    @Test
    public void testOnMessageWithIrrelevantMessage() throws Exception {
        ServiceData serviceData = TestUtils.createServiceData("irrelevantServiceCode");

        String stringMessage = OBJECT_MAPPER.writeValueAsString(serviceData);
        byte[] message = StandardCharsets.UTF_8.encode(stringMessage).array();

        given(mockObjectMapper.readValue(any(String.class), eq(ServiceData.class))).willReturn(serviceData);

        hmcHearingsEventTopicListener.onMessage(message);

        verify(hmcMessageProcessor, never()).processMessage(any(ServiceData.class));
    }

}
