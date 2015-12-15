/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.nio.file.Path;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;

/**
 * Addon to {@link DataManager} to handle schema metadata actions
 * 
 * @author delawen
 * 
 * 
 */
public interface IMetadataSchemaUtils {
    
    /**
     *FIXME
     * To remove when Spring autowiring works right
     * @param context
     */
    public void init(ServiceContext context);
    /**
     * Checks autodetect elements in installed schemas to determine whether the
     * metadata record belongs to that schema. Use this method when you want the
     * default schema from the geonetwork config to be returned when no other
     * match can be found.
     *
     * @param md
     *            Record to checked against schemas
     * @throws SchemaMatchConflictException
     * @throws NoSchemaMatchesException
     * @return
     */
    public @CheckForNull String autodetectSchema(Element md)
            throws SchemaMatchConflictException, NoSchemaMatchesException;

    /**
     * Checks autodetect elements in installed schemas to determine whether the
     * metadata record belongs to that schema. Use this method when you want to
     * set the default schema to be returned when no other match can be found.
     *
     * @param md
     *            Record to checked against schemas
     * @param defaultSchema
     *            Schema to be assigned when no other schema matches
     * @throws SchemaMatchConflictException
     * @throws NoSchemaMatchesException
     * @return
     */
    public @CheckForNull String autodetectSchema(Element md,
            String defaultSchema) throws SchemaMatchConflictException,
                    NoSchemaMatchesException;


    /**
     * TODO javadoc.
     *
     * @param id
     * @return
     * @throws Exception
     */
    public String getMetadataSchema(String id) throws Exception;
    
    /**
     *
     * @param name
     * @return
     */
    public MetadataSchema getSchema(String name);

    /**
     *
     * @return
     */
    public Set<String> getSchemas();

    /**
     *
     * @param name
     * @return
     */
    public boolean existsSchema(String name);

    /**
     *
     * @param name
     * @return
     */
    public Path getSchemaDir(String name);
}