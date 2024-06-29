package uk.gov.hmcts.reform.iahearingsapi.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.infrastructure.ConfigValidatorAppListener.CLUSTER_NAME;

@ExtendWith(MockitoExtension.class)
class ConfigValidatorAppListenerTest {

    @Mock
    private Environment env;

    private ConfigValidatorAppListener configValidatorAppListener;
    private static final String PREVIEW_CLUSTER_NAME = "cft-preview-01-aks";
    private static final String SECRET = "secret";

    @BeforeEach
    public void setup() {
        configValidatorAppListener = new ConfigValidatorAppListener();
        configValidatorAppListener.setEnvironment(env);
    }

    @ParameterizedTest
    @MethodSource("provideSecretsAndClusterNames")
    void shouldValidateConfig(String secret, String clusterName, Class<Throwable> expectedException) {
        configValidatorAppListener.setIaConfigValidatorSecret(secret);
        when(env.getProperty(CLUSTER_NAME)).thenReturn(clusterName);

        if (expectedException != null) {
            assertThrows(expectedException, configValidatorAppListener::breakOnMissingIaConfigValidatorSecret);
        } else {
            configValidatorAppListener.breakOnMissingIaConfigValidatorSecret();
        }

        verify(env).getProperty(CLUSTER_NAME);
    }

    private static Stream<Arguments> provideSecretsAndClusterNames() {
        return Stream.of(
            Arguments.of(null, null, null),
            Arguments.of(null, PREVIEW_CLUSTER_NAME, IllegalArgumentException.class),
            Arguments.of("", null, null),
            Arguments.of("", PREVIEW_CLUSTER_NAME, IllegalArgumentException.class),
            Arguments.of(SECRET, null, null),
            Arguments.of(SECRET, PREVIEW_CLUSTER_NAME, null)
        );
    }
}
