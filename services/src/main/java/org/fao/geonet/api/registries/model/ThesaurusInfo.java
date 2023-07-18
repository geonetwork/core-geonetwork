/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.api.registries.model;

import java.util.Map;

/**
 * Thesaurus information dto class.
 */
public class ThesaurusInfo {
    private String filename;
    private String description;
    private Map<String, String> multilingualTitles;

    private Map<String, String> multilingualDescriptions;
    private String title;
    private String defaultNamespace;
    private String dname;
    private String type;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename.trim().replaceAll("\\s+", "");

        if (!this.filename.endsWith(".rdf")) {
            this.filename = filename + ".rdf";
        }

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getMultilingualTitles() {
        return multilingualTitles;
    }

    public void setMultilingualTitles(Map<String, String> multilingualTitles) {
        this.multilingualTitles = multilingualTitles;
    }

    public Map<String, String> getMultilingualDescriptions() {
        return multilingualDescriptions;
    }

    public void setMultilingualDescriptions(Map<String, String> multilingualDescriptions) {
        this.multilingualDescriptions = multilingualDescriptions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
