package uk.gov.hmcts.reform.iahearingsapi.consumer;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.pactfoundation.consumer.dsl.LambdaDsl;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;

@PactTestFor(providerName = "hmc_cft_hearings_api", port = "4561")
public class HmcHearingApiConsumerTest extends HmcHearingApiConsumerTestBase {

    @Pact(provider = "hmc_cft_hearings_api", consumer = "ia_hearingsApi")
    public RequestResponsePact generatePactFragmentForGetHearingRequest(
        PactDslWithProvider builder) {
        // @formatter:off
        return builder.given("Hearings exist")
            .uponReceiving("A Request to get hearing response")
            .method("GET")
            .headers(
                SERVICE_AUTHORIZATION_HEADER,
                SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER,
                AUTHORIZATION_TOKEN)
            .path("/hearing/2000000056")
            .willRespondWith()
            .body(buildHearingGetResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetHearingRequest")
    public void verifyGetHearingRequest() {
        HearingGetResponse hearingGetResponse =
            hmcHearingApi.getHearingRequest(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, "2000000056", null);

        assertNotNull(hearingGetResponse);
    }

    protected DslPart buildHearingGetResponseDsl() {
        return LambdaDsl.newJsonBody((body) -> {
            body
                .object("requestDetails", (requestDetails) -> {
                    requestDetails.stringType("status", "HEARING_REQUESTED");
                    requestDetails.stringType("timestamp", "2023-09-20T10:09:19.93734");
                    requestDetails.numberType("versionNumber", 1);
                    requestDetails.stringType("hearingRequestID", "2000000056");
                })
                .object("hearingDetails", (hearingDetails) -> {
                    hearingDetails.stringType("listingAutoChangeReasonCode", "user-added-comments");
                    hearingDetails.stringType("hearingType", "BFA1-SUB");
                    hearingDetails.object("hearingWindow", (hearingWindow) -> {
                        hearingWindow.stringType("dateRangeStart", "2023-09-20");
                        hearingWindow.stringType("dateRangeEnd", "2023-10-04");
                    });
                })
                .numberType("duration", 150)
                .stringType("hearingPriorityType", "Standard")
                .numberType("numberOfPhysicalAttendees", 0)
                .booleanType("hearingInWelshFlag", false)
                .array("hearingLocations", (hearingLocations) -> {
                    hearingLocations.object(hearingLocation -> {
                        hearingLocation.stringType("locationType", "court");
                        hearingLocation.stringType("locationId", "386417");
                    });
                })
                .array("facilitiesRequired", (facilitiesRequired) -> {
                    facilitiesRequired.numberType(5);
                })
                .stringType("listingComments", "some adjustments")
                .booleanType("privateHearingRequiredFlag", true)
                .object("panelRequirements", (panelRequirements) -> {
                    panelRequirements.array("roleType", roleType -> roleType.stringType("84"));
                    panelRequirements.array("authorisationTypes", authorisationTypes ->
                        authorisationTypes.stringType(""));
                    panelRequirements.array("authorisationSubType", authorisationSubType ->
                        authorisationSubType.stringType(""));
                    panelRequirements.array("panelPreferences", panelPreferences ->
                        panelPreferences.stringType(""));
                    panelRequirements.array("panelSpecialisms", panelSpecialisms ->
                        panelSpecialisms.stringType(""));
                })
                .booleanType("hearingIsLinkedFlag", false)
                .array("hearingChannels", hearingChannels -> hearingChannels.stringType("INTER"))
                .booleanType("autolistFlag", false)
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
                })
                .array("caseCategories", (caseCategories) -> {
                    caseCategories.object(caseCategory -> {
                        caseCategory.stringType("categoryType", "caseType");
                        caseCategory.stringType("categoryValue", "BFA1-TST");
                        caseCategory.stringType("categoryParent", "");
                    });
                })
                .stringType("caseManagementLocationCode", "227101")
                .booleanType("caserestrictedFlag", false)
                .stringType("caseSLAStartDate", "2023-09-20")
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
                });
        }).build();
    }

    @Pact(provider = "hmc_cft_hearings_api", consumer = "ia_hearingsApi")
    public RequestResponsePact generatePactFragmentForGetPartiesNotified(
        PactDslWithProvider builder) {
        // @formatter:off
        return builder.given("Hearings exist")
            .uponReceiving("A Request to get parties notified")
            .method("GET")
            .headers(
                SERVICE_AUTHORIZATION_HEADER,
                SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER,
                AUTHORIZATION_TOKEN)
            .path("/partiesNotified/2000000056")
            .willRespondWith()
            .body(buildGetPartiesNotifiedRequestDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetPartiesNotified")
    public void verifyGetPartiesNotified() {
        PartiesNotifiedResponses hearingGetResponse =
            hmcHearingApi.getPartiesNotifiedRequest(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, "2000000056");

        assertNotNull(hearingGetResponse);
    }

    protected DslPart buildGetPartiesNotifiedRequestDsl() {
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
}
