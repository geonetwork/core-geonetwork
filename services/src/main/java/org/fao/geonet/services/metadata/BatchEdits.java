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

package org.fao.geonet.services.metadata;

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.editorconfig.BatchEditing;
import org.fao.geonet.kernel.schema.editorconfig.Editor;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.nio.file.Path;
import java.util.*;

/**
 *
 */
@Controller("md.edit.batch")
@ReadWriteController
public class BatchEdits implements ApplicationContextAware { // extends NotInReadOnlyModeService {
    private ApplicationContext context;

    @Autowired
    SchemaManager _schemaManager;


    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Service returning the batch editor configuration for all
     * schema plugin loaded.
     *
     * Batch editor configuration is defined in config-editor.xml in each
     * schema plugins.
     *
     * @param schema Optional schema identifier to retrieve config to one or more
     *               schema and not all.
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{lang}/md.edit.batch.config", produces = {MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    Map<String, BatchEditing> getConfiguration(
            @RequestParam(required = false) String[] schema) throws Exception {
        List<String> listOfRequestedSchema = schema == null ? new ArrayList<String>() : Arrays.asList(schema);
        Set<String> listOfSchemas = _schemaManager.getSchemas();
        Map<String, BatchEditing> schemasConfig = new HashMap<>();
        for (String schemaIdentifier : listOfSchemas) {
            if (listOfRequestedSchema.size() == 0 || listOfRequestedSchema.contains(schemaIdentifier)) {
                MetadataSchema metadataSchema = _schemaManager.getSchema(schemaIdentifier);
                Path metadataSchemaConfig = metadataSchema.getSchemaDir().resolve("layout").resolve("config-editor.xml");
                if (metadataSchemaConfig.toFile().exists()) {
                    JAXBContext jaxbContext = null;
                    Editor editorConfiguration = null;
                    try {
                        jaxbContext = JAXBContext.newInstance(Editor.class);
                        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                        editorConfiguration = (Editor) unmarshaller.unmarshal(metadataSchemaConfig.toFile());
                        schemasConfig.put(schemaIdentifier, editorConfiguration.getBatchEditing());
                    } catch (JAXBException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return schemasConfig;
    }

        /**
         * Service providing 2 types of metadata edits:
         * <ul>
         *     <li>Search & replace</li>
         *     <li>Inserts</li>
         * </ul>
         *
         * The service applies to the current selection or a set of uuids.
         *
         * <h4>Search & replace</h4>
         * Parameters for search and replace is composed of a set of parameters composed of
         * <ul>
         *     <li>xpath_id: The XPath of the element to search in</li>
         *     <li>search_id: The value to search in the matching element (can be a regex)</li>
         *     <li>replace_id: The replacement.</li>
         *     <li>? case insensitive ?</li>
         * </ul>
         *
         *
         * eg.
         * <ul>
         *     <li>xpath: //gmd:MD_Keywords</li>
         * </ul>
         *
         *
         * <h4>Schema plugin replacement list</h4>
         *
         * The schema plugin can provide a list of predifined elements that can be updated
         * to avoid end-user to type XPath. The location is described by:
         * <ul>
         *     <li>An XPath expression</li>
         *     <li>A label</li>
         * </ul>
         *
         * <h4>Batch editing</h4>
         *
         * Parameters:
         * <ul>
         *     <li>xpath_id: The XPath of the element to search in</li>
         *     <li>replace_id: The value to search in the matching element (can be a regex)</li>
         *     <li>mode: Adding, Replace</li>
         * </ul>
         *
         * field, value, mode (add if absent, replace existing)
         * eg.
         * Identification:
         * title
         * keyword
         * constraints
         * Contact: metadata contact, resource contact, distributor : from directory only XML + role
         * Related resources: parentIdentifier, source dataset
         *
         *
         * Actions
         * Run / Stop
         * Process report
         * md.batch.edit
         * xpath_1=
         * value=string or XML
         *
         * <h4>Deprecated features</h4>
         *
         * This will replace:
         * <ul>
         *     <li>search & replace process</li>
         *     <li>keyword mapper</li>
         * </ul>
         *
         * @return
         * @throws Exception
         */
    @RequestMapping(value = "/{lang}/md.edit.batch", produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    XslProcessingReport serviceSpecificExec(
                                            @RequestParam(required = false) String[] uuid,
                                            @RequestParam(required = false) String mode,
                                            @RequestParam MultiValueMap<String, String> parameters,
                                            @PathVariable String lang,
                                            HttpSession session,
                                            HttpServletRequest request) throws Exception {

        ServiceContext serviceContext = ServiceContext.get();
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        DataManager dataMan = appContext.getBean(DataManager.class);
        XslProcessing xslProcessing = appContext.getBean(XslProcessing.class);
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);


        Iterator<String> keyIterator = parameters.keySet().iterator();
        List<BatchEditParameter> listOfupdates = new ArrayList<BatchEditParameter>();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            // Check all xpaths defined
            if (key.startsWith("xpath_")) {
                String[] token = key.split("\\_");
                if (token.length != 2) {
                    throw new IllegalArgumentException(String.format(
                            "Parameter %s is not well formed. It should be xpath_<id>.", key));
                }

                String tripletId = token[1];
                // TODO: We should check no duplicated parameters
                // Avoid NPE on missing param
                String xpath = parameters.get("xpath_" + tripletId).get(0);
                String searchValue = parameters.get("search_" + tripletId).get(0);
                String replaceValue = parameters.get("replace_" + tripletId).get(0);

                // TODO: Warning on parameter checks
                // If no replace return error
                if (StringUtils.isEmpty(xpath)) {
                    throw new IllegalArgumentException(String.format(
                            "Parameter xpath_%s is not set. It should be not empty and define the XPath of the element to update.", tripletId, xpath));
                }
                if (replaceValue == null) {
                    throw new IllegalArgumentException(String.format(
                            "Parameter replace_%s is not set (XPath is '%s').", tripletId, xpath));
                }
                listOfupdates.add(new BatchEditParameter(xpath, searchValue, replaceValue));
            }
        }

        if (listOfupdates.size() == 0) {
            throw new IllegalArgumentException("At least one replacement must be defined. Use xpath_<id>, search_<id>, replace_<id> parameters in order to define edits.");
        }

        final String siteURL = request.getRequestURL().toString() + "?" + request.getQueryString();
        if (serviceContext == null) {
            throw new IllegalStateException("There needs to be a ServiceContext in the thread local for this thread");
        }

        SelectionManager selectionManager =
                SelectionManager.getManager(serviceContext.getUserSession());

        final Set<String> setOfUuidsToValidate;

        if(uuid == null) {
            synchronized (selectionManager.getSelection("metadata")) {
                final Set<String> selection = selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
                setOfUuidsToValidate = Sets.newHashSet(selection);
            }
        } else {
            setOfUuidsToValidate = Sets.newHashSet(Arrays.asList(uuid));
        }
        SchemaManager _schemaManager = context.getBean(SchemaManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);
        String changeDate = null;
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        for (String recordUuid : setOfUuidsToValidate) {
            Metadata record = metadataRepository.findOneByUuid(recordUuid);
            if (record == null) {
//                        this.report.get("notFoundRecords").add(record.getId());
            } else if (!accessMan.isOwner(serviceContext, String.valueOf(record.getId()))) {
//                        this.report.get("notOwnerRecords").add(record.getId());
            } else {
                // Processing
                EditLib editLib = new EditLib(_schemaManager);
                MetadataSchema metadataSchema = _schemaManager.getSchema(record.getDataInfo().getSchemaId());
                final String settingId = SettingManager.CSW_TRANSACTION_XPATH_UPDATE_CREATE_NEW_ELEMENTS;
                boolean createXpathNodeIfNotExists = context.getBean(SettingManager.class).getValueAsBool(settingId);
                Element metadata = record.getXmlData(false);
                boolean metadataChanged = false;

                Iterator<BatchEditParameter> listOfUpdatesIterator = listOfupdates.iterator();
                while (listOfUpdatesIterator.hasNext()) {
                    BatchEditParameter batchEditParameter = listOfUpdatesIterator.next();

                    Element xmlValue = null;
                    AddElemValue propertyValue = null;
                    propertyValue = new AddElemValue(batchEditParameter.getReplaceValue());

                    metadataChanged = editLib.addElementOrFragmentFromXpath(
                            metadata,
                            metadataSchema,
                            batchEditParameter.getXpath(),
                            propertyValue,
                            createXpathNodeIfNotExists
                    );
                }
                if (metadataChanged) {
                    boolean validate = false;
                    boolean ufo = false;
                    boolean index = true;
                    dataMan.updateMetadata(serviceContext, record.getId() + "", metadata,
                            validate, ufo, index, lang, changeDate, false);
//                    updatedMd.add(id);
//                    totalUpdated++;
                }

            }
        }

        //ServiceContext context = serviceManager.createServiceContext("md.processing.batch", lang, request);

        return null;
    }
}
