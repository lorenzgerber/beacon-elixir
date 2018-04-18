package org.ega_archive.elixirbeacon.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ega_archive.elixirbeacon.constant.BeaconConstants;
import org.ega_archive.elixirbeacon.convert.Operations;
import org.ega_archive.elixirbeacon.dto.Beacon;
import org.ega_archive.elixirbeacon.dto.BeaconAlleleRequest;
import org.ega_archive.elixirbeacon.dto.BeaconAlleleResponse;
import org.ega_archive.elixirbeacon.dto.BeaconRequest;
import org.ega_archive.elixirbeacon.dto.Dataset;
import org.ega_archive.elixirbeacon.dto.DatasetAlleleResponse;
import org.ega_archive.elixirbeacon.dto.Error;
import org.ega_archive.elixirbeacon.enums.ErrorCode;
import org.ega_archive.elixirbeacon.enums.FilterDatasetResponse;
import org.ega_archive.elixirbeacon.enums.VariantType;
import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconData;
import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDataset;
import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDatasetConsentCode;
import org.ega_archive.elixirbeacon.properties.SampleRequests;
import org.ega_archive.elixirbeacon.repository.elixirbeacon.BeaconDataRepository;
import org.ega_archive.elixirbeacon.repository.elixirbeacon.BeaconDatasetConsentCodeRepository;
import org.ega_archive.elixirbeacon.repository.elixirbeacon.BeaconDatasetRepository;
import org.ega_archive.elixircore.enums.DatasetAccessType;
import org.ega_archive.elixircore.exception.NotFoundException;
import org.ega_archive.elixircore.helper.CommonQuery;
import org.ega_archive.elixircore.util.StoredProcedureUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

@Service
public class ElixirBeaconServiceImpl implements ElixirBeaconService {
  
  @Autowired
  private SampleRequests sampleRequests;

  @Autowired
  private BeaconDatasetRepository beaconDatasetRepository;
  
  @Autowired
  private BeaconDataRepository beaconDataRepository;
  
  @Autowired
  private BeaconDatasetConsentCodeRepository beaconDatasetConsentCodeRepository;
  
  @Override
  public Beacon listDatasets(CommonQuery commonQuery, String referenceGenome)
      throws NotFoundException {

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
      List<BeaconDatasetConsentCode> ccDataUseConditions =
          beaconDatasetConsentCodeRepository.findByDatasetId(dataset.getId());

      convertedDatasets.add(Operations.convert(dataset, authorized, ccDataUseConditions));

      size += dataset.getVariantCnt();
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
        .referenceName(sampleRequests.getReferenceName1())
        .alternateBases(StringUtils.isBlank(sampleRequests.getAlternateBases1()) ? null : sampleRequests.getAlternateBases1())
        .datasetIds(sampleRequests.getDatasetIds1().isEmpty() ? null : sampleRequests.getDatasetIds1())
        .build());
    sampleAlleleRequests.add(BeaconAlleleRequest.builder()
        .assemblyId(sampleRequests.getAssemblyId2())
        .start(sampleRequests.getPosition2())
        .referenceName(sampleRequests.getReferenceName2())
        .alternateBases(StringUtils.isBlank(sampleRequests.getAlternateBases2()) ? null : sampleRequests.getAlternateBases2())
        .datasetIds(sampleRequests.getDatasetIds2().isEmpty() ? null : sampleRequests.getDatasetIds2())
        .build());
    sampleAlleleRequests.add(BeaconAlleleRequest.builder()
        .assemblyId(sampleRequests.getAssemblyId3())
        .start(sampleRequests.getPosition3())
        .referenceName(sampleRequests.getReferenceName3())
        .alternateBases(StringUtils.isBlank(sampleRequests.getAlternateBases3()) ? null : sampleRequests.getAlternateBases3())
        .datasetIds(sampleRequests.getDatasetIds3().isEmpty() ? null : sampleRequests.getDatasetIds3())
        .build());
    return sampleAlleleRequests;
  }

  @Override
  public BeaconAlleleResponse queryBeacon(List<String> datasetStableIds, String variantType,
      String alternateBases, String referenceBases, String chromosome, Integer start,
      Integer startMin, Integer startMax, Integer end, Integer endMin, Integer endMax,
      String referenceGenome, String includeDatasetResponses) {

    BeaconAlleleResponse result = new BeaconAlleleResponse();
    
    alternateBases = StringUtils.upperCase(alternateBases);
    referenceBases = StringUtils.upperCase(referenceBases);
    
    BeaconAlleleRequest request = BeaconAlleleRequest.builder()
        .alternateBases(alternateBases)
        .referenceBases(referenceBases)
        .referenceName(chromosome)
        .datasetIds(datasetStableIds)
        .start(start)
        .startMin(startMin)
        .startMax(startMax)
        .end(end)
        .endMin(endMin)
        .endMax(endMax)
        .variantType(variantType)
        .assemblyId(referenceGenome)
        .includeDatasetResponses(FilterDatasetResponse.parse(includeDatasetResponses))
        .build();
    result.setAlleleRequest(request);
    
    VariantType type = VariantType.parse(variantType);

    List<Integer> datasetIds =
        checkParams(result, datasetStableIds, type, alternateBases, referenceBases, chromosome,
            start, startMin, startMax, end, endMin, endMax, referenceGenome);

    boolean globalExists = false;
    if (result.getError() == null) {
      globalExists = queryDatabase(datasetIds, type, referenceBases, alternateBases, chromosome,
          start, startMin, startMax, end, endMin, endMax, referenceGenome, result);
    }
    result.setExists(globalExists);
    return result;
  }

  @Override
  public List<Integer> checkParams(BeaconAlleleResponse result, List<String> datasetStableIds,
      VariantType type, String alternateBases, String referenceBases, String chromosome,
      Integer start, Integer startMin, Integer startMax, Integer end, Integer endMin,
      Integer endMax, String referenceGenome) {

    List<Integer> datasetIds = new ArrayList<Integer>();

    if (StringUtils.isBlank(chromosome) || StringUtils.isBlank(referenceGenome)) {
      Error error = Error.builder()
          .errorCode(ErrorCode.GENERIC_ERROR)
          .message("'referenceName' and/or 'assemblyId' are required")
          .build();
      result.setError(error);
      return datasetIds;
    }
    
    if (type == null && StringUtils.isBlank(alternateBases)) {
      Error error = Error.builder()
          .errorCode(ErrorCode.GENERIC_ERROR)
          .message("Either 'alternateBases' or 'variantType' is required")
          .build();
      result.setError(error);
    }
    
    if (start == null) {
      if(end != null) {
        Error error = Error.builder()
            .errorCode(ErrorCode.GENERIC_ERROR)
            .message("'start' is required if 'end' is provided")
            .build();
        result.setError(error);
        return datasetIds;
      } else if (startMin == null && startMax == null && endMin == null && endMax == null) {
        Error error = Error.builder()
            .errorCode(ErrorCode.GENERIC_ERROR)
            .message("Either 'start' or all of 'startMin', 'startMax', 'endMin' and 'endMax' are required")
            .build();
        result.setError(error);
        return datasetIds;
      } else if (startMin == null || startMax == null || endMin == null || endMax == null) {
        Error error = Error.builder()
            .errorCode(ErrorCode.GENERIC_ERROR)
            .message("All of 'startMin', 'startMax', 'endMin' and 'endMax' are required")
            .build();
        result.setError(error);
        return datasetIds;
      }
    } else if (startMin != null || startMax != null || endMin != null || endMax != null) {
      Error error = Error.builder()
          .errorCode(ErrorCode.GENERIC_ERROR)
          .message("'start' cannot be provided at the same time as 'startMin', 'startMax', 'endMin' and 'endMax'")
          .build();
      result.setError(error);
      return datasetIds;
    } else if (end == null && StringUtils.isBlank(referenceBases)) {
      Error error = Error.builder()
          .errorCode(ErrorCode.GENERIC_ERROR)
          .message("'referenceBases' is required if 'start' is provided and 'end' is missing")
          .build();
      result.setError(error);
      return datasetIds;
    }
    
    if (datasetStableIds != null) {
      // Remove empty/null strings
      datasetStableIds =
          datasetStableIds.stream().filter(s -> (StringUtils.isNotBlank(s)))
              .collect(Collectors.toList());
      
      for (String datasetStableId : datasetStableIds) {
        // 1) Dataset exists
        BeaconDataset dataset = beaconDatasetRepository.findByStableId(datasetStableId);
        if (dataset == null) {
          Error error = Error.builder()
              .errorCode(ErrorCode.NOT_FOUND)
              .message("Dataset not found")
              .build();
          result.setError(error);
          return datasetIds;
        } else {
          datasetIds.add(dataset.getId());
        }

        DatasetAccessType datasetAccessType = DatasetAccessType.parse(dataset.getAccessType());
        if (datasetAccessType != DatasetAccessType.PUBLIC) {
          Error error = Error.builder()
              .errorCode(ErrorCode.UNAUTHORIZED)
              .message("Unauthenticated users cannot access this dataset")
              .build();
          result.setError(error);
          return datasetIds;
        }

        // Check that the provided reference genome matches the one specified in the DB for this
        // dataset
        if (!StringUtils.equalsIgnoreCase(dataset.getReferenceGenome(), referenceGenome)) {
          Error error = Error.builder()
              .errorCode(ErrorCode.GENERIC_ERROR)
              .message("The reference genome of this dataset (" + datasetStableId
                  + ") does not match the provided value")
              .build();
          result.setError(error);
          return datasetIds;
        }
      }
    }
    // Allele has a valid value
    if (StringUtils.isNotBlank(alternateBases)) {
      boolean matches = Pattern.matches("[ACTG]+|(\\.){1}", alternateBases);
      if (!matches) {
        Error error = Error.builder().errorCode(ErrorCode.GENERIC_ERROR)
            .message("Invalid 'alternateBases' parameter, it must match the pattern [ACTG]+|(.){1}")
            .build();
        result.setError(error);
        return datasetIds;
      }
    }
    if (StringUtils.isNotBlank(referenceBases)) {
      boolean matches = Pattern.matches("[ACTG]+", referenceBases);
      if (!matches) {
        Error error = Error.builder().errorCode(ErrorCode.GENERIC_ERROR)
            .message("Invalid 'referenceBases' parameter, it must match the pattern [ACTG]+").build();
        result.setError(error);
        return datasetIds;
      }
    }
    
    return datasetIds;
  }

  private boolean queryDatabase(List<Integer> datasetIds, VariantType type, String referenceBases,
      String alternateBases, String chromosome, Integer start, Integer startMin, Integer startMax,
      Integer end, Integer endMin, Integer endMax, String referenceGenome,
      BeaconAlleleResponse result) {

    if (datasetIds == null || datasetIds.isEmpty()) {
      // Limit the query to only the authorized datasets
      datasetIds = findAuthorizedDatasets(referenceGenome);
    }

    long numResults = 0L;
    boolean globalExists = false;
    
    String variantType = type != null ? type.getType() : null;
    List<BeaconData> dataList = beaconDataRepository.searchForVariantsQuery(variantType, start,
        startMin, startMax, end, endMin, endMax, chromosome, referenceBases, alternateBases,
        referenceGenome, StoredProcedureUtils.joinArray(datasetIds));
    numResults = dataList.size();
    globalExists = numResults > 0;

    for (BeaconData data : dataList) {
      if (result.getAlleleRequest().getIncludeDatasetResponses() == FilterDatasetResponse.ALL
          || result.getAlleleRequest().getIncludeDatasetResponses() == FilterDatasetResponse.HIT) {
        DatasetAlleleResponse datasetResponse = new DatasetAlleleResponse();
        BeaconDataset dataset = beaconDatasetRepository.findOne(data.getDatasetId());
        datasetResponse.setDatasetId(dataset.getStableId());
        datasetResponse.setExists(true);
        datasetResponse.setFrequency(data.getFrequency());
        datasetResponse
            .setVariantCount(data.getVariantCnt() != null ? (long) data.getVariantCnt() : null);
        datasetResponse.setCallCount(data.getCallCnt() != null ? (long) data.getCallCnt() : null);
        datasetResponse
            .setSampleCount(data.getSampleCnt() != null ? (long) data.getSampleCnt() : null);
        result.addDatasetAlleleResponse(datasetResponse);
      }
    }

    List<Integer> datasetIdsWithData =
        dataList.stream().map(data -> data.getDatasetId()).collect(Collectors.toList());

    // Check that all requested datasets are present in the response
    // (maybe some of them are not present because they have no data for this query)
    @SuppressWarnings("unchecked")
    Collection<Integer> missingDatasets =
        CollectionUtils.disjunction(datasetIds, datasetIdsWithData);

    if (!missingDatasets.isEmpty() && (result.getAlleleRequest()
        .getIncludeDatasetResponses() == FilterDatasetResponse.MISS
        || result.getAlleleRequest().getIncludeDatasetResponses() == FilterDatasetResponse.ALL)) {
      for (Integer datasetId : missingDatasets) {
        DatasetAlleleResponse datasetResponse = new DatasetAlleleResponse();
        BeaconDataset dataset = beaconDatasetRepository.findOne(datasetId);
        datasetResponse.setDatasetId(dataset.getStableId());
        datasetResponse.setExists(false);
        result.addDatasetAlleleResponse(datasetResponse);
      }
    }
    return globalExists;
  }

  private List<Integer> findAuthorizedDatasets(String referenceGenome) {
    referenceGenome = StringUtils.lowerCase(referenceGenome);
    List<Integer> publicDatasets = beaconDatasetRepository
        .findByReferenceGenomeAndAccessType(referenceGenome, DatasetAccessType.PUBLIC.getType());
    return publicDatasets;
  }

  @Override
  public BeaconAlleleResponse queryBeacon(BeaconRequest request) {

    return queryBeacon(request.getDatasetIds(), request.getVariantType(),
        request.getAlternateBases(), request.getReferenceBases(), request.getReferenceName(),
        request.getStart(), request.getStartMin(), request.getStartMax(), request.getEnd(),
        request.getEndMin(), request.getEndMax(), request.getAssemblyId(),
        request.getIncludeDatasetResponses());
  }

}