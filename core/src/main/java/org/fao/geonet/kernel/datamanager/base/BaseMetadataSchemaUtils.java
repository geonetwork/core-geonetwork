package org.fao.geonet.kernel.datamanager.base;

import java.nio.file.Path;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.context.ServiceContext;

public class BaseMetadataSchemaUtils implements IMetadataSchemaUtils {

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private MetadataRepository metadataRepository;

    public void init(ServiceContext context, Boolean force) throws Exception {
        schemaManager = context.getBean(SchemaManager.class);
        metadataRepository = context.getBean(MetadataRepository.class);
    }

    /**
     *
     * @param name
     * @return
     */
    @Override
    public MetadataSchema getSchema(String name) {
        return schemaManager.getSchema(name);
    }

    /**
     *
     * @return
     */
    @Override
    public Set<String> getSchemas() {
        return schemaManager.getSchemas();
    }

    /**
     *
     * @param name
     * @return
     */
    @Override
    public boolean existsSchema(String name) {
        return schemaManager.existsSchema(name);
    }

    /**
     *
     * @param name
     * @return
     */
    @Override
    public Path getSchemaDir(String name) {
        return schemaManager.getSchemaDir(name);
    }

    /**
     * TODO javadoc.
     */
    @Override
    public String getMetadataSchema(String id) throws Exception {
        IMetadata md = metadataRepository.findOne(id);

        if (md == null) {
            throw new IllegalArgumentException("Metadata not found for id : " + id);
        } else {
            // get metadata
            return md.getDataInfo().getSchemaId();
        }
    }

    /**
     * Checks autodetect elements in installed schemas to determine whether the metadata record belongs to that schema. Use this method when
     * you want the default schema from the geonetwork config to be returned when no other match can be found.
     *
     * @param md Record to checked against schemas
     */
    @Override
    public @CheckForNull String autodetectSchema(Element md) throws SchemaMatchConflictException, NoSchemaMatchesException {
        return autodetectSchema(md, schemaManager.getDefaultSchema());
    }

    /**
     * Checks autodetect elements in installed schemas to determine whether the metadata record belongs to that schema. Use this method when
     * you want to set the default schema to be returned when no other match can be found.
     *
     * @param md Record to checked against schemas
     * @param defaultSchema Schema to be assigned when no other schema matches
     */
    @Override
    public @CheckForNull String autodetectSchema(Element md, String defaultSchema)
            throws SchemaMatchConflictException, NoSchemaMatchesException {

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER,
                    "Autodetect schema for metadata with :\n * root element:'" + md.getQualifiedName() + "'\n * with namespace:'"
                            + md.getNamespace() + "\n * with additional namespaces:" + md.getAdditionalNamespaces().toString());
        String schema = schemaManager.autodetectSchema(md, defaultSchema);
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Schema detected was " + schema);
        return schema;
    }

}
