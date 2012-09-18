package org.fao.geonet.kernel.search;
/**
 *
 * Names of fields in the Lucene index.
 *
 * @author heikki doeleman
 *
 */
public class LuceneIndexField {

    /**
     * Groups allowed to view.
     */
	public static final String _OP0 = "_op0";
    /**
     * Groups allowed to download.
     */
    public static final String _OP1 = "_op1";
    /**
     * Groups allowed to edit.
     */
    public static final String _OP2 = "_op2";
    /**
     * Groups allowed to be notified.
     */
    public static final String _OP3 = "_op3";
    /**
     * Groups allowed "dynamic".
     */
    public static final String _OP5 = "_op5";
    /**
     * Groups allowed to feature.
     */
	public static final String _OP6 = "_op6";

	public static final String ABSTRACT = "abstract";
	public static final String ANY = "any";
    public static final String _CAT = "_cat";
    public static final String CHANGE_DATE = "changeDate";
    public static final String _CHANGE_DATE = "_changeDate";
    public static final String CREATE_DATE = "createDate";
    public static final String _CREATE_DATE = "_createDate";
    public static final String CREDIT = "credit";
    public static final String DATAPARAM = "dataparam";
    public static final String DENOMINATOR_FROM = "denominatorFrom";
    public static final String DENOMINATOR_TO = "denominatorTo";
    public static final String DENOMINATOR = "denominator";
    public static final String DIGITAL = "digital";
    public static final String DOWNLOAD = "download";
    public static final String _DUMMY = "_dummy";
    public static final String EAST = "eastBL";
    public static final String _HASXLINKS  ="_hasxlinks";
    //***
    // public static final String GROUP_OWNER = "_groupOwner";
    public static final String _ID = "_id";
    public static final String INSPIRE_ANNEX = "inspireannex";
    public static final String INSPIRE_CAT = "inspirecat";
    public static final String INSPIRE_THEME = "inspiretheme";
    public static final String _IS_LOCKED = "_isLocked";
    public static final String _IS_HARVESTED = "_isHarvested";
    public static final String _IS_TEMPLATE = "_isTemplate";
    public static final String _IS_WORKSPACE = "_isWorkspace";
	public static final String KEYWORD = "keyword";
    public static final String _LOCKEDBY = "_lockedBy";
	public static final String METADATA_STANDARD_NAME = "metadataStandardName";
	public static final String NORTH = "northBL";
    public static final String OPERATESON = "operatesOn";
    public static final String ORG_NAME = "orgName";
	public static final String _OWNER = "_owner";
    public static final String _OWNERNAME = "_ownerName";
    public static final String PAPER = "paper";
    public static final String PARENTUUID = "parentUuid";
    public static final String _POPULARITY = "_popularity";
	public static final String PROTOCOL = "protocol";
    public static final String PUBLICATION_DATE = "publicationDate";
    public static final String REVISION_DATE = "revisionDate";
    public static final String _RATING = "_rating";
    public static final String _ROOT = "_root";
    public static final String _SCHEMA =  "_schema";
    public static final String SERVICE_TYPE = "serviceType";
	public static final String _SOURCE = "_source";
	public static final String SOUTH = "southBL";
    public static final String SPATIALREPRESENTATIONTYPE = "spatialRepresentationType";
    public static final String _STATUS = "_status";
    public static final String _STATUSCHANGEDATE = "_statusChangeDate";
    public static final String SUBJECT = "subject";
    public static final String TAXON = "taxon:name";
    public static final String TEMPORALEXTENT_BEGIN = "tempExtentBegin";
    public static final String TEMPORALEXTENT_END = "tempExtentEnd";
    public static final String _TITLE = "_title";
	public static final String TOPIC_CATEGORY = "topicCat";
	public static final String TYPE = "type";
    public static final String _USERINFO = "_userinfo";
    public static final String _UUID = "_uuid";
    public static final String _VALID = "_valid";
	public static final String WEST = "westBL";
    public static final String _XLINK = "_xlink";

}
