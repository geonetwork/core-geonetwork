//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

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
