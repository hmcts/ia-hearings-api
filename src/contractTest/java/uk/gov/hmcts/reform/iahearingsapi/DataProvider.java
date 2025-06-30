package uk.gov.hmcts.reform.iahearingsapi;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

import au.com.dius.pact.consumer.dsl.DslPart;
import io.pactfoundation.consumer.dsl.LambdaDsl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLinkData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingRequestDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HearingRequestGenerator;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.DeleteHearingRequest;

public class DataProvider {

    public static final String CASE_REFERENCE = "1111222233334444";
    public static final String HMC_PROVIDER = "hmcHearingServiceProvider";
    public static final String IAC_PROVIDER = "iacHearingApiProvider";
    public static final String CONSUMER = "iacHearingApiConsumer";
    public static final String AUTH_TOKEN = "Bearer some-access-token";
    public static final String SERVICE_AUTH_HEADER = "ServiceAuthorization";
    public static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String PORT = "4561";

    public static DslPart buildHearingGetResponseDsl() {
        return LambdaDsl.newJsonBody((body) -> {
            body
                .object("requestDetails", (requestDetails) -> {
                    requestDetails.stringType("status", "HEARING_REQUESTED");
                    requestDetails.stringType("timestamp", "2023-09-20T10:09:19.93734");
                    requestDetails.numberType("versionNumber", 1);
                    requestDetails.stringType("hearingRequestID", "2000000056");
                    requestDetails.stringType("hearingGroupRequestId", "hearingGroupRequestId");
                    requestDetails.stringType("partiesNotified", "2023-09-20T10:09:19.93734");
                })
                .object("hearingDetails", (hearingDetails) -> {
                    hearingDetails.stringType("listingAutoChangeReasonCode", "user-added-comments");
                    hearingDetails.stringType("hearingType", "BFA1-SUB");
                    hearingDetails.object("hearingWindow", (hearingWindow) -> {
                        hearingWindow.stringType("dateRangeStart", "2023-09-20");
                        hearingWindow.stringType("dateRangeEnd", "2023-10-04");
                    });
                    hearingDetails.numberType("duration", 150);
                    hearingDetails.stringType("hearingPriorityType", "Standard");
                    hearingDetails.numberType("numberOfPhysicalAttendees", 0);
                    hearingDetails.booleanType("hearingInWelshFlag", false);
                    hearingDetails.array("hearingLocations", (hearingLocations) -> {
                        hearingLocations.object(hearingLocation -> {
                            hearingLocation.stringType("locationType", "court");
                            hearingLocation.stringType("locationId", "386417");
                        });
                    });
                    hearingDetails.array("facilitiesRequired", (facilitiesRequired) -> {
                        facilitiesRequired.stringType("facilitiesRequired");
                    });
                    hearingDetails.stringType("listingComments", "some adjustments");
                    hearingDetails.stringType("hearingRequester", "hearingRequester");
                    hearingDetails.booleanType("privateHearingRequiredFlag", true);
                    hearingDetails.stringType("leadJudgeContractType", "leadJudgeContractType");
                    hearingDetails.object("panelRequirements", (panelRequirements) -> {
                        panelRequirements.array("roleType", roleType -> roleType.stringType("84"));
                        panelRequirements.array("authorisationTypes", authorisationTypes ->
                            authorisationTypes.stringType(""));
                        panelRequirements.array("authorisationSubType", authorisationSubType ->
                            authorisationSubType.stringType(""));
                        panelRequirements.array("panelPreferences", panelPreferences ->
                            panelPreferences.object(panelPreference -> {
                                panelPreference.stringType("memberID", "memberID");
                                panelPreference.stringType("memberType", "JUDGE");
                                panelPreference.stringType("requirementType", "EXCLUDE");
                            })
                        );
                        panelRequirements.array("panelSpecialisms", panelSpecialisms ->
                            panelSpecialisms.stringType("panelSpecialisms"));
                    });
                    hearingDetails.booleanType("hearingIsLinkedFlag", false);
                    hearingDetails.array("hearingChannels", hearingChannels ->
                        hearingChannels.stringType("INTER"));
                    hearingDetails.booleanType("autolistFlag", false);
                    hearingDetails.stringType("caseSLAStartDate", "caseSLAStartDate");
                    hearingDetails.stringType("caserestrictedFlag", "caserestrictedFlag");
                })
                .object("caseDetails", (caseDetails) -> {
                    caseDetails.stringType("hmctsServiceCode", "BFA1");
                    caseDetails.stringType("caseRef", "1694535157958319");
                    caseDetails.stringType("externalCaseReference", "111112222");
                    caseDetails.stringType(
                        "caseDeepLink",
                        "http://localhost:3002/cases/case-details/1694535157958319#Overview");
                    caseDetails.stringType("hmctsInternalCaseName", "mmm nnn");
                    caseDetails.stringType("publicCaseName", "mmm nnn");
                    caseDetails.booleanType("caseAdditionalSecurityFlag", true);
                    caseDetails.booleanType("caseInterpreterRequiredFlag", true);
                    caseDetails.array("caseCategories", (caseCategories) -> {
                        caseCategories.object(caseCategory -> {
                            caseCategory.stringType("categoryType", "caseType");
                            caseCategory.stringType("categoryValue", "BFA1-TST");
                            caseCategory.stringType("categoryParent", "");
                        });
                    });
                    caseDetails.stringType("caseManagementLocationCode", "227101");
                    caseDetails.booleanType("caserestrictedFlag", false);
                    caseDetails.stringType("caseSLAStartDate", "2023-09-20");
                })
                .array("partyDetails", (partyDetails) -> {
                    partyDetails.object(partyDetail -> {
                        partyDetail.stringType("partyID", "111112222");
                        partyDetail.stringType("partyType", "ORG");
                        partyDetail.stringType("partyRole", "RESP");
                        partyDetail.object("organisationDetails", (organisationDetails) -> {
                            organisationDetails.stringType("name","Secretary of State");
                            organisationDetails.stringType("organisationType","ORG");
                            organisationDetails.stringType("cftOrganisationID",null);
                        });
                        partyDetail.object("individualDetails", (individualDetails) -> {
                            individualDetails.stringType("title", null);
                            individualDetails.stringType("firstName", "eee");
                            individualDetails.stringType("lastName", "fff");
                            individualDetails.stringType("preferredHearingChannel", "INTER");
                            individualDetails.stringType("interpreterLanguage", null);
                            individualDetails.array("reasonableAdjustments", reasonableAdjustments ->
                                reasonableAdjustments.stringType(""));
                            individualDetails.stringType("vulnerableFlag", null);
                            individualDetails.stringType("vulnerabilityDetails", null);
                            individualDetails.array("hearingChannelEmail", hearingChannelEmail ->
                                hearingChannelEmail.stringType(""));
                            individualDetails.array("hearingChannelPhone", hearingChannelPhone ->
                                hearingChannelPhone.stringType(""));
                            individualDetails.array("relatedParties", relatedParties ->
                                relatedParties.object(relatedParty -> {
                                    relatedParty.stringType("relatedPartyID", "");
                                    relatedParty.stringType("relationshipType", "");
                                }));
                            individualDetails.stringType("custodyStatus", "");
                            individualDetails.stringType("otherReasonableAdjustmentDetails", "");
                        });
                    });
                })
                .object("hearingResponse", (hearingResponse) -> {
                    hearingResponse.stringType("listAssistTransactionID", "listAssistTransactionID");
                    hearingResponse.stringType("receivedDateTime", "2023-09-20T10:09:19.93734");
                    hearingResponse.stringType("laCaseStatus", "LISTED");
                    hearingResponse.stringType("listingStatus", "FIXED");
                    hearingResponse.stringType("hearingCancellationReason", "hearingCancellationReason");
                    hearingResponse.array("hearingDaySchedule", hearingDaySchedule ->
                        hearingDaySchedule.object(schedule -> {
                            schedule.stringType("hearingStartDateTime", "2023-09-20T10:09:19.93734");
                            schedule.stringType("hearingEndDateTime", "2023-09-20T10:09:19.93734");
                            schedule.stringType("listAssistSessionId", "listAssistSessionId");
                            schedule.stringType("hearingVenueId", "hearingVenueId");
                            schedule.stringType("hearingRoomId", "hearingRoomId");
                            schedule.stringType("hearingJudgeId", "hearingJudgeId");
                            schedule.array("panelMemberIds", panelMemberIds ->
                                panelMemberIds.stringType("panelMemberIds"));
                            schedule.array("attendees", attendees -> attendees.object(attendee -> {
                                attendee.stringType("partyID", "partyID");
                                attendee.stringType("hearingSubChannel", "INTER");
                            }));
                        })
                    );
                });
        }).build();
    }

    public static DslPart buildHearingsGetResponseDsl(String caseRef) {
        return LambdaDsl.newJsonBody((body) -> {
            body
                .stringType("caseRef", caseRef)
                .array("caseHearings", (caseHearings) ->
                    caseHearings.object(caseHearing -> {
                        caseHearing.array("hearingChannels", hearingChannels ->
                            hearingChannels.stringType("hearingChannels"));
                        caseHearing.array("hearingDaySchedule", hearingDaySchedule ->
                            hearingDaySchedule.object(schedule -> {
                                schedule.stringType("hearingStartDateTime", "2023-09-20T10:09:19.93734");
                                schedule.stringType("hearingEndDateTime", "2023-09-20T10:09:19.93734");
                                schedule.stringType("listAssistSessionId", "listAssistSessionId");
                                schedule.stringType("hearingVenueId", "hearingVenueId");
                                schedule.stringType("hearingRoomId", "hearingRoomId");
                                schedule.stringType("hearingJudgeId", "hearingJudgeId");
                                schedule.array("panelMemberIds", panelMemberIds ->
                                    panelMemberIds.stringType("panelMemberIds"));
                                schedule.array("attendees", attendees -> attendees.object(attendee -> {
                                    attendee.stringType("partyID", "partyID");
                                    attendee.stringType("hearingSubChannel", "INTER");
                                }));
                            })
                        );
                        caseHearing.stringType("hearingGroupRequestId", "hearingGroupRequestId");
                        caseHearing.stringType("hearingID", "hearingID");
                        caseHearing.booleanType("hearingIsLinkedFlag", false);
                        caseHearing.stringType("hearingListingStatus", "hearingListingStatus");
                        caseHearing.stringType("hearingRequestDateTime", "2023-09-20T10:09:19.93734");
                        caseHearing.stringType("hearingType", "hearingType");
                        caseHearing.stringType("hmcStatus", "LISTED");
                        caseHearing.stringType("lastResponseReceivedDateTime", "2023-09-20T10:09:19.93734");
                        caseHearing.stringType("listAssistCaseStatus", "listAssistCaseStatus");
                        caseHearing.stringType("requestVersion", "requestVersion");
                    })
                )
                .stringType("hmctsServiceCode", "hmctsServiceCode");
        }).build();
    }

    public static DslPart buildGetPartiesNotifiedRequestDsl() {
        return newJsonBody((body) -> {
            body
                .stringType("hearingID", "2000000056")
                .array("responses", (responses) -> {
                    responses.object(response -> {
                        response.stringType("responseReceivedDateTime", "");
                        response.numberType("requestVersion", 1);
                        response.stringType("partiesNotified", "");
                        response.object("serviceData", (serviceData) -> {
                            serviceData.booleanType("hearingNoticeGenerated", true);
                            serviceData.stringType("hearingDate", "2023-09-20T10:09:19.93734");
                            serviceData.stringType("hearingLocation",
                                                   "Hatton Cross Tribunal Hearing Centre");
                            serviceData.array("days", days -> {
                                days.object(day -> {
                                    day.stringType("hearingStartDateTime", "2023-09-20T10:09:19.93734");
                                    day.stringType("hearingEndDateTime", "2023-09-20T11:09:19.93734");
                                });
                            });
                        });
                    });
                });
        }).build();
    }

    public static DslPart buildHmcHearingResponse() {
        return newJsonBody((body) -> {
            body
                .numberType("hearingRequestId", 12345)
                .numberType("requestVersion", 12345)
                .numberType("responseVersion", 12345)
                .stringType("status", "status")
                .stringType("timeStamp", "2023-09-20T10:09:19.93734")
                .numberType("versionNumber", 12345);
        }).build();
    }

    public static DslPart buildGetUnNotifiedHearingsResponseDsl() {
        return newJsonBody((body) -> {
            body.numberType("totalFound", 3)
                .array("hearingIds", ids -> {
                    ids.stringValue("hearingId1");
                    ids.stringValue("hearingId2");
                    ids.stringValue("hearingId3");
                });
        }).build();
    }

    public static DeleteHearingRequest getDeleteHearingRequest() {

        DeleteHearingRequest deleteHearingRequest = new DeleteHearingRequest();
        deleteHearingRequest.setCancellationReasonCodes(List.of("cancellationCode1", "cancellationCode2"));

        return deleteHearingRequest;
    }

    public static UpdateHearingRequest buildUpdateHearingRequest() {
        CreateHearingRequest createHearingRequest = HearingRequestGenerator.generateTestHearingRequest(CASE_REFERENCE);

        return UpdateHearingRequest.builder()
            .partyDetails(createHearingRequest.getPartyDetails())
            .caseDetails(createHearingRequest.getCaseDetails())
            .hearingDetails(createHearingRequest.getHearingDetails())
            .requestDetails(
                HearingRequestDetails.builder()
                    .hearingRequestId("hearingRequestId")
                    .hearingGroupRequestId("hearingGroupRequestId")
                    .status("status")
                    .partiesNotified(LocalDateTime.now())
                    .timestamp(LocalDateTime.now())
                    .versionNumber(1234L)
                    .build())
            .build();
    }

    public static List<HearingLinkData> generateHearingLinkData(String caseRef) {
        return List.of(
            HearingLinkData.hearingLinkDataWith()
                .caseName("Case name")
                .caseReference(caseRef)
                .reasonsForLink(List.of("Reason1", "Reason2"))
                .build()
        );
    }

    public static ServiceHearingValuesModel generateServiceHearingValues() {
        return ServiceHearingValuesModel.builder()
            .hmctsServiceId("hmctsServiceId")
            .hmctsInternalCaseName("internalCaseName")
            .publicCaseName("publicName")
            .caseAdditionalSecurityFlag(false)
            .caseCategories(Collections.emptyList())
            .caseDeepLink("caseDeepLink")
            .caserestrictedFlag(false)
            .externalCaseReference("externalCaseReference")
            .caseSlaStartDate(LocalDate.now().toString())
            .caseManagementLocationCode("caseManagementLocationCode")
            .autoListFlag(false)
            .hearingType("hearingType")
            .hearingWindow(null)
            .duration(60)
            .hearingPriorityType(PriorityType.STANDARD)
            .numberOfPhysicalAttendees(3)
            .hearingInWelshFlag(false)
            .hearingLocations(Collections.emptyList())
            .facilitiesRequired(Collections.emptyList())
            .listingComments("listingComments")
            .hearingRequester("hearingRequester")
            .privateHearingRequiredFlag(false)
            .caseInterpreterRequiredFlag(false)
            .panelRequirements(null)
            .leadJudgeContractType("leadJudgeContractType")
            .judiciary(JudiciaryModel.builder()
                           .roleType(List.of("roleType"))
                           .authorisationTypes(List.of("authorisationTypes"))
                           .authorisationSubType(List.of("authorisationSubType"))
                           .build())
            .hearingIsLinkedFlag(false)
            .parties(Collections.emptyList())
            .caseFlags(Caseflags.builder()
                           .flags(Collections.emptyList())
                           .flagAmendUrl("flagAmendUrl")
                           .build())
            .screenFlow(null)
            .vocabulary(Collections.emptyList())
            .hearingChannels(Collections.emptyList())
            .hearingLevelParticipantAttendance(Collections.emptyList())
            .build();
    }
}
