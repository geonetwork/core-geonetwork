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

package org.fao.geonet.csw.common;

import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

// TODO this class is not used anywhere
public enum TypeName {
    DATASET("dataset"), DATASET_COLLECTION("datasetcollection"), SERVICE("service"), APPLICATION("application"),

    // TODO heikki: these are the only 2 legal values
    RECORD("csw:Record"), METADATA("gmd:MD_Metadata");

    //------------------------------------------------------------------------

    private String typeName;

    //------------------------------------------------------------------------

    private TypeName(String typeName) {
        this.typeName = typeName;
    }

    //------------------------------------------------------------------------

    public static TypeName getTypeName(String typeName) {
        if (typeName.equals(DATASET.toString()))
            return DATASET;

        else if (typeName.equals(DATASET_COLLECTION.toString()))
            return DATASET_COLLECTION;

        else if (typeName.equals(SERVICE.toString()))
            return SERVICE;

        else if (typeName.equals(APPLICATION.toString()))
            return APPLICATION;

        else if (typeName.equals(RECORD.toString()))
            return RECORD;

        else if (typeName.equals(METADATA.toString()))
            return METADATA;

        else
            return null;

    }
    //------------------------------------------------------------------------

    public static Set<TypeName> parse(String typeNames) throws InvalidParameterValueEx {
        HashSet<TypeName> hs = new HashSet<TypeName>();

        if (typeNames != null) {
            StringTokenizer st = new StringTokenizer(typeNames, " ");

            while (st.hasMoreTokens()) {
                String typeName = st.nextToken();

                if (typeName.equals(DATASET.toString()))
                    hs.add(DATASET);

                else if (typeName.equals(DATASET_COLLECTION.toString()))
                    hs.add(DATASET_COLLECTION);

                else if (typeName.equals(SERVICE.toString()))
                    hs.add(SERVICE);

                else if (typeName.equals(APPLICATION.toString()))
                    hs.add(APPLICATION);

                else if (typeName.equals(RECORD.toString()))
                    hs.add(RECORD);

                else if (typeName.equals(METADATA.toString()))
                    hs.add(METADATA);

                    // These two are explicitly not allowed as search targets in CSW 2.0.2,
                    // so we throw an exception if the client asks for them
                else if (typeName.equals("csw:BriefRecord"))
                    throw new InvalidParameterValueEx("typeName", typeName);

                else if (typeName.equals("csw:SummaryRecord"))
                    throw new InvalidParameterValueEx("typeName", typeName);

            }
        }

        return hs;
    }

    //------------------------------------------------------------------------

    public String toString() {
        return typeName;
    }
}
