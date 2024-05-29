package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.AWAITING_ACTUALS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils.convertFromUTC;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;

public class HearingsToDynamicListMapper {

    private HearingsToDynamicListMapper() {
    }

    public static DynamicList map(HearingsGetResponse hearings) {

        List<Value> hearingValues = hearings == null ? Collections.emptyList() : hearings.getCaseHearings()
            .stream()
            .filter(hearing -> hearing.getHearingType().equals(HearingType.SUBSTANTIVE.getKey()))
            .map(hearing -> new Value(hearing.getHearingRequestId(), mapHearingLabel(hearing)))
            .filter(value -> !Strings.isNullOrEmpty(value.getLabel()))
            .toList();

        return new DynamicList(new Value("", ""), hearingValues);
    }

    private static String mapHearingLabel(CaseHearing caseHearing) {
        return switch (caseHearing.getHmcStatus()) {
            case AWAITING_LISTING ->
                caseHearing.getHearingTypeDescription()
                + " (Waiting to be listed)";
            case LISTED, AWAITING_ACTUALS ->
                getListedAndAwaitingHearingDetailsDescription(caseHearing);
            case UPDATE_SUBMITTED ->
                caseHearing.getHearingTypeDescription()
                + " (Update requested)";
            default -> null;
        };
    }

    private static String getListedAndAwaitingHearingDetailsDescription(CaseHearing caseHearing) {
        if (caseHearing.getHearingDaySchedule() != null
            && !caseHearing.getHearingDaySchedule().isEmpty()) {
            return caseHearing.getHearingTypeDescription()
                   + (caseHearing.getHmcStatus() == AWAITING_ACTUALS ? " (Awaiting hearing details)" : " (Listed)")
                   + " - " + HearingsUtils.convertToLocalStringFormat(convertFromUTC(caseHearing
                                                                                 .getHearingDaySchedule()
                                                                                 .get(0)
                                                                                 .getHearingStartDateTime()));
        } else {
            return null;
        }
    }
}
