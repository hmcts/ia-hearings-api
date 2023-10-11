package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.webjars.NotFoundException;
import uk.gov.hmcts.reform.iahearingsapi.TestUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.HmcMessageProcessor;

@ExtendWith(MockitoExtension.class)
class HmcHearingsEventTopicListenerTest {

    private static final String SERVICE_CODE = "BFA1";

    private HmcHearingsEventTopicListener hmcHearingsEventTopicListener;

    @Mock
    private HmcMessageProcessor hmcMessageProcessor;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private AsylumCase asylumCase;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        hmcHearingsEventTopicListener = new HmcHearingsEventTopicListener(SERVICE_CODE,
                                                                          hmcMessageProcessor,
                                                                          coreCaseDataService);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "objectMapper", mockObjectMapper);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "hmctsServiceId", SERVICE_CODE);
    }

    @Test
    public void testOnMessageWithRelevantMessage() throws Exception {
        HmcMessage hmcMessage = TestUtils.createHmcMessage(SERVICE_CODE);

        String stringMessage = OBJECT_MAPPER.writeValueAsString(hmcMessage);
        byte[] message = StandardCharsets.UTF_8.encode(stringMessage).array();

        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        hmcHearingsEventTopicListener.onMessage(message);

        verify(hmcMessageProcessor).processMessage(any(HmcMessage.class));
    }

    @Test
    public void testOnMessageWithExceptionHmcStatus() throws Exception {
        HmcMessage hmcMessage = TestUtils.createHmcMessage(SERVICE_CODE);
        hmcMessage.setHearingUpdate(HearingUpdate.builder().hmcStatus(HmcStatus.EXCEPTION).build());

        String stringMessage = OBJECT_MAPPER.writeValueAsString(hmcMessage);
        byte[] message = StandardCharsets.UTF_8.encode(stringMessage).array();

        String caseId = String.valueOf(hmcMessage.getCaseId());

        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);
        given(coreCaseDataService.getCase(caseId)).willReturn(asylumCase);

        hmcHearingsEventTopicListener.onMessage(message);

        verify(coreCaseDataService, times(1)).triggerEvent(Event.HANDLE_HEARING_EXCEPTION, caseId, asylumCase);
        verify(hmcMessageProcessor, never()).processMessage(any(HmcMessage.class));
    }

    @Test
    public void testOnMessageWithExceptionHmcStatusWhenNoCaseCanBeFound() throws Exception {
        HmcMessage hmcMessage = TestUtils.createHmcMessage(SERVICE_CODE);
        hmcMessage.setHearingUpdate(HearingUpdate.builder().hmcStatus(HmcStatus.EXCEPTION).build());

        String stringMessage = OBJECT_MAPPER.writeValueAsString(hmcMessage);
        byte[] message = StandardCharsets.UTF_8.encode(stringMessage).array();

        String caseId = String.valueOf(hmcMessage.getCaseId());

        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);
        given(coreCaseDataService.getCase(caseId)).willThrow(new NotFoundException("Case not found"));

        hmcHearingsEventTopicListener.onMessage(message);

        verify(coreCaseDataService, never()).triggerEvent(Event.HANDLE_HEARING_EXCEPTION, caseId, asylumCase);
        verify(hmcMessageProcessor, never()).processMessage(any(HmcMessage.class));
    }

    @Test
    public void testOnMessageWithIrrelevantMessage() throws Exception {
        HmcMessage hmcMessage = TestUtils.createHmcMessage("irrelevantServiceCode");
        String stringMessage = OBJECT_MAPPER.writeValueAsString(hmcMessage);
        byte[] message = StandardCharsets.UTF_8.encode(stringMessage).array();

        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);
        hmcHearingsEventTopicListener.onMessage(message);

        verify(hmcMessageProcessor, never()).processMessage(any(HmcMessage.class));
    }

}
