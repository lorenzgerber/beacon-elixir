package org.ega_archive.elixirbeacon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataUseCondition {
  
  protected DataUseHeader header;
  
}
