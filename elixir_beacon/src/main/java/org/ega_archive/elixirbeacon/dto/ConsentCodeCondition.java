package org.ega_archive.elixirbeacon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentCodeCondition {
  
  // Consent code abbreviation, e.g. `NRES` for no restrictions primary category.
  private String code;
  
  // Description of the condition.
  private String description;
  
  @JsonInclude(Include.NON_EMPTY)
  private String details;

}
