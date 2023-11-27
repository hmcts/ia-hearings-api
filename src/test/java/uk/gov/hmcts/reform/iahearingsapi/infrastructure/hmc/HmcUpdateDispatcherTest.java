package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HmcUpdateDispatcherTest {

    @Mock
    private ServiceDataHandler<ServiceData> handler1;
    @Mock
    private ServiceDataHandler<ServiceData> handler2;
    @Mock
    private ServiceDataHandler<ServiceData> handler3;
    @Mock
    private ServiceData serviceData;

    private HmcUpdateDispatcher<ServiceData> dispatcher;

    @BeforeEach
    public void setUp() {
        dispatcher = new HmcUpdateDispatcher<>(
            Arrays.asList(
                handler1,
                handler2,
                handler3
            )
        );

    }

    @Test
    void should_dispatch_message_to_handlers_according_to_priority() {

        when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
        when(handler1.canHandle(any(ServiceData.class))).thenReturn(true);

        when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
        when(handler2.canHandle(any(ServiceData.class))).thenReturn(true);

        when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
        when(handler3.canHandle(any(ServiceData.class))).thenReturn(true);

        dispatcher.dispatch(serviceData);

        InOrder inOrder = inOrder(handler1, handler3, handler2);

        inOrder.verify(handler1, times(1)).canHandle(any(ServiceData.class));
        inOrder.verify(handler1, times(1)).handle(any(ServiceData.class));

        inOrder.verify(handler3, times(1)).canHandle(any(ServiceData.class));
        inOrder.verify(handler3, times(1)).handle(any(ServiceData.class));

        inOrder.verify(handler2, times(1)).canHandle(any(ServiceData.class));
        inOrder.verify(handler2, times(1)).handle(any(ServiceData.class));

    }

    @Test
    void should_only_dispatch_callback_to_handlers_that_can_handle_it() {

        when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
        when(handler1.canHandle(any(ServiceData.class))).thenReturn(false);

        when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
        when(handler2.canHandle(any(ServiceData.class))).thenReturn(false);

        when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
        when(handler3.canHandle(any(ServiceData.class))).thenReturn(true);

        dispatcher.dispatch(serviceData);

        verify(handler1, times(1)).canHandle(any(ServiceData.class));
        verify(handler1, times(0)).handle(any(ServiceData.class));

        verify(handler2, times(1)).canHandle(any(ServiceData.class));
        verify(handler2, times(0)).handle(any(ServiceData.class));

        verify(handler3, times(1)).canHandle(any(ServiceData.class));
        verify(handler3, times(1)).handle(any(ServiceData.class));
    }

    @Test
    void should_not_error_if_no_handlers_are_provided() {

        HmcUpdateDispatcher<ServiceData> hmcUpdateDispatcher =
            new HmcUpdateDispatcher<>(Collections.emptyList());

        try {

            hmcUpdateDispatcher.dispatch(serviceData);

        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new HmcUpdateDispatcher<>(null))
            .hasMessage("Handlers must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_argument() {

        assertThatThrownBy(() -> dispatcher.dispatch(null))
            .hasMessage("Service data must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
