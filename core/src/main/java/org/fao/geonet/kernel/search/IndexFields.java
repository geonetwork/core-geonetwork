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

package org.fao.geonet.kernel.search;

/**
 * Names of fields in the Lucene index.
 *
 * @author heikki doeleman
 */
public class IndexFields {

    public static final String ANY = "any";
    public static final String DOWNLOAD = "download";
    public static final String DRAFT = "draft";
    public static final String ID = "_id";
    public static final String SCHEMA = "_schema";
    public static final String SOURCE_CATALOGUE = "sourceCatalogue";
    public static final String TITLE = "title";
    public static final String TYPE = "type";
    public static final String UUID = "_uuid";
    public static final String VALID = "_valid";
    public static final String RESOURCE_TITLE = "resourceTitle";
    public static final String ROOT = "_root";
    public static final String INDEXING_ERROR_MSG = "indexingErrorMsg";
    public static final String INDEXING_ERROR_FIELD = "indexingError";
    public static final String DBID = "id";
    public static final String FILESTORE = "filestore";
}
