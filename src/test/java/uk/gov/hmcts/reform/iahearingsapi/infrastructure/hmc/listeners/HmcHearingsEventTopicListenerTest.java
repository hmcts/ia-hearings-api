package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import java.nio.charset.StandardCharsets;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.ProcessHmcMessageService;

@ExtendWith(MockitoExtension.class)
class HmcHearingsEventTopicListenerTest {

    public static final String SERVICE_CODE = "BFA1";

    private HmcHearingsEventTopicListener hmcHearingsEventTopicListener;

    @Mock
    private ProcessHmcMessageService processHmcMessageService;

    @Mock
    private JmsBytesMessage bytesMessage;

    @Mock
    private ObjectMapper mockObjectMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        hmcHearingsEventTopicListener = new HmcHearingsEventTopicListener(SERVICE_CODE, processHmcMessageService);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "objectMapper", mockObjectMapper);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "hmctsServiceId", SERVICE_CODE);
    }

    @Test
    public void testOnMessageWithRelevantMessage() throws Exception {
        HmcMessage hmcMessage = createHmcMessage(SERVICE_CODE);

        byte[] messageBytes = OBJECT_MAPPER.writeValueAsString(hmcMessage).getBytes(StandardCharsets.UTF_8);

        given(bytesMessage.getBodyLength()).willReturn((long) messageBytes.length);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        hmcHearingsEventTopicListener.onMessage(bytesMessage);

        verify(processHmcMessageService).processEventMessage(any(HmcMessage.class));
    }

    @Test
    public void testOnMessageWithIrrelevantMessage() throws Exception {
        HmcMessage hmcMessage = createHmcMessage("irrelevantServiceCode");

        byte[] messageBytes = OBJECT_MAPPER.writeValueAsString(hmcMessage).getBytes(StandardCharsets.UTF_8);

        given(bytesMessage.getBodyLength()).willReturn((long) messageBytes.length);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        hmcHearingsEventTopicListener.onMessage(bytesMessage);

        verify(processHmcMessageService, never()).processEventMessage(any(HmcMessage.class));
    }

    private HmcMessage createHmcMessage(String messageServiceCode) {
        return HmcMessage.builder()
            .hmctsServiceCode(messageServiceCode)
            .caseId(1234L)
            .hearingId("testId")
            .hearingUpdate(HearingUpdate.builder()
                .hmcStatus(HmcStatus.LISTED)
                .build())
            .build();
    }

}
