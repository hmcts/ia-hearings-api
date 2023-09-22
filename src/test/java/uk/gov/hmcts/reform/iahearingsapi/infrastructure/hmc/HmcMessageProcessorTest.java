package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.TestUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HmcMessageProcessorTest {

    @Mock
    private HmcMessageDispatcher<HmcMessage> dispatcher;

    private HmcMessageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new HmcMessageProcessor(dispatcher);
    }

    @Test
    void should_process_hmc_message() {
        HmcMessage message = TestUtils.createHmcMessage("BFA1");

        assertNotNull(message.getHearingUpdate().getHmcStatus());
        assertNotNull(message.getHearingId());
        assertNotNull(message.getCaseId());

        processor.processMessage(message);

        verify(dispatcher, times(1)).dispatch(message);
    }

}
