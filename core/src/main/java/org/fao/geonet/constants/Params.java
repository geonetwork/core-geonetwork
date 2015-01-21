//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.constants;

/**
 * Constants for service parameters (elements and attributes).
 */
public final class Params {
    public static final String BACKUP_FILE  = "backup_file";
	public static final String ACCESS       = "access";
	public static final String BTN          = "btn";
    public static final String RECURSE      = "recurse";
	public static final String CATEGORY     = "category";
	public static final String CHOICE             = "choice";
	public static final String JURISDICTION       = "jurisdiction";
	public static final String CHILD        = "child";
	public static final String COMMENTS     = "comments";
	public static final String SUBJECT      = "subject";
	public static final String CURRTAB      = "currTab";
	public static final String FINISHED     = "finished";
	public static final String DATA         = "data";
	public static final String DIR          = "dir";
	public static final String DESCRIPTION  = "description";
	public static final String EMAIL        = "email";
	public static final String FNAME        = "fname";
	public static final String FORGET       = "forget";
	public static final String GROUP        = "group";
	public static final String GROUPS       = "groups";
	public static final String ID           = "id";
    public static final String JUST_CREATED = "just-created";
	public static final String RELATED_ID   = "relatedid";
    public static final String PARENT_ID    = "parentId";
    public static final String PARENT_UUID  = "parentUuid";
    public static final String CHILD_ID     = "childId";
    public static final String CHILD_UUID   = "childUuid";
	public static final String UUID         = "uuid";
	public static final String URI          = "uri";
	public static final String URL          = "url";
    public static final String MINOREDIT    = "minor";
	public static final String NAME         = "name";
	public static final String OPERATION    = "operation";
	public static final String ORIGINALCHOICE     = "originalchoice";
	public static final String ORG          = "org";
	public static final String OVERWRITE    = "overwrite";
	public static final String PASSWORD     = "password";
	public static final String PARENT       = "parent";
	public static final String POSITION     = "position";
	public static final String NEW_PASSWORD = "newPassword";
	public static final String PROFILE      = "profile";
	public static final String REF          = "ref";
	public static final String MODE         = "mode";
	public static final String SCHEMA       = "schema";
	public static final String SHOWVALIDATIONERRORS  = "showvalidationerrors";
	public static final String SITE_ID      = "siteId";
	public static final String SITENAME     = "sitename";
	public static final String SITEORG      = "siteorg";
	public static final String STYLESHEET   = "styleSheet";
	public static final String SURNAME      = "surname";
	public static final String TEMPLATE     = "template";
	public static final String TITLE        = "title";
	public static final String USER         = "user";
	public static final String USERNAME     = "username";
	public static final String VERSION      = "version";
	public static final String VALIDATE     = "validate";
	public static final String SELECTED     = "selected";
	public static final String TYPE         = "type";
	public static final String ADDRESS      = "address";
	public static final String CITY         = "city";
	public static final String STATE        = "state";
	public static final String ZIP          = "zip";
	public static final String COUNTRY      = "country";
	public static final String KIND         = "kind";
	public static final String RATING       = "rating";
	public static final String FILE_TYPE    = "file_type";
	public static final String GENERATE_UUID= "generateUUID";
	public static final String OPERATES_ON  = "operatesOn";	
	public static final String UUID_ACTION  = "uuidAction";
	public static final String NOTHING      = "nothing";
	public static final String PROCESS      = "process";

	public static final String SCALING              = "scaling";
	public static final String SCALING_DIR          = "scalingDir";
	public static final String SCALING_FACTOR       = "scalingFactor";
	public static final String CREATE_SMALL         = "createSmall";
	public static final String SMALL_SCALING_DIR    = "smallScalingDir";
	public static final String SMALL_SCALING_FACTOR = "smallScalingFactor";

	public static final String THUMBNAIL    = "thumbnail";
	public static final String RESOURCES    = "resources";
	public static final String METADATA     = "metadata";
	public static final String FAIL_ON_ERROR = "failOnError";
    public static final String FAVICON = "favicon";
    public static final String SAVE = "save";
    public static final String INFO = "info";
    public static final String STATUS = "status";
    public static final String CHANGE_MESSAGE = "changeMessage";
    public static final String XPATH = "xpath";
    public static final String XPATH_TITLE = "xpathTitle";
		public static final String EXTRACT_TITLE = "extractTitle";
    public static final String DOCHANGES = "doChanges";

    public static final String IS_UPLOAD    = "isUpload";
    public static final String TEMPLATEID    = "templateId";
    public static final String USE_SESSION  = "use_session";
    public static final String ORGANIZATION  = "organization";
    public static final String PHONE  = "phone";
    public static final String SEX  = "sex";
    public static final String CAPTCHARESPONSE  = "captcharesponse";
    public static final String REMARKS  = "remarks";
    public static final String FUNCTION  = "function";
    public static final String FEEDBACK_FUNCTION  = "feedback-function";
    public static final String METADATA_ORGANIZATION  = "metadataorganisation";
    public static final String METADATA_EMAIL  = "metadataemail";
    public static final String DATE  = "date";
    public static final String FULL_PRIVILEGES = "fullPrivileges";
    
	public static final String SERVICENAME     = "servicename";
	public static final String SERVICEDESCRIPTION     = "servicedescription";
	public static final String CLASSNAME     = "classname";
	public static final String FILTER_ANY     = "any";	
	public static final String FILTER_TITLE     = "title";
	public static final String FILTER_SUBJECT     = "abstract";
	public static final String FILTER_KEYWORD     = "keyword";
	public static final String FILTER_TYPE     = "type";
	public static final String FILTER_CATALOG     = "_source";
	public static final String FILTER_GROUP     = "_groupPublished";
	public static final String FILTER_CATEGORY     = "_cat";
    public static final String FILTER_DENOMINATOR = "denominator";

    public static final String CONTENT_TYPE = "_content_type";
	
	
	
    public static final String FILENAME = "filename";
    public static final String UPDATEONLY = "update";
    public static final String ACTION = "action";
    public static final String START_EDITING_SESSION = "starteditingsession";

    /**
     * TODO javadoc.
     */
	public static final class Access {
		public static final String PUBLIC  = "public";
		public static final String PRIVATE = "private";
	}

    /**
     * TODO javadoc.
     */
	public static final class Operation {
		public static final String NEWUSER  = "newuser";
		public static final String EDITINFO = "editinfo";
		public static final String FULLUPDATE = "fullupdate";
		public static final String RESETPW = "resetpw";
		
		public static final String NEWSERVICE  = "newservice";
		public static final String UPDATESERVICE  = "updateservice";
	}

    /**
     * TODO javadoc.
     */
	public static final class Status { 
        public static final String UNKNOWN   = "0";
        public static final String DRAFT     = "1";
        public static final String APPROVED  = "2";
        public static final String RETIRED   = "3";
        public static final String SUBMITTED = "4";
        public static final String REJECTED  = "5";
	}

    /**
     * TODO javadoc.
     */
	public static final class Svn {
		public static final String DBURLPROP = "geonet:dburl";
		public static final String OPERATOR  = "geonet:operator";
		public static final String USERNAME  = "geonet:username";
		public static final String NAME      = "geonet:name";
		public static final String SURNAME   = "geonet:surname";
		public static final String SERVICE   = "geonet:service";
		public static final String IPADDR    = "geonet:ipaddress";
	}

}
