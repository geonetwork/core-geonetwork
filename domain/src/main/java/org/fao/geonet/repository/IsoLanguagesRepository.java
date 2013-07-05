package org.fao.geonet.repository;

import org.fao.geonet.domain.IsoLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link IsoLanguage} entities.
 * 
 * @author Jesse
 */
public interface IsoLanguagesRepository extends JpaRepository<IsoLanguage, Integer>, JpaSpecificationExecutor<IsoLanguage> {
}
