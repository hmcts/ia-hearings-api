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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.StartEventDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.SubmitEventDetails;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.iahearingsapi.util.IdamAuthProvider;
import uk.gov.hmcts.reform.iahearingsapi.util.MapValueExpander;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDataContent;

@SpringBootTest()
@ActiveProfiles("functional")
public class CcdCaseCreationTest {

    @Value("classpath:templates/start-appeal.json")
    protected Resource startAppeal;

    @Autowired
    protected IdamAuthProvider idamAuthProvider;

    @Autowired
    protected AuthTokenGenerator s2sAuthTokenGenerator;

    protected RequestSpecification hearingsSpecification;
    protected RequestSpecification hmcApiSpecification;

    private long caseId;
    protected Map<String, Object> caseData;
    protected String s2sToken;
    protected String legalRepToken;
    protected String systemUserToken;
    protected String caseOfficerToken;
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
            System.getenv("TEST_URL"),
            "http://localhost:8100"
        );


    @Autowired
    private CcdDataApi ccdApi;
    protected boolean setupHasStarted;
    protected boolean setupIsDone;

    protected void setup() {
        if (setupHasStarted || setupIsDone) {
            return;
        }

        setupHasStarted = true;

        hearingsSpecification = new RequestSpecBuilder()
            .setBaseUri(targetInstance)
            .setRelaxedHTTPSValidation()
            .build();

        hmcApiSpecification = new RequestSpecBuilder()
            .setBaseUri(hmcInstance)
            .setRelaxedHTTPSValidation()
            .build();

        startAppeal();
        submitAppeal();
        listCase();

        setupIsDone = true;
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
        StartEventDetails startEventDetails =
            ccdApi.startCaseCreation(legalRepToken, s2sToken, legalRepUserId, jurisdiction, caseType, eventId);

        Map<String, Object> event = new HashMap<>();
        event.put("id", eventId);
        CaseDataContent content =
            new CaseDataContent(null, data, event, startEventDetails.getToken(), true);

        CaseDetails<AsylumCase> caseDetails =
            ccdApi.submitCaseCreation(legalRepToken, s2sToken, legalRepUserId, jurisdiction, caseType, content);

        caseId = caseDetails.getId();
    }

    private void submitAppeal() {
        caseData = new HashMap<>();
        caseData.put("decisionHearingFeeOption", "decisionWithHearing");
        caseData.put("hmctsCaseNameInternal", "testCase");

        MapValueExpander.expandValues(caseData);

        String eventId = "submitAppeal";
        StartEventDetails startEventDetails =
            ccdApi.startEvent(legalRepToken, s2sToken, legalRepUserId, jurisdiction,
                              caseType, String.valueOf(caseId), eventId);

        Map<String, Object> event = new HashMap<>();
        event.put("id", eventId);
        CaseDataContent content =
            new CaseDataContent(String.valueOf(caseId), caseData, event, startEventDetails.getToken(), true);

        SubmitEventDetails submitEventDetails =
            ccdApi.submitEvent(legalRepToken, s2sToken, String.valueOf(caseId), content);

        paymentReference = submitEventDetails.getData().get("paymentReference").toString();
    }

    private void listCase() {
        caseData.put("listCaseHearingLength", "120");

        String eventId = "listCaseForFTOnly";
        StartEventDetails startEventDetails =
            ccdApi.startEvent(caseOfficerToken, s2sToken, caseOfficerUserId, jurisdiction,
                caseType, String.valueOf(caseId), eventId);

        Map<String, Object> event = new HashMap<>();
        event.put("id", eventId);
        CaseDataContent content =
            new CaseDataContent(String.valueOf(caseId), caseData, event, startEventDetails.getToken(), true);

        ccdApi.submitEvent(caseOfficerToken, s2sToken, String.valueOf(caseId), content);
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
