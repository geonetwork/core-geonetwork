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

package org.fao.geonet.kernel.csw.services;

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.getrecords.SearchController;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//=============================================================================

/**
 * CSW transaction operation. 
 * 
 */
public class Transaction extends AbstractOperation implements CatalogService
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public Transaction() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getName() { return "Transaction"; }

	//---------------------------------------------------------------------------

	public Element execute(Element request, ServiceContext context) throws CatalogException
	{
		checkService( request );
		checkVersion( request );
		
		//num counter
		int	totalInserted = 0;
		int	totalUpdated = 0;
		int	totalDeleted = 0;
		
		// Response element
		Element response = new Element(getName() +"Response", Csw.NAMESPACE_CSW);
		
		List<String>	strFileIds = new ArrayList<String>();
		
		//process the transaction from the first to the last
		List<Element> childList = request.getChildren();
				
		try
		{			
			//process the childlist
            for (Element transRequest : childList) {
                String transactionType = transRequest.getName().toLowerCase();
                if (transactionType.equals("insert") || transactionType.equals("update") || transactionType.equals("delete")) {
                    List<Element> mdList = transRequest.getChildren();
                    GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                    DataManager dataMan = gc.getDataManager();

                    // insert to database, and get the number of inserted successful
                    if (transactionType.equals("insert")) {
                        Iterator<Element> inIt = mdList.iterator();
                        dataMan.startIndexGroup();
                        try {
                            while (inIt.hasNext()) {
                                Element metadata = (Element) inIt.next().clone();
                                boolean insertSuccess = insertTransaction(metadata, strFileIds, context);
                                if (insertSuccess) {
                                    totalInserted++;
                                }
                            }
                        }
                        finally {
                            dataMan.endIndexGroup();
                        }
                    }
                    // Update
                    else if (transactionType.equals("update")) {
                        Iterator<Element> inIt = mdList.iterator();
                        dataMan.startIndexGroup();
                        try {
                            while (inIt.hasNext()) {
                                Element metadata = (Element) inIt.next().clone();
                                if (!metadata.getName().equals("Constraint") && !metadata.getNamespace().equals(Csw.NAMESPACE_CSW)) {
                                    boolean updateSuccess = updateTransaction(transRequest, metadata, context);
                                    if (updateSuccess) {
                                        totalUpdated++;
                                    }
                                }
                            }
                        }
                        finally {
                            dataMan.endIndexGroup();
                        }
                    }
                    // Delete
                    else {
                        totalDeleted = deleteTransaction(transRequest, context);
                    }
                }
            }
		}
		catch( Exception e )
		{
			Log.error(Geonet.CSW, "Cannot process transaction");
			Log.error(Geonet.CSW, " (C) StackTrace\n"+ Util.getStackTrace(e));

			throw new NoApplicableCodeEx("Cannot process transaction: " + e.getMessage());
		}
		finally
		{
			getResponseResult( request, response, strFileIds,totalInserted, totalUpdated, totalDeleted  );
			
		}	
		
		return response;
	}

	//---------------------------------------------------------------------------

	public Element adaptGetRequest(Map<String, String> params)
	{

        return new Element(getName(), Csw.NAMESPACE_CSW);
	}
	
	//---------------------------------------------------------------------------

	public Element retrieveValues(String parameterName) throws CatalogException {
		return null;
	}
	
	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------
	
	/**
	 * @param xml
	 * @param fileIds
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private boolean insertTransaction( Element xml, List<String> fileIds, ServiceContext context ) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();
		
		String 	schema = dataMan.autodetectSchema(xml);
		
//		String category   = Util.getParam(request, Params.CATEGORY);
		String 	category  = null, source = null, createDate = null, changeDate = null;
		
		String uuid;
		uuid = dataMan.extractUUID(schema, xml);
		if (uuid.length() == 0)
			uuid = UUID.randomUUID().toString();
		
		// -----------------------------------------------------------------------
		// --- insert metadata into the system

		UserSession us = context.getUserSession();
		
		if (us.getUserId() == null)
			throw new NoApplicableCodeEx("User not authenticated.");
		
		String profile = us.getProfile(); 
		
		// Only editors and above are allowed to insert metadata
		if (!profile.equals(Geonet.Profile.EDITOR) && !profile.equals(Geonet.Profile.REVIEWER)
				&& !profile.equals(Geonet.Profile.USER_ADMIN) && !profile.equals(Geonet.Profile.ADMINISTRATOR))
			throw new NoApplicableCodeEx("User not allowed to insert metadata.");
		
		int userId = us.getUserIdAsInt();

        AccessManager am = gc.getAccessManager();
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        // Set default group: user first group
        Set<String> userGroups = am.getVisibleGroups(dbms, userId);
        String group = (String) userGroups.toArray()[0];

        //
        // insert metadata
        //
        String docType = null, title = null, isTemplate = null;
        boolean ufo = false, indexImmediate = false;
        String id = dataMan.insertMetadata(dbms, schema, xml, context.getSerialFactory().getSerial(dbms, "Metadata"), uuid, userId, group, source,
                         isTemplate, docType, title, category, createDate, changeDate, ufo, indexImmediate);

		if( id == null )
			return false;

        // Set metadata as public if setting enabled
        SettingManager sm = gc.getSettingManager();
        boolean metadataPublic = sm.getValueAsBool("system/csw/metadataPublic", false);

        if (metadataPublic) {
            dataMan.setOperation(dbms, id, "1", AccessManager.OPER_VIEW);
        }

		dataMan.indexMetadataGroup(dbms, id);
		
		fileIds.add( uuid );
		
		dbms.commit();
				
		return true;
	}
	
	
	/**
	 * @param request
	 * @param xml
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private boolean updateTransaction(Element request, Element xml, ServiceContext context ) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();
		
		if (context.getUserSession().getUserId() == null)
			throw new NoApplicableCodeEx("User not authenticated.");
		
		boolean	bReturn = false;
		
		//first, search the record in the database to get the record id
		Element constr = (Element) request.getChild("Constraint",Csw.NAMESPACE_CSW ).clone();
		List<Element> results = getResultsFromConstraints(context, constr);
		
		
		//second, update the metadata in the dbms using the id
		Iterator<Element> it = results.iterator();
		if( !it.hasNext() )
			return	bReturn;
		
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		// only update first result matched
		while(true)
		{
            if (!(it.hasNext())) {
                break;
            }
            Element result = it.next();
			String uuid = result.getChildText("identifier", Csw.NAMESPACE_DC);
			String id = dataMan.getMetadataId(dbms, uuid);
			String changeDate = null;
			
			if( id == null )
				return false;
			
			if (!dataMan.getAccessManager().canEdit(context, id))
				throw new NoApplicableCodeEx("User not allowed to update this metadata("+id+").");

            //
            // update metadata
            //
            boolean validate = false;
            boolean ufo = false;
            boolean index = false;
            String language = context.getLanguage();
            UserSession session = context.getUserSession();
            dataMan.updateMetadata(session, dbms, id, xml, validate, ufo, index, language, changeDate, null);

			dataMan.indexMetadataGroup(dbms, id);

			bReturn = true;
			break;
		}
		
		return bReturn;		
	}
	
	/**
	 * @param request
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private int deleteTransaction(Element request, ServiceContext context ) throws Exception	
	{
		int deleted = 0;
		
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();
		
		if (context.getUserSession().getUserId() == null)
			throw new NoApplicableCodeEx("User not authenticated.");
		
		//first, search the record in the database to get the record id
		Element constr = request.getChild("Constraint",Csw.NAMESPACE_CSW );
		List<Element> results = getResultsFromConstraints(context, constr);
		
		//second, delete the metadata in the dbms using the id
		Iterator<Element>	i = results.iterator();
		if( !i.hasNext() )
			return	deleted;
		
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		
		// delete all matching records
		while( i.hasNext() )
		{
			Element result = i.next();
			String uuid = result.getChildText("identifier", Csw.NAMESPACE_DC);
			String id = dataMan.getMetadataId(dbms, uuid);
			
			if( id == null )
				return deleted;
			
			if (!dataMan.getAccessManager().canEdit(context, id))
				throw new NoApplicableCodeEx("User not allowed to delete metadata : "+id);
	
			dataMan.deleteMetadata(dbms, id);
			deleted++;
		}
		
		return deleted;		
		
	}

	/**
	 * @param context
	 * @param constr
	 * @return
	 * @throws CatalogException
	 */
	private List<Element> getResultsFromConstraints(ServiceContext context, Element constr) throws CatalogException {
		SearchController sc = new SearchController(null, null);

        Element filterExpr  = getFilterExpression(constr);
		String filterVersion = getFilterVersion(constr);
		
		ElementSetName  setName = ElementSetName.BRIEF;
		
		Pair<Element, Element> results= sc.search(context, 1, 100, ResultType.RESULTS,
				OutputSchema.OGC_CORE, setName, filterExpr, filterVersion, null, null, 0);
		
		return results.two().getChildren();
	}

    /**
	 * @param request
	 * @param response
	 * @param fileIds
	 * @param totalInserted
	 * @param totalUpdated
	 * @param totalDeleted
	 */
	private void getResponseResult( Element request, Element response, List<String> fileIds, 
			int totalInserted, int totalUpdated, int totalDeleted )
	{
		// transactionSummary
		Element transactionSummary = getTransactionSummary( totalInserted, totalUpdated, totalDeleted );		
		
		// requestID					
		String strRequestId = request.getAttributeValue("requestId");
		if (strRequestId != null)
			transactionSummary.setAttribute( "requestId", strRequestId );
		
		response.addContent( transactionSummary );			
		
		if (totalInserted > 0)
		{
			Element insertResult = new Element("InsertResult", Csw.NAMESPACE_CSW );
			insertResult.setAttribute("handleRef", "handleRefValue");
			
			//Namespace NAMESPACE_DC = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
            for (String fileId : fileIds) {
                Element briefRecord = new Element("BriefRecord", Csw.NAMESPACE_CSW);
                Element identifier = new Element("identifier"); //,Csw.NAMESPACE_DC );
                identifier.setText(fileId);
                briefRecord.addContent(identifier);
                insertResult.addContent(briefRecord);
            }
			
			response.addContent(insertResult);
		}

	}
	
	/**
	 * @param totalInserted
	 * @param totalUpdated
	 * @param totalDeleted
	 * @return
	 */
	private Element getTransactionSummary( int	totalInserted, int	totalUpdated, int	totalDeleted  )
	{
		Element transactionSummary = new Element("TransactionSummary", Csw.NAMESPACE_CSW );		
//		if( totalInserted>0 )
//		{			
			Element insert = new Element("totalInserted",Csw.NAMESPACE_CSW);
			insert.setText(Integer.toString(totalInserted));
			transactionSummary.addContent( insert );
			
//		}
//		if( totalUpdated>0 )
//		{
			Element update = new Element("totalUpdated",Csw.NAMESPACE_CSW);
			update.setText(Integer.toString(totalUpdated));
			transactionSummary.addContent( update );
//		}
//		if( totalDeleted>0 )
//		{
			Element delete = new Element("totalDeleted",Csw.NAMESPACE_CSW);
			delete.setText(Integer.toString(totalDeleted));	
			transactionSummary.addContent( delete );
//		}
		
		return transactionSummary;
	}
	
}

//=============================================================================
