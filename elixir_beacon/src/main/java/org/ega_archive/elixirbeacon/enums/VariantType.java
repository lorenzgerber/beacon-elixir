package org.ega_archive.elixirbeacon.enums;

import org.apache.commons.lang3.StringUtils;
import org.ega_archive.elixircore.exception.TypeNotFoundException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VariantType {

  INSERTION("INS"), 
  DELELETION("DEL"), 
  DUPLICATION("DUP");

  private String type;

  public static VariantType parse(String value) {
    VariantType type = null;

    if (StringUtils.isNotBlank(value)) {
      if (value.equalsIgnoreCase(INSERTION.type)) {
        type = INSERTION;
      } else if (value.equalsIgnoreCase(DELELETION.type)) {
        type = DELELETION;
      } else if (value.equalsIgnoreCase(DUPLICATION.type)) {
        type = DUPLICATION;
      } else {
        throw new TypeNotFoundException("Structural variant type not implemented yet", value);
      }
    }
    return type;
  }

}
