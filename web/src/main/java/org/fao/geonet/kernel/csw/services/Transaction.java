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
import org.fao.geonet.constants.Edit;
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
import org.fao.geonet.kernel.csw.services.getrecords.FieldMapper;
import org.fao.geonet.kernel.csw.services.getrecords.SearchController;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

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

   private SearchController _searchController;

	public Transaction(File summaryConfig, LuceneConfig luceneConfig) {
    	_searchController = new SearchController(summaryConfig, luceneConfig);
    }


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

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getDataManager();

        Set<String> toIndex = new HashSet<String>();

		try
		{			
			//process the childlist
            for (Element transRequest : childList) {
                String transactionType = transRequest.getName().toLowerCase();
                if (transactionType.equals("insert") || transactionType.equals("update") || transactionType.equals("delete")) {
                    List<Element> mdList = transRequest.getChildren();

                    // insert to database, and get the number of inserted successful
                    if (transactionType.equals("insert")) {
                        Iterator<Element> inIt = mdList.iterator();
                            while (inIt.hasNext()) {
                                Element metadata = (Element) inIt.next().clone();
                                boolean insertSuccess = insertTransaction(metadata, strFileIds, context, toIndex);
                                if (insertSuccess) {
                                    totalInserted++;
                                }
                            }
                    }
                    // Update
                    else if (transactionType.equals("update")) {
                        Iterator<Element> inIt = mdList.iterator();
                            Element metadata = null;

                            while (inIt.hasNext()){
                                Element reqElem = (Element) inIt.next();
                                if (reqElem.getNamespace() != Csw.NAMESPACE_CSW)
                                {
                                    metadata = (Element) reqElem.clone();
                                }
                            }

                            totalUpdated = updateTransaction( transRequest, metadata, context, toIndex );
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
            try {
                dataMan.indexInThreadPool(context, new ArrayList<String>(toIndex), null);
            } catch (SQLException e) {
                Log.error(Geonet.CSW, "cannot index");
                Log.error(Geonet.CSW, " (C) StackTrace\n" + Util.getStackTrace(e));
            }
            getResponseResult(request, response, strFileIds, totalInserted, totalUpdated, totalDeleted);
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
	 *
     * @param xml
     * @param fileIds
     * @param context
     * @param toIndex
     * @return
	 * @throws Exception
	 */
	private boolean insertTransaction(Element xml, List<String> fileIds, ServiceContext context, Set<String> toIndex) throws Exception
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
        String group;
		if (userGroups.isEmpty()) {
			group = null;
		} else {
			group = (String) userGroups.iterator().next();
		}
        //
        // insert metadata
        //
        String docType = null, title = null, isTemplate = null;
        boolean ufo = true, indexImmediate = false;
        String id = dataMan.insertMetadata(context, dbms, schema, xml, context.getSerialFactory().getSerial(dbms, "Metadata"), uuid, userId, group, source,
                         isTemplate, docType, title, category, createDate, changeDate, ufo, indexImmediate);

		if( id == null )
			return false;

        // Set metadata as public if setting enabled
        SettingManager sm = gc.getSettingManager();
        boolean metadataPublic = sm.getValueAsBool("system/csw/metadataPublic", false);

        if (metadataPublic) {
            dataMan.setOperation(context, dbms, id, "1", AccessManager.OPER_VIEW);
        }


		dataMan.indexMetadataGroup(dbms, id);
		
		fileIds.add( uuid );
		
		dbms.commit();
        toIndex.add(id);
		return true;
	}
	
	
	/**
	 *
     * @param request
     * @param xml
     * @param context
     * @param toIndex
     * @return
	 * @throws Exception
	 */
	private int updateTransaction(Element request, Element xml, ServiceContext context, Set<String> toIndex) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();

		if (context.getUserSession().getUserId() == null)
			throw new NoApplicableCodeEx("User not authenticated.");

		int totalUpdated = 0;



        // Update full metadata
        if (xml != null) {

            // Retrieve schema and the related Namespaces
            String schemaId = gc.getSchemamanager().autodetectSchema(xml);

            if (schemaId == null) {
              throw new NoApplicableCodeEx("Can't identify metadata schema");
            }

            // Retrieve the metadata identifier
            Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);


            String uuid = gc.getDataManager().extractUUID(schemaId, xml);

            if (uuid.length() == 0) {
              throw new NoApplicableCodeEx("Metadata identifier not provided");
            }

             // Update metadata record
            String id = dataMan.getMetadataId(dbms, uuid);

            if (id == null)
                return totalUpdated;

            if (!gc.getAccessManager().canEdit(context, id))
                throw new NoApplicableCodeEx("User not allowed to update this metadata("+id+").");

            String changeDate = null;

            boolean validate = false;
            boolean ufo = false;
            boolean index = false;
            String language = context.getLanguage();
            dataMan.updateMetadata(context, dbms, id, xml, validate, ufo, index, language, changeDate, false);

            dbms.commit();
            toIndex.add(id);

           totalUpdated++;

           return totalUpdated;

        // Update properties
        } else {
            //first, search the record in the database to get the record id
            Element constr = (Element) request.getChild("Constraint",Csw.NAMESPACE_CSW ).clone();
            List<Element> results = getResultsFromConstraints(context, constr);


            List<Element> recordProperties = (List<Element>) request.getChildren("RecordProperty",Csw.NAMESPACE_CSW );


            Iterator<Element> it = results.iterator();
            if( !it.hasNext() )
                return totalUpdated;

            Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

            Set updatedMd = new HashSet<String>();
            // Process all records selected
            while( it.hasNext() )
            {
                Element result = it.next();
                String uuid = result.getChildText("identifier", Csw.NAMESPACE_DC);
                String id = dataMan.getMetadataId(dbms, uuid);
                String changeDate = null;

                if( id == null )
                    continue;

                if (!dataMan.getAccessManager().canEdit(context, id))
                    throw new NoApplicableCodeEx("User not allowed to update this metadata("+id+").");


                Element metadata = dataMan.getMetadata(context, id, false, false, true);
                metadata.removeChild("info", Edit.NAMESPACE);

                // Retrieve the schema and Namespaces of metadata to update
                String schemaId = gc.getDataManager().autodetectSchema(metadata);

                if (schemaId == null) {
                  throw new NoApplicableCodeEx("Can't identify metadata schema");
                }

                Map mapNs = retrieveNamepacesForSchema(gc.getDataManager().getSchema(schemaId));

                boolean metadataChanged = false;

                // Process properties to update
                for(Element recordProperty : recordProperties) {
                    Element propertyNameEl = recordProperty.getChild("Name" ,Csw.NAMESPACE_CSW );
                    Element propertyValueEl = recordProperty.getChild("Value" ,Csw.NAMESPACE_CSW );

                    String propertyName = propertyNameEl.getText();

                    String propertyValue = propertyValueEl.getText();

                    // Get XPath for queriable name, i provided in propertyName.
                    // Otherwise assume propertyName contains full XPath to property to update
                    String xpathProperty = FieldMapper.mapXPath(propertyName, schemaId);
                    if (xpathProperty == null) {
                        xpathProperty = propertyName;
                    }

                    Log.info(Geonet.CSW, "Xpath of property: " + xpathProperty);
                    XPath xpath = new JDOMXPath(xpathProperty);
                    xpath.setNamespaceContext(new SimpleNamespaceContext(mapNs));

                    Object propEl = xpath.selectSingleNode(metadata);
                    Log.info(Geonet.CSW, "XPath found in metadata: " + (propEl != null));

                    // If a property is not found in metadata, just ignore it.
                    if (propEl != null) {
                        if (propEl instanceof Element) {
                            ((Element) propEl).setText(propertyValue);
                            metadataChanged = true;

                        } else if (propEl instanceof Attribute) {
                            ((Attribute) propEl).setValue(propertyValue);
                            metadataChanged = true;
                        }
                    }

                } // for(Element recordProperty : recordProperties)

                // Update the metadata with changes
                if (metadataChanged) {
                    boolean validate = false;
                    boolean ufo = false;
                    boolean index = false;
                    String language = context.getLanguage();
                    dataMan.updateMetadata(context, dbms, id, metadata, validate, ufo, index, language, changeDate, false);

                    updatedMd.add(id);

                    totalUpdated++;
                }

            }
            dbms.commit();
            toIndex.addAll(updatedMd);

           return totalUpdated;

        }
	}
	
	/**
	 *
     * @param request
     * @param context
     * @return
	 * @throws Exception
	 */
	private int deleteTransaction(Element request, ServiceContext context) throws Exception
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
	
			dataMan.deleteMetadata(context, dbms, id);
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
        Element filterExpr  = getFilterExpression(constr);
		String filterVersion = getFilterVersion(constr);
		
		ElementSetName  setName = ElementSetName.BRIEF;
		
		Pair<Element, Element> results= _searchController.search(context, 1, 100, ResultType.RESULTS,
				OutputSchema.OGC_CORE, setName, filterExpr, filterVersion, null, null, null, 0, null, null);
		
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

    /**
     * Retrieves namespaces based on metadata schema
     *
     * @param mdSchema
     *
     * @return
     * @throws NoApplicableCodeEx
     */
    private Map<String, String> retrieveNamepacesForSchema(MetadataSchema mdSchema) throws NoApplicableCodeEx {
        Map<String, String> mapNs = new HashMap<String, String>();

        List<Namespace> schemaNsList = mdSchema.getSchemaNS();

        for(Namespace ns : schemaNsList) {
          mapNs.put(ns.getPrefix(), ns.getURI());
        }

        return mapNs;
    }
	
}

//=============================================================================
