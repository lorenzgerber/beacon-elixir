package org.ega_archive.elixirbeacon.dto;

import java.util.List;

import org.ega_archive.elixirbeacon.enums.FilterDatasetResponse;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"alternateBases", "referenceBases", "referenceName", "start", "startMin",
    "startMax", "end", "endMin", "endMax", "variantType", "assemblyId", "datasetIds",
    "includeDatasetResponses"})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BeaconAlleleRequest {

  // The bases that appear instead of the reference bases
  // Accepted values: see the ALT field in VCF 4.2 specification
  // (https://samtools.github.io/hts-specs/VCFv4.2.pdf).    
  private String alternateBases;

  // Reference bases for this variant (starting from `start`). OPTIONAL.
  // Accepted values: see the REF field in VCF 4.2 specification
  // (https://samtools.github.io/hts-specs/VCFv4.2.pdf).
  private String referenceBases;

  // Chromosome identifier. Accepted values: 1-22, X, Y
  private String referenceName;

  // Position, genomic locus (0-based).
  //
  // start only:
  // - for single positions, e.g. the start of a specified sequence alteration
  // where the size is given through the specified alternate_bases
  // - typical use are queries for SNV and small InDels
  // - the use of "start" without an "end" parameter requires the use of
  // "reference_bases"
  //
  // start and end:
  // - special use case for exactly determined structural changes
  //
  // start_min + start_max + end_min + end_max
  // - for querying imprecise positions (e.g. identifying all structural
  // variants starting anywhere between start_min <-> start_max, and ending
  // anywhere between end_min <-> end_max
  // - single or double sided precise matches can be achieved by setting
  // start_min = start_max XOR end_min = end_max
  //
  private Integer start;

  private Integer startMin;

  private Integer startMax;

  private Integer end;

  private Integer endMin;

  private Integer endMax;
  
  // The "variant_type" is used to denote e.g. structural variants.
  // Examples:
  // DUP : duplication of sequence following "start"; not necessarily in situ
  // DEL : deletion of sequence following "start"
  // Optional (either "alternate_bases" or "variant_type" is required)
  private String variantType;

  // Assembly identifier (GRC notation, e.g. `GRCh37`).
  private String assemblyId;

  // Identifiers of datasets, as defined in `BeaconDataset`.
  // If this field is null/not specified, all datasets should be queried.
  private List<String> datasetIds;

  // Indicator of whether responses for individual datasets
  // (`datasetAlleleResponses`) should be included in the response
  // (`BeaconAlleleResponse`) to this request or not.
  private FilterDatasetResponse includeDatasetResponses;

}
