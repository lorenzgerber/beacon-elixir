package org.ega_archive.elixirbeacon.dto;

import lombok.Data;

import org.ega_archive.elixirbeacon.enums.DataUseDefinition;

@Data
public class ConsentCodeDataUseProfile extends DataUseCondition {
  
  private ConsentCode profile;
 
  public ConsentCodeDataUseProfile() {
    header = new DataUseHeader(DataUseDefinition.CONSENT_CODE);
  }
  
}
