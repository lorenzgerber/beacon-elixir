package org.ega_archive.elixirbeacon.dto;

import java.util.Map;

import lombok.Data;

import org.ega_archive.elixirbeacon.enums.DataUseDefinition;

@Data
public class AdamDataUseProfile extends DataUseCondition {
  
  private Map<String, String> profile;
  
  public AdamDataUseProfile() {
    header = new DataUseHeader(DataUseDefinition.ADAM);
  }
  
}
