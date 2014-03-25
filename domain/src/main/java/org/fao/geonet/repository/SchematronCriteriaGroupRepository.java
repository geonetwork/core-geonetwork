package org.fao.geonet.repository;

import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronCriteriaGroupId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Data Access object for the {@link org.fao.geonet.domain.Schematron} entities.
 *
 * @author delawen
 */
public interface SchematronCriteriaGroupRepository extends
        GeonetRepository<SchematronCriteriaGroup, SchematronCriteriaGroupId>,
		JpaSpecificationExecutor<SchematronCriteriaGroup> {
    /**
     * Look up a schematrons by its schema
     *
     * @param schematronId
     *            the id of the schematron
     */
    @Nonnull
    public List<SchematronCriteriaGroup> findAllById_SchematronId(@Nonnull int schematronId);

}
