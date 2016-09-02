package org.ega_archive.elixirbeacon.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import org.ega_archive.elixirbeacon.constant.BeaconConstants;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {

  // Unique identifier of the dataset
  private String id;

  // Name of the dataset
  private String name;

  // Description of the dataset
  private String description;

  // Assembly identifier, e.g. `GRCh37`
  private String assemblyId;

  // The time the dataset was created (ISO 8601 format)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BeaconConstants.ISO8601_DATE_TIME_PATTERN)
  private DateTime createDateTime;

  // The time the dataset was updated in (ISO 8601 format)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BeaconConstants.ISO8601_DATE_TIME_PATTERN)
  private DateTime updateDateTime;

  // Version of the dataset
  private String version;

  // Total number of variants in the dataset
  private Long variantCount;

  // Total number of calls in the dataset
  private Long callCount;

  // Total number of samples in the dataset
  private Long sampleCount;

  // URL to an external system providing more dataset information.
  private String externalUrl;

  // Additional structured metadata, key-value pairs.
  private Map<String, String> info;

}
