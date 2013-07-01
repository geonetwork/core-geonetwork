package org.fao.geonet.repository;

import org.fao.geonet.domain.IsoLanguages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link IsoLanguages} entities.
 * 
 * @author Jesse
 */
public interface IsoLanguagesRepository extends JpaRepository<IsoLanguages, Integer>, JpaSpecificationExecutor<IsoLanguages> {
}
