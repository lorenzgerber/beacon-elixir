package org.ega_archive.elixirbeacon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyValuePair {

  private String key;
  private String value;

}
