package org.fao.geonet.repository;

import org.fao.geonet.domain.Schematron;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for the {@link org.fao.geonet.domain.Schematron} entities.
 * 
 * @author delawen
 */
public interface SchematronRepository extends
        GeonetRepository<Schematron, Integer>,
		JpaSpecificationExecutor<Schematron> {
    /**
     * Look up a schematrons by its schema
     *
     * @param schemaName
     *            the name of the schema
     */
    public List<Schematron> findAllBySchemaName(String schemaName);
    /**
     * Look up a schematrons by its file
     *
     * @param file
     *            path from schema directory to the file
     * @param schemaName
     *            name of the schema
     * @param
     */
    public Schematron findOneByFileAndSchemaName(String file, String schemaName);

}
