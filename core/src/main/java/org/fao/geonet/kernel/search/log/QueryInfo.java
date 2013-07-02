package org.fao.geonet.kernel.search.log;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;
import org.fao.geonet.constants.Geonet;

import java.sql.SQLException;

/** 
 * A bean representing basic information for lucene query (keywords, hits, date, ip.etc)
 * Represent a lazy mapping with the Database tables storing query parameters
 * 
 * @author nicolas Ribot
 *
 */
public class QueryInfo {
	
	// some static constants to test the query type we are currently processing
	public static final String BOOLEAN_QUERY = "BOOLEAN_QUERY";
	public static final String TERM_QUERY = "TERM_QUERY";
	public static final String FUZZY_QUERY = "FUZZY_QUERY";
	public static final String PREFIX_QUERY = "PREFIX_QUERY";
	public static final String MATCH_ALL_DOCS_QUERY = "MATCH_ALL_DOCS_QUERY";
	public static final String WILDCARD_QUERY = "WILDCARD_QUERY";
	public static final String PHRASE_QUERY = "PHRASE_QUERY";
	public static final String RANGE_QUERY = "RANGE_QUERY";
	public static final String NUMERIC_RANGE_QUERY = "NUMERIC_RANGE_QUERY";
	
	/** the field to search for a value in a query */
	private String field;
	
	/** the value of the field to search for in a query */
	private String text;
	
	/** */
	private Float similarity;
	
	/** The type of lucene query (see static class constants) */
	private String luceneQueryType;
	
	/** the lower bound/term text for range query 
	 *  only valid for RANGE_QUERY
	 */
	private String lowerText;
	/** the upper bound/term text for range query 
	 *  only valid for RANGE_QUERY
	 */
	private String upperText;
	
	/** How to treat upper/lower bounds: inclusive or exclusive*/
	private Boolean inclusive;
	
	
	/** 
	 * ctor with some basic information, setting inclusive to false. 
	 * 
	 * @param field the field concerned by the search
	 * @param text the value of the search
	 * @param queryType the type of query 
	 * @param similarity the value for similarity
	 */
	public QueryInfo(String field, String text, String queryType, Float similarity) {
		this(field, text, queryType);
		this.similarity = similarity;
	}
	
	/**
	 * 
	 * @param field the field concerned by the search
	 * @param text the value of the search
	 * @param queryType the type of query 
	 */
	public QueryInfo(String field, String text, String queryType) {
		this();
		this.field = field;
		this.luceneQueryType = queryType;
		this.text = text;
	}
	
	/** 
	 * default ctor
	 * 
	 */
	public QueryInfo() {
		super();
		this.text = "";
		this.inclusive = false;
		this.similarity = Float.MIN_VALUE;
	}
	
	public QueryInfo(String queryType) {
		this();
		this.luceneQueryType = queryType;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Float getSimilarity() {
		return similarity;
	}

	public void setSimilarity(Float similarity) {
		this.similarity = similarity;
	}

	public String getLuceneQueryType() {
		return luceneQueryType;
	}

	public void setLuceneQueryType(String luceneQueryType) {
		this.luceneQueryType = luceneQueryType;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public String getLowerText() {
		return lowerText;
	}

	public void setLowerText(String lowerText) {
		this.lowerText = lowerText;
	}

	public String getUpperText() {
		return upperText;
	}

	public void setUpperText(String upperText) {
		this.upperText = upperText;
	}

	public Boolean getInclusive() {
		return inclusive;
	}

	public void setInclusive(Boolean inclusive) {
		this.inclusive = inclusive;
	}
	
	@Override 
	public String toString() {
		StringBuilder b = new StringBuilder("QueryInfo fields:\t");
		
		b.append("luc. type : ").append(this.getLuceneQueryType()).append("\t");
		b.append("field     : ").append(this.getField()).append("\t");
		b.append("text      : ").append(this.getText()).append("\t");
		b.append("similarity: ").append(this.getSimilarity()).append("\t");
		b.append("lowerText : ").append(this.getLowerText()).append("\t");
		b.append("upperText : ").append(this.getUpperText()).append("\t");
		b.append("inclusive : ").append(this.getInclusive()).append("\t");
		return b.toString();
	}

	/**
	 * Stores this object into the database
	 * @param con the SQL connection to use to make the query
	 * @param requestId the Request unique identifier, used as foreign key into the Params table
	 * @return true if insertion was successful, false otherwise. todo: use exception handling ?
	 */
	public boolean storeToDb(Dbms dbms, SerialFactory sf, int requestId) {
		if (dbms == null || dbms.isClosed()) {
            if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                Log.debug(Geonet.SEARCH_LOGGER, "null or closed dbms object");
			return false;
		}
		try {
			//--- generate a new metadata id
			int paramId = sf.getSerial(dbms, "Params");
			
			String query = "insert into params (id,requestId,queryType,termField,termText,";
			query += "similarity,lowerText,upperText,inclusive) values (?,?,?,?,?,?,?,?,?)";
			int res = dbms.execute(
					query, 
					paramId,
					requestId, 
					this.luceneQueryType, 
					this.getField(), 
					this.getText(), 
					this.getSimilarity(),
					this.getLowerText(),
					this.getUpperText(),
					this.getInclusive() ? "y": "n");
            if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                Log.debug(Geonet.SEARCH_LOGGER, "Returned " + res + " for queryInfo: " + toString());
		} catch (SQLException sqle) {
			dbms.abort();
			Log.warning(Geonet.SEARCH_LOGGER, "an error occuring during QueryInfo database storage. Aborting :" + 
					sqle.getMessage() + 
					". " + 
					"Is rollback occured ?");
			sqle.printStackTrace();
		}
	
		return true;
	}
	
	/** 
	 *  Is this QueryInfo object describing a Query Type ?
	 * @return true if this.field equals 'type' and this.text is one of  QueryRequest.QUERY_TYPE_xxx constant
	 */
	public boolean isQueryTypeDescriptor() {
		return "type".equals(this.getField()) && 
				(QueryRequest.QUERY_TYPE_GEO.equals(this.getText()) ||
				 QueryRequest.QUERY_TYPE_MDD.equals(this.getText()) ||
				 QueryRequest.QUERY_TYPE_MDS.equals(this.getText()));
	}
	
	/**
	 * 
	 * @return one of the QueryRequest.QUERY_TYPE_xxx constant if isQueryTypeDescriptor returns true, or null otherwise
	 */
	public String getMdQueryType() {
		if ("type".equals(this.getField())) {
			if (QueryRequest.QUERY_TYPE_GEO.equals(this.getText())) {
				return QueryRequest.QUERY_TYPE_GEO;
			} else if (QueryRequest.QUERY_TYPE_MDD.equals(this.getText())) {
				return QueryRequest.QUERY_TYPE_MDD;
			} else if (QueryRequest.QUERY_TYPE_MDS.equals(this.getText())) {
				return QueryRequest.QUERY_TYPE_MDS;
			}
		}
		return "";
	}
}
