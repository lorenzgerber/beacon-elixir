package org.ega_archive.elixirbeacon.model.elixirbeacon;

import java.io.Serializable;

import javax.persistence.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * The primary key class for the beacon_data database table.
 * 
 */
@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class BeaconDataPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="dataset_id", insertable=false, updatable=false)
	private String datasetId;

	private String chromosome;

	private Integer position;

	@Column(name="alternate")
	private String alternateBases;
	
	@Column(name="reference_genome")
	private String referenceGenome;

	public BeaconDataPK() {
	}

}