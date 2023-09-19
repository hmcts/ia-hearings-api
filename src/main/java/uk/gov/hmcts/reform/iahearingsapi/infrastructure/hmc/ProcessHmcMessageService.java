package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessHmcMessageService {
    //    private final MessageDispatcher messageDispatcher;
    private final HearingService hearingService;
    private final CoreCaseDataService coreCaseDataService;

    public void processEventMessage(HmcMessage hmcMessage) {

        Long caseId = hmcMessage.getCaseId();
        AsylumCase asylumCase = coreCaseDataService.getCase(String.valueOf(caseId));

        String hearingId = hmcMessage.getHearingId();

        try {
            HearingGetResponse hearing = hearingService.getHearing(hearingId);

            HmcStatus hmcStatus = hmcMessage.getHearingUpdate().getHmcStatus();

            // TODO: Implement handling of different HMC status
            // messageDispatcher.handle(hearing);

        } catch (Exception e) {
            log.error("Processing hearingId [{}] failed due to error: {}", hearingId, e.getMessage());
        }
    }
}
