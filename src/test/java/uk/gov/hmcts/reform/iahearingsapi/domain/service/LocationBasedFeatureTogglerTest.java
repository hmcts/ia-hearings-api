package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;

import com.launchdarkly.sdk.LDValue;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class LocationBasedFeatureTogglerTest {

    private static final String AUTO_HEARING_REQUEST_LOCATIONS_LIST = "auto-hearing-request-locations-list";
    private static final LDValue DEFAULT_VALUE = LDValue.parse("{\"epimsIds\":[]}");
    private static final LDValue EXAMPLE_VALUE = LDValue.parse("{\"epimsIds\":[111111,22222]}");
    private static final String ENABLED_LOCATION = "111111";
    private static final String DISABLED_LOCATION = "333333";
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseManagementLocation caseManagementLocation;
    @Mock
    private BaseLocation baseLocation;

    private LocationBasedFeatureToggler locationBasedFeatureToggler;

    @BeforeEach
    void setup() {
        locationBasedFeatureToggler = new LocationBasedFeatureToggler(featureToggler);

        when(featureToggler.getJsonValue(AUTO_HEARING_REQUEST_LOCATIONS_LIST, DEFAULT_VALUE)).thenReturn(EXAMPLE_VALUE);
        when(asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class))
            .thenReturn(Optional.of(caseManagementLocation));
        when(caseManagementLocation.getBaseLocation()).thenReturn(baseLocation);
    }

    @Test
    void isAutoHearingRequestEnabled_should_return_yes() {
        when(baseLocation.getId()).thenReturn(ENABLED_LOCATION);

        assertEquals(YES, locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase));

    }

    @Test
    void isAutoHearingRequestEnabled_should_return_no() {
        when(baseLocation.getId()).thenReturn(DISABLED_LOCATION);

        assertEquals(NO, locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase));

    }

}
