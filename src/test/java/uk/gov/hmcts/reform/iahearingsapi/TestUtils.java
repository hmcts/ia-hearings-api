package uk.gov.hmcts.reform.iahearingsapi;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;

public class TestUtils {

    public static HmcMessage createHmcMessage(String messageServiceCode, HmcStatus hmcStatus) {
        return HmcMessage.builder()
            .hmctsServiceCode(messageServiceCode)
            .caseId(1234L)
            .hearingId("testId")
            .hearingUpdate(
                HearingUpdate.builder()
                    .hmcStatus(hmcStatus).build())
            .build();
    }
}
