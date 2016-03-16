package org.ega_archive.elixirbeacon.service;

import java.util.List;

import org.ega_archive.elixirbeacon.dto.Beacon;
import org.ega_archive.elixirbeacon.dto.BeaconAlleleResponse;
import org.ega_archive.elixircore.helper.CommonQuery;

public interface ElixirBeaconService {

  /**
   * Returns the information about this beacon implementation and all datasets. It also specifies
   * the access type for each dataset:
   * <ul>
   * <li>PUBLIC: all.</li>
   * <li>REGISTERED: if the user is authenticated.</li>
   * <li>PROTECTED: if the user is authorized to access it.</li>
   * 
   * @param commonQuery
   * @param referenceGenome
   * @return
   */
  public Beacon listDatasets(CommonQuery commonQuery, String referenceGenome);

  /**
   * Executes the query against the beacon and basically answers yes or no.
   * 
   * @param datasetStableIds
   * @param alternateBases
   * @param referenceBases
   * @param chromosome
   * @param position
   * @param start TODO
   * @param referenceGenome
   * @return
   */
  public BeaconAlleleResponse queryBeacon(List<String> datasetStableIds, String alternateBases,
      String referenceBases, String chromosome, Integer position, Integer start, String referenceGenome);

  /**
   * Verifies that mandatory parameters are present and that all parameters are valid.
   * 
   * @param result
   * @param datasetStableIds
   * @param alternateBases
   * @param referenceBases
   * @param chromosome
   * @param position
   * @param start TODO
   * @param referenceGenome
   * @return
   */
  public List<String> checkParams(BeaconAlleleResponse result, List<String> datasetStableIds,
      String alternateBases, String referenceBases, String chromosome, Integer position,
      Integer start, String referenceGenome);

}
