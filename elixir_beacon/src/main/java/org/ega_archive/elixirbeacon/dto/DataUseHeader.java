package org.ega_archive.elixirbeacon.dto;

import org.ega_archive.elixirbeacon.enums.DataUseDefinition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataUseHeader {
  
  private String name;
  
  private String version;
  
  private String furtherDetails;
  
  public DataUseHeader(DataUseDefinition definition) {
    name = definition.getName();
    version = definition.getVersion();
    furtherDetails = definition.getFurtherDetails();
  }

}
