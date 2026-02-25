package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi.HMCTS_DEPLOYMENT_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iahearingsapi.TestUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.HmcMessageProcessor;

import javax.jms.JMSException;
import java.nio.charset.StandardCharsets;

@ExtendWith(MockitoExtension.class)
class HmcHearingsEventTopicListenerTest {

    private static final String SERVICE_CODE = "BFA1";

    private HmcHearingsEventTopicListener hmcHearingsEventTopicListenerWithDeploymentFilterDisabled;

    private HmcHearingsEventTopicListener hmcHearingsEventTopicListenerWithDeploymentFilterEnabled;

    @Mock
    private HmcMessageProcessor hmcMessageProcessor;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Mock
    private JmsBytesMessage mockJmsBytesMessage;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        hmcHearingsEventTopicListenerWithDeploymentFilterDisabled = new HmcHearingsEventTopicListener(
            SERVICE_CODE, "ia", false, hmcMessageProcessor);
        hmcHearingsEventTopicListenerWithDeploymentFilterEnabled = new HmcHearingsEventTopicListener(
            SERVICE_CODE, "ia", true, hmcMessageProcessor);

        ReflectionTestUtils.setField(
            hmcHearingsEventTopicListenerWithDeploymentFilterDisabled, "objectMapper", mockObjectMapper);
        ReflectionTestUtils.setField(
            hmcHearingsEventTopicListenerWithDeploymentFilterEnabled, "objectMapper", mockObjectMapper);
    }

    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = { "LISTED", "CANCELLED", "EXCEPTION" })
    public void testOnMessageWithRelevantMessage(HmcStatus hmcStatus) throws Exception {
        HmcMessage hmcMessage = TestUtils.createHmcMessage(SERVICE_CODE, hmcStatus);

        String stringMessage = OBJECT_MAPPER.writeValueAsString(hmcMessage);

        mocksToReadJmsByteMessage(stringMessage);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        hmcHearingsEventTopicListenerWithDeploymentFilterDisabled.onMessage(mockJmsBytesMessage);

        verify(hmcMessageProcessor, times(1)).processMessage(any(HmcMessage.class));
    }

    @Test
    public void testOnMessageWithIrrelevantMessage() throws Exception {
        HmcMessage hmcMessage = TestUtils.createHmcMessage("irrelevantServiceCode", HEARING_REQUESTED);
        String stringMessage = OBJECT_MAPPER.writeValueAsString(hmcMessage);

        mocksToReadJmsByteMessage(stringMessage);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        hmcHearingsEventTopicListenerWithDeploymentFilterDisabled.onMessage(mockJmsBytesMessage);

        verify(hmcMessageProcessor, never()).processMessage(any(HmcMessage.class));
    }

    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = {"HEARING_REQUESTED", "AWAITING_LISTING", "UPDATE_REQUESTED",
        "UPDATE_SUBMITTED", "CANCELLATION_REQUESTED", "CANCELLATION_SUBMITTED", "CLOSED", "EXCEPTION"})
    public void testOnMessageWithIrrelevantHmcStatuses(HmcStatus hmcStatus) throws Exception {
        HmcMessage hmcMessage = TestUtils.createHmcMessage("irrelevantServiceCode", hmcStatus);
        String stringMessage = OBJECT_MAPPER.writeValueAsString(hmcMessage);

        mocksToReadJmsByteMessage(stringMessage);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        hmcHearingsEventTopicListenerWithDeploymentFilterDisabled.onMessage(mockJmsBytesMessage);

        verify(hmcMessageProcessor, never()).processMessage(any(HmcMessage.class));
    }

    @Test
    public void doesntProcessMessageNotForThisDeployment() throws Exception {
        given(mockJmsBytesMessage.getStringProperty(HMCTS_DEPLOYMENT_ID)).willReturn("some-non-ia-deployment-id");

        hmcHearingsEventTopicListenerWithDeploymentFilterEnabled.onMessage(mockJmsBytesMessage);

        verify(hmcMessageProcessor, never()).processMessage(any(HmcMessage.class));
    }

    @Test
    public void processesMessagesForThisDeploymentWhenDeploymentIdsMatch() throws Exception {
        given(mockJmsBytesMessage.getStringProperty(HMCTS_DEPLOYMENT_ID)).willReturn("ia");
        HmcMessage hmcMessage = TestUtils.createHmcMessage(SERVICE_CODE, LISTED);
        String stringMessage = OBJECT_MAPPER.writeValueAsString(hmcMessage);
        mocksToReadJmsByteMessage(stringMessage);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        hmcHearingsEventTopicListenerWithDeploymentFilterEnabled.onMessage(mockJmsBytesMessage);

        verify(hmcMessageProcessor, times(1)).processMessage(any(HmcMessage.class));
    }

    @Test
    public void processMessagesForThisDeploymentWhenNoDeploymentIdsConfigured() throws Exception {
        ReflectionTestUtils.setField(
            hmcHearingsEventTopicListenerWithDeploymentFilterEnabled, "hmctsDeploymentId", "");
        given(mockJmsBytesMessage.getStringProperty(HMCTS_DEPLOYMENT_ID)).willReturn(null);
        HmcMessage hmcMessage = TestUtils.createHmcMessage(SERVICE_CODE, LISTED);
        String stringMessage = OBJECT_MAPPER.writeValueAsString(hmcMessage);
        mocksToReadJmsByteMessage(stringMessage);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        hmcHearingsEventTopicListenerWithDeploymentFilterEnabled.onMessage(mockJmsBytesMessage);

        verify(hmcMessageProcessor, times(1)).processMessage(any(HmcMessage.class));
    }

    private void mocksToReadJmsByteMessage(String stringMessage) throws JMSException {
        byte[] byteMessage = stringMessage.getBytes(StandardCharsets.UTF_8);

        given(mockJmsBytesMessage.getBodyLength()).willReturn((long) byteMessage.length);
        given(mockJmsBytesMessage.readBytes(any(byte[].class))).willAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(byteMessage, 0, buffer, 0, byteMessage.length);
            return byteMessage.length;
        });
    }
}
