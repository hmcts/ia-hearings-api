package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.idam.UserInfo;

import java.util.Map;

@Component
public class IdamApiFallback implements IdamApi {

    @Override
    public UserInfo userInfo(String userToken) {
        throw new RuntimeException("IDAM service: userInfo unavailable, call failed after retries");
    }

    @Override
    public Token token(Map<String, ?> form) {
        throw new RuntimeException("IDAM service: token unavailable, call failed after retries");
    }
}
