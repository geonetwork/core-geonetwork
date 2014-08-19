//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.reusable;

import com.google.common.base.Function;
import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.jdom.Element;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.apache.lucene.search.WildcardQuery.WILDCARD_STRING;

public abstract class ReplacementStrategy implements FindMetadataReferences {
    static final Pair<Collection<Element>, Boolean> NULL = Pair.read((Collection<Element>) Collections
            .<Element>emptySet(), false);
    public static final String REPORT_ROOT = "records";
    static final String REPORT_ELEMENT = "record";
    static final String REPORT_DESC = "desc";
    static final String REPORT_URL = "url";
    public static final String REPORT_ID = "id";
    public static final String REPORT_XLINK = "xlink";
    public static final String REPORT_TYPE = "type";
    public static final String REPORT_SEARCH = "search";
    public static final Function<String, String> ID_FUNC = new Function<String, String>() {

        public String apply(String s) {
            return s;
        }
    };

    /**
     * try to find a match for the original object
     *
     * @return returns a pair with the collection of replaced elements and a true if all elements were matched.  If false
     * is returned then some elements were found but others need to be added
     */
    public abstract Pair<Collection<Element>, Boolean> find(Element placeholder, Element originalElem, String defaultMetadataLang)
            throws Exception;

    /**
     * Adds the object as a non_validated reusable object and returns the
     * Element(s) to add to the metadata in place of the originalElem
     *
     * @param placeholder TODO
     */
    public abstract Collection<Element> add(Element placeholder, Element originalElem, Dbms dbms, String metadataLanguage)
            throws Exception;

    /**
     * Construct a list of the non_validated objects
     */
    public abstract Element find(UserSession session, boolean validated) throws Exception;

    /**
     * Deletes the objects. No other function
     *
     * @param ids                  ids to delete
     * @param session              TODO
     * @param strategySpecificData indicates the source to delete from. If null then assume
     *                             non_valid source
     */
    public abstract void performDelete(String[] ids, Dbms dbms, UserSession session, String strategySpecificData) throws Exception;

    /**
     * Constructs an xlink href for the id. It should be as simple as possible
     * because it is only for matching an xlink within the document so portions
     * that are not part of identifying the object should be left off. For
     * example contacts have which schema to display the xlink in. This should
     * be left off.
     *
     * @param strategySpecificData indicates the source to delete from. If null then assume
     *                             non_valid source
     */
    public abstract String createXlinkHref(String id, UserSession session, String strategySpecificData) throws Exception;

    /**
     * Mark the reusable objects identified by the ids as validated
     *
     * @return returns a map of oldId -> newId
     */
    public abstract Map<String, String> markAsValidated(String[] ids, Dbms dbms, UserSession session) throws Exception;

    /**
     * Update the non_validated ref to the validated ref.
     *
     * @param id      TODO
     * @param session TODO
     * @throws java.io.UnsupportedEncodingException
     */
    public abstract String updateHrefId(String oldHref, String id, UserSession session) throws UnsupportedEncodingException;

    /**
     * Updates the SharedObject with the contents of the object below
     *
     * @param xlink new data
     * @return elements that were added to the xlink but are not supported by
     * the xlink and must be added as a sibling to the xlink object.
     */
    public abstract Collection<Element> updateObject(Element xlink, Dbms dbms, String metadataLang) throws Exception;

    /**
     * Returns true if xlink refers to a validated element
     */
    public abstract boolean isValidated(Dbms dbms, String href) throws Exception;

    /**
     * Return the field that contains the xlinks references to invalid metadata
     */
    public abstract String[] getInvalidXlinkLuceneField();

    /**
     * Return the field that contains the xlinks references to valid metadata
     */
    public abstract String[] getValidXlinkLuceneField();

    /**
     * Create a default shared object as defined by href
     */
    public abstract String createAsNeeded(String href, UserSession session) throws Exception;

    public Function<String, String> numericIdToConcreteId(final UserSession session) {
        return ID_FUNC;
    }

    @Override
    public Query createFindMetadataQuery(String field, String concreteId, boolean isValidated) {
        Term term = new Term(field, WILDCARD_STRING + "id=" + concreteId + WILDCARD_STRING);
        return new WildcardQuery(term);
    }
}