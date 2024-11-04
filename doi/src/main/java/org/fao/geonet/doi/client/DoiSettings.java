//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.doi.client;

/**
 * DOI constants and settings stored in database.
 */
public class DoiSettings {
    public static final String SETTING_PUBLICATION_DOI_DOIURL =
        "system/publication/doi/doiurl";
    public static final String SETTING_PUBLICATION_DOI_DOIPUBLICURL =
        "system/publication/doi/doipublicurl";
    public static final String SETTING_PUBLICATION_DOI_DOIUSERNAME =
        "system/publication/doi/doiusername";
    public static final String SETTING_PUBLICATION_DOI_DOIPASSWORD =
        "system/publication/doi/doipassword";
    public static final String SETTING_PUBLICATION_DOI_DOIKEY =
        "system/publication/doi/doikey";
    public static final String SETTING_PUBLICATION_DOI_DOIPATTERN =
        "system/publication/doi/doipattern";
    public static final String SETTING_PUBLICATION_DOI_LANDING_PAGE_TEMPLATE =
        "system/publication/doi/doilandingpagetemplate";

    protected static final String LOGGER_NAME = "geonetwork.doi";
}
