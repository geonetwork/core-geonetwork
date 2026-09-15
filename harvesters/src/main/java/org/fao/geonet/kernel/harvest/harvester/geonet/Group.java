//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geonet;

import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.jdom.Element;

//=============================================================================

/**
 * Represents a group with a specific name and copy policy configuration.
 * This class is responsible for parsing group information from an XML
 * element and managing group-related operations.
 */
public class Group {


    public String name;
    public CopyPolicy policy;


    Group() {
    }


    public Group(Element group) throws BadInputEx {
        name = group.getAttributeValue("name");

        if (name == null)
            throw new MissingParameterEx("attribute:name", group);

        String t = group.getAttributeValue("policy");

        if (t == null)
            throw new MissingParameterEx("attribute:policy", group);

        policy = CopyPolicy.parse(t);

        if (policy == null)
            throw new BadParameterEx("attribute:policy", policy);

        //--- '1' is the 'All' group

        if (policy == CopyPolicy.COPY_TO_INTRANET && !isAllGroup())
            throw new BadParameterEx("attribute:policy", policy);

        if (policy == CopyPolicy.CREATE_AND_COPY && isAllGroup())
            throw new BadParameterEx("attribute:policy", policy);
    }


    /**
     * Creates a deep copy of the current Group object, replicating its name and policy attributes.
     *
     * @return a new Group instance with the same name and policy as the original object
     */
    public Group copy() {
        Group m = new Group();

        m.name = name;
        m.policy = policy;

        return m;
    }

    /**
     * Determines if the group is identified as the "all" group.
     *
     * @return true if the group's name equals "all", otherwise false
     */
    public boolean isAllGroup() {
        return name.equals("all");
    }


    /**
     * Represents the copy policies that can be applied to a group.
     * <p>
     * The copy policies define different modes of copying, including simple copying,
     * creating and copying, or copying to intranet-specific destinations. Each policy
     * is associated with a specific string value, which can be used for parsing or
     * representation purposes.
     */
    public enum CopyPolicy {
        COPY("copy"),
        CREATE_AND_COPY("createAndCopy"),
        COPY_TO_INTRANET("copyToIntranet");

        private String policy;

        CopyPolicy(String policy) {
            this.policy = policy;
        }

        /**
         * Parses a string value to determine the corresponding {@link CopyPolicy} enumeration constant.
         * If no matching policy is found, this method returns null.
         *
         * @param policy The string representation of the copy policy to parse. It should match
         *               the string representation of one of the {@link CopyPolicy} constants.
         * @return The {@link CopyPolicy} constant that matches the given string representation,
         * or null if no match is found.
         */
        public static CopyPolicy parse(String policy) {
            if (policy.equals(COPY.toString())) return COPY;
            if (policy.equals(CREATE_AND_COPY.toString())) return CREATE_AND_COPY;
            if (policy.equals(COPY_TO_INTRANET.toString())) return COPY_TO_INTRANET;

            return null;
        }


        @Override
        public String toString() {
            return policy;
        }
    }
}
