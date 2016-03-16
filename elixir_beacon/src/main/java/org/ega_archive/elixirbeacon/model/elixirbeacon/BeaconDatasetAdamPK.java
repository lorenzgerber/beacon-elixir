package org.ega_archive.elixirbeacon.model.elixirbeacon;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Embeddable
public class BeaconDatasetAdamPK implements Serializable {

  private static final long serialVersionUID = -898723960396707863L;

  private String attribute;

  @Column(name="dataset_id")
  private String datasetId;
  
}
