package uk.gov.hmcts.reform.iahearingsapi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static java.nio.charset.StandardCharsets.UTF_8;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.iahearingsapi.util.IdamAuthProvider;
import uk.gov.hmcts.reform.iahearingsapi.util.MapValueExpander;

@Slf4j
@SpringBootTest()
@ActiveProfiles("functional")
public class CcdCaseCreationTest {

    @Value("classpath:templates/start-appeal.json")
    protected Resource startAppeal;

    @Autowired
    protected IdamAuthProvider idamAuthProvider;

    @Autowired
    protected AuthTokenGenerator s2sAuthTokenGenerator;

    protected static RequestSpecification hearingsSpecification;
    protected static RequestSpecification hmcApiSpecification;

    private static long caseId;
    protected Map<String, Object> caseData;
    protected static String s2sToken;
    protected static String legalRepToken;
    protected String systemUserToken;
    protected static String caseOfficerToken;
    private String legalRepUserId;
    private String caseOfficerUserId;
    private String systemUserId;
    public String paymentReference;

    private static final String jurisdiction = "IA";
    private static final String caseType = "Asylum";
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

    protected void setup() {
        hearingsSpecification = new RequestSpecBuilder()
            .setBaseUri(targetInstance)
            .setRelaxedHTTPSValidation()
            .build();

        hmcApiSpecification = new RequestSpecBuilder()
            .setBaseUri(hmcInstance)
            .setRelaxedHTTPSValidation()
            .build();

        log.info("hmcInstance: " + hmcInstance);
        log.info("targetInstance: " + targetInstance);

        startAppeal();
        submitAppeal();
        listCase();
    }

    private void startAppeal() {
        s2sToken = s2sAuthTokenGenerator.generate();

        legalRepToken = idamAuthProvider.getLegalRepToken();
        caseOfficerToken = idamAuthProvider.getCaseOfficerToken();

        legalRepUserId = idamAuthProvider.getUserId(legalRepToken);
        caseOfficerUserId = idamAuthProvider.getUserId(caseOfficerToken);

        Map<String, Object> data = getStartAppealData();
        data.put("paAppealTypePaymentOption", "payNow");

        MapValueExpander.expandValues(data);

        String eventId = "startAppeal";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startForCaseworker(legalRepToken, s2sToken, legalRepUserId, jurisdiction, caseType, eventId);

        Event event = Event.builder().id(eventId).build();

        CaseDataContent content = CaseDataContent.builder()
            .caseReference(null)
            .data(data)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        CaseDetails caseDetails =
            coreCaseDataApi.submitForCaseworker(legalRepToken, s2sToken, legalRepUserId, jurisdiction, caseType, true, content);

        caseId = caseDetails.getId();
    }

    private void submitAppeal() {
        caseData = new HashMap<>();
        caseData.put("decisionHearingFeeOption", "decisionWithHearing");
        caseData.put("hmctsCaseNameInternal", "testCase");

        MapValueExpander.expandValues(caseData);

        String eventId = "submitAppeal";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startEventForCaseWorker(legalRepToken, s2sToken, legalRepUserId, jurisdiction,
                              caseType, String.valueOf(caseId), eventId);

        Event event = Event.builder().id(eventId).build();
        CaseDataContent content = CaseDataContent.builder()
            .caseReference(String.valueOf(caseId))
            .data(caseData)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        coreCaseDataApi.createEvent(legalRepToken, s2sToken, String.valueOf(caseId), content);
    }

    private void listCase() {
        caseData.put("listCaseHearingLength", "120");

        String eventId = "listCaseForFTOnly";
        StartEventResponse startEventDetails =
            coreCaseDataApi.startEvent(caseOfficerToken, s2sToken, String.valueOf(caseId), eventId);

        Event event = Event.builder().id(eventId).build();
        CaseDataContent content = CaseDataContent.builder()
            .caseReference(String.valueOf(caseId))
            .data(caseData)
            .event(event)
            .eventToken(startEventDetails.getToken())
            .ignoreWarning(true)
            .build();

        coreCaseDataApi.createEvent(caseOfficerToken, s2sToken, String.valueOf(caseId), content);
    }

    private Map<String, Object> getStartAppealData() {

        Map<String, Object> data = Collections.emptyMap();

        try {
            data = new ObjectMapper()
                .readValue(asString(startAppeal), new TypeReference<Map<String, Object>>(){});
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

    public String getCaseId() {
        return Long.toString(caseId);
    }
}
