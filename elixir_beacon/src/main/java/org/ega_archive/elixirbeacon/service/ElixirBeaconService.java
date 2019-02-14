package org.ega_archive.elixirbeacon.service;

import java.util.List;

import org.ega_archive.elixirbeacon.dto.Beacon;
import org.ega_archive.elixirbeacon.dto.BeaconAlleleResponse;
import org.ega_archive.elixirbeacon.dto.BeaconRequest;
import org.ega_archive.elixirbeacon.enums.VariantType;
import org.ega_archive.elixircore.helper.CommonQuery;

import javassist.NotFoundException;

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
   * @throws NotFoundException 
   */
  public Beacon listDatasets(CommonQuery commonQuery, String referenceGenome) throws NotFoundException;

  /**
   * Executes the query against the beacon and basically answers yes or no.
   * 
   * @param start
   * @param startMin
   * @param startMax
   * @param end
   * @param endMin
   * @param endMax
   * @param chromosome
   * @param referenceBases
   * @param alternateBases
   * @param datasetStableIds
   * @param referenceGenome
   * @param includeDatasetResponses
   * @return
   */
  public BeaconAlleleResponse queryBeacon(List<String> datasetStableIds, String variantType,
      String alternateBases, String referenceBases, String chromosome, Integer start,
      Integer startMin, Integer startMax, Integer end, Integer endMin, Integer endMax,
      String referenceGenome, String includeDatasetResponses);

  /**
   * Verifies that mandatory parameters are present and that all parameters are valid.
   * 
   * @param result
   * @param datasetStableIds
   * @param type
   * @param alternateBases
   * @param referenceBases
   * @param chromosome
   * @param start
   * @param referenceGenome
   * @return
   */
  public List<Integer> checkParams(BeaconAlleleResponse result, List<String> datasetStableIds,
      VariantType type, String alternateBases, String referenceBases, String chromosome,
      Integer start, Integer startMin, Integer startMax, Integer end, Integer endMin,
      Integer endMax, String referenceGenome);

  public BeaconAlleleResponse queryBeacon(BeaconRequest request);

}
