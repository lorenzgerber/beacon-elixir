//package org.ega_archive.elixirbeacon.dto.datause;
//
//import org.ega_archive.elixirbeacon.enums.AdamValue;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonInclude.Include;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//import lombok.experimental.Builder; 
//
//@JsonInclude(Include.NON_DEFAULT) 
//@Data 
//@Builder 
//@NoArgsConstructor 
//@AllArgsConstructor 
//@EqualsAndHashCode(callSuper = false)
//public class Adam extends DataUse { 
//   
//  private AdamValue anyCountry = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyOrganisation = AdamValue.NOT_SPECIFIED; 
// 
//  private AdamValue anyNonProfitOrganisation = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyProfitOrganisation = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyPerson = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyAcademicProfessional = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyClinicalProfessional = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyProfitmakingProfessional = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyNonProfessional = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyDomain = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyResearch = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyFundamentalBiologyResearch = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyMethodsDevelopmentResearch = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyPopulationResearch = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyAncestryResearch = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyGeneticResearch = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyDrugDevelopmentResearch = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyDiseaseResearch = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyClinicalCare = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyDiseasesClinicalCare = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyProfitPurpose = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue anyNonProfitPurpose = AdamValue.NOT_SPECIFIED; 
//   
//  /* 
//   * Meta-Conditions 
//   */ 
//   
//  private AdamValue metaConditions = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noOtherConditions = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue sensitivePopulations = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue uniformConsent = AdamValue.NOT_SPECIFIED; 
//   
//  /* 
//   * Terms of agreement 
//   */ 
//   
//  private AdamValue termsOfAgreement = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noAuthorizationObligations = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noPublicationObligations = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noTimelineObligations = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noSecurityObligations = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noExpungingObligations = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noLinkingObligations = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noRecontactProvisions = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noIPClaimObligations = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noReportingObligations = AdamValue.NOT_SPECIFIED; 
//   
//  private AdamValue noPaymentObligations = AdamValue.NOT_SPECIFIED; 
//   
//} 
