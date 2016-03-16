package org.ega_archive.elixirbeacon.service;

import java.util.Arrays;

import org.ega_archive.elixirbeacon.Application;
import org.ega_archive.elixirbeacon.constant.BeaconConstants;
import org.ega_archive.elixirbeacon.dto.AdamDataUseProfile;
import org.ega_archive.elixirbeacon.dto.Beacon;
import org.ega_archive.elixirbeacon.dto.BeaconAlleleResponse;
import org.ega_archive.elixirbeacon.dto.ConsentCodeDataUseProfile;
import org.ega_archive.elixircore.helper.CommonQuery;
import org.ega_archive.elixircore.test.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionDbUnitTestExecutionListener.class})
@DbUnitConfiguration(databaseConnection = {"elixirbeaconDataSource"})
@DatabaseSetup(value = {"/db/beacon_dataset_table.xml", 
    "/db/beacon_data_table.xml",
    "/db/adam_value_table.xml", 
    "/db/adam_table.xml", 
    "/db/beacon_dataset_adam_detailed_table.xml",
    "/db/beacon_dataset_adam_table.xml", 
    "/db/consent_code_category_table.xml", 
    "/db/consent_code_table.xml",
    "/db/beacon_dataset_consent_code_table.xml"}, type = DatabaseOperation.CLEAN_INSERT)
@DatabaseTearDown(value = {"/db/beacon_dataset_adam_table.xml",
    "/db/beacon_dataset_adam_detailed_table.xml", "/db/beacon_dataset_consent_code_table.xml"},
    type = DatabaseOperation.DELETE_ALL)
@DatabaseTearDown(value = {"/db/beacon_dataset_table.xml", "/db/beacon_data_table.xml",
    "/db/beacon_data_table.xml", "/db/adam_value_table.xml", "/db/adam_table.xml",
    "/db/consent_code_category_table.xml", "/db/consent_code_table.xml"},
    type = DatabaseOperation.DELETE_ALL)
public class ElixirBeaconServiceTest {
  
  private static final CommonQuery COMMON_QUERY = CommonQuery.builder().skip(0).limit(0).build();

  @Autowired
  private ElixirBeaconService elixirBeaconService;
  
  @Before
  public void setUp() {
    TestUtils.removeUserFromContext();
  }
  
  @Test
  public void listDatasets() throws Exception {
    // Mock 3 calls to that method
    Beacon allowedDatasets = elixirBeaconService.listDatasets(COMMON_QUERY, null);
    
    assertThat(allowedDatasets.getDatasets().size(), equalTo(5));
    int i = 0;
    assertThat(allowedDatasets.getDatasets().get(i).getId(), equalTo("EGAD00000000001"));
    assertThat(allowedDatasets.getDatasets().get(i).getInfo().get(BeaconConstants.AUTHORIZED), equalTo(Boolean.toString(true)));//public
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().size(), equalTo(2));
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getPrimaryCategory(), notNullValue());
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getSecondaryCategories().size(), equalTo(1));
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getRequirements(), nullValue());
    assertThat(((AdamDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(1)).getProfile().size(), equalTo(4));
    i++;
    assertThat(allowedDatasets.getDatasets().get(1).getId(), equalTo("EGAD00000000002"));
    assertThat(allowedDatasets.getDatasets().get(1).getInfo().get(BeaconConstants.AUTHORIZED), equalTo(Boolean.toString(false)));//registered
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().size(), equalTo(2));
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getPrimaryCategory(), notNullValue());
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getSecondaryCategories().size(), equalTo(1));
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getRequirements().size(), equalTo(1));
    assertThat(((AdamDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(1)).getProfile().size(), equalTo(5));
    i++;
    assertThat(allowedDatasets.getDatasets().get(2).getId(), equalTo("EGAD00000000003"));
    assertThat(allowedDatasets.getDatasets().get(2).getInfo().get(BeaconConstants.AUTHORIZED), equalTo(Boolean.toString(false)));//protected
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().size(), equalTo(2));
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getPrimaryCategory(), notNullValue());
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getSecondaryCategories(), nullValue());
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getRequirements(), nullValue());
    assertThat(((AdamDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(1)).getProfile().size(), equalTo(1));
    i++;
    assertThat(allowedDatasets.getDatasets().get(3).getId(), equalTo("EGAD00000000004"));
    assertThat(allowedDatasets.getDatasets().get(3).getInfo().get(BeaconConstants.AUTHORIZED), equalTo(Boolean.toString(true)));//public
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().size(), equalTo(2));
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getPrimaryCategory(), notNullValue());
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getSecondaryCategories().size(), equalTo(2));
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getRequirements().size(), equalTo(2));
    assertThat(((AdamDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(1)).getProfile().size(), equalTo(1));
    i++;
    assertThat(allowedDatasets.getDatasets().get(4).getId(), equalTo("EGAD00000000005"));
    assertThat(allowedDatasets.getDatasets().get(4).getInfo().get(BeaconConstants.AUTHORIZED), equalTo(Boolean.toString(true)));//public
    assertThat(allowedDatasets.getDatasets().get(i).getDataUseConditions().size(), equalTo(2));
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getPrimaryCategory(), notNullValue());
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getSecondaryCategories().size(), equalTo(1));
    assertThat(((ConsentCodeDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(0)).getProfile().getRequirements(), nullValue());
    assertThat(((AdamDataUseProfile) allowedDatasets.getDatasets().get(i).getDataUseConditions().get(1)).getProfile().size(), equalTo(3));
  }
  
  @Test
  public void listDatasetsByReferenceGenome() throws Exception {
    Beacon allowedDatasets = elixirBeaconService.listDatasets(COMMON_QUERY, "grch37");

    assertThat(allowedDatasets.getDatasets().size(), equalTo(3));
    assertThat(allowedDatasets.getDatasets().get(0).getId(), equalTo("EGAD00000000001"));
    assertThat(allowedDatasets.getDatasets().get(0).getInfo().get(BeaconConstants.AUTHORIZED), equalTo(Boolean.toString(true)));//public
    assertThat(allowedDatasets.getDatasets().get(1).getId(), equalTo("EGAD00000000002"));
    assertThat(allowedDatasets.getDatasets().get(1).getInfo().get(BeaconConstants.AUTHORIZED), equalTo(Boolean.toString(false)));//registered
    assertThat(allowedDatasets.getDatasets().get(2).getId(), equalTo("EGAD00000000005"));
    assertThat(allowedDatasets.getDatasets().get(2).getInfo().get(BeaconConstants.AUTHORIZED), equalTo(Boolean.toString(true)));//public
  }
  
  @Test
  public void checkParamPositionAndStartMissing() {
    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, null, null, null, "12", null, null, "grch37");

    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.PRECONDITION_FAILED.value()));
  }
  
  @Test
  public void checkParamBothPositionAndStartArePresent() {
    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, null, null, null, "12", 123, 123, "grch37");

    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.PRECONDITION_FAILED.value()));
  }
  
  @Test
  public void checkParamChromosomeMissing() {
    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, null, null, null, "", 429, null, "grch37");

    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.PRECONDITION_FAILED.value()));
  }
  
  @Test
  public void checkParamReferenceGenomeMissing() {
    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, null, null, null, "12", 429, null, null);

    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.PRECONDITION_FAILED.value()));
  }
  
  @Test
  public void checkDatasetParamNotFound() {
    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, Arrays.asList("invented dataset"), "", null, "1", 1234, null, "GRCh37");
    
    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.NOT_FOUND.value()));
  }
  
  @Test
  public void checkDatasetParamUnauthenticatedUser() {
    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, Arrays.asList("EGAD00000000002"), "", null, "1", 1234, null, "GRCh37");
    
    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.UNAUTHORIZED.value()));
  }
  
  @Test
  public void checkDatasetParamUnauthorizedAnonymousUser() throws Exception {
    String datasetStableId = "EGAD00000000003";

    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, Arrays.asList(datasetStableId), "", null, "1", 1234, null, "GRCh37");
    
    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.UNAUTHORIZED.value()));
  }
  
  @Test
  public void checkReferenceGenomeParamMatchesDataset() {
    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, Arrays.asList("EGAD00000000001"), "", null, "1", 1234, null, "GRCh38");
    
    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.PRECONDITION_FAILED.value()));
  }

  @Test
  public void checkAlternateBasesParamNotValid1() {
    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, null, "R", null, "13", 1, null, "GRCh37");
    
    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.PRECONDITION_FAILED.value()));
  }

  @Test
  public void checkAlternateBasesParamNotValid2() {
    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, null, "DA", null, "13", 1, null, "GRCh37");
    
    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.PRECONDITION_FAILED.value()));
  }
  
  @Test
  public void checkReferenceBasesParamNotValid1() {
    BeaconAlleleResponse response = new BeaconAlleleResponse();
    elixirBeaconService.checkParams(response, null, null, "P", "13", 1, null, "GRCh37");
    
    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.PRECONDITION_FAILED.value()));
  }

  @Test
  public void queryOneDataset() throws Exception {
    BeaconAlleleResponse response =
        elixirBeaconService.queryBeacon(Arrays.asList("EGAD00000000001"), "A", null, "X", null, 1, "GRCh37");

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(), equalTo("EGAD00000000001"));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(true));
    
    response =
        elixirBeaconService.queryBeacon(Arrays.asList("EGAD00000000001"), "A", null, "X", 11111, null, "GRCh37");

    assertThat(response.isExists(), equalTo(false));
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(), equalTo("EGAD00000000001"));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(false));
  }
  
  @Test
  public void queryMultipleDatasets() throws Exception {
    
    BeaconAlleleResponse response =
        elixirBeaconService.queryBeacon(Arrays.asList("EGAD00000000001", "EGAD00000000005"), "A", null, "X", 1, null, "GRCh37");

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses().get(0).getDatasetId(), equalTo("EGAD00000000001"));
    assertThat(response.getDatasetAlleleResponses().get(0).isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses().get(1).getDatasetId(), equalTo("EGAD00000000005"));
    assertThat(response.getDatasetAlleleResponses().get(1).isExists(), equalTo(false));
  }
  
  @Test
  public void queryAllDatasetAndGetAPositiveAnswer() throws Exception {
    BeaconAlleleResponse response =
        elixirBeaconService.queryBeacon(null, "A", "C", "X", 1, null, "GRCh37");

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
  }
  
  @Test
  public void queryAllDatasetAndGetANegativeAnswer() throws Exception {
    BeaconAlleleResponse response =
        elixirBeaconService.queryBeacon(null, "T", null, "12", 1, null, "GRCh37");

    assertThat(response.isExists(), equalTo(false));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
  }
  
  @Test
  public void queryAllDatasetPassingEmptyString() throws Exception {
    BeaconAlleleResponse response =
        elixirBeaconService.queryBeacon(Arrays.asList(""), null, null, "X", 1, null, "GRCh37");

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
  }
  
  @Test
  public void queryAllDatasetPassingEmptyArray() throws Exception {
    BeaconAlleleResponse response =
        elixirBeaconService.queryBeacon(Arrays.asList(), null, null, "X", 1, null, "GRCh37");

    assertThat(response.isExists(), equalTo(true));
    assertThat(response.getDatasetAlleleResponses(), nullValue());
  }

  @Test
  public void queryControlledDatasetByUnauthenticatedUser() throws Exception {
    BeaconAlleleResponse response = elixirBeaconService.queryBeacon(Arrays.asList("EGAD00000000002"), "A", null, "X", 1, null, "GRCh37");
    
    assertThat(response.getError(), notNullValue());
    assertThat(response.getError().getErrorCode(), equalTo(HttpStatus.UNAUTHORIZED.value()));
  }
  
}
