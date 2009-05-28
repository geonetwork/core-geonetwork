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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.TypeName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.getrecords.SearchController;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.jdom.Element;

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
		
		ArrayList<String>	strFileIds = new ArrayList<String>();
		
		//process the transaction from the first to the last
		List<Element> childList = request.getChildren();
				
		try
		{			
			//process the childlist
			Iterator<Element>  i = childList.iterator();
			while (i.hasNext())
			{
				Element transRequest = (Element) i.next();
				
				String transactionType = transRequest.getName().toLowerCase();
				if( transactionType.equals("insert") || transactionType.equals("update") || transactionType.equals("delete") )
				{	
					List<Element> mdList = transRequest.getChildren();
					
					// insert to database, and get the number of inserted successful
					if( transactionType.equals("insert" ) )
					{
						Iterator<Element> inIt = mdList.iterator();
						while (inIt.hasNext()){
							Element metadata = (Element) inIt.next().clone();
							boolean insertSuccess = insertTransaction( metadata, strFileIds, context);
							if (insertSuccess)
								totalInserted++;
						}
					}
					// Update
					else if( transactionType.equals("update" ) ) 
					{
						Iterator<Element> inIt = mdList.iterator();
						while (inIt.hasNext()){
							Element metadata = (Element) inIt.next().clone();
							if (!metadata.getName().equals("Constraint") && !metadata.getNamespace().equals(Csw.NAMESPACE_CSW))
							{
								boolean updateSuccess = updateTransaction( transRequest, metadata, context );
								if (updateSuccess)
									totalUpdated++;
							}
						}
					}
					// Delete
					else
					{
						totalDeleted = deleteTransaction( transRequest, context);
					}
				}
				else
				{
					continue;
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
		Element request = new Element(getName(), Csw.NAMESPACE_CSW);

		return request;
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
	private boolean insertTransaction( Element xml, ArrayList<String> fileIds, ServiceContext context ) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();
		
		String 	schema = dataMan.autodetectSchema(xml);
		
		// Set default group 
		String 	group = "2";
		
//		String category   = Util.getParam(request, Params.CATEGORY);
		String 	category  = "_none_";
		String isTemplate = "n";
		String title      = "";
		String source = null;
		String createDate = null;
		String changeDate = null;
		
		String uuid;
		uuid = dataMan.extractUUID(schema, xml);
		if (uuid.length() == 0)
			uuid = UUID.randomUUID().toString();
		
		// -----------------------------------------------------------------------
		// --- insert metadata into the system

		if (context.getUserSession().getUserId() == null)
			throw new NoApplicableCodeEx("User not authenticated.");
		
		int userId = context.getUserSession().getUserIdAsInt();
		
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		String id = dataMan.insertMetadataExt(dbms, schema, xml, context.getSerialFactory(), 
				source, createDate, changeDate, uuid, userId, group);
		
		if( id == null )
			return false;
		
		dataMan.indexMetadata(dbms, id);
		
		fileIds.add( uuid );
		
		// --- Insert category if requested
		if (!"_none_".equals(category))
			dataMan.setCategory(dbms, id, category);
		
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
		while( it.hasNext() )
		{
			Element result = it.next();
			String uuid = result.getChildText("identifier", Csw.NAMESPACE_DC);
			String id = dataMan.getMetadataId(dbms, uuid);
			String changeDate = null;
			
			if( id == null )
				return false;
			
			if (!dataMan.getAccessManager().canEdit(context, id))
				throw new NoApplicableCodeEx("User not allowed to update this metadata("+id+").");

			dataMan.updateMetadataExt(dbms, id, xml, changeDate);
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
		SearchController sc = new SearchController(null);
		
		Set<TypeName> typeNames = getTypeNames(constr);
		Element filterExpr  = getFilterExpression(constr, context);
		String filterVersion = getFilterVersion(constr);
		
		ElementSetName  setName = ElementSetName.BRIEF;
		
		Pair<Element, Element> results= sc.search(context, 1, 100, -1, ResultType.RESULTS, 
				OutputSchema.OGC_CORE, setName, typeNames, filterExpr, filterVersion, null, null);
		
		return results.two().getChildren();
	}

	/**
	 * @param request
	 * @return
	 * @throws CatalogException
	 */
	private Set<TypeName> getTypeNames(Element request) throws CatalogException
	{
		//Element query = request.getChild("Query", Csw.NAMESPACE_CSW);		
		//return TypeName.parse(request.getAttributeValue("typeNames"));
		String	strTypeNames = request.getAttributeValue("typeNames");
		Set<TypeName>	tnNames = TypeName.parse( strTypeNames );
		return 	tnNames;
	}
	
	/**
	 * @param request
	 * @param response
	 * @param fileIds
	 * @param totalInserted
	 * @param totalUpdated
	 * @param totalDeleted
	 */
	private void getResponseResult( Element request, Element response, ArrayList<String> fileIds, 
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
			Iterator<String> i = fileIds.iterator();
			while( i.hasNext() )
			{
				Element  briefRecord = new Element("BriefRecord", Csw.NAMESPACE_CSW );
				Element  identifier = new Element("identifier"); //,Csw.NAMESPACE_DC );
				identifier.setText( (String)i.next() );
				briefRecord.addContent( identifier );
				insertResult.addContent( briefRecord );
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
