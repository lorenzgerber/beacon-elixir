package org.ega_archive.elixirbeacon.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

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
  
  // Data use conditions for this dataset
  private List<DataUseCondition> dataUseConditions;
  
  // The time the dataset was created in the beacon in ms from the epoch
  private Long created;
  
  // The time the dataset was last updated in the beacon in ms from the epoch
  private Long updated;
  
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
  
  public void addDataUseCondition(DataUseCondition condition) {
    if(dataUseConditions == null) {
      dataUseConditions = new ArrayList<DataUseCondition>();
    }
    dataUseConditions.add(condition);
  }
  
}
