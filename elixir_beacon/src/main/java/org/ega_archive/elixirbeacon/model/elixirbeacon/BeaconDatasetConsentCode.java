package org.ega_archive.elixirbeacon.model.elixirbeacon;

import java.io.Serializable;

import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * The persistent class for the beacon_dataset_consent_code database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="beacon_dataset_consent_code")
@NamedQuery(name="BeaconDatasetConsentCode.findAll", query="SELECT b FROM BeaconDatasetConsentCode b")
public class BeaconDatasetConsentCode implements Serializable {
  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private BeaconDatasetConsentCodePK id;

  private String category;
  
  private String description;
  
  private String detail;
  
}