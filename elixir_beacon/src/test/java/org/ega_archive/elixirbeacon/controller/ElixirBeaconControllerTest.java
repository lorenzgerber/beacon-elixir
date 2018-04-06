package org.ega_archive.elixirbeacon.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.ega_archive.elixirbeacon.Application;
import org.ega_archive.elixirbeacon.dto.BeaconAlleleResponse;
import org.ega_archive.elixircore.constant.ParamName;
import org.ega_archive.elixircore.test.util.TestUtils;
import org.ega_archive.elixircore.util.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@WebAppConfiguration
@SpringBootTest("server.port:0")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionDbUnitTestExecutionListener.class})
@DbUnitConfiguration(databaseConnection = {"elixirbeaconDataSource"})
@DatabaseSetup(value = {"/db/beacon_dataset_table.xml", "/db/beacon_data_table.xml"},
    type = DatabaseOperation.CLEAN_INSERT)
@DatabaseTearDown(value = {"/db/beacon_dataset_table.xml", "/db/beacon_data_table.xml"},
    type = DatabaseOperation.DELETE_ALL)
public class ElixirBeaconControllerTest {

  @Autowired
  @InjectMocks
  private ElixirBeaconController controller;
  
  private MockMvc mockMvc;
  
  @Autowired
  private ObjectMapper objectMapper;
  
  @Autowired
  private WebApplicationContext wac;
  
  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac)
        .alwaysExpect(status().isOk())
        .alwaysExpect(content().contentType(TestUtils.APPLICATION_JSON_CHARSET_UTF_8))
        .build();
  }
  
  @Test
  public void callRoot() throws Exception {
    
    MvcResult mvcResult = mockMvc.perform(get("/beacon/")
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();
    
    String response = mvcResult.getResponse().getContentAsString();
    
    assertThat(response, notNullValue());
  }
  
  @Test
  public void callQuery() throws Exception {
    
    MvcResult mvcResult = mockMvc.perform(get("/beacon/query")
        .param(ParamName.BEACON_ALTERNATE_BASES, "A")
        .param(ParamName.BEACON_CHROMOSOME, "19")
        .param(ParamName.BEACON_DATASET_IDS, "EGAD00000000001")
        .param(ParamName.BEACON_START, "1234")
        .param(ParamName.BEACON_REFERENCE_GENOME, "grch37")
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();
    
    BeaconAlleleResponse response = JsonUtils.jsonToObject(mvcResult.getResponse().getContentAsString(), BeaconAlleleResponse.class, objectMapper);
    
    assertThat(response, notNullValue());
    assertThat(response.getError(), nullValue());
    
    mvcResult = mockMvc.perform(post("/beacon/query")
        .param(ParamName.BEACON_CHROMOSOME, "19")
        .param(ParamName.BEACON_START, "1234")
        .param(ParamName.BEACON_REFERENCE_GENOME, "grch37")
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();
    
    response = JsonUtils.jsonToObject(mvcResult.getResponse().getContentAsString(), BeaconAlleleResponse.class, objectMapper);
    
    assertThat(response, notNullValue());
    assertThat(response.getError(), nullValue());
  }
  
  /**
   * Both position and start are accepted as parameter.
   * 
   * @throws Exception
   */
  @Test
  public void callQueryWithAlternateParameterNameStart() throws Exception {

    MvcResult mvcResult = mockMvc.perform(post("/beacon/query")
        .param(ParamName.BEACON_CHROMOSOME, "19")
        .param(ParamName.BEACON_START, "1234")
        .param(ParamName.BEACON_REFERENCE_GENOME, "grch37")
        .accept(MediaType.APPLICATION_JSON))
        .andReturn();
    
    BeaconAlleleResponse response = JsonUtils.jsonToObject(mvcResult.getResponse().getContentAsString(), BeaconAlleleResponse.class, objectMapper);
    
    assertThat(response, notNullValue());
    assertThat(response.getError(), nullValue());
  }
  
}
