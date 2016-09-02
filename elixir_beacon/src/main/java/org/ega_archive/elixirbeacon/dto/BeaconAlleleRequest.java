package org.ega_archive.elixirbeacon.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

@JsonPropertyOrder({"alternateBases", "referenceBases", "chromosome", "start", "assemblyId",
    "datasetIds", "includeDatasetResponses"})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BeaconAlleleRequest {

  // The bases that appear instead of the reference bases
  private String alternateBases;

  // Reference bases for this variant, starting from `position`
  private String referenceBases;

  // Chromosome identifier. Accepted values: 1-22, X, Y
  @JsonProperty("referenceName")
  private String chromosome;

  // Position, allele locus (0-based)
  private Integer start;

  // Assembly identifier, e.g. `GRCh37
  private String assemblyId;

  // Identifiers of datasets, as defined in `BeaconDataset`. If this field is null/not specified,
  // all datasets should be queried.
  private List<String> datasetIds;

  // Indicator of whether responses for individual datasets (`datasetAlleleResponses`) should be
  // included (not null) in the response (`BeaconAlleleResponse`) to this request. If null (not
  // specified), the default value of false is assumed.
  private boolean includeDatasetResponses;

}
