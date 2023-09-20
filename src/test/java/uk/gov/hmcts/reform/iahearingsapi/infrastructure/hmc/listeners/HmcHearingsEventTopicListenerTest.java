package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.HmcMessageProcessor;

@ExtendWith(MockitoExtension.class)
class HmcHearingsEventTopicListenerTest {

    public static final String SERVICE_CODE = "BFA1";

    private HmcHearingsEventTopicListener hmcHearingsEventTopicListener;

    @Mock
    private HmcMessageProcessor processHmcMessageService;

    @BeforeEach
    public void setUp() {
        hmcHearingsEventTopicListener = new HmcHearingsEventTopicListener(SERVICE_CODE, processHmcMessageService);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "hmctsServiceId", SERVICE_CODE);
    }

    @Test
    public void testOnMessageWithRelevantMessage() {
        HmcMessage hmcMessage = createHmcMessage(SERVICE_CODE);

        hmcHearingsEventTopicListener.onMessage(hmcMessage);

        verify(processHmcMessageService).processMessage(any(HmcMessage.class));
    }

    @Test
    public void testOnMessageWithIrrelevantMessage() {
        HmcMessage hmcMessage = createHmcMessage("irrelevantServiceCode");

        hmcHearingsEventTopicListener.onMessage(hmcMessage);

        verify(processHmcMessageService, never()).processMessage(any(HmcMessage.class));
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
