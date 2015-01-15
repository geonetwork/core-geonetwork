//==============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.search;

/**
 * 
 * Parameter names used in search.
 *
 * @author heikki doeleman
 * 
 */
public class SearchParameter {

    public static final String OWNER = "owner";
    public static final String GROUP = "group";
    public static final String GROUPOWNER = "groupOwner";
    public static final String ISREVIEWER = "isReviewer";
    public static final String ISUSERADMIN = "isUserAdmin";
    public static final String ISADMIN = "isAdmin";

    public static final String TOPICCATEGORY = "topic-category";
    public static final String SIMILARITY = "similarity";
    public static final String UUID = "uuid";
    public static final String ANY = "any";
    public static final String ALL = "all";
    public static final String OR = "or";
    public static final String WITHOUT = "without";
    public static final String PHRASE = "phrase";
    public static final String DOWNLOAD = "download";
    public static final String DYNAMIC = "dynamic";
    public static final String PROTOCOL = "protocol";
    public static final String FEATURED = "featured";
    public static final String CATEGORY = "category";
    public static final String TEMPLATE = "template";
    public static final String DATETO = "dateTo";
    public static final String DATEFROM = "dateFrom";
    public static final String REVISIONDATETO = "revisionDateTo";
    public static final String REVISIONDATEFROM = "revisionDateFrom";
    public static final String PUBLICATIONDATETO = "publicationDateTo";
    public static final String PUBLICATIONDATEFROM = "publicationDateFrom";
    public static final String CREATIONDATETO = "creationDateTo";
    public static final String CREATIONDATEFROM = "creationDateFrom";
    public static final String EXTTO = "extTo";
    public static final String EXTFROM = "extFrom";
    public static final String METADATASTANDARDNAME = "metadataStandardName";
    public static final String _SCHEMA = "_schema";
    public static final String PARENTUUID = "parentUuid";
    public static final String OPERATESON = "operatesOn";
    public static final String SERVICETYPE = "serviceType";
    public static final String TYPE = "type";
    public static final String INSPIRE = "inspire";
    public static final String INSPIRETHEME = "inspiretheme";
    public static final String INSPIREANNEX = "inspireannex";
    public static final String SITEID = "siteId";
    public static final String THEMEKEY = "themekey";
    public static final String DIGITAL = "digital";
    public static final String PAPER = "paper";
    public static final String TITLE = "title";
    public static final String ABSTRACT = "abstract";
    public static final String EASTBL = "eastBL";
    public static final String WESTBL = "westBL";
    public static final String NORTHBL = "northBL";
    public static final String SOUTHBL = "southBL";
    public static final String RELATION = "relation";
    public static final String EDITABLE = "editable";
    public static final String DENOMINATOR = "denominator";
    public static final String DENOMINATORFROM = "denominatorFrom";
    public static final String DENOMINATORTO = "denominatorTo";
    public static final String TAXON = "taxon";
    public static final String CREDIT = "credit";
    public static final String DATAPARAM = "dataparam";
    public static final String ORGNAME = "orgName";
	public static final String SPATIALREPRESENTATIONTYPE = "spatialRepresentationType";
    public static final String VALID = "valid";
    public static final String HASFEATURECAT="hasfeaturecat";
    public static final String ROOT = "_root";
    public static final String ISTEMPLATE = "_isTemplate";
    public static final String RESULTTYPE = "resultType";
    public static final String FACET_QUERY = "facet.q";

	public static final String OP_VIEW 	   = "_operation0";
	public static final String OP_DOWNLOAD = "_operation1";
	public static final String OP_EDITING  = "_operation2";
	public static final String OP_NOTIFY   = "_operation3";
	public static final String OP_DYNAMIC  = "_operation5";
	public static final String OP_FEATURED = "_operation6";

}
