package org.ega_archive.elixirbeacon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Error {
  
  // Numeric status code
  private int errorCode;
  
  // Error message
  private String message;

}
