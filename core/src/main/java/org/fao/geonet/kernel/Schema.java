/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel;

import jeeves.server.dispatchers.guiservices.XmlFile;

import org.fao.geonet.kernel.schema.MetadataSchema;

import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A bean representing the basic information for a Schema.
 *
 * @author Simon Pigot
 */
public class Schema {

    private String id;
    private String version;
    private String schemaLocation;
    private List<Element> autodetectElements = new ArrayList<Element>();
    private Map<String, XmlFile> schemaInfo = new HashMap<String, XmlFile>();
    private Path dir;
    private MetadataSchema mds;
    private SchemaSuggestions sugg;
    private boolean plugin = false;
    private List<Element> conversionElements = new ArrayList<Element>();
    private List<Element> dependElements = new ArrayList<Element>();

    /**
     * Get id of this schema.
     */
    public String getId() {
        return id;
    }

    /**
     * Set id of this schema.
     *
     * @param id UUID of this schema
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get version of this schema.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set version of this schema.
     *
     * @param version version string of this schema
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get schemaLocation = official URLs which locate xsds for the various namespaces used by this
     * schema.
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    /**
     * Set schemaLocation = official URLs which locate xsds for the various namespaces used by this
     * schema.
     *
     * @param schemaLocation namespaces and URLs of XSDs arranged in pairs
     */
    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    /**
     * Get List of XML elements that describes how to detect metadata records that belong to this
     * schema.
     */
    public List<Element> getAutodetectElements() {
        return autodetectElements;
    }

    /**
     * Set List of XML elements that describes how to detect metadata records that belong to this
     * schema.
     *
     * @param autodetectElements List of JDOM elements
     */
    public void setAutodetectElements(List<Element> autodetectElements) {
        this.autodetectElements = autodetectElements;
    }

    /**
     * Get map of localized xml files describing schema elements - at present these are labels,
     * codelists and strings and are hashed on the name of the localized file.
     */
    public Map<String, XmlFile> getInfo() {
        return schemaInfo;
    }

    /**
     * Set map of localized xml files describing schema elements - at present these are labels,
     * codelists and strings and are hashed on the name of the localized file.
     *
     * @param schemaInfo Map containing schema information for each file
     */
    public void setInfo(Map<String, XmlFile> schemaInfo) {
        this.schemaInfo = schemaInfo;
    }

    /**
     * Get file system directory in which schema resides - used by methods that need to access xslt
     * files in the schema directory.
     */
    public Path getDir() {
        return dir;
    }

    /**
     * Set file system directory in which schema resides.
     *
     * @param dir file system directory in which schema resides
     */
    public void setDir(Path dir) {
        this.dir = dir;
    }

    ;

    /**
     * Get MetadataSchema object that describes the XML schema for the editor.
     */
    public MetadataSchema getMetadataSchema() {
        return mds;
    }

    /**
     * Set MetadataSchema object that describes the XML schema for the editor.
     *
     * @param mds the MetadataSchema object describing the XML schema
     */
    public void setMetadataSchema(MetadataSchema mds) {
        this.mds = mds;
    }

    /**
     * Get SchemaSuggestions object that describes the suggested elements for this schema (used by
     * the editor).
     */
    public SchemaSuggestions getSuggestions() {
        return sugg;
    }

    /**
     * Set SchemaSuggestions object that describes the suggested elements for this schema (used by
     * the editor).
     *
     * @param sugg the SchemaSuggestions object describing the schema suggestions
     */
    public void setSuggestions(SchemaSuggestions sugg) {
        this.sugg = sugg;
    }

    /**
     * Get boolean which states whether the schema is a plugin schema or not.
     */
    public boolean isPluginSchema() {
        return plugin;
    }

    /**
     * Set boolean which states whether the schema is a plugin schema or not.
     *
     * @param plugin boolean set to true if schema is a plugin schema
     */
    public void setPluginSchema(boolean plugin) {
        this.plugin = plugin;
    }

    /**
     * Get List of XML elements that describes how to convert metadata records that belong to this
     * schema to other schemas.
     */
    public List<Element> getConversionElements() {
        return conversionElements;
    }

    /**
     * Set List of XML elements that describes how to convert metadata records that belong to this
     * schema to other schemas.
     *
     * @param conversionElements List of JDOM elements
     */
    public void setConversionElements(List<Element> conversionElements) {
        this.conversionElements = conversionElements;
    }

    /**
     * Get List of XML elements whose text values are the names of schemas that this schema depends
     * upon.
     */
    public List<Element> getDependElements() {
        return dependElements;
    }

    /**
     * Set List of XML elements whose text values are the names of schemas that this schema depends
     * upon.
     *
     * @param dependElements of JDOM elements
     */
    public void setDependElements(List<Element> dependElements) {
        this.dependElements = dependElements;
    }

}
