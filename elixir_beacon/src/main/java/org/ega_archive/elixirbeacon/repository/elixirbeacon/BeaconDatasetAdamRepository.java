package org.ega_archive.elixirbeacon.repository.elixirbeacon;

import java.util.List;

import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDatasetAdam;
import org.ega_archive.elixircore.repository.CustomQuerydslJpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BeaconDatasetAdamRepository extends CustomQuerydslJpaRepository<BeaconDatasetAdam, String> {

  @Query("SELECT a FROM BeaconDatasetAdam a WHERE a.id.datasetId=?1")
  public List<BeaconDatasetAdam> findByDatasetId(String datasetId);
  
}
