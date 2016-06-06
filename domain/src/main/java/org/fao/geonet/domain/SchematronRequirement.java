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

package org.fao.geonet.domain;

/**
 * The indicator for how the schematron should be interpreted if it satisfies the criteria to run on
 * a metadata.
 *
 * Created by Jesse on 2/6/14.
 */
public enum SchematronRequirement {
    /**
     * Indicates the schematron has to pass for a metadata to be considered "valid".
     */
    REQUIRED,
    /**
     * Indicates that the schematron will be ran during validation but only for purposes of
     * reporting the status of that schematron.
     *
     * The schematron can fail and still be considered valid.
     */
    REPORT_ONLY,
    /**
     * Indicates the schematron is disabled and will not be shown in any reports and will be ignored
     * during validation
     */
    DISABLED;


    public SchematronRequirement highestRequirement(SchematronRequirement requirement) {
        if (requirement.ordinal() < ordinal()) {
            return requirement;
        }
        return this;
    }
}
