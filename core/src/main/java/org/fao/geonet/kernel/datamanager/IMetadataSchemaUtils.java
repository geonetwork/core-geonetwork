package org.fao.geonet.kernel.datamanager;

import java.nio.file.Path;
import java.util.Set;

import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;

/**
 * Utility interface for schemas
 * 
 * @author delawen
 *
 */
public interface IMetadataSchemaUtils {

    /**
     * This is a hopefully soon to be deprecated initialization function to replace the @Autowired annotation
     * 
     * @param context
     * @param force
     * @throws Exception
     */
    public void init(ServiceContext context, Boolean force) throws Exception;

    /**
     * Returns the schema folder path
     * 
     * @param name
     * @return
     */
    Path getSchemaDir(String name);

    /**
     * Checks if a schema exists on the platform
     * 
     * @param name
     * @return
     */
    boolean existsSchema(String name);

    /**
     * Returns the full list of schemas available on the platform
     * 
     * @return
     */
    Set<String> getSchemas();

    /**
     * Given a schema name, returns the schema
     * 
     * @param name
     * @return
     */
    MetadataSchema getSchema(String name);

    /**
     * Given a record id, returns the name of the schema
     * 
     * @param id
     * @return
     * @throws Exception
     */
    String getMetadataSchema(String id) throws Exception;

    /**
     * Checks autodetect elements in installed schemas to determine whether the metadata record belongs to that schema. Use this method when
     * you want the default schema from the geonetwork config to be returned when no other match can be found.
     *
     * @param md Record to checked against schemas
     */
    String autodetectSchema(Element md) throws SchemaMatchConflictException, NoSchemaMatchesException;

    /**
     * Checks autodetect elements in installed schemas to determine whether the metadata record belongs to that schema. Use this method when
     * you want to set the default schema to be returned when no other match can be found.
     *
     * @param md Record to checked against schemas
     * @param defaultSchema Schema to be assigned when no other schema matches
     */
    String autodetectSchema(Element md, String defaultSchema) throws SchemaMatchConflictException, NoSchemaMatchesException;
}
