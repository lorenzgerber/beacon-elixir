package org.ega_archive.elixirbeacon.repository.elixirbeacon;

import java.util.List;

import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconDataset;
import org.ega_archive.elixircore.repository.CustomQuerydslJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BeaconDatasetRepository extends CustomQuerydslJpaRepository<BeaconDataset, Integer> {
  
  public Page<BeaconDataset> findByReferenceGenome(String referenceGenome, Pageable page);
  
  public BeaconDataset findByStableId(String stableId);
  
  @Query("SELECT d.id FROM BeaconDataset d WHERE d.accessType=?1 ORDER BY d.id")
  public List<Integer> findByAccessType(String accessType);
  
  @Query("SELECT d.id FROM BeaconDataset d WHERE d.referenceGenome=?1 AND d.accessType=?2 ORDER BY d.id")
  public List<Integer> findByReferenceGenomeAndAccessType(String referenceGenome, String accessType);

}
