package org.ega_archive.elixirbeacon.model.elixirbeacon;

import java.io.Serializable;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the beacon_data database table.
 * 
 */
@Getter
@Setter
@Entity
@Table(name="beacon_data")
@NamedQuery(name="BeaconData.findAll", query="SELECT e FROM BeaconData e")
public class BeaconData implements Serializable {
	private static final long serialVersionUID = 1L;

    @EmbeddedId
    private BeaconDataPK id;

	public BeaconData() {
	}

}