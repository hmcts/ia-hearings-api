package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;


@Component
@Slf4j
public class HearingsDynamicListPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    HearingService hearingService;


    public HearingsDynamicListPreparer(
        HearingService hearingService
    ) {
        this.hearingService = hearingService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START && Objects.equals(
            Event.UPDATE_HEARING_REQUEST,
            callback.getEvent()
        );
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        HearingsGetResponse hearings = hearingService.getHearings(callback.getCaseDetails().getId());
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        DynamicList dynamicListOfHearings = new DynamicList(
            new uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value("", ""),
            mapCaseHearingsValuesToDynamicListValues(hearings.getCaseHearings())
        );

        asylumCase.write(CHANGE_HEARINGS, dynamicListOfHearings);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private List<Value> mapCaseHearingsValuesToDynamicListValues(List<CaseHearing> caseHearings) {
        return caseHearings
            .stream()
            .map(hearing -> new Value(hearing.getHearingRequestId(), mapHearingLabel(hearing)))
            .filter(value -> !Strings.isNullOrEmpty(value.getLabel()))
            .collect(Collectors.toList());
    }

    private String mapHearingLabel(CaseHearing caseHearing) {
        return switch (caseHearing.getHmcStatus()) {
            case HEARING_REQUESTED, UPDATE_REQUESTED, UPDATE_SUBMITTED, AWAITING_LISTING, LISTED ->
                caseHearing.getHearingTypeDescription()
                    + " - "
                    + HearingsUtils.convertToLocalStringFormat(caseHearing.getHearingRequestDateTime());
            default -> null;
        };
    }
}
