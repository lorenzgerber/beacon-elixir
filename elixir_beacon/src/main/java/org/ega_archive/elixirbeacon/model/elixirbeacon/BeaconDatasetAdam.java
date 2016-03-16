package org.ega_archive.elixirbeacon.model.elixirbeacon;

import java.io.Serializable;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the beacon_dataset_adam database table.
 * 
 */
@Getter
@Setter
@Entity
@Table(name="beacon_dataset_adam")
@NamedQuery(name="BeaconDatasetAdam.findAll", query="SELECT b FROM BeaconDatasetAdam b")
public class BeaconDatasetAdam implements Serializable {
  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private BeaconDatasetAdamPK id;

  private String value;
  
}