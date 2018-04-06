package org.ega_archive.elixirbeacon.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ega_archive.elixirbeacon.constant.BeaconConstants;
import org.ega_archive.elixirbeacon.convert.Operations;
import org.ega_archive.elixirbeacon.dto.Beacon;
import org.ega_archive.elixirbeacon.dto.BeaconAlleleRequest;
import org.ega_archive.elixirbeacon.dto.BeaconAlleleResponse;
import org.ega_archive.elixirbeacon.dto.Dataset;
import org.ega_archive.elixirbeacon.dto.DatasetAlleleResponse;
import org.ega_archive.elixirbeacon.dto.Error;
import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDataset;
import org.ega_archive.elixirbeacon.model.elixirbeacon.QBeaconData;
import org.ega_archive.elixirbeacon.properties.SampleRequests;
import org.ega_archive.elixirbeacon.repository.elixirbeacon.BeaconDataRepository;
import org.ega_archive.elixirbeacon.repository.elixirbeacon.BeaconDatasetRepository;
import org.ega_archive.elixircore.enums.DatasetAccessType;
import org.ega_archive.elixircore.helper.CommonQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class ElixirBeaconServiceImpl implements ElixirBeaconService {
  
  @Autowired
  private SampleRequests sampleRequests;

  @Autowired
  private BeaconDatasetRepository beaconDatasetRepository;
  
  @Autowired
  private BeaconDataRepository beaconDataRepository;
  
  @Override
  public Beacon listDatasets(CommonQuery commonQuery, String referenceGenome) {
    
    commonQuery.setSort(new Sort(new Order(Direction.ASC, "id")));
    
    List<Dataset> convertedDatasets = new ArrayList<Dataset>();

    Page<BeaconDataset> allDatasets = null;
    if (StringUtils.isNotBlank(referenceGenome)) {
      referenceGenome = StringUtils.lowerCase(referenceGenome);
      allDatasets =
          beaconDatasetRepository.findByReferenceGenome(referenceGenome, commonQuery.getPageable());
    } else {
      allDatasets = beaconDatasetRepository.findAll(commonQuery);
    }

    Integer size = 0;
    for (BeaconDataset dataset : allDatasets) {
      DatasetAccessType accessType = DatasetAccessType.parse(dataset.getAccessType());
      boolean authorized = false;
      if (accessType == DatasetAccessType.PUBLIC) {
        authorized = true;
      }
      convertedDatasets.add(Operations.convert(dataset, authorized));
      size += dataset.getSize();
    }

    Map<String, String> info = new HashMap<String, String>();
    info.put(BeaconConstants.SIZE, size.toString());
    
    Beacon response = new Beacon();
    response.setDatasets(convertedDatasets);
    response.setInfo(info);
    response.setSampleAlleleRequests(getSampleAlleleRequests());
    return response;
  }

  private List<BeaconAlleleRequest> getSampleAlleleRequests() {
    List<BeaconAlleleRequest> sampleAlleleRequests = new ArrayList<BeaconAlleleRequest>();
    sampleAlleleRequests.add(BeaconAlleleRequest.builder()
        .assemblyId(sampleRequests.getAssemblyId1())
        .start(sampleRequests.getPosition1())
        .chromosome(sampleRequests.getReferenceName1())
        .alternateBases(StringUtils.isBlank(sampleRequests.getAlternateBases1()) ? null : sampleRequests.getAlternateBases1())
        .datasetIds(sampleRequests.getDatasetIds1().isEmpty() ? null : sampleRequests.getDatasetIds1())
        .build());
    sampleAlleleRequests.add(BeaconAlleleRequest.builder()
        .assemblyId(sampleRequests.getAssemblyId2())
        .start(sampleRequests.getPosition2())
        .chromosome(sampleRequests.getReferenceName2())
        .alternateBases(StringUtils.isBlank(sampleRequests.getAlternateBases2()) ? null : sampleRequests.getAlternateBases2())
        .datasetIds(sampleRequests.getDatasetIds2().isEmpty() ? null : sampleRequests.getDatasetIds2())
        .build());
    sampleAlleleRequests.add(BeaconAlleleRequest.builder()
        .assemblyId(sampleRequests.getAssemblyId3())
        .start(sampleRequests.getPosition3())
        .chromosome(sampleRequests.getReferenceName3())
        .alternateBases(StringUtils.isBlank(sampleRequests.getAlternateBases3()) ? null : sampleRequests.getAlternateBases3())
        .datasetIds(sampleRequests.getDatasetIds3().isEmpty() ? null : sampleRequests.getDatasetIds3())
        .build());
    return sampleAlleleRequests;
  }

  @Override
  public BeaconAlleleResponse queryBeacon(List<String> datasetStableIds, String alternateBases,
      String referenceBases, String chromosome, Integer start, String referenceGenome,
      boolean includeDatasetResponses) {

    BeaconAlleleResponse result = new BeaconAlleleResponse();
    
    alternateBases = StringUtils.upperCase(alternateBases);
    referenceBases = StringUtils.upperCase(referenceBases);

    BeaconAlleleRequest request = BeaconAlleleRequest.builder()
        .alternateBases(alternateBases)
        .referenceBases(referenceBases)
        .chromosome(chromosome)
        .datasetIds(datasetStableIds)
        .start(start)
        .assemblyId(referenceGenome)
        .includeDatasetResponses(includeDatasetResponses)
        .build();
    result.setAlleleRequest(request);
    
    datasetStableIds =
        checkParams(result, datasetStableIds, alternateBases, referenceBases, chromosome, start,
            referenceGenome);

    boolean globalExists = false;
    if (result.getError() == null) {
      globalExists =
          queryDatabase(datasetStableIds, alternateBases, chromosome, start, referenceGenome,
              result);
    }
    result.setExists(globalExists);
    return result;
  }

  @Override
  public List<String> checkParams(BeaconAlleleResponse result, List<String> datasetStableIds,
      String alternateBases, String referenceBases, String chromosome, Integer start,
      String referenceGenome) {

    if (StringUtils.isBlank(chromosome) || start == null || StringUtils.isBlank(referenceGenome)) {
      Error error = Error.builder()
          .errorCode(HttpStatus.PRECONDITION_FAILED.value())
          .message("Missing mandatory parameters: referenceName, start and/or assemblyId")
          .build();
      result.setError(error);
      return datasetStableIds;
    }
    
    if (datasetStableIds != null) {
      // Remove empty/null strings
      datasetStableIds =
          datasetStableIds.stream().filter(s -> (StringUtils.isNotBlank(s)))
              .collect(Collectors.toList());
      
      for (String datasetStableId : datasetStableIds) {
        // 1) Dataset exists
        BeaconDataset dataset = beaconDatasetRepository.findOne(datasetStableId);
        if (dataset == null) {
          Error error = Error.builder()
              .errorCode(HttpStatus.NOT_FOUND.value())
              .message("Dataset not found")
              .build();
          result.setError(error);
          return datasetStableIds;
        }

        DatasetAccessType datasetAccessType = DatasetAccessType.parse(dataset.getAccessType());
        if (datasetAccessType != DatasetAccessType.PUBLIC) {
          Error error = Error.builder()
              .errorCode(HttpStatus.UNAUTHORIZED.value())
              .message("Unauthenticated users cannot access this dataset")
              .build();
          result.setError(error);
          return datasetStableIds;
        }

        // Check that the provided reference genome matches the one specified in the DB for this
        // dataset
        if (!StringUtils.equalsIgnoreCase(dataset.getReferenceGenome(), referenceGenome)) {
          Error error = Error.builder()
              .errorCode(HttpStatus.PRECONDITION_FAILED.value())
              .message("The reference genome of this dataset (" + datasetStableId
                  + ") does not match the provided value")
              .build();
          result.setError(error);
          return datasetStableIds;
        }
      }
    }
    
    // Allele has a valid value
    if (StringUtils.isNotBlank(alternateBases)) {
      boolean matches = Pattern.matches("[ACTG]+|I[ACTG]+|D[0-9]+", alternateBases);
      if (!matches) {
        Error error = Error.builder()
            .errorCode(HttpStatus.PRECONDITION_FAILED.value())
            .message("Invalid alternateBases parameter, can only be a [ACTG]+ or I[ACTG]+ or D[0-9]+")
            .build();
        result.setError(error);
        return datasetStableIds;
      }
    }
    if (StringUtils.isNotBlank(referenceBases)) {
      boolean matches = Pattern.matches("[ACTG]+|I[ACTG]+|D[0-9]+", referenceBases);
      if (!matches) {
        Error error = Error.builder()
            .errorCode(HttpStatus.PRECONDITION_FAILED.value())
            .message("Invalid referenceBases parameter, can only be a [ACTG]+ or I[ACTG]+ or D[0-9]+")
            .build();
        result.setError(error);
        return datasetStableIds;
      }
    }
    return datasetStableIds;
  }

  private boolean queryDatabase(List<String> datasetStableIds, String alternateBases,
      String chromosome, Integer start, String referenceGenome, BeaconAlleleResponse result) {

    QBeaconData qBeacon = QBeaconData.beaconData;
    BooleanExpression condition = qBeacon.id.chromosome.eq(chromosome);
    condition = condition.and(qBeacon.id.position.eq(start));
    if (StringUtils.isNotBlank(alternateBases)) {
      condition = condition.and(qBeacon.id.alternateBases.eq(alternateBases));
    }
    referenceGenome = StringUtils.lowerCase(referenceGenome);
    condition = condition.and(qBeacon.id.referenceGenome.eq(referenceGenome));

    if (datasetStableIds == null || datasetStableIds.isEmpty()) {
      List<String> authorizedDatasets = findAuthorizedDatasets(referenceGenome);
      // Limit the query to only the authorized datasets
      if (!result.getAlleleRequest().isIncludeDatasetResponses()) {
        condition = condition.and(qBeacon.id.datasetId.in(authorizedDatasets));
      }
      datasetStableIds = authorizedDatasets;
    }

    long numResults = 0L;
    boolean globalExists = false;

    if (result.getAlleleRequest().isIncludeDatasetResponses() && datasetStableIds != null
        && !datasetStableIds.isEmpty()) {
      for (String datasetStableId : datasetStableIds) {
        BooleanExpression datasetCondition =
            condition.and(qBeacon.id.datasetId.eq(datasetStableId));
        numResults = beaconDataRepository.count(datasetCondition);
        DatasetAlleleResponse datasetResponse = new DatasetAlleleResponse();
        datasetResponse.setDatasetId(datasetStableId);
        datasetResponse.setExists(numResults > 0);
        result.addDatasetAlleleResponse(datasetResponse);
        globalExists |= numResults > 0;
      }
    } else {
      numResults = beaconDataRepository.count(condition);
      globalExists = numResults > 0;
    }
    return globalExists;
  }

  private List<String> findAuthorizedDatasets(String referenceGenome) {
    List<String> publicDatasets =
        beaconDatasetRepository.findByReferenceGenomeAndAccessType(referenceGenome,
            DatasetAccessType.PUBLIC.getType());
    return publicDatasets;
  }

}
