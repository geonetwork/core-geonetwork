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

package org.fao.geonet.kernel.schema;

import com.google.common.collect.ImmutableSet;
import org.jdom.Element;
import org.jdom.Namespace;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

public abstract class SchemaPlugin implements CSWPlugin {
    public static final String LOGGER_NAME = "geonetwork.schema-plugin";

    /**
     * List of output schemas supported by the CSW for this plugin.
     * The key correspond to the XSLT filename to use for the corresponding value (usually URI).
     * XSLT are in the folder present/csw/{key-?(brief|summary|full)?}.xsl
     */
    private Map<String, String> outputSchemas = new HashMap<>();

    protected SchemaPlugin(String identifier,
                           ImmutableSet<Namespace> allNamespaces) {
        this.identifier = identifier;
        this.allNamespaces = allNamespaces;
    }

    public final String identifier;

    public String getIdentifier() {
        return identifier;
    }

    private List<SavedQuery> savedQueries = new ArrayList<>();

    public List<SavedQuery> getSavedQueries() {
        return savedQueries;
    }

    public void setSavedQueries(List<SavedQuery> savedQueries) {
        this.savedQueries = savedQueries;
    }

    public
    @Nullable
    SavedQuery getSavedQuery(@Nonnull String queryKey) {
        Iterator<SavedQuery> iterator = this.getSavedQueries().iterator();
        while (iterator.hasNext()) {
            SavedQuery query = iterator.next();
            if (queryKey.equals(query.getId())) {
                return query;
            }
        }
        return null;
    }

    private ImmutableSet<Namespace> allNamespaces;

    public Set<Namespace> getNamespaces() {
        return allNamespaces;
    }

    private List<String> xpathTitle;

    public void setXpathTitle(List<String> xpathTitle) {
        this.xpathTitle = xpathTitle;
    }

    public List<String> getXpathTitle() {
        return xpathTitle;
    }

    protected List<String> elementsToProcess = new ArrayList<>();

    public void setElementsToProcess(List<String> elementsToProcess) {
        this.elementsToProcess = elementsToProcess;
    }

    /**
     Links to analyze in a metadata record
     */
    protected List<HttpLink> analyzedLinks;

    public void setAnalyzedLinks(List<HttpLink> analyzedLinks) {
        this.analyzedLinks = analyzedLinks;
    }

    public List<HttpLink> getAnalyzedLinks() {
        return analyzedLinks;
    }


    /**
     * Processes the passed element. This base class just return the same element without modifications
     * but can be overridden in a schema plugin in order to modify an element
     * by one of its substitutes.
     *
     * @param el element to process.
     * @param attributeName
     * @param parsedAttributeName the name of the attribute, for example <code>xlink:href</code>
     * @param attributeValue
     *
     * @return the processed element.
     */
    public Element processElement(Element el, String attributeName, String parsedAttributeName, String attributeValue) {
        return el;
    };

    public Map<String, String> getOutputSchemas() {
        return outputSchemas;
    }

    public void setOutputSchemas(Map<String, String> outputSchemas) {
        this.outputSchemas = outputSchemas;
    }
}
