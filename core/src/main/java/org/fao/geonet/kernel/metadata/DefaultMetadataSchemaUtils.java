/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.nio.file.Path;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public class DefaultMetadataSchemaUtils implements IMetadataSchemaUtils {
    
    @Autowired
    private MetadataRepository mdRepository;
    
    private SchemaManager schemaManager;

    /**
     * @param schemaManager
     *            the schemaManager to set
     */
    @Autowired
    public void setSchemaManager(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataSchemaUtils#autodetectSchema(org.jdom.Element)
     * @param md
     * @return
     * @throws SchemaMatchConflictException
     * @throws NoSchemaMatchesException
     */
    @Override
    public @CheckForNull String autodetectSchema(Element md)
            throws SchemaMatchConflictException, NoSchemaMatchesException {
        return autodetectSchema(md, schemaManager.getDefaultSchema());
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataSchemaUtils#getMetadataSchema(java.lang.String)
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public String getMetadataSchema(String id) throws Exception {
        Metadata md = mdRepository.findOne(id);

        if (md == null) {
            throw new IllegalArgumentException(
                    "Metadata not found for id : " + id);
        } else {
            // get metadata
            return md.getDataInfo().getSchemaId();
        }
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataSchemaUtils#autodetectSchema(org.jdom.Element,
     *      java.lang.String)
     * @param md
     * @param defaultSchema
     * @return
     * @throws SchemaMatchConflictException
     * @throws NoSchemaMatchesException
     */
    @Override
    public @CheckForNull String autodetectSchema(Element md,
            String defaultSchema) throws SchemaMatchConflictException,
                    NoSchemaMatchesException {

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER,
                    "Autodetect schema for metadata with :\n * root element:'"
                            + md.getQualifiedName() + "'\n * with namespace:'"
                            + md.getNamespace()
                            + "\n * with additional namespaces:"
                            + md.getAdditionalNamespaces().toString());
        String schema = schemaManager.autodetectSchema(md, defaultSchema);
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Schema detected was " + schema);
        return schema;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataSchemaUtils#getSchema(java.lang.String)
     * @param name
     * @return
     */
    @Override
    public MetadataSchema getSchema(String name) {
        return schemaManager.getSchema(name);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataSchemaUtils#getSchemas()
     * @return
     */
    @Override
    public Set<String> getSchemas() {
        return schemaManager.getSchemas();
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataSchemaUtils#existsSchema(java.lang.String)
     * @param name
     * @return
     */
    @Override
    public boolean existsSchema(String name) {
        return schemaManager.existsSchema(name);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataSchemaUtils#getSchemaDir(java.lang.String)
     * @param name
     * @return
     */
    @Override
    public Path getSchemaDir(String name) {
        return schemaManager.getSchemaDir(name);
    }

}
