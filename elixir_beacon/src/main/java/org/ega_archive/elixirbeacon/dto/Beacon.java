package org.ega_archive.elixirbeacon.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import org.ega_archive.elixirbeacon.constant.BeaconConstants;
import org.ega_archive.elixircore.constant.CoreConstants;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Beacon {
  
  // Unique identifier of the beacon
  private String id = BeaconConstants.BEACON_ID;
  
  // Name of the beacon
  private String name = BeaconConstants.BEACON_NAME;
  
  // Version of the API provided by the beacon
  private String apiVersion = BeaconConstants.API;
  
  // Organization owning the beacon
  private Organization organization = new Organization();
  
  // Description of the beacon
  private String description = BeaconConstants.BEACON_DESCRIPTION;
  
  //  Version of the beacon
  private String version = CoreConstants.API_VERSION;
  
  // URL to the welcome page/UI for this beacon
  private String welcomeUrl = BeaconConstants.BEACON_HOMEPAGE;
  
  // Alternative URL to the API, e.g. a restricted version of this beacon
  private String alternativeUrl = BeaconConstants.BEACON_ALTERNATIVE_URL;
  
  // The time the beacon was created (ISO 8601 format)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BeaconConstants.ISO8601_DATE_TIME_PATTERN)
  private DateTime createDateTime = BeaconConstants.BEACON_CREATED;
  
  // The time this beacon was last updated (ISO 8601 format)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BeaconConstants.ISO8601_DATE_TIME_PATTERN)
  private DateTime updateDateTime = BeaconConstants.BEACON_EDITED;
  
  // Datasets served by the beacon. Any beacon should specify at least one dataset.
  private List<Dataset> datasets;
  
  // Examples of interesting queries, e.g. a few queries demonstrating different responses.
  private List<BeaconAlleleRequest> sampleAlleleRequests;
  
  // Additional structured metadata, key-value pairs
  private Map<String, String> info;
  
}
