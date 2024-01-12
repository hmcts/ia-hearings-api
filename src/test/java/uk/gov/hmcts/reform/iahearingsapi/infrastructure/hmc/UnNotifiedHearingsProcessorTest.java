package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_REQUEST_VERSION_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_CATEGORY;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CategoryType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingDaySchedule;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingRequestDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UnNotifiedHearingsProcessorTest {

    private static final String HMCTS_SERVICE_CODE = "BFA1";
    private static final long CASE_ID_1 = 1111L;
    private static final long CASE_ID_2 = 2222L;
    private static final String HEARING_ID_1 = "2000000051";
    private static final String HEARING_ID_2 = "2000000052";

    private static final HearingChannel IN_PERSON = HearingChannel.INTER;
    private static final String HEARING_VENUE_ID = "366559"; // Glasgow T.C.
    private static final HmcStatus HMC_STATUS_LISTED = HmcStatus.LISTED;
    private static final int TWO_HOURS = 120;
    private static final long VERSION_1 = 1L;
    private static final HearingType SUBSTANTIVE = HearingType.SUBSTANTIVE;
    private static final HmcStatus HMC_STATUS_EXCEPTION = HmcStatus.EXCEPTION;
    private static final ListingStatus HEARING_LISTING_STATUS = ListingStatus.FIXED;
    private static final ListAssistCaseStatus LIST_ASSIST_CASE_STATUS = ListAssistCaseStatus.LISTED;
    @Mock
    private HmcUpdateDispatcher<ServiceData> dispatcher;
    @Mock
    private HearingService hearingService;
    @Mock
    private HmcMessage hmcMessage;
    @Mock
    private HearingUpdate hearingUpdate;
    @Mock
    private HearingGetResponse hearingGetResponse;
    @Mock
    private HearingDetails hearingDetails;


    @Mock
    private UnNotifiedHearingsResponse unNotifiedHearingsResponse;

    private LocalDateTime now;
    private LocalDateTime anHourLater;
    private LocalDateTime twoDaysLater;
    private UnNotifiedHearingsProcessor unNotifiedHearingsProcessor;

    @BeforeEach
    void setUp() {
        unNotifiedHearingsProcessor = new UnNotifiedHearingsProcessor(dispatcher, hearingService);
        now = LocalDateTime.now();
        anHourLater = now.plusHours(1L);
        twoDaysLater = now.plusDays(2L);
    }

    private List<HearingGetResponse> createHearingGetResponses() {
        CaseCategoryModel caseCategoryModel = new CaseCategoryModel();
        caseCategoryModel.setCategoryType(CategoryType.CASE_TYPE);
        caseCategoryModel.setCategoryValue("BFA1-BLS");
        caseCategoryModel.setCategoryParent("");
        HearingGetResponse hearing1 = HearingGetResponse.builder()
            .requestDetails(HearingRequestDetails.builder()
                                .status(HMC_STATUS_LISTED.name())
                                .versionNumber(VERSION_1)
                                .build())
            .caseDetails(CaseDetailsHearing.builder()
                             .caseRef(String.valueOf(CASE_ID_1))
                             .caseCategories(List.of(caseCategoryModel))
                             .hmctsServiceCode(HMCTS_SERVICE_CODE)
                             .build())
            .hearingResponse(HearingResponse.builder()
                                 .hearingDaySchedule(List.of(HearingDaySchedule.builder()
                                                                 .hearingStartDateTime(twoDaysLater)
                                                                 .hearingVenueId(HEARING_VENUE_ID)
                                                                 .build()))
                                 .listingStatus(HEARING_LISTING_STATUS)
                                 .laCaseStatus(LIST_ASSIST_CASE_STATUS)
                                 .receivedDateTime(anHourLater)
                                 .build())
            .hearingDetails(HearingDetails.builder()
                                .hearingChannels(List.of(IN_PERSON.name()))
                                .hearingType(SUBSTANTIVE.getKey())
                                .duration(TWO_HOURS)
                                .build())
            .build();

        HearingGetResponse hearing2 = HearingGetResponse.builder()
            .requestDetails(HearingRequestDetails.builder()
                                .status(HMC_STATUS_LISTED.name())
                                .versionNumber(VERSION_1)
                                .build())
            .caseDetails(CaseDetailsHearing.builder()
                             .caseRef(String.valueOf(CASE_ID_2))
                             .caseCategories(List.of(caseCategoryModel))
                             .hmctsServiceCode(HMCTS_SERVICE_CODE)
                             .build())
            .hearingResponse(HearingResponse.builder()
                                 .hearingDaySchedule(List.of(HearingDaySchedule.builder()
                                                                 .hearingStartDateTime(twoDaysLater)
                                                                 .hearingVenueId(HEARING_VENUE_ID)
                                                                 .build()))
                                 .listingStatus(HEARING_LISTING_STATUS)
                                 .laCaseStatus(LIST_ASSIST_CASE_STATUS)
                                 .receivedDateTime(anHourLater)
                                 .build())
            .hearingDetails(HearingDetails.builder()
                                .hearingChannels(List.of(IN_PERSON.name()))
                                .hearingType(SUBSTANTIVE.getKey())
                                .duration(TWO_HOURS)
                                .build())
            .build();

        return List.of(hearing1, hearing2);
    }

    @Test
    void should_process_unnotified_hearings() {
        List<HearingGetResponse> hearings = createHearingGetResponses();

        when(hearingService.getUnNotifiedHearings(any(LocalDateTime.class))).thenReturn(unNotifiedHearingsResponse);
        when(unNotifiedHearingsResponse.getHearingIds()).thenReturn(List.of(HEARING_ID_1, HEARING_ID_2));
        when(hearingService.getHearing(HEARING_ID_1)).thenReturn(hearings.get(0));
        when(hearingService.getHearing(HEARING_ID_2)).thenReturn(hearings.get(1));

        unNotifiedHearingsProcessor.processUnNotifiedHearings();

        ServiceData serviceData1 = new ServiceData();
        serviceData1.write(ServiceDataFieldDefinition.HMCTS_SERVICE_CODE, HMCTS_SERVICE_CODE);
        serviceData1.write(CASE_REF, CASE_ID_1);
        serviceData1.write(ServiceDataFieldDefinition.HEARING_ID, HEARING_ID_1);
        serviceData1.write(ServiceDataFieldDefinition.NEXT_HEARING_DATE, twoDaysLater);
        serviceData1.write(ServiceDataFieldDefinition.HEARING_VENUE_ID, HEARING_VENUE_ID);
        serviceData1.write(ServiceDataFieldDefinition.HMC_STATUS, HMC_STATUS_LISTED);
        serviceData1.write(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, HEARING_LISTING_STATUS);
        serviceData1.write(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, LIST_ASSIST_CASE_STATUS);
        serviceData1.write(ServiceDataFieldDefinition.HEARING_RESPONSE_RECEIVED_DATE_TIME,
                          anHourLater);
        serviceData1.write(HEARING_CHANNELS, List.of(HearingChannel.INTER));
        serviceData1.write(HEARING_TYPE, HearingType.SUBSTANTIVE.getKey());
        serviceData1.write(DURATION, 120);
        serviceData1.write(HEARING_REQUEST_VERSION_NUMBER,VERSION_1);

        ServiceData serviceData2 = new ServiceData();
        serviceData2.write(ServiceDataFieldDefinition.HMCTS_SERVICE_CODE, HMCTS_SERVICE_CODE);
        serviceData2.write(CASE_REF, CASE_ID_2);
        serviceData2.write(ServiceDataFieldDefinition.HEARING_ID, HEARING_ID_2);
        serviceData2.write(ServiceDataFieldDefinition.NEXT_HEARING_DATE, twoDaysLater);
        serviceData2.write(ServiceDataFieldDefinition.HEARING_VENUE_ID, HEARING_VENUE_ID);
        serviceData2.write(ServiceDataFieldDefinition.HMC_STATUS, HMC_STATUS_LISTED);
        serviceData2.write(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, HEARING_LISTING_STATUS);
        serviceData2.write(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, LIST_ASSIST_CASE_STATUS);
        serviceData2.write(ServiceDataFieldDefinition.HEARING_RESPONSE_RECEIVED_DATE_TIME,
                           anHourLater);
        serviceData2.write(HEARING_CHANNELS, List.of(HearingChannel.INTER));
        serviceData2.write(HEARING_TYPE, HearingType.SUBSTANTIVE.getKey());
        serviceData2.write(DURATION, 120);
        serviceData2.write(HEARING_REQUEST_VERSION_NUMBER,VERSION_1);

        ArgumentCaptor<ServiceData> serviceDataArgumentCaptor = ArgumentCaptor.forClass(ServiceData.class);
        verify(dispatcher, times(2)).dispatch(serviceDataArgumentCaptor.capture());

        ServiceData actualServiceData1 = serviceDataArgumentCaptor.getAllValues().get(0);
        ServiceData actualServiceData2 = serviceDataArgumentCaptor.getAllValues().get(1);

        assertTrue(actualServiceData1.keySet().containsAll(Set.of(ServiceDataFieldDefinition.HMCTS_SERVICE_CODE.value(),
                                                        ServiceDataFieldDefinition.HEARING_ID.value(),
                                                        ServiceDataFieldDefinition.NEXT_HEARING_DATE.value(),
                                                        ServiceDataFieldDefinition.HEARING_VENUE_ID.value(),
                                                        ServiceDataFieldDefinition.HMC_STATUS.value(),
                                                        ServiceDataFieldDefinition.HEARING_LISTING_STATUS.value(),
                                                        ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS.value(),
                                                        ServiceDataFieldDefinition.HEARING_RESPONSE_RECEIVED_DATE_TIME
                                                                      .value(),
                                                        HEARING_CHANNELS.value(),
                                                        HEARING_TYPE.value(),
                                                        DURATION.value(),
                                                        CASE_CATEGORY.value())));
        assertTrue(actualServiceData2.keySet().containsAll(Set.of(ServiceDataFieldDefinition.HMCTS_SERVICE_CODE.value(),
                                                                  ServiceDataFieldDefinition.HEARING_ID.value(),
                                                                  ServiceDataFieldDefinition.NEXT_HEARING_DATE.value(),
                                                                  ServiceDataFieldDefinition.HEARING_VENUE_ID.value(),
                                                                  ServiceDataFieldDefinition.HMC_STATUS.value(),
                                                                  ServiceDataFieldDefinition.HEARING_LISTING_STATUS
                                                                      .value(),
                                                                  ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS
                                                                      .value(),
                                                                  ServiceDataFieldDefinition
                                                                      .HEARING_RESPONSE_RECEIVED_DATE_TIME.value(),
                                                                  HEARING_CHANNELS.value(),
                                                                  HEARING_TYPE.value(),
                                                                  DURATION.value(),
                                                                  CASE_CATEGORY.value())));

        assertEquals(HEARING_ID_1, actualServiceData1.read(HEARING_ID, String.class).orElse(""));
        assertEquals(HEARING_ID_2, actualServiceData2.read(HEARING_ID, String.class).orElse(""));
    }

}

