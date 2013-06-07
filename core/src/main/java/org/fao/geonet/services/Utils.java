package org.fao.geonet.services;

import jeeves.exceptions.MissingParameterEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.jdom.Element;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

	/**
	 * Search for a UUID or an internal identifier parameter and return an
	 * internal identifier using default UUID and identifier parameter names
	 * (ie. uuid and id).
	 * 
	 * @param params
	 *            The params to search ids in
	 * @param context
	 *            The service context
	 * @param uuidParamName		UUID parameter name
	 * @param uuidParamName		Id parameter name
	 *  
	 * @return
	 * @throws Exception
	 */
	public static String getIdentifierFromParameters(Element params,
			ServiceContext context, String uuidParamName, String idParamName)
			throws Exception {

		// the metadata ID
		String id;
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dm = gc.getBean(DataManager.class);

		id = lookupByFileId(params,gc);
		if(id==null) {
    		// does the request contain a UUID ?
    		try {
    			String uuid = Util.getParam(params, uuidParamName);
    			// lookup ID by UUID
                Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
    			id = dm.getMetadataId(dbms, uuid);
    		}
            catch (MissingParameterEx x) {
    			// request does not contain UUID; use ID from request
    			try {
    				id = Util.getParam(params, idParamName);
    			} catch (MissingParameterEx xx) {
    				// request does not contain ID
    				// give up
    				throw new Exception("Request must contain a UUID ("
    						+ uuidParamName + ") or an ID (" + idParamName + ")");
    			}
    		}
		}
		return id;
	}

	private static String lookupByFileId(Element params, GeonetContext gc) throws Exception {
	    String fileId = Util.getParam(params, "fileIdentifier", null);
	    if(fileId == null) {
	        return null;
	    }

	    return lookupMetadataIdFromFileId(gc, fileId);
    }

    public static String lookupMetadataIdFromFileId(GeonetContext gc, String fileId) throws IOException,
            InterruptedException {
        TermQuery query = new TermQuery(new Term("fileId", fileId));

        SearchManager searchManager = gc.getBean(SearchManager.class);

        IndexAndTaxonomy indexAndTaxonomy = searchManager.getIndexReader(null, -1);
		GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;

        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs tdocs = searcher.search(query, 1);

            if (tdocs.totalHits > 0) {

                Set<String> id = Collections.singleton("_id");
                Document element = reader.document(tdocs.scoreDocs[0].doc, id);
                return element.get("_id");
            }

            return null;
        } finally {
            searchManager.releaseIndexReader(indexAndTaxonomy);
        }
    }

    /**
	 * Search for a UUID or an internal identifier parameter and return an
	 * internal identifier using default UUID and identifier parameter names
	 * (ie. uuid and id).
	 *
	 * @param params
	 *            The params to search ids in
	 * @param context
	 *            The service context
	 * @return
	 * @throws Exception
	 */
	public static String getIdentifierFromParameters(Element params,
			ServiceContext context) throws Exception {
		return getIdentifierFromParameters(params, context, Params.UUID, Params.ID);
	}

    public static void addGroup(Dbms dbms, int userId, int groupId, String profile) throws Exception {
        dbms.execute("INSERT INTO UserGroups(userId, groupId, profile) VALUES (?, ?, ?)",
                userId, groupId, profile);
    }

    /**
     * Method to query Relation table and get a Set of identifiers of related
     * metadata
     *
     * @param id
     * @param relation
     * @param context
     * @return
     * @throws Exception
     */
    public static Set<String> getRelationIds(int id, String relation, ServiceContext context) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager()
                .open(Geonet.Res.MAIN_DB);

        Set<String> result = new HashSet<String>();

        // --- perform proper queries to retrieve the id set
        if (relation.equals("normal") || relation.equals("full")) {
            String query = "SELECT relatedId FROM Relations WHERE id=?";
            result.addAll(retrieveIds(dbms, query, "relatedid", id));
        }

        if (relation.equals("reverse") || relation.equals("full")) {
            String query = "SELECT id FROM Relations WHERE relatedId=?";
            result.addAll(retrieveIds(dbms, query, "id", id));
        }

        return result;
    }

    /**
     * Run the query and load a Set based on query results.
     *
     * @param dbms
     * @param query
     * @param field
     * @param id
     * @return
     * @throws java.sql.SQLException
     */
    private static Set<String> retrieveIds(Dbms dbms, String query,
                                           String field, int id) throws SQLException {
        @SuppressWarnings("unchecked")
        List<Element> records = dbms.select(query, Integer.valueOf(id)).getChildren();
        Set<String> results = new HashSet<String>();

        for (Element rec : records) {
            String val = rec.getChildText(field);

            results.add(val);
        }

        return results;
    }
}
