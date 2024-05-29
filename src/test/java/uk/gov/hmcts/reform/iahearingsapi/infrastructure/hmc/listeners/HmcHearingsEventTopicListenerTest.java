package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iahearingsapi.TestUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcEventProcessingException;
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
    private HmcMessage hmcMessage;
    private byte[] message;

    @BeforeEach
    public void setUp() throws Exception {
        hmcHearingsEventTopicListener = new HmcHearingsEventTopicListener(SERVICE_CODE, hmcMessageProcessor);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "objectMapper", mockObjectMapper);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "hmctsServiceId", SERVICE_CODE);

        hmcMessage = TestUtils.createHmcMessage(SERVICE_CODE);
        String stringMessage = OBJECT_MAPPER.writeValueAsString(hmcMessage);
        message = StandardCharsets.UTF_8.encode(stringMessage).array();
    }

    @Test
    void testOnMessageWithRelevantMessage() throws Exception {
        hmcMessage.setHearingUpdate(HearingUpdate.builder().hmcStatus(EXCEPTION).build());
        processMessage();
        verify(hmcMessageProcessor, times(1)).processMessage(any(HmcMessage.class));
    }

    @Test
    void testOnMessageWithIrrelevantMessage() throws Exception {
        hmcMessage.setHmctsServiceCode("irrelevantServiceCode");
        processMessage();
        verify(hmcMessageProcessor, never()).processMessage(any(HmcMessage.class));
    }

    // Disable this test if using live-processing instead of batch-processing of messages
    @Test
    void should_not_process_messages_with_hmc_status_different_than_exception() throws Exception {
        hmcMessage.setHearingUpdate(HearingUpdate.builder().hmcStatus(LISTED).build());
        processMessage();
        verify(hmcMessageProcessor, never()).processMessage(any(HmcMessage.class));
    }

    private void processMessage() throws JsonProcessingException, HmcEventProcessingException {
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);
        hmcHearingsEventTopicListener.onMessage(message);
    }

}
