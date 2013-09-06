package org.fao.geonet.repository;

import org.fao.geonet.domain.IsoLanguage;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link IsoLanguage} entities.
 *
 * @author Jesse
 */
public interface IsoLanguageRepository extends GeonetRepository<IsoLanguage, Integer>, JpaSpecificationExecutor<IsoLanguage> {
    /**
     * Find all the IsoLanguages based on the code (the longer code).
     *
     * @param code the required code
     * @return all the IsoLanguages based on the code (the longer code).
     */
    List<IsoLanguage> findAllByCode(String code);

    /**
     * Find all the IsoLanguages based on the short code.
     *
     * @param code the required code
     * @return all the IsoLanguages based on the short code.
     */
    List<IsoLanguage> findAllByShortCode(String code);
}
