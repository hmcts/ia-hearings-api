package uk.gov.hmcts.reform.iahearingsapi.consumer.hmc;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import io.pactfoundation.consumer.dsl.LambdaDsl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UnNotifiedHearingsResponse;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@TestPropertySource(properties = {"hmc.baseUrl=localhost:4561"})
@ContextConfiguration(classes = { HmcHearingApiConsumerApplication.class })
@PactTestFor(providerName = "hmc_cft_hearings_api", port = "4561")
public class HmcHearingApiGetConsumerTest extends HmcHearingApiConsumerTestBase {

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

    @Pact(provider = "hmc_cft_hearings_api", consumer = "ia_hearingsApi")
    public RequestResponsePact generatePactFragmentForGetUnNotifiedHearings(
        PactDslWithProvider builder) {
        // @formatter:off
        return builder.given("Hearings exist")
            .uponReceiving("A Request to get unnotified hearings")
            .method("GET")
            .headers(
                SERVICE_AUTHORIZATION_HEADER,
                SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER,
                AUTHORIZATION_TOKEN)
            .path("/unNotifiedHearings/BFA1")
            .query("hearing_start_date_from=2024-09-20 00:00:00"
                   + "&hearing_start_date_to=2024-10-20 00:00:00&hearingStatus=LISTED&hearingStatus=CANCELLED")
            .willRespondWith()
            .body(buildGetUnNotifiedHearingsResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetUnNotifiedHearings")
    public void verifyGetUnNotifiedHearings() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime fromDate = LocalDateTime.parse("2024-09-20 00:00:00", formatter);
        LocalDateTime toDate = LocalDateTime.parse("2024-10-20 00:00:00", formatter);
        UnNotifiedHearingsResponse unNotifiedHearingsResponse =
            hmcHearingApi.getUnNotifiedHearings(
                AUTHORIZATION_TOKEN,
                SERVICE_AUTH_TOKEN,
                fromDate,
                toDate,
                List.of("LISTED", "CANCELLED"),
                "BFA1");

        System.out.println(unNotifiedHearingsResponse);
        assertNotNull(unNotifiedHearingsResponse);
        assertEquals(3, unNotifiedHearingsResponse.getHearingIds().size());
    }

    protected DslPart buildGetUnNotifiedHearingsResponseDsl() {
        return newJsonBody((body) -> {
            body.numberType("totalFound", "3")
                .array("hearingIds", ids -> {
                    ids.stringValue("hearingId1");
                    ids.stringValue("hearingId2");
                    ids.stringValue("hearingId3");
                });
        }).build();
    }
}
