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

package org.fao.geonet.kernel.harvest.harvester.fragment;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;

//=============================================================================

/**
 * A fragment harvester used by other harvesters (THREDDS/Metadata Fragments) to create metadata
 * and/or sub-templates from metadata fragments they have harvested
 **/

public class FragmentHarvester extends BaseAligner {

    static private final Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

    private static final String REPLACEMENT_GROUP = "replacementGroup";
    private Logger log;
    private ServiceContext context;
    private DataManager dataMan;
    private IMetadataManager metadataManager;
    private FragmentParams params;
    private String metadataGetService;
    private List<Namespace> metadataTemplateNamespaces = new ArrayList<Namespace>();
    private Set<String> templateIdAtts = new HashSet<String>();
    private Element metadataTemplate;
    private String harvestUri;
    private CategoryMapper localCateg;
    private GroupMapper localGroups;
    private HarvestSummary harvestSummary;

    //---------------------------------------------------------------------------
    private List<Privileges> fragmentAllPrivs = new ArrayList<Privileges>();


    /**
     * Constructor
     *
     * @param context Jeeves context
     * @param params  Fragment harvesting configuration parameters
     */
    public FragmentHarvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, FragmentParams params) {
        super(cancelMonitor);
        this.log = log;
        this.context = context;
        this.params = params;

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        dataMan = gc.getBean(DataManager.class);
        metadataManager = gc.getBean(IMetadataManager.class);

        SettingInfo si = context.getBean(SettingInfo.class);
        String siteUrl = si.getSiteUrl() + context.getBaseUrl();
        metadataGetService = "local://"+context.getNodeId()+"/api/records/";

        if (params.templateId != null && !params.templateId.equals("") && !params.templateId.equals("0")) {
            loadTemplate();
        }

        // create privileges for fragments - visible to all (group 1)
        Privileges fragmentPrivs = new Privileges("1"); // group 1 = All
        fragmentPrivs.add(0);  // operation 0 = view
        fragmentAllPrivs.add(fragmentPrivs);

    }

    //---------------------------------------------------------------------------

    /**
     * Create subtemplates/metadata from fragments read from WFS
     *
     * Typical response expected:
     *
     * <?xml version="1.0" encoding="utf-8"?> <records> <record> <fragment title="John P BlockHead -
     * CSIRO" id="contactinfo" uuid="contactinfo-18"> <gmd:CI_ResponsibleParty> <gmd:individualName>
     * <gco:CharacterString>John P BlockHead</gco:CharacterString> </gmd:individualName>
     * <gmd:organisationName> <gco:CharacterString>CSIRO Division of Marine and Atmospheric Research
     * (CMAR)</gco:CharacterString> </gmd:organisationName> <gmd:role> <gmd:CI_RoleCode
     * codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_RoleCode"
     * codeListValue="custodian"/> </gmd:role> </gmd:CI_ResponsibleParty> </fragment> <fragment ...>
     * ... </fragment> </record> </records>
     *
     * @param fragments  Fragments to use to create metadata/subtemplates
     * @param harvestUri URI being harvested if any
     */
    public HarvestSummary harvest(Element fragments, String harvestUri) throws Exception {
        this.harvestUri = harvestUri;
        harvestSummary = new HarvestSummary();

        if (fragments == null || !fragments.getName().equals("records")) {
            throw new BadXmlResponseEx("<records> not found in response: \n" + Xml.getString(fragments));
        }

        //--- Loading categories and groups
        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);

        @SuppressWarnings("unchecked")
        List<Element> recs = fragments.getChildren();

        for (Element rec : recs) {
            if (cancelMonitor.get()) {
                break;
            }

            addRecord(rec);
        }

        return harvestSummary;
    }

    /**
     * Load metadata template to be used to generate metadata
     */
    private void loadTemplate() {
        try {
            //--- Load template to be used to create metadata from fragments
            metadataTemplate = metadataManager.getMetadata(params.templateId);

            //--- Build a list of all Namespaces in the metadata document
            Namespace ns = metadataTemplate.getNamespace();
            if (ns != null) {
                metadataTemplateNamespaces.add(ns);
                @SuppressWarnings("unchecked")
                List<Namespace> additionalNamespaces = metadataTemplate.getAdditionalNamespaces();
                metadataTemplateNamespaces.addAll(additionalNamespaces);
            }

            // --- Build a list of all id attributes in metadata document so
            // --- that we can remove any that are left over afterwards
            new Document(metadataTemplate);
            List<?> elems = Xml.selectNodes(metadataTemplate, "//*[@id]", metadataTemplateNamespaces);
            for (Iterator<?> iter = elems.iterator(); iter.hasNext(); ) {
                Object ob = iter.next();
                if (ob instanceof Element) {
                    Element elem = (Element) ob;
                    String idValue = elem.getAttributeValue("id");
                    templateIdAtts.add(idValue);
                }
            }

        } catch (Exception e) {
            log.error("Thrown Exception " + e + " opening template with id: " + params.templateId);
            log.error(e);
        }
    }

    /**
     * Add subtemplates and/or metadata using fragments and metadata template
     *
     * @param rec record containing fragments to add to GeoNetwork database
     */
    private void addRecord(Element rec) throws Exception {
        @SuppressWarnings("unchecked")
        List<Element> fragments = rec.getChildren();

        Element recordMetadata = null;
        Set<String> recordMetadataRefs = new HashSet<String>();

        if (metadataTemplate != null) {
            recordMetadata = (Element) metadataTemplate.clone();
            recordMetadataRefs.addAll(templateIdAtts);

            //Jaxen requires element to be associated with a
            //document to correctly interpret XPath expressions
            new Document(recordMetadata);
        }

        for (Element fragment : fragments) {
            addMetadata(fragment);

            if (params.createSubtemplates) {
                createSubtemplates(fragment);
            }

            if (recordMetadata != null) {
                updateMetadataReferences(recordMetadata, recordMetadataRefs, fragment);
            }
        }

        if (recordMetadata != null) {
            cleanupRecord(recordMetadata, recordMetadataRefs);
        }

        harvestSummary.fragmentsReturned += fragments.size();

        if (recordMetadata != null) {
            String recUuid = rec.getAttributeValue("uuid");

            // Use random uuid if none provided
            if (recUuid == null || recUuid.trim().equals("")) {
                recUuid = UUID.randomUUID().toString();
            }

            String id = dataMan.getMetadataId(recUuid);
            if (id == null) {
                log.debug("Adding metadata " + recUuid);
                createMetadata(recUuid, recordMetadata);
            } else {
                log.debug("Updating metadata " + recUuid + " with id : " + id);
                updateMetadata(recUuid, id, recordMetadata);
            }
        }
    }

    /**
     * Add fragment metadata to the fragment
     *
     * @param fragment fragment to which metadata should be added
     */
    private void addMetadata(Element fragment) {
        if (fragment.getName().equals(REPLACEMENT_GROUP)) {
            @SuppressWarnings("unchecked")
            List<Element> children = fragment.getChildren();
            for (Element child : children) {
                addFragmentMetadata(child);
            }
        } else {
            addFragmentMetadata(fragment);
        }
    }

    /**
     * Add a random uuid to the xml fragment if one hasn't been provided and the schema of the xml
     * fragment
     *
     * @param fragment fragment to which metadata should be added
     */
    private void addFragmentMetadata(Element fragment) {
        // Add uuid if not supplied
        String uuid = fragment.getAttributeValue("uuid");

        if (uuid == null || uuid.equals("")) {
            uuid = UUID.randomUUID().toString();
            if (log.isDebugEnabled())
                log.debug("  - Metadata fragment did not have uuid! Fragment XML is " + Xml.getString(fragment));
            fragment.setAttribute("uuid", uuid);
        }

        // Add schema as an attribute (if not already present)
        String setSchema = fragment.getAttributeValue("schema");
        if (setSchema == null || setSchema.trim().length() == 0) {
            fragment.setAttribute("schema", params.outputSchema);
        } else {
            if (!dataMan.existsSchema(setSchema)) {
                log.warning("Skipping fragment with schema set to unknown schema: " + setSchema);
                harvestSummary.fragmentsUnknownSchema++;
            }
        }

    }

    /**
     * Create a sub-templates for the provided fragment
     *
     * @param fragment fragment for which sub-templates should be created
     */
    private void createSubtemplates(Element fragment) throws Exception {
        if (fragment.getName().equals(REPLACEMENT_GROUP)) {
            @SuppressWarnings("unchecked")
            List<Element> children = fragment.getChildren();
            for (Element child : children) {
                createOrUpdateSubtemplate(child);
            }
        } else {
            createOrUpdateSubtemplate(fragment);
        }
    }

    /**
     * Create or update a sub-template for an xml fragment
     *
     * @param fragment fragment for which a sub-template should be created
     */
    private void createOrUpdateSubtemplate(Element fragment) throws Exception {
        String uuid = fragment.getAttributeValue("uuid");

        String schema = fragment.getAttributeValue("schema");

        String title = fragment.getAttributeValue("title");

        if (schema == null) {
            return;  //skip fragments with unknown schema
        }

        Element md = (Element) fragment.getChildren().get(0);

        String reference = metadataGetService+uuid;
        String id = dataMan.getMetadataId(uuid);
        if (id == null) {
            createSubtemplate(schema, md, uuid, title);
        } else {
            updateSubtemplate(id, uuid, md, title);
            Processor.uncacheXLinkUri(reference);
        }

        // Now shove the subtemplate into the xlink cache so it is ready for use
        Processor.addXLinkToCache(reference, md);
    }

    /**
     * Update a sub-template for an xml fragment
     *
     * @param id   id of subtemplate to update
     * @param uuid uuid of subtemplate being updated
     * @param md   Subtemplate
     * @param title     Subtemplate title
     */
    private void updateSubtemplate(String id, String uuid, Element md, String title) throws Exception {
        update(id, md, title, true);
        harvestSummary.updatedMetadata.add(uuid);
        harvestSummary.fragmentsUpdated++;
    }
    ;

    /**
     * Create a sub-template for an xml fragment
     *
     * @param schema Schema to which the sub-template belongs
     * @param md     Subtemplate
     * @param uuid   Uuid of subtemplate
     */
    private void createSubtemplate(String schema, Element md, String uuid, String title) throws Exception {
        //
        // insert metadata
        //
        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(uuid);
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(md.getQualifiedName()).
            setType(MetadataType.SUB_TEMPLATE).
            setTitle(title);
        metadata.getSourceInfo().
            setSourceId(params.uuid).
            setOwner(Integer.parseInt(params.owner));
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.uuid).
            setUri(harvestUri);

        addCategories(metadata, params.categories, localCateg, context, null, false);

        metadata = metadataManager.insertMetadata(context, metadata, md, true, false, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        // Note: we use fragmentAllPrivs here because subtemplates need to be
        // visible/accessible to all
        addPrivileges(id, fragmentAllPrivs, localGroups, context);
        dataMan.indexMetadata(id, true, null);

        metadataManager.flush();

        harvestSummary.fragmentsAdded++;
    }

    /**
     * Update references in the template to the fragment with the fragment or an xlink to the
     * sub-template created for it
     *
     * @param template     template to update
     * @param templateRefs names of id attributes in template
     * @param fragment     fragment referenced
     */
    private void updateMetadataReferences(Element template, Set<String> templateRefs, Element fragment) throws Exception {
        String matchId = fragment.getAttributeValue("id");

        if (matchId == null || matchId.equals("")) {
            log.error(fragment.getName() + " can't be matched because it has no id attribute " + Xml.getString(fragment));
            return;
        }

        // find all elements that have an attribute id with the matchId
        if (log.isDebugEnabled())
            log.debug("Attempting to search metadata for " + matchId);
        List<?> elems = Xml.selectNodes(template, "//*[@id='" + matchId + "']", metadataTemplateNamespaces);

        // for each of these elements...
        for (Iterator<?> iter = elems.iterator(); iter.hasNext(); ) {
            Object ob = iter.next();
            if (ob instanceof Element) {
                Element elem = (Element) ob;

                if (fragment.getName().equals(REPLACEMENT_GROUP)) {
                    Element parent = elem.getParentElement();
                    int insertionIndex = parent.indexOf(elem);
                    @SuppressWarnings("unchecked")
                    List<Element> children = fragment.getChildren();

                    for (Element child : children) {
                        //insert a copy of the referencing element
                        Element copy = (Element) elem.clone();
                        parent.addContent(insertionIndex++, copy);
                        //link the copy to or replace the copy with this xml fragment as requested
                        updateTemplateReference(copy, child);
                    }

                    //remove the original reference
                    parent.removeContent(elem);
                } else {
                    updateTemplateReference(elem, fragment);
                }
            }
        }

        if (elems.size() > 0) {
            harvestSummary.fragmentsMatched++;
            templateRefs.remove(matchId);
        }
    }

    /**
     * Cleanup unused references in the metadata record created from the template.
     *
     * @param record     metadata record to remove unused elements with id attributes from
     * @param recordRefs names of id attributes not filled in
     */
    private void cleanupRecord(Element record, Set<String> recordRefs) throws Exception {
        log.debug("Cleaning up record with ids " + recordRefs);

        for (String matchId : recordRefs) {
            if (log.isDebugEnabled())
                log.debug("Attempting to search metadata for " + matchId);
            List<?> elems = Xml.selectNodes(record, "//*[@id='" + matchId + "']", metadataTemplateNamespaces);

            // for each of these elements remove it as no fragment has matched it
            for (Iterator<?> iter = elems.iterator(); iter.hasNext(); ) {
                Object ob = iter.next();
                if (ob instanceof Element) {
                    Element elem = (Element) ob;
                    Element parent = elem.getParentElement();
                    parent.removeContent(elem);
                }
            }
        }
    }

    /**
     * Create a metadata record from the filled in template
     *
     * @param fragment filled in template
     */
    private void updateTemplateReference(Element reference, Element fragment) throws Exception {
        String title = fragment.getAttributeValue("title");
        String uuid = fragment.getAttributeValue("uuid");
        String schema = fragment.getAttributeValue("schema");
        boolean addUuid = fragment.getAttributeValue("addUuidAsId", "false").equals("true");

        if (schema == null) return;  //skip fragments with unknown schema

        // get the metadata fragment from the fragment container
        Element md = (Element) fragment.getChildren().get(0);

        if (params.createSubtemplates) {
            // if(log.isDebugEnabled()) log.debug("Element found "+Xml.getString(elem));

            // Add an xlink to the subtemplate created for this fragment to the referencing element
            reference.setAttribute("uuidref", uuid);
            reference.setAttribute("href", metadataGetService + uuid, xlink);
            reference.setAttribute("show", "replace", xlink);
            if (title != null) reference.setAttribute("title", title, xlink);
        } else {
            // if(log.isDebugEnabled()) log.debug("Element found "+Xml.getString(elem));
            // Replace the referencing element with the fragment
            Element parent = reference.getParentElement();
            Element newMd = (Element) md.clone();
            // add the fragment uuid as an id attribute so that any xlinks local to
            // the document that use the uuid will resolve - do this if the fragment
            // has an attribute addUuidAsId="true"
            if (addUuid) newMd.setAttribute("id", uuid);
            parent.setContent(parent.indexOf(reference), newMd);
        }
    }

    /**
     * Update a metadata record with the filled in template
     *
     * @param recUuid  Uuid of metadata record being updated
     * @param id       Metadata id of record being updated
     * @param template filled in template
     */
    private void updateMetadata(String recUuid, String id, Element template) throws Exception, SQLException {
        // now update existing record with the filled in template
        if (log.isDebugEnabled()) {
            log.debug("	- Attempting to update metadata record " + id + " with links");
        }
        template = dataMan.setUUID(params.outputSchema, recUuid, template);
        update(id, template, null, false);
        harvestSummary.recordsUpdated++;
        harvestSummary.updatedMetadata.add(recUuid);
    }

    /**
     * Update an existing metadata record or subtemplate.
     *
     * @param id Metadata id of record being updated
     * @param template a metadata record or metadata fragment/subtemplate
     * @param title title of fragment/subtemplate: optional, null for records
     * @param isSubtemplate Are we updating a subtemplate?
     *
     */
    private void update(String id, Element template, String title, boolean isSubtemplate) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        //
        // update metadata
        //
        boolean validate = false;
        boolean ufo = false;
        boolean index = false;
        String language = context.getLanguage();
        metadataManager.updateMetadata(context, id, template, validate, ufo, index, language, df.format(date), false);

        int iId = Integer.parseInt(id);

        final IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);
        AbstractMetadata metadata = metadataRepository.findOne(iId);
        OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
        repository.deleteAllByMetadataId(iId);

        if (isSubtemplate) {
          // Note: we use fragmentAllPrivs here because subtemplates need to be
          // visible/accessible to all
          addPrivileges(id, fragmentAllPrivs, localGroups, context);
        } else {
          addPrivileges(id, params.privileges, localGroups, context);
        }

        metadata.getCategories().clear();
        addCategories(metadata, params.categories, localCateg, context, null, true);

        if (isSubtemplate) {
            dataMan.setSubtemplateTypeAndTitleExt(iId, title);
        }
        dataMan.setHarvestedExt(iId, params.uuid, Optional.of(harvestUri));

        dataMan.indexMetadata(id, true, null);

        metadataManager.flush();
    }

    /**
     * Create a metadata record from the filled in template
     *
     * @param template filled in template
     */
    private void createMetadata(String recUuid, Element template) throws Exception, SQLException {
        // now add any record built from template with linked in fragments
        if (log.isDebugEnabled())
            log.debug("	- Attempting to insert metadata record with link");
        template = dataMan.setUUID(params.outputSchema, recUuid, template);

        //
        // insert metadata
        //
        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(recUuid);
        metadata.getDataInfo().
            setSchemaId(params.outputSchema).
            setRoot(template.getQualifiedName()).
            setType(MetadataType.METADATA);
        metadata.getSourceInfo().
            setSourceId(params.uuid).
            setOwner(Integer.parseInt(params.owner));
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.uuid).
            setUri(harvestUri);
        if (params.isoCategory != null) {
            MetadataCategory metadataCategory = context.getBean(MetadataCategoryRepository.class).findOneByName(params.isoCategory);

            if (metadataCategory == null) {
                throw new IllegalArgumentException("No category found with name: " + params.isoCategory);
            }
            metadata.getCategories().add(metadataCategory);
        }
        metadata = metadataManager.insertMetadata(context, metadata, template, true, false, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        if (log.isDebugEnabled()) {
            log.debug("	- Set privileges, category, template and harvested");
        }
        addPrivileges(id, params.privileges, localGroups, context);

        dataMan.indexMetadata(id, true, null);

        if (log.isDebugEnabled()) {
            log.debug("	- Commit " + id);
        }

        metadataManager.flush();

        harvestSummary.recordsBuilt++;
    }

    static public class FragmentParams {
        public String uuid;
        public String owner;
        public String templateId;
        public String outputSchema;
        public String isoCategory;
        public Boolean createSubtemplates;
        public Iterable<Privileges> privileges;
        public Iterable<String> categories;
    }

    public static class HarvestSummary {
        public int fragmentsMatched;
        public int recordsBuilt;
        public int recordsUpdated;
        public int fragmentsReturned;
        public int fragmentsAdded;
        public int fragmentsUpdated;
        public int fragmentsUnknownSchema;
        public Set<String> updatedMetadata = new HashSet<String>();
    }


}
