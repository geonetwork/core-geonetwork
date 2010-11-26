package org.fao.geonet.kernel.oaipmh.services;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
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
import org.fao.oaipmh.responses.ResumptionToken;
import org.fao.oaipmh.util.ISODate;
import org.fao.oaipmh.util.SearchResult;
import org.jdom.Element;

public abstract class AbstractTokenLister implements OaiPmhService {

	protected ResumptionTokenCache cache;
	private SettingManager settingMan;
	
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
	
	public AbstractTokenLister(ResumptionTokenCache cache, SettingManager sm) {
		this.cache=cache;
		this.settingMan = sm;
	}
	
	
	public AbstractResponse execute(AbstractRequest request,
			ServiceContext context) throws Exception {
		
		Log.debug(Geonet.OAI_HARVESTER,"OAI " +this.getClass().getSimpleName()+ " execute: ");
		
		TokenListRequest  req = (TokenListRequest)  request;

		//UserSession  session = context.getUserSession();
		SearchResult result;

		//String token = req.getResumptionToken();
		String strToken = req.getResumptionToken();
		ResumptionToken token = null;
		

		
		int pos = 0;

		if ( strToken == null )
		{
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

			result     = new SearchResult(prefix);
			result.setIds(Lib.search(context, params));

			if (result.getIds().size() == 0)
				throw new NoRecordsMatchException("No results");

			// we only need a new token if the result set is big enough
			if (result.getIds().size() > Lib.MAX_RECORDS ) {
				token = new ResumptionToken(req,result);
				cache.storeResumptionToken(token);
			}
			
		}
		else
		{
			//result = (SearchResult) session.getProperty(Lib.SESSION_OBJECT);
			token = cache.getResumptionToken( ResumptionToken.buildKey(req)  );
			Log.debug(Geonet.OAI_HARVESTER,"OAI ListRecords : using ResumptionToken :"+ResumptionToken.buildKey(req));
			
			if (token  == null)
				throw new BadResumptionTokenException("No session for token : "+ ResumptionToken.buildKey(req));

			result = token.getRes();
			
			//pos = result.parseToken(token);
			pos = ResumptionToken.getPos(req);
		}

		ListResponse res = processRequest(req,pos,result,context);
		pos = pos + res.getSize();
		
		if (token == null && res.getSize() == 0)
			throw new NoRecordsMatchException("No results");
		

		//result.setupToken(res, pos);
		if (token != null) // is null if no token needed
			token.setupToken(pos);
		res.setResumptionToken(token);
		

		return res;
		
		
	}

	public abstract String getVerb(); 
	public abstract ListResponse processRequest(TokenListRequest req, int pos, SearchResult result, ServiceContext context) throws Exception ;

}
