CREATE TABLE beacon_dataset_table (
    id character varying(50) NOT NULL PRIMARY KEY,
    description character varying(800),
    access_type character varying(10),
    reference_genome character varying(50),
    size integer
);

CREATE TABLE beacon_data_table
(
  id serial NOT NULL PRIMARY KEY,
  dataset_id character varying(50) NOT NULL REFERENCES beacon_dataset_table(id),
  chromosome character varying(2) NOT NULL,
  "position" integer NOT NULL,
  alternate character varying(100) NOT NULL,
  UNIQUE (dataset_id, chromosome, "position", alternate)
);

CREATE OR REPLACE VIEW beacon_dataset AS 
    SELECT bdat.id,
        bdat.description,
        bdat.access_type,
        bdat.reference_genome,
        bdat.size
    FROM beacon_dataset_table bdat
    WHERE (bdat.access_type::text = ANY (ARRAY['PUBLIC'::character varying::text, 'REGISTERED'::character varying::text, 'CONTROLLED'::character varying::text])) 
    AND bdat.size > 0 AND bdat.reference_genome::text <> ''::text;

CREATE OR REPLACE VIEW beacon_data AS 
    SELECT bd.dataset_id,
        bd.chromosome,
        bd."position",
        bd.alternate,
        ebdat.reference_genome
    FROM beacon_data_table bd
    INNER JOIN beacon_dataset ebdat ON bd.dataset_id::text = ebdat.id::text;

-----------------------------------
---------- CONSENT CODES ----------
-----------------------------------
CREATE TABLE consent_code_category_table (
    id serial PRIMARY KEY,
    name character varying(11)
);

INSERT INTO consent_code_category_table(name) VALUES ('PRIMARY');
INSERT INTO consent_code_category_table(name) VALUES ('SECONDARY');
INSERT INTO consent_code_category_table(name) VALUES ('REQUIREMENT');

CREATE TABLE consent_code_table (
    id serial PRIMARY KEY,
    name character varying(100) NOT NULL,
    abbr character varying(4) NOT NULL,
    description character varying(400) NOT NULL,
    category_id int NOT NULL REFERENCES consent_code_category_table(id)
);

INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('No restrictions', 'NRES', 'No restrictions on data use.', 1);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('General research use and clinical care', 'GRU', 'For health/medical/biomedical purposes, including the study of population origins or ancestry.', 1);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Health/medical/biomedical research and clinical care', 'HMB', 'Use of the data is limited to health/medical/biomedical purposes; does not include the study of population origins or ancestry.', 1);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Disease-specific research and clinical care', 'DS', 'Use of the data must be related to [disease].', 1);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Population origins/ancestry research', 'POA', 'Use of the data is limited to the study of population origins or ancestry.', 1);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Oher research-specific restrictions', 'RS', 'Use of the data is limited to studies of [research type] (e.g., pediatric research).', 2);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Research use only', 'RUO', 'Use of data is limited to research purposes (e.g., does not include its use in clinical care).', 2);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('No “general methods” research', 'NMDS', 'Use of the data includes methods development research (e.g., development of software or algorithms) ONLY within the bounds of other data use limitations.', 2);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Genetic studies only', 'GSO', 'Use of the data is limited to genetic studies only (i.e., no “phenotype-only” research).', 2);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Not-for-profit use only', 'NPU', 'Use of the data is limited to not-for-profit organizations.', 3);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Publication required', 'PUB', 'Requestor agrees to make results of studies using the data available to the larger scientific community.', 3);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Collaboration required', 'COL', 'Requestor must agree to collaboration with the primary study investigator(s).', 3);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Ethics approval required', 'IRB', 'Requestor must provide documentation of local IRB/REC approval.', 3);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Geographical restrictions', 'GS', 'Use of the data is limited to within [geographic region].', 3);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Publication moratorium/embargo', 'MOR', 'Requestor agrees not to publish results of studies until [date].', 3);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Time limits on use', 'TS', 'Use of data is approved for [x months].', 3);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('User-specific restrictions', 'US', 'Use of data is limited to use by approved users.', 3);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Project-specific restrictions', 'PS', 'Use of data is limited to use within an approved project.', 3);
INSERT INTO consent_code_table(name, abbr, description, category_id) VALUES ('Institution-specific restrictions', 'IS', 'Use of data is limited to use within an approved institution.', 3);

CREATE TABLE beacon_dataset_consent_code_table (
    dataset_id character varying(50) NOT NULL REFERENCES beacon_dataset_table(id),
    consent_code_id int NOT NULL REFERENCES consent_code_table(id),
    detail character varying(1000),
    PRIMARY KEY (dataset_id, consent_code_id)
);

-----------------------------------
-------------- ADA-M --------------
-----------------------------------

CREATE TABLE adam_value_table(
    id serial PRIMARY KEY,
    value character varying(13) NOT NULL
);

INSERT INTO adam_value_table(value) VALUES ('NOT SPECIFIED');
INSERT INTO adam_value_table(value) VALUES ('UNTRUE');
INSERT INTO adam_value_table(value) VALUES ('TRUE');

CREATE TABLE adam_table(
    id serial PRIMARY KEY,
    attribute character varying(50) NOT NULL CONSTRAINT adam_attribute_unique UNIQUE,
    description character varying(400)  
);

INSERT INTO adam_table(attribute, description) VALUES ('anyCountry','within any country/location');
INSERT INTO adam_table(attribute, description) VALUES ('allowedCountries','within specified countries/locations');
INSERT INTO adam_table(attribute, description) VALUES ('excludedCountries','within any country/location other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('anyOrganisation','by all organisations');
INSERT INTO adam_table(attribute, description) VALUES ('anyNonProfitOrganisation','by any non-profit organisations');
INSERT INTO adam_table(attribute, description) VALUES ('allowedNonProfitOrganisations','by specified non-profit organisations');
INSERT INTO adam_table(attribute, description) VALUES ('excludedNonProfitOrganisations','by any non-profit organisations other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('anyProfitOrganisation','by any profit organisations');
INSERT INTO adam_table(attribute, description) VALUES ('allowedProfitOrganisations','by specified profit organisations');
INSERT INTO adam_table(attribute, description) VALUES ('excludedProfitOrganisations','by any profit organisation other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('anyPerson','by any category of person');
INSERT INTO adam_table(attribute, description) VALUES ('anyAcademicProfessional','by any category of academic professional');
INSERT INTO adam_table(attribute, description) VALUES ('allowedAcademicProfessionals','by specified categories of academic professional');
INSERT INTO adam_table(attribute, description) VALUES ('excludedAcademicProfessionals','by any category of academic professional other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('anyClinicalProfessional','by any category of clinical professional');
INSERT INTO adam_table(attribute, description) VALUES ('allowedClinicalProfessionals','by specified categories of clinical professional');
INSERT INTO adam_table(attribute, description) VALUES ('excludedClinicalProfessionals','by any category of clinical professional other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('anyProfitmakingProfessional','by any category of profit-making professional');
INSERT INTO adam_table(attribute, description) VALUES ('allowedProfitmakingProfessionals','by specified categories of profit-making professional');
INSERT INTO adam_table(attribute, description) VALUES ('excludedProfitmakingProfessionals','by any category of profit-making professional other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('anyNonProfessional','by any category of non-professional');
INSERT INTO adam_table(attribute, description) VALUES ('allowedNonProfessionals','by specified categories of non-professional');
INSERT INTO adam_table(attribute, description) VALUES ('excludedNonProfessionals','by any category of non-professional other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('anyDomain','for any domain');
INSERT INTO adam_table(attribute, description) VALUES ('anyResearch','for any research purpose');
INSERT INTO adam_table(attribute, description) VALUES ('anyFundamentalBiologyResearch','for research w.r.t. fundamental biology');
INSERT INTO adam_table(attribute, description) VALUES ('anyMethodsDevelopmentResearch','for research w.r.t. methods development');
INSERT INTO adam_table(attribute, description) VALUES ('anyPopulationResearch','for research w.r.t. populations');
INSERT INTO adam_table(attribute, description) VALUES ('anyAncestryResearch','for research w.r.t. ancestry');
INSERT INTO adam_table(attribute, description) VALUES ('anyGeneticResearch','for research w.r.t. genetics');
INSERT INTO adam_table(attribute, description) VALUES ('anyDrugDevelopmentResearch','for research w.r.t. drug development');
INSERT INTO adam_table(attribute, description) VALUES ('anyDiseaseResearch','for research w.r.t. any disease');
INSERT INTO adam_table(attribute, description) VALUES ('allowedDiseasesResearch','for research w.r.t. any disease other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('excludedDiseasesResearch','for research w.r.t. specified diseases');
INSERT INTO adam_table(attribute, description) VALUES ('allowedAgeCategoriesResearch','for research w.r.t. specified age categories');
INSERT INTO adam_table(attribute, description) VALUES ('allowedGenderCategoriesResearch','for research w.r.t. specified gender categories');
INSERT INTO adam_table(attribute, description) VALUES ('allowedOtherResearch','for other specified categories of research');
INSERT INTO adam_table(attribute, description) VALUES ('anyClinicalCare','for any clinical care purpose');
INSERT INTO adam_table(attribute, description) VALUES ('anyDiseasesClinicalCare','for clinical care w.r.t.  any disease');
INSERT INTO adam_table(attribute, description) VALUES ('allowedDiseasesClinicalCare','for clinical care w.r.t.  any disease other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('excludedDiseasesClinicalCare','for clinical care w.r.t. specified diseases');
INSERT INTO adam_table(attribute, description) VALUES ('allowedOtherClinicalCare','for other specified categories of clinical care');
INSERT INTO adam_table(attribute, description) VALUES ('anyProfitPurpose','for any profit purpose');
INSERT INTO adam_table(attribute, description) VALUES ('allowedProfitPurposes','for specified profit purposes');
INSERT INTO adam_table(attribute, description) VALUES ('excludedProfitPurposes','for any profit purpose other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('anyNonProfitPurpose','for any non-profit purpose');
INSERT INTO adam_table(attribute, description) VALUES ('allowedNonProfitPurposes','for specified non-profit purposes');
INSERT INTO adam_table(attribute, description) VALUES ('excludedNonProfitPurposes','for any non-profit purpose other than those specified');
INSERT INTO adam_table(attribute, description) VALUES ('metaConditions','Meta-Conditions:');
INSERT INTO adam_table(attribute, description) VALUES ('noOtherConditions','There are no other restrictions/limitations in force which are not herein specified');
INSERT INTO adam_table(attribute, description) VALUES ('whichOtherConditions','Other permissions/limitations may apply as specified');
INSERT INTO adam_table(attribute, description) VALUES ('sensitivePopulations','No special evaluation required for access requests involving sensitive/restricted populations');
INSERT INTO adam_table(attribute, description) VALUES ('uniformConsent','Identical consent permissions have been provided by all subjects');
INSERT INTO adam_table(attribute, description) VALUES ('termsOfAgreement','Terms of agreement:');
INSERT INTO adam_table(attribute, description) VALUES ('noAuthorizationObligations','There are no requirements for any formal approval, contract or review conditions to be satisfied');
INSERT INTO adam_table(attribute, description) VALUES ('whichAuthorizationObligations','Formal approval, contract or review conditions are to be met, as specified');
INSERT INTO adam_table(attribute, description) VALUES ('noPublicationObligations','There are no requirements regarding publication or disclosure of derived results');
INSERT INTO adam_table(attribute, description) VALUES ('whichPublicationObligations','Publication or disclosure of derived results is subject to restrictions, as specified');
INSERT INTO adam_table(attribute, description) VALUES ('noTimelineObligations','There are no timeline restrictions');
INSERT INTO adam_table(attribute, description) VALUES ('whichTimelineObligations','The period of access has time limitations, as specified');
INSERT INTO adam_table(attribute, description) VALUES ('noSecurityObligations','There are no requirements regarding data security measures');
INSERT INTO adam_table(attribute, description) VALUES ('whichSecurityObligations','User must have adequate data security measures, as specified');
INSERT INTO adam_table(attribute, description) VALUES ('noExpungingObligations','There are no requirements regarding withdrawal, destruction or return of any subject data');
INSERT INTO adam_table(attribute, description) VALUES ('whichExpungingObligations','Some subject data must be withdrawn, destroyed or returned, as specified');
INSERT INTO adam_table(attribute, description) VALUES ('noLinkingObligations','There are no restrictions regarding the linking of accessed records to other datasets');
INSERT INTO adam_table(attribute, description) VALUES ('whichLinkingObligations','Accessed records may only be linked to other datasets, as specified');
INSERT INTO adam_table(attribute, description) VALUES ('noRecontactProvisions','There is no possibility of recontacting data subjects');
INSERT INTO adam_table(attribute, description) VALUES ('allowedRecontactProvisions','Subject recontact may occur in certain circumstances, as specified');
INSERT INTO adam_table(attribute, description) VALUES ('compulsoryRecontactProvisions','Subject recontact must occur in certain circumstances, as specified');
INSERT INTO adam_table(attribute, description) VALUES ('noIPClaimObligations','There are no restrictions regarding intellectual property claims based on use of the accessed resource');
INSERT INTO adam_table(attribute, description) VALUES ('whichIPClaimObligations','Options for intellectual property claims based on use of the accessed resources are limited, as specified');
INSERT INTO adam_table(attribute, description) VALUES ('noReportingObligations','There are no requirements to report back regarding use of the accessed resources');
INSERT INTO adam_table(attribute, description) VALUES ('whichReportingObligations','Reporting on use of the accessed resources may be required, as specified');
INSERT INTO adam_table(attribute, description) VALUES ('noPaymentObligations','No fees will be levied for access of the resources');
INSERT INTO adam_table(attribute, description) VALUES ('whichPaymentObligations','Fees may be levied for access of the resources, as specified');

CREATE TABLE beacon_dataset_adam_table(
    dataset_id character varying(50) NOT NULL,
    adam_id int NOT NULL REFERENCES adam_table(id),
    value_id int NOT NULL REFERENCES adam_value_table(id),
    PRIMARY KEY (dataset_id, adam_id)
);

CREATE TABLE beacon_dataset_adam_detailed_table(
    dataset_id character varying(50) NOT NULL,
    adam_id int NOT NULL REFERENCES adam_table(id),
    value character varying(200) NOT NULL,
    PRIMARY KEY (dataset_id, adam_id)
);

-----------------------------------
-------------- VIEWS --------------
-----------------------------------
CREATE OR REPLACE VIEW beacon_dataset_consent_code AS
SELECT dc.dataset_id,
    code.abbr AS code,
    code.description AS description,
    dc.detail,
    cat.name AS category
FROM beacon_dataset_consent_code_table dc
INNER JOIN consent_code_table code ON code.id=dc.consent_code_id
INNER JOIN consent_code_category_table cat ON cat.id=code.category_id
ORDER BY dc.dataset_id, cat.id, code.id
;

CREATE OR REPLACE VIEW beacon_dataset_adam AS
SELECT  subq.dataset_id,
    a.attribute,
    subq.value
FROM (
    SELECT da.dataset_id,
        da.adam_id,
        av.value
    FROM beacon_dataset_adam_table da
    INNER JOIN adam_value_table av ON av.id=da.value_id
    UNION
    SELECT detailed.dataset_id,
        detailed.adam_id,
        detailed.value
    FROM beacon_dataset_adam_detailed_table detailed
    ORDER BY dataset_id, adam_id
) subq
INNER JOIN adam_table a ON a.id=subq.adam_id
;
