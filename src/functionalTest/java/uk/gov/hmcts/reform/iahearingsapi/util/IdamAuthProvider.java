package uk.gov.hmcts.reform.iahearingsapi.util;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
public class IdamAuthProvider {

    @Value("${idam.redirectUrl}")
    protected String idamRedirectUri;

    protected String userScope = "openid profile roles";

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    protected String idamClientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    protected String idamClientSecret;

    @Autowired private IdamApi idamApi;

    public String getUserToken(String username, String password) {

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("redirect_uri", idamRedirectUri);
        map.add("client_id", idamClientId);
        map.add("client_secret", idamClientSecret);
        map.add("username", username);
        map.add("password", password);
        map.add("scope", userScope);
        try {
            Token tokenResponse = idamApi.token(map);
            return "Bearer " + tokenResponse.getAccessToken();
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException("Could not get user token from IDAM", ex);
        }
    }

    @Cacheable(value = "systemUserTokenCache")
    public String getSystemUserToken() {
        return getUserToken(
            System.getenv("IA_SYSTEM_USERNAME"),
            System.getenv("IA_SYSTEM_PASSWORD")
        );
    }

    @Cacheable(value = "legalRepATokenCache")
    public String getLegalRepToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_USERNAME"),
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_PASSWORD")
        );
    }

    @Cacheable(value = "bailsLegalRepTokenCache")
    public String getBailsLegalRepToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_BAILS_USERNAME"),
            System.getenv("TEST_LAW_FIRM_BAILS_PASSWORD")
        );
    }

    @Cacheable(value = "caseOfficerTokenCache")
    public String getCaseOfficerToken() {
        return getUserToken(
            System.getenv("TEST_CASEOFFICER_USERNAME"),
            System.getenv("TEST_CASEOFFICER_PASSWORD")
        );
    }

    @Cacheable(value = "citizenTokenCache")
    public String getCitizenToken() {
        return getUserToken(
            System.getenv("TEST_CITIZEN_USERNAME"),
            System.getenv("TEST_CITIZEN_PASSWORD")
        );
    }

    public String getUserId(String token) {
        try {
            UserInfo userInfo = idamApi.userInfo(token);
            return userInfo.getUid();
        } catch (FeignException ex) {
            throw new IdentityManagerResponseException("Could not get user token from IDAM", ex);
        }
    }
}
