//=============================================================================
//===	Copyright (C) 2001-2020 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.mef;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.MetadataResourceDatabaseMigration;
import org.fao.geonet.Util;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadFormatEx;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.oaipmh.exceptions.BadArgumentException;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.fao.geonet.domain.Localized.translationXmlToLangMap;

public class Importer {
    @Deprecated
    public static List<String> doImport(final Element params, final ServiceContext context, final Path mefFile,
        final Path stylePath) throws Exception {
        String fileType = Util.getParam(params, "file_type", "mef");
        String style = Util.getParam(params, Params.STYLESHEET, "_none_");
        String uuidAction = Util.getParam(params, Params.UUID_ACTION, Params.NOTHING);
        String source = Util.getParam(params, Params.SITE_ID, context.getBean(SettingManager.class).getSiteId());
        MetadataType isTemplate = MetadataType.lookup(Util.getParam(params, Params.TEMPLATE, "n"));
        String category = Util.getParam(params, Params.CATEGORY, "");
        String groupId = Util.getParam(params, Params.GROUP, "");
        boolean validate = Util.getParam(params, Params.VALIDATE, "off").equals("on");
        boolean assign = Util.getParam(params, "assign", "off").equals("on");

        return doImport(fileType, MEFLib.UuidAction.parse(uuidAction), style, source, isTemplate, new String[] { category }, groupId,
            validate, assign, context, mefFile);
    }

    public static List<String> doImport(String fileType, final MEFLib.UuidAction uuidAction, final String style, final String source,
        final MetadataType isTemplate, final String[] category, final String groupId, final boolean validate, final boolean assign,
        final ServiceContext context, final Path mefFile) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        final DataManager dm = applicationContext.getBean(DataManager.class);
        final SettingManager sm = applicationContext.getBean(SettingManager.class);

        // Load preferred schema and set to iso19139 by default
        String preferredSchema = applicationContext.getBean(ServiceConfig.class).getValue("preferredSchema", "iso19139");

        final List<String> metadataIdMap = new ArrayList<String>();
        final List<Element> md = new ArrayList<Element>();
        final List<Element> fc = new ArrayList<Element>();

        // Try to define MEF version from mef file not from parameter
        if (fileType.equals("mef")) {
            MEFLib.Version version = MEFLib.getMEFVersion(mefFile);
            if (version != null && version.equals(MEFLib.Version.V2)) {
                fileType = "mef2";
            }
        }

        IVisitor visitor;

        if (fileType.equals("single"))
            visitor = new XmlVisitor();
        else if (fileType.equals("mef"))
            visitor = new MEFVisitor();
        else if (fileType.equals("mef2"))
            visitor = new MEF2Visitor();
        else
            throw new BadArgumentException("Bad file type parameter.");

        // --- import metadata from MEF, Xml, ZIP files
        final String finalPreferredSchema = preferredSchema;
        MEFLib.visit(mefFile, visitor, new IMEFVisitor() {

            public void handleMetadata(Element metadata, int index) throws Exception {
                if (Log.isDebugEnabled(Geonet.MEF))
                    Log.debug(Geonet.MEF, "Collecting metadata:\n" + Xml.getString(metadata));
                md.add(index, metadata);
            }

            public void handleMetadataFiles(DirectoryStream<Path> metadataXmlFiles, Element info, int index) throws Exception {
                String infoSchema = "_none_";
                String uuid = null;
                if (info != null && info.getContentSize() != 0) {
                    Element general = info.getChild("general");
                    if (general != null && general.getContentSize() != 0) {
                        if (general.getChildText("schema") != null) {
                            infoSchema = general.getChildText("schema");
                        }
                        if (general.getChildText("uuid") != null) {
                            uuid = general.getChildText("uuid");
                        }
                    }
                }

                Path lastUnknownMetadataFolderName = null;
                if (Log.isDebugEnabled(Geonet.MEF))
                    Log.debug(Geonet.MEF, "info.xml says schema should be " + infoSchema);

                Element metadataValidForImport;

                Map<String, Pair<String, Element>> mdFiles = new HashMap<String, Pair<String, Element>>();
                for (Path file : metadataXmlFiles) {
                    if (file != null && java.nio.file.Files.isRegularFile(file)) {
                        Element metadata = Xml.loadFile(file);

                        // Important folder name to identify metadata should be ../../
                        lastUnknownMetadataFolderName = file.getParent().getParent().relativize(file);

                        try {
                            String metadataSchema = dm.autodetectSchema(metadata, null);
                            // If local node doesn't know metadata
                            // schema try to load next xml file.
                            if (metadataSchema == null) {
                                continue;
                            }

                            String currFile = "Found metadata file " + file.getParent().getParent().relativize(file);

                            mdFiles.put(metadataSchema, Pair.read(currFile, metadata));

                        } catch (NoSchemaMatchesException e) {
                            Log.debug(Geonet.MEF, "No schema match for " + lastUnknownMetadataFolderName + ".");
                        }
                    }
                }

                if (mdFiles.size() == 0) {
                    throw new BadFormatEx(uuid + " / No valid metadata file found" + ((lastUnknownMetadataFolderName == null) ?
                        "" :
                        (" in " + lastUnknownMetadataFolderName)) + ".");
                }

                // 1st: Select metadata with schema in info file
                Pair<String, Element> mdInform = mdFiles.get(infoSchema);
                if (mdInform != null) {
                    if (Log.isDebugEnabled(Geonet.MEF)) {
                        Log.debug(Geonet.MEF, mdInform.one() + " with info.xml schema (" + infoSchema + ").");
                    }
                    metadataValidForImport = mdInform.two();
                    handleMetadata(metadataValidForImport, index);
                    return;
                }

                // 2nd: Select metadata with preferredSchema
                mdInform = mdFiles.get(finalPreferredSchema);
                if (mdInform != null) {
                    if (Log.isDebugEnabled(Geonet.MEF)) {
                        Log.debug(Geonet.MEF, mdInform.one() + " with preferred schema (" + finalPreferredSchema + ").");
                    }
                    metadataValidForImport = mdInform.two();
                    handleMetadata(metadataValidForImport, index);
                    return;
                }

                // Lastly: Select the first metadata in the map
                String metadataSchema = (String) mdFiles.keySet().toArray()[0];
                mdInform = mdFiles.get(metadataSchema);
                if (Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, mdInform.one() + " with known schema (" + metadataSchema + ").");
                }
                metadataValidForImport = mdInform.two();

                // Import valid metadata
                handleMetadata(metadataValidForImport, index);
            }

            // --------------------------------------------------------------------

            public void handleFeatureCat(Element featureCat, int index) throws Exception {
                if (featureCat != null) {
                    if (Log.isDebugEnabled(Geonet.MEF))
                        Log.debug(Geonet.MEF, "Collecting feature catalog:\n" + Xml.getString(featureCat));
                }
                fc.add(index, featureCat);
            }

            // --------------------------------------------------------------------

            /**
             * Record is not a template by default. No category attached to
             * record by default. No stylesheet used by default. If no site
             * identifier provided, use current node id by default. No
             * validation by default.
             * <p/>
             * If record is a template and not a MEF file always generate a new
             * UUID.
             */
            public void handleInfo(Element info, int index) throws Exception {
                String uuid = null;
                String createDate = null;
                String changeDate = null;
                String sourceName = null;
                Map<String, String> sourceTranslations = Maps.newHashMap();
                // Schema in info.xml is not used here anymore.
                // It is used in handleMetadataFiles as the first option to pick a
                // metadata file from those in a metadata dir in a MEF2
                // String schema = null;
                String rating = null;
                String popularity = null;
                Element categs = null;
                final Element privileges;

                // Apply a stylesheet transformation if requested

                if (!style.equals("_none_")) {
                    FilePathChecker.verify(style);

                    final GeonetworkDataDirectory dataDirectory = applicationContext.getBean(GeonetworkDataDirectory.class);
                    Path xsltPath = dataDirectory.getXsltConversion(style);
                    if (Files.exists(xsltPath)) {
                        md.add(index, Xml.transform(md.get(index), xsltPath));
                    } else {
                        throw new Exception(String.format("XSL transformation '%s' not found.", style));
                    }
                }

                final Element metadata = md.get(index);
                String schema = dm.autodetectSchema(metadata, null);

                if (schema == null)
                    throw new Exception("Unknown schema");

                // Handle non MEF files insertion
                if (info.getChildren().size() == 0) {
                    if (category != null) {
                        categs = new Element("categories");
                        for (String c : category) {
                            // TODO: convert id to name ?
                            categs.addContent((new Element("category")).setAttribute("name", c));
                        }
                    }
                    privileges = new Element("group");
                    privileges.addContent(new Element("operation").setAttribute("name", "view"));
                    privileges.addContent(new Element("operation").setAttribute("name", "editing"));
                    privileges.addContent(new Element("operation").setAttribute("name", "download"));
                    privileges.addContent(new Element("operation").setAttribute("name", "notify"));
                    privileges.addContent(new Element("operation").setAttribute("name", "dynamic"));
                    privileges.addContent(new Element("operation").setAttribute("name", "featured"));

                    if (isTemplate == MetadataType.METADATA) {
                        // Get the Metadata uuid if it's not a template.
                        uuid = dm.extractUUID(schema, md.get(index));
                    } else if (isTemplate == MetadataType.SUB_TEMPLATE) {
                        // Get subtemplate uuid if defined in @uuid at root
                        uuid = md.get(index).getAttributeValue("uuid");
                    } else if (isTemplate == MetadataType.TEMPLATE_OF_SUB_TEMPLATE) {
                        // Get subtemplate uuid if defined in @uuid at root
                        uuid = md.get(index).getAttributeValue("uuid");
                    }

                } else {
                    if (Log.isDebugEnabled(Geonet.MEF))
                        Log.debug(Geonet.MEF, "Collecting info file:\n" + Xml.getString(info));

                    categs = info.getChild("categories");
                    privileges = info.getChild("privileges");

                    Element general = info.getChild("general");

                    uuid = general.getChildText("uuid");
                    createDate = general.getChildText("createDate");
                    changeDate = general.getChildText("changeDate");
                    // If "assign" checkbox is set to true, we assign the metadata to the current catalog siteID/siteName
                    if (assign) {
                        if (Log.isDebugEnabled(Geonet.MEF)) {
                            Log.debug(Geonet.MEF, "Assign to local catalog");
                        }
                    } else {
                        // --- If siteId is not set, set to current node
                        sourceName = general.getChildText("siteName");
                        sourceTranslations = translationXmlToLangMap(general.getChildren("siteTranslations"));
                        if (Log.isDebugEnabled(Geonet.MEF))
                            Log.debug(Geonet.MEF, "Assign to catalog: " + source);
                    }
                    rating = general.getChildText("rating");
                    popularity = general.getChildText("popularity");
                }

                if (schema.startsWith("iso19139")) {
                    // In GeoNetwork 3.x, links to resources changed:
                    // * thumbnails contains full URL instead of file name only
                    // * API mode change old URL structure.
                    try {
                        MetadataResourceDatabaseMigration.updateMetadataResourcesLink(metadata, null, sm);
                    } catch (UnsupportedOperationException ex) {
                        // Ignore, this is triggered when importing templates with empty gmd:fileIdentifier, should not fail.
                    }
                }

                if (validate) {
                    Integer groupIdVal = null;
                    if (org.apache.commons.lang.StringUtils.isNotEmpty(groupId)) {
                        groupIdVal = Integer.parseInt(groupId);
                    }

                    // Validate xsd and schematron
                    DataManager.validateExternalMetadata(schema, metadata, context, groupIdVal);
                }

                try {
                    importRecord(uuid, uuidAction, md, schema, index, source, sourceName, sourceTranslations, context, metadataIdMap,
                        createDate, changeDate, groupId, isTemplate);
                } catch (Exception e) {
                    throw new Exception("Failed to import metadata with uuid '" + uuid + "'. " + e.getLocalizedMessage(), e);
                }

                if (fc.size() != 0 && fc.get(index) != null) {
                    // UUID is set as @uuid in root element
                    uuid = UUID.randomUUID().toString();

                    fc.add(index, dm.setUUID("iso19110", uuid, fc.get(index)));

                    //
                    // insert metadata
                    //
                    int userid = context.getUserSession().getUserIdAsInt();
                    String group = null, docType = null, title = null, category = null;
                    boolean ufo = false;
                    String fcId = dm
                        .insertMetadata(context, "iso19110", fc.get(index), uuid, userid, group, source, isTemplate.codeString, docType,
                            category, createDate, changeDate, ufo, IndexingMode.full);

                    if (Log.isDebugEnabled(Geonet.MEF))
                        Log.debug(Geonet.MEF, "Adding Feature catalog with uuid: " + uuid);

                    // Create database relation between metadata and feature
                    // catalog
                    String mdId = metadataIdMap.get(index);

                    final MetadataRelationRepository relationRepository = context.getBean(MetadataRelationRepository.class);
                    final MetadataRelation relation = new MetadataRelation();
                    relation.setId(new MetadataRelationId(Integer.valueOf(mdId), Integer.valueOf(fcId)));

                    relationRepository.save(relation);

                    metadataIdMap.add(fcId);
                    // TODO : privileges not handled for feature catalog ...
                }

                final int iMetadataId = Integer.valueOf(metadataIdMap.get(index));

                final String finalPopularity = popularity;
                final String finalRating = rating;
                final Element finalCategs = categs;
                final String finalGroupId = groupId;
                context.getBean(IMetadataManager.class).update(iMetadataId, new Updater<Metadata>() {
                    @Override public void apply(@Nonnull final Metadata metadata) {
                        final MetadataDataInfo dataInfo = metadata.getDataInfo();
                        if (finalPopularity != null) {
                            dataInfo.setPopularity(Integer.valueOf(finalPopularity));
                        }
                        if (finalRating != null) {
                            dataInfo.setRating(Integer.valueOf(finalRating));
                        }
                        dataInfo.setType(isTemplate);

                        metadata.getHarvestInfo().setHarvested(false);

                        addCategoriesToMetadata(metadata, finalCategs, context);


                        if (finalGroupId == null || finalGroupId.equals("")) {
                            Group ownerGroup = addPrivileges(context, dm, iMetadataId, privileges);
                            if (ownerGroup != null) {
                                metadata.getSourceInfo().setGroupOwner(ownerGroup.getId());
                            }
                        } else {
                            final OperationAllowedRepository allowedRepository = context.getBean(OperationAllowedRepository.class);
                            final Set<OperationAllowed> allowedSet = addOperations(context, dm, privileges, iMetadataId,
                                Integer.valueOf(finalGroupId));
                            allowedRepository.saveAll(allowedSet);
                        }
                    }
                });

                if (validate) {
                    java.util.Optional<Metadata> md = context.getBean(MetadataRepository.class).findById(iMetadataId);

                    if (md.isPresent()) {
                        // Persist the validation status
                        IMetadataValidator metadataValidator = context.getBean(IMetadataValidator.class);

                        metadataValidator.doValidate(md.get(), context.getLanguage());
                    }
                }

                dm.indexMetadata(metadataIdMap.get(index), true);
            }

            // --------------------------------------------------------------------

            public void handlePublicFile(String file, String changeDate, InputStream is, int index) throws Exception {
                if (Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, "Adding public file with name=" + file);
                }
                saveFile(context, metadataIdMap.get(index), MetadataResourceVisibility.PUBLIC, file, changeDate, is);
            }

            // --------------------------------------------------------------------

            public void handlePrivateFile(String file, String changeDate, InputStream is, int index) throws Exception {
                if (Log.isDebugEnabled(Geonet.MEF))
                    Log.debug(Geonet.MEF, "Adding private file with name=" + file);
                saveFile(context, metadataIdMap.get(index), MetadataResourceVisibility.PRIVATE, file, changeDate, is);
            }

        });

        return metadataIdMap;
    }

    public static void addCategoriesToMetadata(AbstractMetadata metadata, Element finalCategs, ServiceContext context) {
        if (finalCategs != null) {
            final MetadataCategoryRepository categoryRepository = context.getBean(MetadataCategoryRepository.class);
            for (Object cat : finalCategs.getChildren()) {
                Element categoryEl = (Element) cat;
                String catName = categoryEl.getAttributeValue("name");
                final MetadataCategory oneByName = categoryRepository.findOneByName(catName);

                if (oneByName == null) {
                    if (Log.isDebugEnabled(Geonet.MEF)) {
                        Log.debug(Geonet.MEF, " - Skipping non-existent category : " + catName);
                    }
                } else {
                    // --- metadata category exists locally
                    if (Log.isDebugEnabled(Geonet.MEF)) {
                        Log.debug(Geonet.MEF, " - Setting category : " + catName);
                    }
                    metadata.getCategories().add(oneByName);
                }
            }
        }
    }

    public static void importRecord(String uuid, MEFLib.UuidAction uuidAction, List<Element> md, String schema, int index, String source,
        String sourceName, Map<String, String> sourceTranslations, ServiceContext context, List<String> id, String createDate,
        String changeDate, String groupId, MetadataType isTemplate) throws Exception {

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        IMetadataManager metadataManager = gc.getBean(IMetadataManager.class);

        if (uuid == null || uuid.equals("") || uuidAction == MEFLib.UuidAction.GENERATEUUID) {
            String newuuid = UUID.randomUUID().toString();
            source = null;

            Log.debug(Geonet.MEF, "Replacing UUID " + uuid + " with " + newuuid);
            uuid = newuuid;

            // --- set uuid inside metadata
            md.add(index, dm.setUUID(schema, uuid, md.get(index)));
        } else {
            if (sourceName == null)
                sourceName = "???";

            if (source == null || source.trim().length() == 0)
                throw new Exception("Missing siteId parameter from info.xml file");

            // --- only update sources table if source is not current site
            SourceRepository sourceRepository = context.getBean(SourceRepository.class);
            Source sourceObject = sourceRepository.findOneByUuid(source);
            if (sourceObject == null || (!source.equals(gc.getBean(SettingManager.class).getSiteId())
                && SourceType.harvester != sourceObject.getType())) {
                Source source1 = new Source(source, sourceName, sourceTranslations, SourceType.externalportal);
                context.getBean(SourceRepository.class).save(source1);
            }
        }

        try {
            if (dm.existsMetadataUuid(uuid) && uuidAction != MEFLib.UuidAction.NOTHING) {
                // user has privileges to replace the existing metadata
                if (dm.getAccessManager().canEdit(context, dm.getMetadataId(uuid))) {
                    if (Log.isDebugEnabled(Geonet.MEF)) {
                        Log.debug(Geonet.MEF, "Deleting existing metadata with UUID : " + uuid);
                    }
                    metadataManager.deleteMetadata(context, dm.getMetadataId(uuid));
                    metadataManager.flush();
                }
                // user does not hav privileges to replace the existing metadata
                else {
                    throw new UnAuthorizedException("User has no privilege to replace existing metadata", null);
                }
            }
        } catch (Exception e) {
            throw new Exception(" Existing metadata with UUID " + uuid + " could not be deleted. Error is: " + e.getMessage());
        }

        if (Log.isDebugEnabled(Geonet.MEF))
            Log.debug(Geonet.MEF, "Adding metadata with uuid:" + uuid);

        //
        // insert metadata
        //
        int userid = context.getUserSession().getUserIdAsInt();
        String docType = null, category = null;
        boolean ufo = false;

        String metadataId = metadataManager
            .insertMetadata(context, schema, md.get(index), uuid, userid, groupId, source, isTemplate.codeString, docType, category,
                createDate, changeDate, ufo, IndexingMode.none);

        dm.activateWorkflowIfConfigured(context, metadataId, groupId);

        id.add(index, metadataId);

    }

    // --------------------------------------------------------------------------

    private static void saveFile(ServiceContext context, String id, MetadataResourceVisibility access, String file, String changeDate,
        InputStream is) throws Exception {
        final Store store = context.getBean("resourceStore", Store.class);
        final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        final String metadataUuid = metadataUtils.getMetadataUuid(id);
        assert metadataUuid != null;
        store.putResource(context, metadataUuid, file, is, new ISODate(changeDate).toDate(), access, true);
    }

    /**
     * Add privileges according to information file.
     */
    private static Group addPrivileges(final ServiceContext context, final DataManager dm, final int metadataId, final Element privil) {

        final GroupRepository groupRepository = context.getBean(GroupRepository.class);
        final OperationAllowedRepository allowedRepository = context.getBean(OperationAllowedRepository.class);

        @SuppressWarnings("unchecked") List<Element> list = privil.getChildren("group");

        Group owner = null;
        Set<OperationAllowed> opAllowedToAdd = new HashSet<OperationAllowed>();
        List<Group> groupsToAdd = new ArrayList<Group>();

        for (Element group : list) {
            String grpName = group.getAttributeValue("name");
            boolean groupOwner = group.getAttributeValue("groupOwner") != null;
            Group groupEntity = groupRepository.findByName(grpName);

            if (groupEntity == null) {
                if (Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, " - Skipping non-existent group : " + grpName);
                }
            } else {
                // --- metadata group exists locally
                if (Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, " - Setting privileges for group : " + grpName);
                }

                groupsToAdd.add(groupEntity);
                opAllowedToAdd.addAll(addOperations(context, dm, group, metadataId, groupEntity.getId()));
                if (groupOwner) {
                    if (Log.isDebugEnabled(Geonet.MEF)) {
                        Log.debug(Geonet.MEF, grpName + " set as group Owner ");
                    }
                    owner = groupEntity;
                }
            }
        }
        allowedRepository.saveAll(opAllowedToAdd);
        return owner;
    }

    /**
     * Add operations according to information file.
     */
    private static Set<OperationAllowed> addOperations(final ServiceContext context, final DataManager dm, final Element group,
        final int metadataId, final int grpId) {
        @SuppressWarnings("unchecked") List<Element> operations = group.getChildren("operation");

        Set<OperationAllowed> toAdd = new HashSet<OperationAllowed>();
        for (Element operation : operations) {
            String opName = operation.getAttributeValue("name");

            int opId = dm.getAccessManager().getPrivilegeId(opName);

            if (opId == -1) {
                if (Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, "   Skipping --> " + opName);
                }
            } else {
                // --- operation exists locally

                if (Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, "   Adding --> " + opName);
                }
                Optional<OperationAllowed> opAllowed = dm.getOperationAllowedToAdd(context, metadataId, grpId, opId);
                if (opAllowed.isPresent()) {
                    toAdd.add(opAllowed.get());
                }
            }
        }

        return toAdd;
    }

}

// =============================================================================

