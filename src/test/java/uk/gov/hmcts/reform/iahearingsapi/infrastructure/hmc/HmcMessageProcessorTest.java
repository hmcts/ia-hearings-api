package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_UPDATE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.TestUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HmcMessageProcessorTest {

    @Mock
    private HmcMessageDispatcher<ServiceData> dispatcher;

    private HmcMessageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new HmcMessageProcessor(dispatcher);
    }

    @Test
    void should_process_hmc_message() {
        ServiceData serviceData = TestUtils.createServiceData("BFA1");

        assertNotNull(serviceData.read(HEARING_UPDATE, HearingUpdate.class).orElse(null));
        assertNotNull(serviceData.read(HEARING_ID, HearingUpdate.class).orElse(null));
        assertNotNull(serviceData.read(CASE_REF, HearingUpdate.class).orElse(null));

        processor.processMessage(serviceData);

        verify(dispatcher, times(1)).dispatch(serviceData);
    }

}
