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

package org.fao.geonet.component.csw;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.utils.ObjectJSONUtils;
import org.fao.geonet.events.history.RecordDeletedEvent;
import org.fao.geonet.events.history.RecordImportedEvent;
import org.fao.geonet.events.history.RecordUpdatedEvent;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.AbstractOperation;
import org.fao.geonet.kernel.csw.services.getrecords.FieldMapper;
import org.fao.geonet.kernel.csw.services.getrecords.SearchController;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.locationtech.jts.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

//=============================================================================
/**
 * CSW transaction operation.
 */
@Component(CatalogService.BEAN_PREFIX + Transaction.NAME)
public class Transaction extends AbstractOperation implements CatalogService {
    private static final boolean applyUpdateFixedInfo = true;
    private static final boolean applyValidation = false;

    static final String NAME = "Transaction";
    @Autowired
    SchemaManager _schemaManager;
    @Autowired
    private SearchController _searchController;
    @Autowired
    private FieldMapper _fieldMapper;

    @Autowired
    public Transaction(ApplicationContext context) {

    }


    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    @Override
    public String getName() {
        return NAME;
    }

    //---------------------------------------------------------------------------

    static class InsertedMetadata{
        String schema;
        String id;
        Element content;

        public InsertedMetadata(String schema, String id, Element content) {
            this.schema = schema;
            this.id = id;
            this.content = content;
        }
    }

    @Override
    public Element execute(Element request, ServiceContext context) throws CatalogException {
        checkService(request);
        checkVersion(request);

        //num counter
        int totalInserted = 0;
        int totalUpdated = 0;
        int totalDeleted = 0;

        // Response element
        Element response = new Element(getName() + "Response", Csw.NAMESPACE_CSW);

        List<InsertedMetadata> inserted = new ArrayList<>();

        //process the transaction from the first to the last
        @SuppressWarnings("unchecked")
        List<Element> childList = request.getChildren();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);

        Set<String> toIndex = new HashSet<>();

        try {
            //process the childlist
            for (Element transRequest : childList) {
                String transactionType = transRequest.getName().toLowerCase();
                if (transactionType.equals("insert") || transactionType.equals("update") || transactionType.equals("delete")) {
                    @SuppressWarnings("unchecked")
                    List<Element> mdList = transRequest.getChildren();

                    // insert to database, and get the number of inserted successful
                    if (transactionType.equals("insert")) {
                        for (Element aMdList : mdList) {
                            Element metadata = (Element) aMdList.clone();
                            boolean insertSuccess = insertTransaction(metadata, inserted, context, toIndex);
                            if (insertSuccess) {
                                totalInserted++;
                            }
                        }
                    } else if (transactionType.equals("update")) {
                        // Update
                        Element metadata = null;
                        for (Element reqElem : mdList) {
                            if (reqElem.getNamespace() != Csw.NAMESPACE_CSW) {
                                metadata = (Element) reqElem.clone();
                            }
                        }

                        totalUpdated = updateTransaction(transRequest, metadata, context, toIndex);
                    } else {
                        // Delete
                        totalDeleted = deleteTransaction(transRequest, context);
                    }
                }
            }
        } catch (Exception e) {
            Log.error(Geonet.CSW, "Cannot process transaction");
            Log.error(Geonet.CSW, " (C) StackTrace\n" + Util.getStackTrace(e));

            throw new NoApplicableCodeEx("Cannot process transaction: " + e.getMessage());
        } finally {
            try {
                dataMan.indexMetadata(new ArrayList<>(toIndex));
            } catch (Exception e) {
                Log.error(Geonet.CSW, "cannot index");
                Log.error(Geonet.CSW, " (C) StackTrace\n" + Util.getStackTrace(e));
            }
            getResponseResult(context, request, response, inserted, totalInserted, totalUpdated, totalDeleted);
        }

        return response;
    }

    //---------------------------------------------------------------------------

    @Override
    public Element adaptGetRequest(Map<String, String> params) {
        return new Element(getName(), Csw.NAMESPACE_CSW);
    }

    //---------------------------------------------------------------------------

    @Override
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
     * @param context
     * @param toIndex
     * @return
     * @throws Exception
     */
    private boolean insertTransaction(Element xml, List<InsertedMetadata> documents, ServiceContext context, Set<String> toIndex)
        throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);

        String schema = dataMan.autodetectSchema(xml);

//		String category   = Util.getParam(request, Params.CATEGORY);
        String category = null, source = null, createDate = null, changeDate = null;

        String uuid;
        uuid = dataMan.extractUUID(schema, xml);
        if (uuid.length() == 0)
            uuid = UUID.randomUUID().toString();

        // -----------------------------------------------------------------------
        // --- insert metadata into the system

        UserSession us = context.getUserSession();

        if (us.getUserId() == null)
            throw new NoApplicableCodeEx("User not authenticated.");

        Profile profile = us.getProfile();

        // Only editors and above are allowed to insert metadata
        if (profile != Profile.Editor && profile != Profile.Reviewer
            && profile != Profile.UserAdmin && profile != Profile.Administrator)
            throw new NoApplicableCodeEx("User not allowed to insert metadata.");

        int userId = us.getUserIdAsInt();

        AccessManager am = gc.getBean(AccessManager.class);

        // Set default group: user first group
        Set<Integer> userGroups = am.getVisibleGroups(userId);
        String group;
        if (userGroups.isEmpty()) {
            group = null;
        } else {
            group = userGroups.iterator().next().toString();
        }
        //
        // insert metadata
        //
        String docType = null, isTemplate = null;
        String id = dataMan.insertMetadata(context, schema, xml, uuid, userId, group, source,
            isTemplate, docType, category, createDate, changeDate, applyUpdateFixedInfo, IndexingMode.none);

        // Privileges for the first group of the user that inserts the metadata
        // (same permissions as when inserting xml file from UI)
        if (group != null) {
            for (ReservedOperation op : ReservedOperation.values()) {
                dataMan.unsetOperation(context, id, group, op);
            }
        }

        // Set metadata as public if setting enabled
        SettingManager sm = gc.getBean(SettingManager.class);
        boolean metadataPublic = sm.getValueAsBool(Settings.SYSTEM_CSW_METADATA_PUBLIC, false);

        if (metadataPublic) {
            dataMan.setOperation(context, id, "" + ReservedGroup.all.getId(), ReservedOperation.view);
            dataMan.setOperation(context, id, "" + ReservedGroup.all.getId(), ReservedOperation.download);
            dataMan.setOperation(context, id, "" + ReservedGroup.all.getId(), ReservedOperation.dynamic);
        }

        dataMan.indexMetadata(id, true);

        AbstractMetadata metadata = metadataUtils.findOne(id);
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        new RecordImportedEvent(metadata.getId(), us.getUserIdAsInt(),
            ObjectJSONUtils.convertObjectInJsonObject(us.getPrincipal(),
                RecordImportedEvent.FIELD),
            metadata.getData()).publish(applicationContext);

        documents.add(new InsertedMetadata(schema, id, xml));

        toIndex.add(id);
        return true;
    }

    @Autowired
    IMetadataUtils metadataUtils;


    /**
     * @param request
     * @param xml
     * @param context
     * @param toIndex
     * @return
     * @throws Exception
     */
    private int updateTransaction(Element request, Element xml, ServiceContext context, Set<String> toIndex) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);

        if (context.getUserSession().getUserId() == null) {
            throw new NoApplicableCodeEx("User not authenticated.");
        }

        int totalUpdated = 0;


        Element beforeMetadata;
        Element afterMetadata;

        // Update full metadata
        if (xml != null) {

            // Retrieve schema and the related Namespaces
            String schemaId = gc.getBean(SchemaManager.class).autodetectSchema(xml);

            if (schemaId == null) {
                throw new NoApplicableCodeEx("Can't identify metadata schema");
            }

            // Retrieve the metadata identifier

            String uuid = gc.getBean(DataManager.class).extractUUID(schemaId, xml);

            if (uuid.length() == 0) {
                throw new NoApplicableCodeEx("Metadata identifier not provided");
            }

            // Update metadata record
            String id = dataMan.getMetadataId(uuid);

            if (id == null) {
                return totalUpdated;
            }

            if (!gc.getBean(AccessManager.class).canEdit(context, id)) {
                throw new NoApplicableCodeEx("User not allowed to update this metadata(" + id + ").");
            }

            String changeDate = null;

            try {
                changeDate = metadataUtils.extractDateModified(schemaId, xml);
            } catch (Exception ex) {
                changeDate = new ISODate().toString();
            }

            beforeMetadata = metadataUtils.findOneByUuid(uuid).getXmlData(false);

            String language = context.getLanguage();
            dataMan.updateMetadata(context, id, xml,
                applyValidation, applyUpdateFixedInfo,
                language, changeDate, true, IndexingMode.none);

            afterMetadata = metadataUtils.findOneByUuid(uuid).getXmlData(false);

            XMLOutputter outp = new XMLOutputter();
            String xmlBefore = outp.outputString(beforeMetadata);
            String xmlAfter = outp.outputString(afterMetadata);
            new RecordUpdatedEvent(Long.parseLong(id),
                context.getUserSession().getUserIdAsInt(),
                xmlBefore, xmlAfter)
                .publish(ApplicationContextHolder.get());

            toIndex.add(id);

            totalUpdated++;

            return totalUpdated;

            // Update properties
        } else {
            //first, search the record in the database to get the record id
            final List<Element> constraints = request.getChildren("Constraint", Csw.NAMESPACE_CSW);
            Assert.isTrue(constraints.size() == 1, "One and only 1 constraint allowed in update query.  Found : " + constraints.size());
            Element constr = (Element) constraints.get(0).clone();
            List<Element> results = getResultsFromConstraints(context, constr);


            @SuppressWarnings("unchecked")
            List<Element> recordProperties = request.getChildren("RecordProperty", Csw.NAMESPACE_CSW);


            Iterator<Element> it = results.iterator();
            if (!it.hasNext())
                return totalUpdated;

            Set<String> updatedMd = new HashSet<String>();
            // Process all records selected
            while (it.hasNext()) {
                Element result = it.next();
                String uuid = result.getChildText("identifier", Csw.NAMESPACE_DC);
                String id = dataMan.getMetadataId(uuid);
                String changeDate = null;

                if (id == null)
                    continue;

                if (!dataMan.getAccessManager().canEdit(context, id))
                    throw new NoApplicableCodeEx("User not allowed to update this metadata(" + id + ").");


                Element metadata = dataMan.getMetadata(context, id, false, false, true);
                beforeMetadata = metadata;

                metadata.removeChild("info", Edit.NAMESPACE);

                // Retrieve the schema and Namespaces of metadata to update
                String schemaId = gc.getBean(DataManager.class).autodetectSchema(metadata);

                if (schemaId == null) {
                    throw new NoApplicableCodeEx("Can't identify metadata schema");
                }

                boolean metadataChanged = false;
                EditLib editLib = new EditLib(_schemaManager);

                MetadataSchema metadataSchema = _schemaManager.getSchema(schemaId);
                final String settingId = Settings.SYSTEM_CSW_TRANSACTION_XPATH_UPDATE_CREATE_NEW_ELEMENTS;
                boolean createXpathNodeIfNotExists = gc.getBean(SettingManager.class).getValueAsBool(settingId);

                // Process properties to update
                for (Element recordProperty : recordProperties) {
                    Element propertyNameEl = recordProperty.getChild("Name", Csw.NAMESPACE_CSW);
                    Element propertyValueEl = recordProperty.getChild("Value", Csw.NAMESPACE_CSW);

                    String propertyName = propertyNameEl.getText();

                    // Get XPath for queriable name, i provided in propertyName.
                    // Otherwise assume propertyName contains full XPath to property to update
                    String xpathProperty = _fieldMapper.mapXPath(propertyName, schemaId);
                    if (xpathProperty == null) {
                        xpathProperty = propertyName;
                    }

                    Log.info(Geonet.CSW, "Xpath of property: " + xpathProperty);

                    final List<Element> children = propertyValueEl.getChildren();
                    AddElemValue propertyValue;
                    if (children.isEmpty()) {
                        propertyValue = new AddElemValue(propertyValueEl.getText());
                        metadataChanged |= editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, xpathProperty, propertyValue,
                            createXpathNodeIfNotExists);
                    } else {
                        for (Element child : children) {
                            propertyValue = new AddElemValue((Element) child.clone());
                            metadataChanged |= editLib.addElementOrFragmentFromXpath(metadata, metadataSchema, xpathProperty, propertyValue,
                                createXpathNodeIfNotExists);
                        }
                    }

                    Log.info(Geonet.CSW, "Metadata has been updated: " + metadataChanged);

                }

                // Update the metadata with changes
                if (metadataChanged) {
                    try {
                        changeDate = metadataUtils.extractDateModified(schemaId, metadata);
                    } catch (Exception ex) {
                        changeDate = new ISODate().toString();
                    }
                    String language = context.getLanguage();
                    dataMan.updateMetadata(context, id, metadata,
                        applyValidation, applyUpdateFixedInfo,
                        language, changeDate, true, IndexingMode.none);

                    updatedMd.add(id);

                    totalUpdated++;
                }
                afterMetadata = metadataUtils.findOneByUuid(uuid).getXmlData(false);

                XMLOutputter outp = new XMLOutputter();
                String xmlBefore = outp.outputString(beforeMetadata);
                String xmlAfter = outp.outputString(afterMetadata);
                new RecordUpdatedEvent(Long.parseLong(id),
                    context.getUserSession().getUserIdAsInt(),
                    xmlBefore, xmlAfter)
                    .publish(ApplicationContextHolder.get());
            }

            toIndex.addAll(updatedMd);

            return totalUpdated;

        }
    }

    /**
     * @param request
     * @param context
     * @return
     * @throws Exception
     */
    private int deleteTransaction(Element request, ServiceContext context) throws Exception {
        int deleted = 0;

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);
        IMetadataManager metadataManager = gc.getBean(IMetadataManager.class);

        if (context.getUserSession().getUserId() == null)
            throw new NoApplicableCodeEx("User not authenticated.");

        //first, search the record in the database to get the record id
        Element constr = request.getChild("Constraint", Csw.NAMESPACE_CSW);
        List<Element> results = getResultsFromConstraints(context, constr);

        //second, delete the metadata in the dbms using the id
        Iterator<Element> i = results.iterator();
        if (!i.hasNext())
            return deleted;

        // delete all matching records
        while (i.hasNext()) {
            Element result = i.next();
            String uuid = result.getChildText("identifier", Csw.NAMESPACE_DC);
            String id = dataMan.getMetadataId(uuid);

            if (id == null) {
                return deleted;
            }

            if (!dataMan.getAccessManager().canEdit(context, id)) {
                throw new NoApplicableCodeEx("User not allowed to delete metadata : " + id);
            }
            AbstractMetadata metadata = metadataUtils.findOneByUuid(uuid);
            LinkedHashMap<String, String> titles = new LinkedHashMap<>();
            try {
                titles = metadataUtils.extractTitles(Integer.toString(metadata.getId()));
            } catch (Exception e) {
                Log.warning(Geonet.DATA_MANAGER,
                    String.format(
                        "Error while extracting title for the metadata %d " +
                            "while creating delete event. Error is %s.",
                        metadata.getId(), e.getMessage()));
            }
            RecordDeletedEvent recordDeletedEvent = new RecordDeletedEvent(
                metadata.getId(), metadata.getUuid(), titles,
                context.getUserSession().getUserIdAsInt(),
                metadata.getData());

            metadataManager.deleteMetadata(context, id);
            recordDeletedEvent.publish(ApplicationContextHolder.get());

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
        Element filterExpr = getFilterExpression(constr);
        String filterVersion = getFilterVersion(constr);

        ElementSetName setName = ElementSetName.BRIEF;

        Element results = _searchController.search(context, 1, 100, ResultType.RESULTS,
            OutputSchema.DEFAULT.toString(), setName, filterExpr, filterVersion, null, null, null, 0, null);

        @SuppressWarnings("unchecked")
        List<Element> children = results.getChildren();
        return children;
    }

    /**
     * @param request
     * @param response
     * @param totalInserted
     * @param totalUpdated
     * @param totalDeleted
     */
    private void getResponseResult(ServiceContext context, Element request, Element response, List<InsertedMetadata> inserted,
                                   int totalInserted, int totalUpdated, int totalDeleted) {
        // transactionSummary
        Element transactionSummary = getTransactionSummary(totalInserted, totalUpdated, totalDeleted);

        // requestID
        String strRequestId = request.getAttributeValue("requestId");
        if (strRequestId != null)
            transactionSummary.setAttribute("requestId", strRequestId);

        response.addContent(transactionSummary);

        if (totalInserted > 0) {
            Element insertResult = new Element("InsertResult", Csw.NAMESPACE_CSW);
            insertResult.setAttribute("handleRef", "handleRefValue");

            for (InsertedMetadata md : inserted) {
                Element briefRecord;

                try {
                    briefRecord = applyCswBrief(context, _schemaManager, md.schema, md.content, md.id, context.getLanguage());

                } catch(Exception e) {
                    // let's mock a BriefRecord, it's not complete, but it may be useful anyway
                    Log.warning(Geonet.CSW, "Error transforming metadata: " + md.id, e);

                    briefRecord = new Element("BriefRecord", Csw.NAMESPACE_CSW);
                    Element identifier = new Element("identifier", Csw.NAMESPACE_DC );
                    // TODO: title is mandatory
                    identifier.setText(md.id);
                    briefRecord.addContent(identifier);
                }
                insertResult.addContent(briefRecord);
            }

            response.addContent(insertResult);
        }
    }

    private static Element applyCswBrief(ServiceContext context, SchemaManager schemaManager, String schema,
                                         Element md,
                                         String id, String displayLanguage) throws Exception
    {
        return org.fao.geonet.csw.common.util.Xml.applyElementSetName(
            context, schemaManager, schema, md,
            "csw", ElementSetName.BRIEF, ResultType.RESULTS,
            id, displayLanguage);
    }

    /**
     * @param totalInserted
     * @param totalUpdated
     * @param totalDeleted
     * @return
     */
    private Element getTransactionSummary(int totalInserted, int totalUpdated, int totalDeleted) {
        Element transactionSummary = new Element("TransactionSummary", Csw.NAMESPACE_CSW);
//		if( totalInserted>0 )
//		{
        Element insert = new Element("totalInserted", Csw.NAMESPACE_CSW);
        insert.setText(Integer.toString(totalInserted));
        transactionSummary.addContent(insert);

//		}
//		if( totalUpdated>0 )
//		{
        Element update = new Element("totalUpdated", Csw.NAMESPACE_CSW);
        update.setText(Integer.toString(totalUpdated));
        transactionSummary.addContent(update);
//		}
//		if( totalDeleted>0 )
//		{
        Element delete = new Element("totalDeleted", Csw.NAMESPACE_CSW);
        delete.setText(Integer.toString(totalDeleted));
        transactionSummary.addContent(delete);
//		}

        return transactionSummary;
    }

}
