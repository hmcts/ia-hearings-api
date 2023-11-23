package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.AWAITING_ACTUALS;

import com.google.common.base.Strings;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;


@Component
@Slf4j
public class HearingsDynamicListPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public static final String WAITING_TO_BE_LISTED = "(Waiting to be listed)";
    public static final String UPDATE_REQUESTED = "(Update requested)";
    public static final String LISTED = "(Listed)";
    public static final String AWAITING_HEARING_DETAILS = "(Awaiting hearing details)";


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

    protected List<Value> mapCaseHearingsValuesToDynamicListValues(List<CaseHearing> caseHearings) {
        return caseHearings
            .stream()
            .filter(hearing -> hearing.getHearingType().equals(HearingType.SUBSTANTIVE.getKey()))
            .map(hearing -> new Value(hearing.getHearingRequestId(), mapHearingLabel(hearing)))
            .filter(value -> !Strings.isNullOrEmpty(value.getLabel()))
            .collect(Collectors.toList());
    }

    private String mapHearingLabel(CaseHearing caseHearing) {
        return switch (caseHearing.getHmcStatus()) {
            case AWAITING_LISTING -> // WAITING TO BE LISTED
                caseHearing.getHearingTypeDescription()
                    + " " + WAITING_TO_BE_LISTED;
            case LISTED, AWAITING_ACTUALS -> // LISTED
                getListedAndAwaitingHearingDetailsDescription(caseHearing);
            case UPDATE_REQUESTED -> // UPDATE REQUESTED
                caseHearing.getHearingTypeDescription()
                    + " " + UPDATE_REQUESTED;
            default -> null;
        };
    }

    private String getListedAndAwaitingHearingDetailsDescription(CaseHearing caseHearing) {
        String description = null;
        if (caseHearing.getHearingDaySchedule() != null
            && !caseHearing.getHearingDaySchedule().isEmpty()) {
            description = caseHearing.getHearingTypeDescription()
                + " " + (caseHearing.getHmcStatus().equals(AWAITING_ACTUALS) ? AWAITING_HEARING_DETAILS : LISTED)
                + " - " + HearingsUtils.convertToLocalStringFormat(caseHearing
                                                                       .getHearingDaySchedule()
                                                                       .get(0)
                                                                       .getHearingStartDateTime());

        }
        return description;
    }
}
