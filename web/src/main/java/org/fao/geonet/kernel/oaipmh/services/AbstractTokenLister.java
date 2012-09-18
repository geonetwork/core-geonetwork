package org.fao.geonet.kernel.oaipmh.services;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.geonet.kernel.oaipmh.ResumptionTokenCache;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.oaipmh.exceptions.BadArgumentException;
import org.fao.oaipmh.exceptions.BadResumptionTokenException;
import org.fao.oaipmh.exceptions.NoRecordsMatchException;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.TokenListRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.ListResponse;
import org.fao.oaipmh.responses.GeonetworkResumptionToken;
import org.fao.oaipmh.util.ISODate;
import org.fao.oaipmh.util.SearchResult;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTokenLister implements OaiPmhService {

	protected ResumptionTokenCache cache;
	private SettingManager settingMan;
	private SchemaManager schemaMan;
	
	/**
	 * @return the mode
	 */
	public int getMode() {
		return settingMan.getValueAsInt("system/oai/mdmode");
	}

	/**
	 * Get the dateFrom
	 * Possible values are taken from the LuceneQueryBuilder class (hard coding)
	 * @return the dateFrom
	 */
	public String getDateFrom() {
		// Default mode is set to OaiPmhDispatcher.MODE_MODIFIDATE
		String dateFrom = "dateFrom";
		if (getMode() == OaiPmhDispatcher.MODE_TEMPEXTEND) {
			dateFrom = "extFrom";
		}
		return dateFrom;
	}

	/**
	 * Get the dateUntil
	 * Possible values are taken from the LuceneQueryBuilder class (hard coding)
	 * @return the dateUntil
	 */
	public String getDateUntil() {
		// Default mode is set to OaiPmhDispatcher.MODE_MODIFIDATE
		String dateUntil = "dateTo";
		if (getMode() == OaiPmhDispatcher.MODE_TEMPEXTEND) {
			dateUntil = "extTo";
		}
		return dateUntil;
	}
	
	public AbstractTokenLister(ResumptionTokenCache cache, SettingManager sm, SchemaManager scm) {
		this.cache=cache;
		this.settingMan = sm;
		this.schemaMan = scm;
	}
	
	
	public AbstractResponse execute(AbstractRequest request,
			ServiceContext context) throws Exception {

        if(Log.isDebugEnabled(Geonet.OAI_HARVESTER))
            Log.debug(Geonet.OAI_HARVESTER,"OAI " +this.getClass().getSimpleName()+ " execute: ");
		
		TokenListRequest  req = (TokenListRequest)  request;

		//UserSession  session = context.getUserSession();
		SearchResult result;

		//String token = req.getResumptionToken();
		String strToken = req.getResumptionToken();
		GeonetworkResumptionToken token = null;
		

		
		int pos = 0;

		if ( strToken == null )
		{
            if(Log.isDebugEnabled(Geonet.OAI_HARVESTER))
                Log.debug(Geonet.OAI_HARVESTER,"OAI " +this.getClass().getSimpleName()+ " : new request (no resumptionToken)");
			Element params = new Element("request");

			ISODate from   = req.getFrom();
			ISODate until  = req.getUntil();
			String  set    = req.getSet();
			String  prefix = req.getMetadataPrefix();

			if (from != null)
			{
				String sFrom = from.isShort ? from.getDate() : from.toString();
				params.addContent(new Element(getDateFrom()).setText(sFrom));
			}

			if (until != null)
			{
				String sTo = until.isShort ? until.getDate() : until.toString();
				params.addContent(new Element(getDateUntil()).setText(sTo));
			}

			if (from != null && until != null && from.sub(until) > 0)
				throw new BadArgumentException("From is greater than until");

			if (set != null)
				params.addContent(new Element("category").setText(set));

			params.addContent(new Element("_schema").setText(prefix));

			// now do the search
			result     = new SearchResult(prefix);
			if (schemaMan.existsSchema(prefix)) {
				result.setIds(Lib.search(context, params));
			} else {
				// collect up all the schemas that we can convert to create prefix,
				// search ids and add to the result set 
				List<String> schemas = getSchemasThatCanConvertTo(prefix);
				for (String schema : schemas) {
					params.removeChild("_schema");
					params.addContent(new Element("_schema").setText(schema));
					result.addIds(Lib.search(context, (Element)params.clone()));
				}
				if (schemas.size() == 0) result.setIds(new ArrayList<String>());
			}

			if (result.getIds().size() == 0)
				throw new NoRecordsMatchException("No results");

			// we only need a new token if the result set is big enough
			if (result.getIds().size() > Lib.MAX_RECORDS ) {
				token = new GeonetworkResumptionToken(req,result);
				cache.storeResumptionToken(token);
			}
			
		}
		else
		{
			//result = (SearchResult) session.getProperty(Lib.SESSION_OBJECT);
			token = cache.getResumptionToken( GeonetworkResumptionToken.buildKey(req)  );
            if(Log.isDebugEnabled(Geonet.OAI_HARVESTER))
                Log.debug(Geonet.OAI_HARVESTER,"OAI ListRecords : using ResumptionToken :"+GeonetworkResumptionToken.buildKey(req));
			
			if (token  == null)
				throw new BadResumptionTokenException("No session for token : "+ GeonetworkResumptionToken.buildKey(req));

			result = token.getRes();
			
			//pos = result.parseToken(token);
			pos = GeonetworkResumptionToken.getPos(req);
		}

		ListResponse res = processRequest(req,pos,result,context);
		pos = pos + res.getSize();
		
		if (token == null && res.getSize() == 0)
			throw new NoRecordsMatchException("No results");
		
		//result.setupToken(res, pos);
		if (token != null) token.setupToken(pos);
		res.setResumptionToken(token);

		return res;
		
		
	}

	//---------------------------------------------------------------------------
	/** Get list of schemas that can convert to the prefix */

	private List<String> getSchemasThatCanConvertTo(String prefix) {
		List<String> result = new ArrayList<String>();
		for (String schema : schemaMan.getSchemas()) {
			if (Lib.existsConverter(schemaMan.getSchemaDir(schema), prefix)) {
				result.add(schema);
			}
		}
		return result;
	}

	public abstract String getVerb(); 
	public abstract ListResponse processRequest(TokenListRequest req, int pos, SearchResult result, ServiceContext context) throws Exception ;

}
