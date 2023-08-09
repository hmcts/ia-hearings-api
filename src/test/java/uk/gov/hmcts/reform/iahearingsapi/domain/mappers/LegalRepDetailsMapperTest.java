package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class LegalRepDetailsMapperTest {

    @Test
    void should_map_correctly() {

        PartyDetailsModel expected = PartyDetailsModel.builder().build();

        assertEquals(expected, new LegalRepDetailsMapper().map());
    }
}
