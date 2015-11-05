package org.fao.geonet.repository;

import org.fao.geonet.domain.File;
import org.fao.geonet.domain.Group;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Access object for the {@link File} entities.
 *
 * @author Julien Acroute
 */
public interface FileRepository extends GeonetRepository<File, Integer> {}
