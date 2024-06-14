package uk.gov.hmcts.reform.iahearingsapi;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
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

    public static ServiceData createServiceData(String serviceCode) {
        ServiceData serviceData = new ServiceData();

        serviceData.write(ServiceDataFieldDefinition.HMCTS_SERVICE_CODE, serviceCode);
        serviceData.write(ServiceDataFieldDefinition.CASE_REF, "1234L");

        return serviceData;
    }

}
