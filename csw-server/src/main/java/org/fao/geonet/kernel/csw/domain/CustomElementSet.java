package org.fao.geonet.kernel.csw.domain;
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

import java.util.ArrayList;
import java.util.List;

import jeeves.resources.dbms.Dbms;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 *
 * A customized element set.
 *
 */
public class CustomElementSet {

    /**
     * Each included element is described by a full xpath relative to the document root.
     */
    private List<String> xpaths = new ArrayList<String>();

    public List<String> getXpaths() {
        return xpaths;
    }

    public void setXpaths(List<String> xpaths) {
        this.xpaths = xpaths;
    }

    public boolean add(String s) {
        return xpaths.add(s);
    }

    public boolean remove(Object o) {
        return xpaths.remove(o);
    }
    

    /**
     * Replaces the contents of table CustomElementSet.
     *
     * @param dbms database
     * @param customElementSet customelementset definition to save
     * @throws Exception hmm
     */
    public static void saveCustomElementSets(Dbms dbms, CustomElementSet customElementSet) throws Exception {
        dbms.execute("DELETE FROM CustomElementSet");
        for(String xpath : customElementSet.getXpaths()) {
            if(StringUtils.isNotEmpty(xpath)) {
                dbms.execute("INSERT INTO CustomElementSet (xpath) VALUES (?)", xpath);
            }
        }
    }

    /**
     * Retrieves contents of CustomElementSet.
     *
     * @param dbms database
     * @return List of elements (denoted by XPATH)
     * @throws Exception hmm
     */
    public static List<Element> getCustomElementSets(Dbms dbms) throws Exception {
        Element customElementSetList = dbms.select("SELECT * FROM CustomElementSet");
        @SuppressWarnings("unchecked")
        List<Element> records = customElementSetList.getChildren();
        return records;
    }

}