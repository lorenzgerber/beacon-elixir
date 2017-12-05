package org.ega_archive.elixirbeacon.controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import org.ega_archive.elixirbeacon.dto.Beacon;
import org.ega_archive.elixirbeacon.dto.BeaconAlleleResponse;
import org.ega_archive.elixirbeacon.service.ElixirBeaconService;
import org.ega_archive.elixircore.constant.ParamName;
import org.ega_archive.elixircore.helper.CommonQueryHelper;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/beacon")
public class ElixirBeaconController {
  
  @Autowired
  private ElixirBeaconService elixirBeaconService;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public Beacon listDatasets(
      Sort sort,
      @RequestParam(required = false) Map<String, String> params) {
    
    return elixirBeaconService.listDatasets(CommonQueryHelper.parseQuery(params, sort), null);
  }
  
  @RequestMapping(value = "/query", method = {RequestMethod.GET, RequestMethod.POST})
  public BeaconAlleleResponse queryBeacon(
      @RequestParam(value = ParamName.BEACON_DATASET_IDS, required = false) List<String> datasetStableIds, 
      @RequestParam(value = ParamName.BEACON_ALTERNATE_BASES, required = false) String allele, 
      @RequestParam(value = ParamName.BEACON_REFERENCE_BASES, required = false) String referenceBases, 
      @RequestParam(value = ParamName.BEACON_CHROMOSOME, required = false) String chromosome, 
      @RequestParam(value = ParamName.BEACON_START, required = false) Integer start,
      @RequestParam(value = ParamName.BEACON_REFERENCE_GENOME, required = false) String referenceGenome,
      @RequestParam(value = ParamName.BEACON_INCLUDE_DATASET_RESPONSES, required = false) boolean includeDatasetResponses) {
    
    return elixirBeaconService.queryBeacon(datasetStableIds, allele, referenceBases, chromosome,
        start, referenceGenome, includeDatasetResponses);
  }

  /*
   * PROTECTED ENDPOINTS
   */
  
  @RequestMapping(value = {"/protected/info", "/protected/"}, method = RequestMethod.GET)
  public Beacon listDatasetsAuthenticated(
      OAuth2Authentication auth,		  
      Sort sort,
      @RequestParam(required = false) Map<String, String> params) {
    LinkedHashMap<String,String> userDetails = (LinkedHashMap<String, String>) auth.getUserAuthentication().getDetails(); 
    if (userDetails. containsKey("bona_fide_status")) {
	// List protected dataset 
    	return elixirBeaconService.listDatasets(CommonQueryHelper.parseQuery(params, sort), null);
    }
    return null;
  }
  
  @RequestMapping(value = {"/protected/query", "/protected/alleles"}, method = {RequestMethod.GET, RequestMethod.POST})
  public BeaconAlleleResponse queryBeaconAuthenticated(
      OAuth2Authentication auth,		  
      @RequestParam(value = ParamName.BEACON_DATASET_IDS, required = false) List<String> datasetStableIds,
      @RequestParam(value = ParamName.BEACON_ALTERNATE_BASES, required = false) String allele,
      @RequestParam(value = ParamName.BEACON_REFERENCE_BASES, required = false) String referenceBases,
      @RequestParam(value = ParamName.BEACON_CHROMOSOME, required = false) String chromosome,
      @RequestParam(value = ParamName.BEACON_START, required = false) Integer start,
      @RequestParam(value = ParamName.BEACON_REFERENCE_GENOME, required = false) String referenceGenome,
      @RequestParam(value = ParamName.BEACON_INCLUDE_DATASET_RESPONSES, required = false) boolean includeDatasetResponses) {
   
      LinkedHashMap<String,String> userDetails = (LinkedHashMap<String, String>) auth.getUserAuthentication().getDetails();
    if (userDetails. containsKey("bona_fide_status")) {
    	// Make protected query
    	return elixirBeaconService.queryBeacon(datasetStableIds, allele, referenceBases, chromosome,
		            start, referenceGenome, includeDatasetResponses);	    
    }
    return null;
  } 
}
