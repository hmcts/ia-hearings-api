package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_PHONE;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import java.util.Collections;

@Component
public class LegalRepDetailsMapper {

    public PartyDetailsModel map(AsylumCase asylumCase,
                                 CaseDataToServiceHearingValuesMapper caseDataMapper,
                                 HearingDetails persistedHearingDetails,
                                 Event event) {
        String givenNames = caseDataMapper.getName(asylumCase, LEGAL_REP_NAME);
        String familyName = caseDataMapper.getName(asylumCase, LEGAL_REP_FAMILY_NAME);
        if (familyName == null && givenNames != null) {
            String[] parts = givenNames.split(" ");
            if (parts.length > 1) {
                familyName = parts[parts.length - 1];
                givenNames = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1));
            } else if (parts.length == 1) {
                givenNames = parts[0];
            }
        }

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getLegalRepPartyId(asylumCase))
            .partyType(PartyType.IND.getPartyType())
            .partyRole("LGRP")
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName(givenNames)
                    .lastName(familyName)
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase,
                                                                              persistedHearingDetails,
                                                                              event))
                    .hearingChannelEmail(
                        caseDataMapper.getHearingChannelEmail(asylumCase, LEGAL_REPRESENTATIVE_EMAIL_ADDRESS))
                    .hearingChannelPhone(Collections.emptyList())
                    .build())
            .build();
    }

    public PartyDetailsModel map(BailCase bailCase, BailCaseDataToServiceHearingValuesMapper caseDataMapper) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getLegalRepPartyId(bailCase))
            .partyType(PartyType.IND.getPartyType())
            .partyRole("LGRP")
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName(caseDataMapper.getStringValueByDefinition(bailCase,
                                                                         BailCaseFieldDefinition.LEGAL_REP_NAME))
                    .lastName(caseDataMapper.getStringValueByDefinition(bailCase,
                                                                        BailCaseFieldDefinition.LEGAL_REP_FAMILY_NAME))
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(bailCase))
                    .hearingChannelEmail(
                        caseDataMapper.getHearingChannelEmailPhone(bailCase, LEGAL_REP_EMAIL))
                    .hearingChannelPhone(
                        caseDataMapper.getHearingChannelEmailPhone(bailCase, LEGAL_REP_PHONE))
                    .build())
            .build();
    }
}
