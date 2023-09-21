package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.handlers.MessageHandler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HmcMessageDispatcherTest {

    @Mock
    private MessageHandler<HmcMessage> handler1;
    @Mock
    private MessageHandler<HmcMessage> handler2;
    @Mock
    private MessageHandler<HmcMessage> handler3;
    @Mock
    private HmcMessage hmcMessage;

    private HmcMessageDispatcher<HmcMessage> dispatcher;

    @BeforeEach
    public void setUp() {
        dispatcher = new HmcMessageDispatcher<>(
            Arrays.asList(
                handler1,
                handler2,
                handler3
            )
        );

        when(hmcMessage.getCaseId()).thenReturn(123456789L);
        when(hmcMessage.getHearingUpdate()).thenReturn(HearingUpdate.builder().hmcStatus(HmcStatus.LISTED).build());
        when(hmcMessage.getCaseId()).thenReturn(1234509L);
        when(hmcMessage.getHmctsServiceCode()).thenReturn("BFA1");
    }

    @Test
    void should_dispatch_message_to_handlers_according_to_priority() {

        when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
        when(handler1.canHandle(any(HmcMessage.class))).thenReturn(true);

        when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
        when(handler2.canHandle(any(HmcMessage.class))).thenReturn(true);

        when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
        when(handler3.canHandle(any(HmcMessage.class))).thenReturn(true);

        dispatcher.dispatch(hmcMessage);

        InOrder inOrder = inOrder(handler1, handler3, handler2);

        inOrder.verify(handler1, times(1)).canHandle(any(HmcMessage.class));
        inOrder.verify(handler1, times(1)).handle(any(HmcMessage.class));

        inOrder.verify(handler3, times(1)).canHandle(any(HmcMessage.class));
        inOrder.verify(handler3, times(1)).handle(any(HmcMessage.class));

        inOrder.verify(handler2, times(1)).canHandle(any(HmcMessage.class));
        inOrder.verify(handler2, times(1)).handle(any(HmcMessage.class));

    }

    @Test
    void should_only_dispatch_callback_to_handlers_that_can_handle_it() {

        when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
        when(handler1.canHandle(any(HmcMessage.class))).thenReturn(false);

        when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
        when(handler2.canHandle(any(HmcMessage.class))).thenReturn(false);

        when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
        when(handler3.canHandle(any(HmcMessage.class))).thenReturn(true);

        dispatcher.dispatch(hmcMessage);

        verify(handler1, times(1)).canHandle(any(HmcMessage.class));
        verify(handler1, times(0)).handle(any(HmcMessage.class));

        verify(handler2, times(1)).canHandle(any(HmcMessage.class));
        verify(handler2, times(0)).handle(any(HmcMessage.class));

        verify(handler3, times(1)).canHandle(any(HmcMessage.class));
        verify(handler3, times(1)).handle(any(HmcMessage.class));
    }

    @Test
    void should_not_error_if_no_handlers_are_provided() {

        HmcMessageDispatcher<HmcMessage> hmcMessageDispatcher =
            new HmcMessageDispatcher<>(Collections.emptyList());

        try {

            hmcMessageDispatcher.dispatch(hmcMessage);

        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new HmcMessageDispatcher<>(null))
            .hasMessage("Handlers must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_argument() {

        assertThatThrownBy(() -> dispatcher.dispatch(null))
            .hasMessage("Message must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
