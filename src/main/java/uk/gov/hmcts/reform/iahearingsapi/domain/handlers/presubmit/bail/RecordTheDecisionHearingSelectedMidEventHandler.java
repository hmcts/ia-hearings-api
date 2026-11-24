package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.bail;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_CASE_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_METHODS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAPER_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils.convertFromUTC;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils.convertToLocalStringFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

/**
 * Fills in the details of the hearing chosen on the record the decision event from HMC.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RecordTheDecisionHearingSelectedMidEventHandler implements PreSubmitCallbackHandler<BailCase> {

    static final String SELECT_HEARING_PAGE_ID = "selectHearingRecordAttendeesAndDuration";
    private static final DateTimeFormatter DATE_TIME_LABEL_FORMATTER =
        DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.UK);

    private final HearingService hearingService;
    private final HearingActualsService hearingActualsService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.RECORD_THE_DECISION
               && Objects.equals(callback.getPageId(), SELECT_HEARING_PAGE_ID);
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        Optional<String> selectedHearingId = bailCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class)
            .map(DynamicList::getValue)
            .map(Value::getCode)
            .filter(code -> !code.isBlank());

        if (selectedHearingId.isEmpty()) {
            return new PreSubmitCallbackResponse<>(bailCase);
        }

        String hearingId = selectedHearingId.get();
        List<String> hearingChannels = Optional
            .ofNullable(hearingService.getHearing(hearingId).getHearingDetails().getHearingChannels())
            .orElse(List.of());
        List<PlannedHearingDay> plannedHearingDays = plannedHearingDays(hearingId);
        String hearingDate = plannedHearingDays.stream()
            .findFirst()
            .map(PlannedHearingDay::getPlannedStartTime)
            .orElse("");

        bailCase.write(ATTENDEES_AND_DURATION_CASE_REFERENCE, String.valueOf(callback.getCaseDetails().getId()));
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_ID, hearingId);
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_DATE, hearingDate);
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY, toDateTimeLabel(hearingDate));
        bailCase.write(
            ATTENDEES_AND_DURATION_PAPER_HEARING,
            hearingChannels.contains(HearingChannel.ONPPRS.name()) ? YesOrNo.YES.toString() : YesOrNo.NO.toString()
        );
        bailCase.write(
            ATTENDEES_AND_DURATION_HEARING_METHODS,
            hearingChannels.stream().map(this::toChannelLabel).collect(Collectors.joining(", "))
        );
        bailCase.write(ATTENDEES_AND_DURATION_DATE_OF_HEARING, toHearingDates(bailCase, plannedHearingDays));

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private List<PlannedHearingDay> plannedHearingDays(String hearingId) {
        return Optional.ofNullable(hearingActualsService.getHearingActuals(hearingId))
            .map(HearingActualsData::getHearingPlanned)
            .map(HearingPlanned::getPlannedHearingDays)
            .orElse(List.of());
    }

    private DynamicList toHearingDates(BailCase bailCase, List<PlannedHearingDay> plannedHearingDays) {
        List<Value> hearingDates = plannedHearingDays.stream()
            .map(day -> new Value(day.getPlannedStartTime(), toDateLabel(day.getPlannedStartTime())))
            .toList();

        // keeps the day already chosen when the user comes back to this page for the same hearing
        Value selected = bailCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class)
            .map(DynamicList::getValue)
            .filter(hearingDates::contains)
            .orElse(new Value("", ""));

        return new DynamicList(selected, hearingDates);
    }

    private String toChannelLabel(String channel) {
        return Arrays.stream(HearingChannel.values())
            .filter(value -> value.name().equals(channel))
            .map(HearingChannel::getLabel)
            .findFirst()
            .orElse(channel);
    }

    private String toDateLabel(String plannedStartTime) {
        if (plannedStartTime == null || plannedStartTime.isEmpty()) {
            return "";
        }
        return convertToLocalStringFormat(convertFromUTC(LocalDateTime.parse(plannedStartTime)));
    }

    private String toDateTimeLabel(String plannedStartTime) {
        if (plannedStartTime == null || plannedStartTime.isEmpty()) {
            return "";
        }
        return convertFromUTC(LocalDateTime.parse(plannedStartTime)).format(DATE_TIME_LABEL_FORMATTER);
    }
}
