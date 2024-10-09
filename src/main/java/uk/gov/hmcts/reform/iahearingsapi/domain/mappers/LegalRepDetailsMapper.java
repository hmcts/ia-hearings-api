package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME_PAPER_J;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_GIVEN_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_MOBILE_PHONE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_EMAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_PHONE;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
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

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getLegalRepPartyId(asylumCase))
            .partyType(PartyType.IND.getPartyType())
            .partyRole("LGRP")
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName(caseDataMapper.getName(asylumCase, LEGAL_REP_NAME))
                    .lastName(caseDataMapper.getName(asylumCase, LEGAL_REP_FAMILY_NAME))
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase,
                                                                              persistedHearingDetails,
                                                                              event))
                    .hearingChannelEmail(
                        caseDataMapper.getHearingChannelEmail(asylumCase, LEGAL_REPRESENTATIVE_EMAIL_ADDRESS))
                    .hearingChannelPhone(
                        caseDataMapper.getHearingChannelPhone(asylumCase, LEGAL_REP_MOBILE_PHONE_NUMBER))
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

    public PartyDetailsModel mapInternalCaseLegalRep(AsylumCase asylumCase,
                                 CaseDataToServiceHearingValuesMapper caseDataMapper,
                                 HearingDetails persistedHearingDetails,
                                 Event event) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getLegalRepPartyId(asylumCase))
            .partyType(PartyType.IND.getPartyType())
            .partyRole("LGRP")
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName(caseDataMapper.getName(asylumCase, LEGAL_REP_GIVEN_NAME))
                    .lastName(caseDataMapper.getName(asylumCase, LEGAL_REP_FAMILY_NAME_PAPER_J))
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase,
                                                                              persistedHearingDetails,
                                                                              event))
                    .hearingChannelEmail(
                        caseDataMapper.getHearingChannelEmail(asylumCase, AsylumCaseFieldDefinition.LEGAL_REP_EMAIL))
                    .hearingChannelPhone(Collections.emptyList())
                    .build())
            .build();
    }
}
