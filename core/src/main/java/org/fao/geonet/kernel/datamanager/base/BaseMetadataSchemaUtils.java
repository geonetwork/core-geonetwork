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

package org.fao.geonet.kernel.datamanager.base;

import java.nio.file.Path;
import java.util.Set;

import jakarta.annotation.CheckForNull;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.context.ServiceContext;

public class BaseMetadataSchemaUtils implements IMetadataSchemaUtils {

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private IMetadataUtils metadataUtils;

    /**
     * @param name
     * @return
     */
    @Override
    public MetadataSchema getSchema(String name) {
        return schemaManager.getSchema(name);
    }

    /**
     * @return
     */
    @Override
    public Set<String> getSchemas() {
        return schemaManager.getSchemas();
    }

    /**
     * @param name
     * @return
     */
    @Override
    public boolean existsSchema(String name) {
        return schemaManager.existsSchema(name);
    }

    /**
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
        AbstractMetadata md = metadataUtils.findOne(id);

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
    public @CheckForNull
    String autodetectSchema(Element md) throws SchemaMatchConflictException, NoSchemaMatchesException {
        return autodetectSchema(md, schemaManager.getDefaultSchema());
    }

    /**
     * Checks autodetect elements in installed schemas to determine whether the metadata record belongs to that schema. Use this method when
     * you want to set the default schema to be returned when no other match can be found.
     *
     * @param md            Record to checked against schemas
     * @param defaultSchema Schema to be assigned when no other schema matches
     */
    @Override
    public @CheckForNull
    String autodetectSchema(Element md, String defaultSchema)
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
