package uk.gov.hmcts.reform.iahearingsapi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static java.lang.Long.parseLong;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.HmcHearingApi;
import uk.gov.hmcts.reform.iahearingsapi.util.IdamAuthProvider;
import uk.gov.hmcts.reform.iahearingsapi.util.MapValueExpander;

@Slf4j
@SpringBootTest()
@ActiveProfiles("functional")
public class CcdCaseCreationTest {

    @Value("classpath:templates/start-appeal-aip.json")
    protected Resource startAipAppeal;

    @Value("classpath:templates/start-appeal-legalrep.json")
    protected Resource startLegalRepAppeal;

    @Value("classpath:templates/start-bail-legalrep.json")
    protected Resource startBailApplication;

    @Autowired
    protected IdamAuthProvider idamAuthProvider;

    @Autowired
    protected AuthTokenGenerator s2sAuthTokenGenerator;

    @Autowired
    private MapValueExpander mapValueExpander;

    @Autowired
    private HmcHearingApi hmcHearingApi;

    private static long legalRepCaseId;
    private static long aipCaseId;
    private static long caseId;
    private static long bailCaseId;
    private static Map<String, JsonNode> legalRepAppealCaseData;
    private static Map<String, JsonNode> aipAppealCaseData;
    private static Map<String, JsonNode> bailCaseData;
    protected static RequestSpecification hearingsSpecification;
    protected Map<String, Object> caseData;
    protected static String s2sToken;
    protected static String legalRepToken;
    private String legalRepUserId;
    protected String systemUserToken;
    protected static String citizenToken;
    protected String citizenUserId;
    protected static String caseOfficerToken;
    protected static String bailsLegalRepToken;
    private String bailsLegalRepUserId;

    private static final String jurisdiction = "IA";
    private static final String ASYLUM_CASE_TYPE = "Asylum";
    private static final String BAIL_CASE_TYPE = "Bail";
    protected static final String AUTHORIZATION = "Authorization";
    protected static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Value("${hmc.baseUrl}")
    protected String hmcInstance;
    protected final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("HEARINGS_API_URL"),
            "http://localhost:8100"
        );

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    protected void setupForLegalRep() {
        startAppealAsLegalRep();
        submitAppealAsLegalRep();
    }

    protected void setupForAip() {
        startAppealAsCitizen();
        submitAppealAsCitizen();
    }

    protected void setupForBail() {
        startBailApplication();
        submitBailApplication();
    }

    protected void fetchTokensAndUserIds() {
        s2sToken = s2sAuthTokenGenerator.generate();

        legalRepToken = idamAuthProvider.getLegalRepToken();
        bailsLegalRepToken = idamAuthProvider.getBailsLegalRepToken();
        citizenToken = idamAuthProvider.getCitizenToken();
        caseOfficerToken = idamAuthProvider.getCaseOfficerToken();

        citizenUserId = idamAuthProvider.getUserId(citizenToken);
        legalRepUserId = idamAuthProvider.getUserId(legalRepToken);
        bailsLegalRepUserId = idamAuthProvider.getUserId(bailsLegalRepToken);

        hearingsSpecification = new RequestSpecBuilder()
            .setBaseUri(targetInstance)
            .setRelaxedHTTPSValidation()
            .build();

        log.info("targetInstance: " + targetInstance);
    }

    protected HearingsGetResponse getHearingForCase(String caseReference) {
        return hmcHearingApi.getHearingsRequest(
            systemUserToken,
            s2sToken,
            null,
                null,
                null,
                caseReference
        );
    }

    private void startBailApplication() {
        Map<String, Object> data = getStartAppealData(startBailApplication);

        mapValueExpander.expandValues(data);

        String eventId = "startApplication";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startForCaseworker(
                bailsLegalRepToken,
                s2sToken,
                bailsLegalRepUserId,
                jurisdiction, BAIL_CASE_TYPE, eventId);

        Event event = Event.builder().id(eventId).build();

        CaseDataContent content = CaseDataContent.builder()
            .caseReference(null)
            .data(data)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseDetails caseDetails =
            coreCaseDataApi.submitForCaseworker(
                bailsLegalRepToken,
                s2sToken,
                bailsLegalRepUserId,
                jurisdiction, BAIL_CASE_TYPE, true, content);

        bailCaseId = caseDetails.getId();
    }

    private void submitBailApplication() {
        caseData = new HashMap<>();
        mapValueExpander.expandValues(caseData);

        String eventId = "submitApplication";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startEventForCaseWorker(
                bailsLegalRepToken,
                s2sToken,
                bailsLegalRepUserId,
                jurisdiction,
                BAIL_CASE_TYPE,
                String.valueOf(bailCaseId),
                eventId);

        Event event = Event.builder().id(eventId).build();
        CaseDataContent content = CaseDataContent.builder()
            .caseReference(String.valueOf(bailCaseId))
            .data(caseData)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseResource caseResource = coreCaseDataApi.createEvent(
            bailsLegalRepToken,
            s2sToken,
            String.valueOf(bailCaseId),
            content);

        bailCaseData = caseResource.getData();
    }

    private void startAppealAsLegalRep() {
        Map<String, Object> data = getStartAppealData(startLegalRepAppeal);
        data.put("paAppealTypePaymentOption", "payNow");

        mapValueExpander.expandValues(data);

        String eventId = "startAppeal";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startForCaseworker(
                legalRepToken,
                s2sToken,
                legalRepUserId,
                jurisdiction, ASYLUM_CASE_TYPE, eventId);

        Event event = Event.builder().id(eventId).build();

        CaseDataContent content = CaseDataContent.builder()
            .caseReference(null)
            .data(data)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseDetails caseDetails =
            coreCaseDataApi.submitForCaseworker(
                legalRepToken,
                s2sToken,
                legalRepUserId,
                jurisdiction, ASYLUM_CASE_TYPE, true, content);

        legalRepCaseId = caseDetails.getId();

    }

    private void submitAppealAsLegalRep() {
        caseData = new HashMap<>();
        caseData.put("decisionHearingFeeOption", "decisionWithHearing");

        mapValueExpander.expandValues(caseData);

        String eventId = "submitAppeal";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startEventForCaseWorker(
                legalRepToken,
                s2sToken,
                legalRepUserId,
                jurisdiction,
                ASYLUM_CASE_TYPE,
                String.valueOf(legalRepCaseId),
                eventId);

        Event event = Event.builder().id(eventId).build();
        CaseDataContent content = CaseDataContent.builder()
            .caseReference(String.valueOf(legalRepCaseId))
            .data(caseData)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseResource caseResource = coreCaseDataApi.createEvent(
            legalRepToken,
            s2sToken,
            String.valueOf(legalRepCaseId),
            content);

        legalRepAppealCaseData = caseResource.getData();
    }

    private void startAppealAsCitizen() {
        Map<String, Object> data = getStartAppealData(startAipAppeal);

        mapValueExpander.expandValues(data);

        String eventId = "startAppeal";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startForCitizen(
                citizenToken,
                s2sToken,
                citizenUserId,
                jurisdiction, ASYLUM_CASE_TYPE, eventId);

        Event event = Event.builder().id(eventId).build();

        CaseDataContent content = CaseDataContent.builder()
            .caseReference(null)
            .data(data)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseDetails caseDetails =
            coreCaseDataApi.submitForCitizen(
                citizenToken,
                s2sToken,
                citizenUserId,
                jurisdiction, ASYLUM_CASE_TYPE, true, content);

        aipCaseId = caseDetails.getId();

    }

    private void submitAppealAsCitizen() {
        caseData = new HashMap<>();
        caseData.put("decisionHearingFeeOption", "decisionWithHearing");
        caseData.put("appellantPhoneNumber", "07444445555");
        caseData.put("sponsorMobileNumber", "07444445555");

        String eventId = "submitAppeal";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startEventForCitizen(
                citizenToken,
                s2sToken,
                citizenUserId,
                jurisdiction,
                ASYLUM_CASE_TYPE,
                String.valueOf(aipCaseId),
                eventId);

        Event event = Event.builder().id(eventId).build();
        CaseDataContent content = CaseDataContent.builder()
            .caseReference(String.valueOf(aipCaseId))
            .data(caseData)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseResource caseResource = coreCaseDataApi.createEvent(
            citizenToken,
            s2sToken,
            String.valueOf(aipCaseId),
            content);

        aipAppealCaseData = caseResource.getData();
    }


    /**
        Submitting event for assigning values to mandatory fields which requires system/officer permission.
    */
    protected void listCaseWithRequiredFields() {
        systemUserToken = idamAuthProvider.getSystemUserToken();

        caseData.put("listingLength", "120");
        caseData.put("appealType", "protection");
        caseData.put("hearingChannel", "INTER");
        caseData.put("hmctsCaseNameInternal", "Talha Awan");

        String eventId = "listCaseForFTOnly";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startEvent(systemUserToken, s2sToken, String.valueOf(caseId), eventId);

        Event event = Event.builder().id(eventId).build();
        CaseDataContent content = CaseDataContent.builder()
            .caseReference(String.valueOf(caseId))
            .data(caseData)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        coreCaseDataApi.createEvent(systemUserToken, s2sToken, String.valueOf(caseId), content);
    }

    private Map<String, Object> getStartAppealData(Resource appealJson) {

        Map<String, Object> data = Collections.emptyMap();

        try {
            data = new ObjectMapper()
                .readValue(asString(appealJson), new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return data;
    }

    private String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected String getLegalRepCaseId() {
        return Long.toString(legalRepCaseId);
    }

    protected String getAipCaseId() {
        return Long.toString(aipCaseId);
    }

    private AsylumCase getLegalRepCase() {
        AsylumCase asylumCase = new AsylumCase();

        asylumCase.putAll(legalRepAppealCaseData);

        return asylumCase;
    }

    private AsylumCase getAipCase() {
        AsylumCase asylumCase = new AsylumCase();

        asylumCase.putAll(aipAppealCaseData);

        return asylumCase;
    }

    private BailCase getBailCase() {
        BailCase bailCase = new BailCase();

        bailCase.putAll(bailCaseData);

        return bailCase;
    }

    protected record  Case(Long caseId, CaseData caseData) {
        protected Long getCaseId() {
            return caseId;
        }

        protected CaseData getCaseData() {
            return caseData;
        }
    }

    @NotNull
    protected Case createAndGetCase(boolean isAipJourney) {
        AsylumCase caseData;
        if (isAipJourney) {
            setupForAip();
            caseData = getAipCase();
            caseId = parseLong(getAipCaseId());
        } else {
            setupForLegalRep();
            caseData = getLegalRepCase();
            caseId = parseLong(getLegalRepCaseId());
        }

        caseData.put("hmctsCaseNameInternal", "Talha Awan");

        return new Case(caseId, caseData);
    }

    @NotNull
    protected Case createAndGetBailCase() {
        setupForBail();
        return new Case(bailCaseId, getBailCase());
    }
}
