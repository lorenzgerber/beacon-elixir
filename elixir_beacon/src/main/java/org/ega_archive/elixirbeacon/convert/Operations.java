package org.ega_archive.elixirbeacon.convert;

import java.util.HashMap;
import java.util.Map;

import org.ega_archive.elixirbeacon.constant.BeaconConstants;
import org.ega_archive.elixirbeacon.dto.Dataset;
import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDataset;
import org.ega_archive.elixircore.enums.DatasetAccessType;

public class Operations {

  public static Dataset convert(BeaconDataset dataset, boolean authorized) {

    Dataset beaconDataset = new Dataset();
    beaconDataset.setId(dataset.getId());
    beaconDataset.setDescription(dataset.getDescription());
    beaconDataset.setVariantCount(new Long(dataset.getSize()));
    Map<String, String> info = new HashMap<String, String>();
    info.put(BeaconConstants.ACCESS_TYPE, DatasetAccessType.parse(dataset.getAccessType())
        .getType());
    info.put(BeaconConstants.AUTHORIZED, Boolean.toString(authorized));
    beaconDataset.setInfo(info);
    beaconDataset.setAssemblyId(dataset.getReferenceGenome());

    return beaconDataset;
  }

}
