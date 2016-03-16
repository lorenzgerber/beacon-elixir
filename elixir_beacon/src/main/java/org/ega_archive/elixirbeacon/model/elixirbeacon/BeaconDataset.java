package org.ega_archive.elixirbeacon.model.elixirbeacon;

import java.io.Serializable;

import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * The persistent class for the beacon_dataset database table.
 * 
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="beacon_dataset")
@NamedQuery(name="BeaconDataset.findAll", query="SELECT e FROM BeaconDataset e")
public class BeaconDataset implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name="access_type")
	private String accessType;

	private String description;

	@Id
	private String id;

	@Column(name="reference_genome")
	private String referenceGenome;

	private Integer size;

}