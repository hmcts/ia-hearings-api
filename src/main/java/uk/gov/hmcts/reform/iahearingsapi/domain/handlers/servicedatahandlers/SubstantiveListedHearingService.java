package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.ONPPRS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.CASE_MANAGEMENT_REVIEW;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingChannel;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingListingStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingType;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHmcStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;

@Slf4j
public class SubstantiveListedHearingService {

    public boolean isSubstantiveListedHearing(ServiceData serviceData) {
        return isHmcStatus(serviceData, HmcStatus.LISTED)
            && isHearingListingStatus(serviceData, ListingStatus.FIXED)
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, SUBSTANTIVE);
    }

    public boolean isSubstantiveCancelledHearing(ServiceData serviceData) {
        return isHmcStatus(serviceData, HmcStatus.CANCELLATION_REQUESTED)
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, SUBSTANTIVE);
    }

    public boolean isSubstantiveAwaitingListingHearing(ServiceData serviceData) {
        return isHmcStatus(serviceData, HmcStatus.AWAITING_LISTING)
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, SUBSTANTIVE);
    }

    public boolean isBailListedHearing(ServiceData serviceData) {
        return isHmcStatus(serviceData, HmcStatus.LISTED)
            && isHearingListingStatus(serviceData, ListingStatus.FIXED)
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, BAIL);
    }

    public String getCaseReference(ServiceData serviceData) {
        return serviceData.read(CASE_REF, String.class)
            .orElseThrow(() -> new IllegalStateException("Case reference can not be null"));
    }

    public void updateListCaseHearingDetails(ServiceData serviceData, AsylumCase asylumCase) {

        List<HearingChannel> hearingChannels = getHearingChannels(serviceData);
        String hearingVenueId = getHearingVenueId(serviceData);

        String newHearingDateTime = formatHearingDateTime(getHearingDatetime(serviceData, hearingVenueId));
        HearingCentre newHearingCentre = getHearingCenter(hearingChannels, hearingVenueId);
        DynamicList newHearingChannel = buildHearingChannelDynmicList(hearingChannels);

        boolean triggerReviewInterpreterTask;

        triggerReviewInterpreterTask = asylumCase.read(LIST_CASE_HEARING_DATE, String.class)
            .map(hearingDate -> !hearingDate.equals(newHearingDateTime))
            .orElse(true);

        triggerReviewInterpreterTask |= asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(hearingCentre -> !hearingCentre.equals(newHearingCentre))
            .orElse(true);

        triggerReviewInterpreterTask |= asylumCase.read(HEARING_CHANNEL, DynamicList.class)
            .map(hearingChannel -> !hearingChannel.equals(newHearingChannel))
            .orElse(true);

        //Review Interpreter Task will be triggered when List Case event is submitted with this flag set to Yes
        asylumCase.clear(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK);
        if (triggerReviewInterpreterTask) {
            asylumCase.write(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YES);
        }

        asylumCase.write(ARIA_LISTING_REFERENCE, getListingReference());
        asylumCase.write(LIST_CASE_HEARING_DATE, newHearingDateTime);
        asylumCase.write(LIST_CASE_HEARING_LENGTH,
                         String.valueOf(getHearingDuration(serviceData)));
        asylumCase.write(LIST_CASE_HEARING_CENTRE,
                         newHearingCentre);
        asylumCase.write(HEARING_CHANNEL, newHearingChannel);

    }

    public List<HearingChannel> getHearingChannels(ServiceData serviceData) {
        Optional<List<HearingChannel>> optionalHearingChannels = serviceData.read(HEARING_CHANNELS);

        return optionalHearingChannels
            .orElseThrow(() -> new IllegalStateException("hearingChannels can not be empty"));
    }

    public String getHearingVenueId(ServiceData serviceData) {
        return serviceData.read(HEARING_VENUE_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("hearingVenueId can not be null"));
    }

    public String formatHearingDateTime(LocalDateTime hearingDatetime) {
        return hearingDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    public String formatHearingDate(LocalDateTime hearingDatetime) {
        return hearingDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public LocalDateTime getHearingDatetime(
        ServiceData serviceData, String hearingVenueId) {
        LocalDateTime hearingDateTime = serviceData.read(NEXT_HEARING_DATE, LocalDateTime.class)
            .orElseThrow(() -> new IllegalStateException("nextHearingDate can not be null"));

        return HandlerUtils.getHearingDateAndTime(
            hearingDateTime.truncatedTo(ChronoUnit.MINUTES), hearingVenueId);
    }

    public int getHearingDuration(ServiceData serviceData) {
        return serviceData.read(DURATION, Integer.class)
            .orElseThrow(() -> new IllegalStateException("duration can not be null"));
    }

    public String getListingReference() {
        return "LAI";
    }

    public HearingCentre getHearingCenter(List<HearingChannel> hearingChannels, String hearingVenueId) {
        return HandlerUtils.getLocation(hearingChannels, hearingVenueId);
    }

    public DynamicList buildHearingChannelDynmicList(List<HearingChannel> hearingChannels) {
        return new DynamicList(new Value(
            hearingChannels.get(0).name(),
            hearingChannels.get(0).getLabel()
        ), List.of(new Value(
            hearingChannels.get(0).name(),
            hearingChannels.get(0).getLabel()
        )));
    }

    public boolean isCaseManagementReview(ServiceData serviceData) {
        return isHmcStatus(serviceData, HmcStatus.LISTED)
            && isHearingListingStatus(serviceData, ListingStatus.FIXED)
            && !isHearingChannel(serviceData, ONPPRS)
            && serviceData.read(HEARING_TYPE, String.class)
            .map(hearingType -> Objects.equals(hearingType, CASE_MANAGEMENT_REVIEW.getKey()))
            .orElse(false);
    }

    public void updateListCaseSendHomeOfficeDirection(ServiceData serviceData, BailCase bailCase) {

        String dueDate = formatHearingDate(getHearingDatetime(serviceData, null)
                                               .minusDays(1));

        bailCase.write(BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION,
                       "You must upload the Bail Summary by the date indicated below.\n"
                         + "If the applicant does not have a legal representative, "
                           + "you must also send them a copy of the Bail Summary.\n"
                         + "The Bail Summary must include:\n"
                         + "\n"
                         + "- the date when the current period of immigration detention started\n"
                         + "- whether the applicant is subject to Section 2 of the Illegal Migration Act 2023\n"
                         + "- any concerns in relation to the factors listed in paragraph 3(2) of Schedule "
                           + "10 to the 2016 Act\n"
                         + "- the bail conditions being sought should bail be granted\n"
                         + "- whether removal directions are in place\n"
                         + "- whether the applicant’s release is subject to licence, and if so the relevant details\n"
                         + "- any other relevant information\n\n"
                         + "Next steps\n"
                         + "Sign in to your account to upload the Bail Summary.\n"
                         + "You must complete this direction by: " + dueDate
        );

        bailCase.write(BailCaseFieldDefinition.SEND_DIRECTION_LIST, "Home Office");
        bailCase.write(BailCaseFieldDefinition.DATE_OF_COMPLIANCE, dueDate);
    }
}

