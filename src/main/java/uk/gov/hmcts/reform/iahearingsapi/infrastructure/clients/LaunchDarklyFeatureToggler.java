package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.UserInfo;

@Service
public class LaunchDarklyFeatureToggler implements FeatureToggler {

    private final LDClientInterface ldClient;
    private final UserDetails userDetails;
    private final IdamService idamService;

    public LaunchDarklyFeatureToggler(LDClientInterface ldClient,
                                      UserDetails userDetails,
                                      IdamService idamService) {
        this.ldClient = ldClient;
        this.userDetails = userDetails;
        this.idamService = idamService;
    }

    public boolean getValue(String key, Boolean defaultValue) {

        LDContext context = LDContext.builder(userDetails.getId())
            .set("firstName", userDetails.getForename())
            .set("lastName", userDetails.getSurname())
            .set("email", userDetails.getEmailAddress())
            .build();

        return ldClient.boolVariation(
            key,
            context,
            defaultValue
        );
    }

    public boolean getValueAsServiceUser(String key, Boolean defaultValue) {
        String token = idamService.getServiceUserToken();
        UserInfo serviceUser = idamService.getUserInfo(token);
        LDContext context = LDContext.builder(serviceUser.getUid())
            .set("firstName", serviceUser.getGivenName())
            .set("lastName", serviceUser.getFamilyName())
            .set("email", serviceUser.getEmail())
            .build();

        return ldClient.boolVariation(
            key,
            context,
            defaultValue
        );
    }

    public LDValue getJsonValue(String key, LDValue defaultValue) {

        LDContext context = LDContext.builder(userDetails.getId())
            .set("firstName", userDetails.getForename())
            .set("lastName", userDetails.getSurname())
            .set("email", userDetails.getEmailAddress())
            .build();

        return ldClient.jsonValueVariation(
            key,
            context,
            defaultValue
        );
    }

}
