package org.ega_archive.elixirbeacon.repository.elixirbeacon;

import org.ega_archive.elixirbeacon.model.elixirbeacon.BeaconData;
import org.ega_archive.elixircore.repository.CustomQuerydslJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeaconDataRepository extends CustomQuerydslJpaRepository<BeaconData, String> {

}
