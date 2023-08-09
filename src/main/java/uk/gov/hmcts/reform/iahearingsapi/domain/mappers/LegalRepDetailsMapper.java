package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@Component
public class LegalRepDetailsMapper {

    public PartyDetailsModel map() {

        return PartyDetailsModel.builder()
            .build();
    }
}
