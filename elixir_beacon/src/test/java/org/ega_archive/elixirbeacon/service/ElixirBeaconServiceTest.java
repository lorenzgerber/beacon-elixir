package org.ega_archive.elixirbeacon.service;

import static org.hamcrest.CoreMatchers.startsWith;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.ega_archive.elixirbeacon.Application;
import org.ega_archive.elixirbeacon.constant.BeaconConstants;
import org.ega_archive.elixirbeacon.dto.Beacon;
import org.ega_archive.elixirbeacon.dto.BeaconAlleleResponse;
import org.ega_archive.elixirbeacon.enums.ErrorCode;
import org.ega_archive.elixirbeacon.enums.FilterDatasetResponse;
import org.ega_archive.elixirbeacon.enums.VariantType;
import org.ega_archive.elixircore.helper.CommonQuery;
import org.ega_archive.elixircore.test.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@WebAppConfiguration
@SpringBootTest("server.port:0")
public class ElixirBeaconServiceTest {

  private static final CommonQuery COMMON_QUERY = CommonQuery.builder().skip(0).limit(0).build();

  @Autowired
  private ElixirBeaconService elixirBeaconService;

  @Resource(name = "elixirbeaconDataSource")
  private DataSource dataSource;

  @Before
  public void setUp() throws SQLException {
    TestUtils.removeUserFromContext();

    // Truncate + Insert
    TestUtils.populateDatabase(dataSource, 
        "/db/truncate_tables.sql", 
        // Beacon
        "/db/beacon_dataset_table.sql", 
        "/db/beacon_data_table.sql", 
        // CC
        "/db/consent_code_category_table.sql",
        "/db/consent_code_table.sql",
        // Beacon->CC
        "/db/beacon_dataset_consent_code_table.sql");
  }

  @After
  public void tearDown() throws SQLException {
//    TestUtils.populateDatabase(dataSource, "/db/truncate_tables.sql");
  }

  @Test
  public void listDatasets() throws Exception {
    // Mock 3 calls to that method
    Beacon allowedDatasets = elixirBeaconService.listDatasets(COMMON_QUERY, null);

    assertThat(allowedDatasets.getDatasets().size(), equalTo(5));
    int i = 0;
    assertThat(allowedDatasets.getDatasets().get(i).getId(), equalTo("EGAD00000000001"));

    assertThat(allowedDatasets.getDatasets().get(i).getInfo().get(BeaconConstants.AUTHORIZED), equalTo(Boolean.toString(true)));//public
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse(), notNullValue());
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getPrimaryCategory(), notNullValue());
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getSecondaryCategories().size(), equalTo(1));
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getRequirements().size(), equalTo(0));
    i++;
    assertThat(allowedDatasets.getDatasets().get(1).getId(), equalTo("EGAD00000000002"));
    assertThat(allowedDatasets.getDatasets().get(1).getInfo().get(BeaconConstants.AUTHORIZED),
        equalTo(Boolean.toString(false)));// registered
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse(), notNullValue());
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getPrimaryCategory(), notNullValue());
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getSecondaryCategories().size(), equalTo(2));
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getRequirements().size(), equalTo(0));
    i++;
    assertThat(allowedDatasets.getDatasets().get(2).getId(), equalTo("EGAD00000000003"));
    assertThat(allowedDatasets.getDatasets().get(2).getInfo().get(BeaconConstants.AUTHORIZED),
        equalTo(Boolean.toString(false)));// protected
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse(), notNullValue());
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getPrimaryCategory(), notNullValue());
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getSecondaryCategories().size(), equalTo(0));
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getRequirements().size(), equalTo(0));
    i++;
    assertThat(allowedDatasets.getDatasets().get(3).getId(), equalTo("EGAD00000000004"));
    assertThat(allowedDatasets.getDatasets().get(3).getInfo().get(BeaconConstants.AUTHORIZED),
        equalTo(Boolean.toString(true)));// public
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse(), notNullValue());
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getPrimaryCategory(), notNullValue());
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getSecondaryCategories().size(), equalTo(1));
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getRequirements().size(), equalTo(1));
    i++;
    assertThat(allowedDatasets.getDatasets().get(4).getId(), equalTo("EGAD00000000005"));
    assertThat(allowedDatasets.getDatasets().get(4).getInfo().get(BeaconConstants.AUTHORIZED),
        equalTo(Boolean.toString(true)));// public
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse(), notNullValue());
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getPrimaryCategory(), notNullValue());
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getSecondaryCategories().size(), equalTo(1));
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().getConsentCodedataUse().getRequirements().size(), equalTo(2));
  }

  @Test
  public void listDatasetsByReferenceGenome() throws Exception {
    Beacon allowedDatasets = elixirBeaconService.listDatasets(COMMON_QUERY, "grch37");

    assertThat(allowedDatasets.getDatasets().size(), equalTo(3));
    assertThat(allowedDatasets.getDatasets().get(0).getId(), equalTo("EGAD00000000001"));
    assertThat(allowedDatasets.getDatasets().get(0).getInfo().get(BeaconConstants.AUTHORIZED),
        equalTo(Boolean.toString(true)));// public
    assertThat(allowedDatasets.getDatasets().get(1).getId(), equalTo("EGAD00000000002"));
    assertThat(allowedDatasets.getDatasets().get(1).getInfo().get(BeaconConstants.AUTHORIZED),
        equalTo(Boolean.toString(false)));// registered
    assertThat(allowedDatasets.getDatasets().get(2).getId(), equalTo("EGAD00000000005"));
    assertThat(allowedDatasets.getDatasets().get(2).getInfo().get(BeaconConstants.AUTHORIZED),
        equalTo(Boolean.toString(true)));// public
  }
  
  @Test
  public void checkParamChromosomeMissing() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = null;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = "grch37";
    String chromosome = null;
    String referenceBases = "C";
    String alternateBases = null;

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        equalTo("All 'referenceName', 'referenceBases' and/or 'assemblyId' are required"));
  }

  @Test
  public void checkParamReferenceGenomeMissing() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = null;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = 1111;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = null;
    String chromosome = "12";
    String referenceBases = "C";
    String alternateBases = null;

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        equalTo("All 'referenceName', 'referenceBases' and/or 'assemblyId' are required"));
  }
  
  @Test
  public void checkParamReferenceBasesMissing() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = null;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = 1111;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = null;
    String alternateBases = null;

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        equalTo("All 'referenceName', 'referenceBases' and/or 'assemblyId' are required"));
  }
  
  @Test
  public void checkParamAlternateBasesAndVariantTypeMissing() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = 1111;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = 1111;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "C";
    String alternateBases = null;

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        equalTo("Either 'alternateBases' or 'variantType' is required"));
  }
  
  @Test
  public void checkParamAlternateBasesAndVariantTypeProvided() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = VariantType.DELELETION;
    Integer start = 1111;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = 1111;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "C";
    String alternateBases = "A";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        equalTo("If 'variantType' is provided then 'alternateBases' must be empty or equal to 'N'"));
    
    result = new BeaconAlleleResponse();
    alternateBases = "N";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), nullValue());
    
    result = new BeaconAlleleResponse();
    alternateBases = null;

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), nullValue());
  }

  @Test
  public void checkParamStartMissingWhenEndProvided() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = null;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = 1111;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "N";
    String alternateBases = null;

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(), equalTo("'start' is required if 'end' is provided"));
  }
  
  @Test
  public void checkParamStartMissing() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = null;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "C";
    String alternateBases = null;

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(), equalTo(
        "Either 'start' or all of 'startMin', 'startMax', 'endMin' and 'endMax' are required"));
  }

  @Test
  public void checkParamSomeStartXXOrEndXXMissing() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = null;
    Integer startMin = 1111;
    Integer startMax = 1112;
    Integer end = null;
    Integer endMin = 2222;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "C";
    String alternateBases = null;

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        equalTo("All of 'startMin', 'startMax', 'endMin' and 'endMax' are required"));
  }

  @Test
  public void checkParamStartProvidedAndEndMissing() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = 1111;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "N";
    String alternateBases = null;
    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        equalTo("'referenceBases' cannot be 'N' if 'start' is provided and 'end' is missing"));
  }
  
  @Test
  public void checkParamStartStartMinStartMaxEndMinEndMaxProvided() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = 1111;
    Integer startMin = 1111;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "C";
    String alternateBases = null;
    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        equalTo("'start' cannot be provided at the same time as 'startMin', 'startMax', 'endMin' and 'endMax'"));
  }

  @Test
  public void checkParamDatasetFound() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = 1111;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = Arrays.asList("this ID does not exist");
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "A";
    String alternateBases = "AC";
    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.NOT_FOUND));
    assertThat(result.getError().getMessage(), equalTo("Dataset not found"));
  }

  @Test
  public void checkParamDatasetUnauthenticatedUser() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = 1111;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = Arrays.asList("EGAD00000000003");
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "A";
    String alternateBases = "AC";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.UNAUTHORIZED));
    assertThat(result.getError().getMessage(),
        equalTo("Unauthenticated users cannot access this dataset"));
  }

  @Test
  public void checkParamReferenceGenomeDoesNotMatchDataset() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = 1111;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = Arrays.asList("EGAD00000000001");
    String referenceGenome = "grch38";
    String chromosome = "12";
    String referenceBases = "A";
    String alternateBases = "AC";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        startsWith("The reference genome of this dataset ("));
  }

  @Test
  public void checkParamAlternateBasesNotValid() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = 1111;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = Arrays.asList("EGAD00000000001");
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "A";
    String alternateBases = "AR";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        startsWith("Invalid 'alternateBases' parameter, it must match the pattern [ACTG]+|N"));

    result = new BeaconAlleleResponse();
    alternateBases = "A";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), nullValue());

    result = new BeaconAlleleResponse();
    alternateBases = ".";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        startsWith("Invalid 'alternateBases' parameter, it must match the pattern [ACTG]+|N"));
    
    result = new BeaconAlleleResponse();
    alternateBases = "N";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), nullValue());
  }

  @Test
  public void checkReferenceBasesParamNotValid() {
    BeaconAlleleResponse result = new BeaconAlleleResponse();
    VariantType type = null;
    Integer start = 1111;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = Arrays.asList("EGAD00000000001");
    String referenceGenome = "grch37";
    String chromosome = "12";
    String referenceBases = "R";
    String alternateBases = "C";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        startsWith("Invalid 'referenceBases' parameter, it must match the pattern [ACTG]+|N"));

    result = new BeaconAlleleResponse();
    referenceBases = "AT";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), nullValue());

    result = new BeaconAlleleResponse();
    referenceBases = ".";

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), notNullValue());
    assertThat(result.getError().getErrorCode(), equalTo(ErrorCode.GENERIC_ERROR));
    assertThat(result.getError().getMessage(),
        startsWith("Invalid 'referenceBases' parameter, it must match the pattern [ACTG]+|N"));
    
    result = new BeaconAlleleResponse();
    referenceBases = "N";
    end = 123;

    elixirBeaconService.checkParams(result, datasetStableIds, type, alternateBases, referenceBases,
        chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome);

    assertThat(result.getError(), nullValue());
  }

  @Test
  public void queryForSNPs() throws Exception {
    String datasetStableId = "EGAD00000000001";
    String variantType = null;
    Integer start = 14929;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = Arrays.asList(datasetStableId);
    String referenceGenome = "grch37";
    String chromosome = "1";
    String referenceBases = "A";
    String alternateBases = "G";
    String includeDatasetResponses = FilterDatasetResponse.NONE.toString();

    // Query with positive answer WITHOUT detailed response by dataset
    BeaconAlleleResponse response = elixirBeaconService.queryBeacon(datasetStableIds, variantType,
        alternateBases, referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax,
        referenceGenome, includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
    assertThat(response.getApiVersion(), equalTo("0.4"));
    assertThat(response.getAlleleRequest(), notNullValue());

    includeDatasetResponses = FilterDatasetResponse.ALL.toString();
    // Query with positive answer AND detailed response by dataset
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(),
        equalTo(datasetStableId));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses().get(0).getCallCount(), notNullValue());
    assertThat(response.getDatasetAlleleResponses().get(0).getVariantCount(), notNullValue());
    assertThat(response.getDatasetAlleleResponses().get(0).getSampleCount(), notNullValue());
    assertThat(response.getDatasetAlleleResponses().get(0).getFrequency(), notNullValue());

    includeDatasetResponses = FilterDatasetResponse.HIT.toString();
    // Query with positive answer AND detailed response by dataset
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(),
        equalTo(datasetStableId));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(true));

    includeDatasetResponses = FilterDatasetResponse.MISS.toString();
    // Query with positive answer AND detailed response by dataset
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());

    alternateBases = "T";
    // Query with negative answer AND detailed response by dataset
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);

    assertThat(response.isExists(), equalTo(false));
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(),
        equalTo(datasetStableId));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(false));


    includeDatasetResponses = FilterDatasetResponse.NONE.toString();
    // Query with negative answer WITHOUT detailed response by dataset
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);

    assertThat(response.isExists(), equalTo(false));
    assertThat(response.getDatasetAlleleResponses(), nullValue());

    includeDatasetResponses = FilterDatasetResponse.HIT.toString();
    // Query with negative answer AND detailed response by dataset if HIT
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);

    assertThat(response.isExists(), equalTo(false));
    assertThat(response.getDatasetAlleleResponses(), nullValue());

    includeDatasetResponses = FilterDatasetResponse.MISS.toString();
    // Query with negative answer AND detailed response by dataset if MISS
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);

    assertThat(response.isExists(), equalTo(false));
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(),
        equalTo(datasetStableId));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(false));
  }
  
  @Test
  public void queryForDUPs() throws Exception {
    String datasetStableId = "EGAD00000000001";
    String variantType = VariantType.DUPLICATION.getType();
    Integer start = null;
    Integer startMin = 120012493;
    Integer startMax = 120012493;
    Integer end = null;
    Integer endMin = 120012934;
    Integer endMax = 120012934;
    List<String> datasetStableIds = Arrays.asList(datasetStableId);
    String referenceGenome = "grch37";
    String chromosome = "X";
    String referenceBases = "A";
    String alternateBases = null;
    String includeDatasetResponses = FilterDatasetResponse.NONE.toString();

    // Query with positive answer WITHOUT detailed response by dataset
    BeaconAlleleResponse response = elixirBeaconService.queryBeacon(datasetStableIds, variantType,
        alternateBases, referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax,
        referenceGenome, includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
    
    start = 120012493; 
    startMin = null;
    startMax = null;
    endMin = null;
    endMax = null;
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType,
        alternateBases, referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax,
        referenceGenome, includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
    
    // Range query
    start = null; 
    startMin = 120012490;
    startMax = 120012493;
    endMin = 120012930;
    endMax = 120012938;
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType,
        alternateBases, referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax,
        referenceGenome, includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
  }

  @Test
  public void queryForDELs() throws Exception {
    String datasetStableId = "EGAD00000000001";
    String variantType = VariantType.DELELETION.getType();
    Integer start = null;
    Integer startMin = 13000642;
    Integer startMax = 13000642;
    Integer end = null;
    Integer endMin = 13142857;
    Integer endMax = 13142857;
    List<String> datasetStableIds = Arrays.asList(datasetStableId);
    String referenceGenome = "grch37";
    String chromosome = "1";
    String referenceBases = "G";
    String alternateBases = null;
    String includeDatasetResponses = FilterDatasetResponse.NONE.toString();

    // Query with positive answer WITHOUT detailed response by dataset
    BeaconAlleleResponse response = elixirBeaconService.queryBeacon(datasetStableIds, variantType,
        alternateBases, referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax,
        referenceGenome, includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
    
    start = 13000642; 
    startMin = null;
    startMax = null;
    endMin = null;
    endMax = null;
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType,
        alternateBases, referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax,
        referenceGenome, includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
    
    // Range query
    start = null; 
    startMin = 13000640;
    startMax = 13000644;
    endMin = 13142857;
    endMax = 13142860;
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType,
        alternateBases, referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax,
        referenceGenome, includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
  }
  
  @Test
  public void queryMultipleDatasets() throws Exception {
    String variantType = null;
    Integer start = 14929;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = Arrays.asList("EGAD00000000001", "EGAD00000000005");
    String referenceGenome = "grch37";
    String chromosome = "1";
    String referenceBases = "A";
    String alternateBases = "G";
    String includeDatasetResponses = FilterDatasetResponse.ALL.toString();

    BeaconAlleleResponse response = elixirBeaconService.queryBeacon(datasetStableIds, variantType,
        alternateBases, referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax,
        referenceGenome, includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(),
        equalTo("EGAD00000000001"));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses().get(1).getDatasetId(),
        equalTo("EGAD00000000005"));
    assertThat(response.getDatasetAlleleResponses().get(1).isExists(), equalTo(false));

    // Don't include detailed response per dataset
    includeDatasetResponses = FilterDatasetResponse.NONE.toString();
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);
    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());

    // Just return "true" datasets
    includeDatasetResponses = FilterDatasetResponse.HIT.toString();
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);
    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses().size(), equalTo(1));
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(),
        equalTo("EGAD00000000001"));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(true));

    // Just return "false" datasets
    includeDatasetResponses = FilterDatasetResponse.MISS.toString();
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);
    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses().size(), equalTo(1));
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(),
        equalTo("EGAD00000000005"));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(false));
  }

  @Test
  public void queryAllDatasetPassingEmptyString() throws Exception {
    String variantType = null;
    Integer start = 14929;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = null;
    String referenceGenome = "grch37";
    String chromosome = "1";
    String referenceBases = "A";
    String alternateBases = "G";
    String includeDatasetResponses = FilterDatasetResponse.NONE.toString();

    BeaconAlleleResponse response = elixirBeaconService.queryBeacon(datasetStableIds, variantType,
        alternateBases, referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax,
        referenceGenome, includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
    
    // Get only HITS
    includeDatasetResponses = FilterDatasetResponse.HIT.toString();
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), notNullValue());
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(),
        equalTo("EGAD00000000001"));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(true));

    // Get only MISSES
    includeDatasetResponses = FilterDatasetResponse.MISS.toString();
    response = elixirBeaconService.queryBeacon(datasetStableIds, variantType, alternateBases,
        referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax, referenceGenome,
        includeDatasetResponses);

    assertThat(response.isExists(), equalTo(true)); // Global is true
    assertThat(response.getDatasetAlleleResponses(), notNullValue());
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(),
        equalTo("EGAD00000000005"));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(false));
  }

  @Test
  public void queryControlledDatasetByUnauthenticatedUser() throws Exception {
    String datasetStableId = "EGAD00000000002";
    String variantType = null;
    Integer start = 14929;
    Integer startMin = null;
    Integer startMax = null;
    Integer end = null;
    Integer endMin = null;
    Integer endMax = null;
    List<String> datasetStableIds = Arrays.asList(datasetStableId);
    String referenceGenome = "grch37";
    String chromosome = "1";
    String referenceBases = "A";
    String alternateBases = "G";
    String includeDatasetResponses = FilterDatasetResponse.NONE.toString();

    // Query with positive answer WITHOUT detailed response by dataset
    BeaconAlleleResponse response = elixirBeaconService.queryBeacon(datasetStableIds, variantType,
        alternateBases, referenceBases, chromosome, start, startMin, startMax, end, endMin, endMax,
        referenceGenome, includeDatasetResponses);
    
    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(ErrorCode.UNAUTHORIZED));
  }

}
