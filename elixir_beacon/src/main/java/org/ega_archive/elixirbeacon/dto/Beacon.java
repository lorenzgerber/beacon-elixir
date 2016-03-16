package org.ega_archive.elixirbeacon.dto;

import java.util.List;
import java.util.Map;

import org.ega_archive.elixirbeacon.constant.BeaconConstants;
import org.ega_archive.elixircore.constant.CoreConstants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

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
  
  // The time this beacon was created in ms from the epoch
  private Long created = BeaconConstants.BEACON_CREATED;
  
  // The time this beacon was last updated in ms from the epoch
  private Long updated = BeaconConstants.BEACON_EDITED;
  
  // Datasets served by the beacon. Any beacon should specify at least one dataset.
  private List<Dataset> datasets;
  
  // Examples of interesting queries, e.g. a few queries demonstrating different responses.
  private List<BeaconAlleleRequest> sampleAlleleRequests;
  
  // Additional structured metadata, key-value pairs
  private Map<String, String> info;
  
}
