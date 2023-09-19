package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingUpdateService {

    public void updateHearing(HearingGetResponse hearingGetResponse, AsylumCase asylumCase) {
        // call ccd service
    }
}
