package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.CASE_MANAGEMENT_REVIEW;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.AWAITING_ACTUALS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.UPDATE_REQUESTED;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingDaySchedule;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;

@Service
@RequiredArgsConstructor
public class NextHearingDateService {

    private final HearingService hearingService;
    private final FeatureToggler featureToggler;

    public boolean enabled() {
        return featureToggler.getValue("nextHearingDateEnabled", false);
    }

    public NextHearingDetails getNextHearingDetails(long caseId) {

        HearingsGetResponse hearingsGetResponse = hearingService.getHearings(caseId);

        return Optional.of(hearingsGetResponse)
            .map(HearingsGetResponse::getCaseHearings)
            .orElse(Collections.emptyList())
            .stream()
            .filter(this::isTargetHearingForDateCalculation)
            .min(this::compareHearings)
            .map(this::mapHearingToNextHearingDetails)
            .orElseGet(() -> NextHearingDetails.builder().hearingId(null).hearingDateTime(null).build());

    }

    private boolean isTargetHearingForDateCalculation(CaseHearing hearing) {
        LocalDateTime hearingDateTime = getHearingDateTime(hearing);
        return List.of(LISTED, UPDATE_REQUESTED, AWAITING_ACTUALS).contains(hearing.getHmcStatus())
               && List.of(SUBSTANTIVE.getKey(), CASE_MANAGEMENT_REVIEW.getKey()).contains(hearing.getHearingType())
               && hearingDateTime != null
               && !hearingDateTime.isBefore(LocalDateTime.now())
               && hearing.getHearingRequestDateTime() != null;
    }

    protected LocalDateTime getHearingDateTime(CaseHearing caseHearing) {
        // Get the earliest hearing start date
        if (caseHearing.getHearingDaySchedule() == null) {
            return null;
        }
        return caseHearing.getHearingDaySchedule().stream()
            .map(HearingDaySchedule::getHearingStartDateTime)
            .filter(Objects::nonNull)
            .min(LocalDateTime::compareTo).orElse(null);
    }

    private NextHearingDetails mapHearingToNextHearingDetails(CaseHearing caseHearing) {

        String hearingDateTime = Optional.of(getHearingDateTime(caseHearing))
            .map(LocalDateTime::toString).orElse("");

        return NextHearingDetails.builder()
            .hearingId(hearingDateTime.isEmpty() ? null : caseHearing.getHearingRequestId())
            .hearingDateTime(hearingDateTime).build();
    }

    private int compareHearings(CaseHearing hearing1, CaseHearing hearing2) {

        LocalDateTime hearingDateTime1 = getHearingDateTime(hearing1);
        LocalDateTime hearingDateTime2 = getHearingDateTime(hearing2);

        if (hearingDateTime1.equals(hearingDateTime2)) {
            return hearing1.getHearingRequestDateTime().compareTo(hearing2.getHearingRequestDateTime());
        } else {
            return hearingDateTime1.compareTo(hearingDateTime2);
        }
    }
}
