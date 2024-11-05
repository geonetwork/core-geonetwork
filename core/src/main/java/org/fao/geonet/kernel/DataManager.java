//==============================================================================
//===
//=== DataManager
//===
//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.kernel.datamanager.IMetadataCategory;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.IIndexSubmittor;
import org.fao.geonet.repository.UserGroupRepository;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles all operations on metadata (select,insert,update,delete etc...).
 * <p>
 * Deprecated in favor of
 * <p>
 * {@link IMetadataManager} {@link IMetadataUtils} {@link IMetadataIndexer} {@link IMetadataValidator} {@link IMetadataOperations}
 * {@link IMetadataStatus} {@link IMetadataSchemaUtils} {@link IMetadataCategory}
 */
@Deprecated
public class DataManager {

    private static final Logger LOGGER_DATA_MANAGER = LoggerFactory.getLogger(Geonet.DATA_MANAGER);

    @Autowired
    private IMetadataManager metadataManager;
    @Autowired
    private IMetadataUtils metadataUtils;
    @Autowired
    private IMetadataIndexer metadataIndexer;
    @Autowired
    private IMetadataValidator metadataValidator;
    @Autowired
    private IMetadataOperations metadataOperations;
    @Autowired
    private IMetadataStatus metadataStatus;
    @Autowired
    private IMetadataSchemaUtils metadataSchemaUtils;
    @Autowired
    private IMetadataCategory metadataCategory;
    @Autowired
    private AccessManager accessManager;

    @Deprecated
    public static void validateExternalMetadata(String schema, Element xml, ServiceContext context, Integer groupOwner) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        gc.getBean(IMetadataValidator.class).validateExternalMetadata(schema, xml, context, " ", groupOwner);
    }

    @Deprecated
    public static void validateExternalMetadata(String schema, Element xml, ServiceContext context, String fileName, Integer groupOwner) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        gc.getBean(IMetadataValidator.class).validateExternalMetadata(schema, xml, context, fileName, groupOwner);
    }

    @Deprecated
    public static void setNamespacePrefix(final Element md) {
        GeonetContext gc = (GeonetContext) ServiceContext.get().getHandlerContext(Geonet.CONTEXT_NAME);
        gc.getBean(IMetadataValidator.class).setNamespacePrefix(md);
    }

    /**
     * Init Data manager and refresh index if needed. Can also be called after GeoNetwork startup in order to rebuild the lucene index
     *
     * @param force Force reindexing all from scratch
     **/
    public void init(ServiceContext context, Boolean force) throws Exception {
        this.metadataIndexer.init(context, force);
        this.metadataManager.init(context, force);
        this.metadataUtils.init(context, force);

        // FIXME this shouldn't login automatically ever!
        if (context.getUserSession() == null) {
            LOGGER_DATA_MANAGER.debug("Automatically login in as Administrator. Who is this? Who is calling this?");
            UserSession session = new UserSession();
            context.setUserSession(session);
            session.loginAs(new User().setUsername("admin").setId(-1).setProfile(Profile.Administrator));
            LOGGER_DATA_MANAGER.debug("Hopefully this is cron job or routinely background task. Who called us?",
                new Exception("Dummy Exception to know the stacktrace"));
        }
    }

    @Deprecated
    public synchronized void rebuildIndexForSelection(final ServiceContext context, String bucket, boolean clearXlink) throws Exception {
        metadataIndexer.rebuildIndexForSelection(context, bucket, clearXlink);
    }

    @Deprecated
    public void batchIndexInThreadPool(ServiceContext context, List<?> metadataIds) {
        metadataIndexer.batchIndexInThreadPool(context, metadataIds);
    }

    @Deprecated
    public void indexMetadata(final List<String> metadataIds) throws Exception {
        metadataIndexer.indexMetadata(metadataIds);
    }

    @Deprecated
    public void indexMetadata(final String metadataId, IIndexSubmittor indexSubmittor) throws Exception {
        metadataIndexer.indexMetadata(metadataId, indexSubmittor, IndexingMode.full);
    }

    @Deprecated
    public MetadataSchema getSchema(String name) {
        return metadataSchemaUtils.getSchema(name);
    }

    @Deprecated
    public Set<String> getSchemas() {
        return metadataSchemaUtils.getSchemas();
    }

    @Deprecated
    public boolean existsSchema(String name) {
        return metadataSchemaUtils.existsSchema(name);
    }

    @Deprecated
    public Path getSchemaDir(String name) {
        return metadataSchemaUtils.getSchemaDir(name);
    }

    @Deprecated
    public void validate(String schema, Element md) throws Exception {
        metadataValidator.validate(schema, md);
    }

    @Deprecated
    public Element doSchemaTronForEditor(String schema, Element md, String lang, Integer groupOwner) throws Exception {
        return metadataValidator.doSchemaTronForEditor(schema, md, lang, groupOwner);
    }

    @Deprecated
    public String getMetadataSchema(String id) throws Exception {
        return metadataSchemaUtils.getMetadataSchema(id);
    }

    @Deprecated
    public String getMetadataTitle(String id) throws Exception {
        return metadataUtils.getMetadataTitle(id);
    }

    @Deprecated
    public void versionMetadata(ServiceContext context, String id, Element md) throws Exception {
        metadataIndexer.versionMetadata(context, id, md);
    }

    @Deprecated
    public void startEditingSession(ServiceContext context, String id) throws Exception {
        metadataUtils.startEditingSession(context, id);
    }

    @Deprecated
    public void cancelEditingSession(ServiceContext context, String id) throws Exception {
        metadataUtils.cancelEditingSession(context, id);
    }

    @Deprecated
    public void endEditingSession(String id, UserSession session) {
        metadataUtils.endEditingSession(id, session);
    }

    @Deprecated
    public Element enumerateTree(Element md) throws Exception {
        return metadataUtils.enumerateTree(md);
    }

    @Deprecated
    public String extractUUID(String schema, Element md) throws Exception {
        return metadataUtils.extractUUID(schema, md);
    }

    @Deprecated
    public String extractDefaultLanguage(String schema, Element md) throws Exception {
        return metadataUtils.extractDefaultLanguage(schema, md);
    }

    @Deprecated
    public String extractDateModified(String schema, Element md) throws Exception {
        return metadataUtils.extractDateModified(schema, md);
    }

    @Deprecated
    public Element setUUID(String schema, String uuid, Element md) throws Exception {
        return metadataUtils.setUUID(schema, uuid, md);
    }

    @Deprecated
    public Element extractSummary(Element md) throws Exception {
        return metadataUtils.extractSummary(md);
    }

    @Deprecated
    public @Nullable
    String getMetadataId(@Nonnull String uuid) throws Exception {
        return metadataUtils.getMetadataId(uuid);
    }

    @Deprecated
    public @Nullable
    String getMetadataUuid(@Nonnull String id) throws Exception {
        return metadataUtils.getMetadataUuid(id);
    }

    @Deprecated
    public String getVersion(String id) {
        return metadataUtils.getVersion(id);
    }

    @Deprecated
    public String getNewVersion(String id) {
        return metadataUtils.getNewVersion(id);
    }

    @Deprecated
    public void setTemplate(final int id, final MetadataType type, final String title) throws Exception {
        metadataUtils.setTemplate(id, type, title);
    }

    @Deprecated
    public void setTemplateExt(final int id, final MetadataType metadataType) throws Exception {
        metadataUtils.setTemplateExt(id, metadataType);
    }

    @Deprecated
    public void setSubtemplateTypeAndTitleExt(final int id, String title) throws Exception {
        metadataUtils.setSubtemplateTypeAndTitleExt(id, title);
    }

    @Deprecated
    public void setHarvested(int id, String harvestUuid) throws Exception {
        metadataUtils.setHarvested(id, harvestUuid);
    }

    @Deprecated
    public void setHarvestedExt(int id, String harvestUuid) throws Exception {
        metadataUtils.setHarvestedExt(id, harvestUuid);
    }

    @Deprecated
    public void setHarvestedExt(final int id, final String harvestUuid, final Optional<String> harvestUri) throws Exception {
        metadataUtils.setHarvestedExt(id, harvestUuid, harvestUri);
    }

    @Deprecated
    public @CheckForNull
    String autodetectSchema(Element md) throws SchemaMatchConflictException, NoSchemaMatchesException {
        return metadataSchemaUtils.autodetectSchema(md);
    }

    @Deprecated
    public @CheckForNull
    String autodetectSchema(Element md, String defaultSchema)
        throws SchemaMatchConflictException, NoSchemaMatchesException {
        return metadataSchemaUtils.autodetectSchema(md, defaultSchema);
    }

    @Deprecated
    public void updateDisplayOrder(final String id, final String displayOrder) throws Exception {
        metadataUtils.updateDisplayOrder(id, displayOrder);
    }

    @Deprecated
    public void increasePopularity(ServiceContext srvContext, String id) throws Exception {
        metadataUtils.increasePopularity(srvContext, id);
    }

    @Deprecated
    public int rateMetadata(final int metadataId, final String ipAddress, final int rating) throws Exception {
        return metadataUtils.rateMetadata(metadataId, ipAddress, rating);
    }

    @Deprecated
    public String createMetadata(ServiceContext context, String templateId, String groupOwner, String source, int owner, String parentUuid,
                                 String isTemplate, boolean fullRightsForGroup) throws Exception {
        return metadataManager.createMetadata(context, templateId, groupOwner, source, owner, parentUuid, isTemplate, fullRightsForGroup);
    }

    @Deprecated
    public String createMetadata(ServiceContext context, String templateId, String groupOwner, String source, int owner, String parentUuid,
                                 String isTemplate, boolean fullRightsForGroup, String uuid) throws Exception {
        return metadataManager.createMetadata(context, templateId, groupOwner, source, owner, parentUuid, isTemplate, fullRightsForGroup,
            uuid);
    }

    @Deprecated
    public String insertMetadata(ServiceContext context, String schema, Element metadataXml, String uuid, int owner, String groupOwner,
                                 String source, String metadataType, String docType, String category, String createDate, String changeDate, boolean ufo,
                                 IndexingMode indexingMode) throws Exception {
        return metadataManager.insertMetadata(context, schema, metadataXml, uuid, owner, groupOwner, source, metadataType, docType,
            category, createDate, changeDate, ufo, indexingMode);
    }

    @Deprecated
    public AbstractMetadata insertMetadata(ServiceContext context, AbstractMetadata newMetadata, Element metadataXml, IndexingMode indexingMode,
                                           boolean updateFixedInfo, UpdateDatestamp updateDatestamp, boolean fullRightsForGroup, IIndexSubmittor indexSubmittor)
        throws Exception {
        return metadataManager.insertMetadata(context, newMetadata, metadataXml, indexingMode, updateFixedInfo, updateDatestamp,
            fullRightsForGroup, indexSubmittor);
    }

    @Deprecated
    public Element getMetadataNoInfo(ServiceContext srvContext, String id) throws Exception {
        return metadataUtils.getMetadataNoInfo(srvContext, id);
    }

    @Deprecated
    public Element getMetadata(String id) throws Exception {
        return metadataManager.getMetadata(id);
    }

    @Deprecated
    public Element getMetadata(ServiceContext srvContext, String id, boolean forEditing, boolean withEditorValidationErrors,
            boolean keepXlinkAttributes) throws Exception {
        return metadataManager.getMetadata(srvContext, id, forEditing, !forEditing, withEditorValidationErrors, keepXlinkAttributes);
    }

    @Deprecated
    public Element getElementByRef(Element md, String ref) {
        return metadataUtils.getElementByRef(md, ref);
    }

    @Deprecated
    public boolean existsMetadata(int id) throws Exception {
        return metadataUtils.existsMetadata(id);
    }

    @Deprecated
    public boolean existsMetadataUuid(String uuid) throws Exception {
        return metadataUtils.existsMetadataUuid(uuid);
    }

    @Deprecated
    public Element getKeywords() throws Exception {
        return metadataUtils.getKeywords();
    }

    @Deprecated
    public void updateMetadataOwner(final int id, final String owner, final String groupOwner) throws Exception {
        metadataManager.updateMetadataOwner(id, owner, groupOwner);
    }

    public AbstractMetadata updateMetadata(final ServiceContext context, final String metadataId, final Element md,
                                           final boolean validate, final boolean ufo,  final String lang, final String changeDate,
                                           final boolean updateDateStamp, final IndexingMode indexingMode) throws Exception {
        return metadataManager.updateMetadata(context, metadataId, md, validate, ufo, lang, changeDate,
            updateDateStamp, indexingMode);
    }

    @Deprecated
    public boolean validate(Element xml) {
        return metadataValidator.validate(xml);
    }

    @Deprecated
    public Element applyCustomSchematronRules(String schema, int metadataId, Element md, String lang,
                                              List<MetadataValidation> validations) {
        return metadataValidator.applyCustomSchematronRules(schema, metadataId, md, lang, validations);
    }

    @Deprecated
    public void deleteMetadataOper(ServiceContext context, String metadataId, boolean skipAllReservedGroup) throws Exception {
        metadataOperations.deleteMetadataOper(context, metadataId, skipAllReservedGroup);
    }

    @Deprecated
    public void setDataCommons(ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction,
                               String licensename, String type) throws Exception {
        metadataUtils.setDataCommons(context, id, licenseurl, imageurl, jurisdiction, licensename, type);
    }

    @Deprecated
    public void setCreativeCommons(ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction,
                                   String licensename, String type) throws Exception {
        metadataUtils.setCreativeCommons(context, id, licenseurl, imageurl, jurisdiction, licensename, type);
    }

    @Deprecated
    public void setOperation(ServiceContext context, String mdId, String grpId, ReservedOperation op) throws Exception {
        metadataOperations.setOperation(context, mdId, grpId, op);
    }

    @Deprecated
    public void setOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception {
        metadataOperations.setOperation(context, mdId, grpId, opId);
    }

    @Deprecated
    public boolean setOperation(ServiceContext context, int mdId, int grpId, int opId) throws Exception {
        return metadataOperations.setOperation(context, mdId, grpId, opId);
    }

    @Deprecated
    public Optional<OperationAllowed> getOperationAllowedToAdd(final ServiceContext context, final int mdId, final int grpId,
                                                               final int opId) {
        return metadataOperations.getOperationAllowedToAdd(context, mdId, grpId, opId);
    }

    @Deprecated
    public void checkOperationPermission(ServiceContext context, int grpId, UserGroupRepository userGroupRepo) {
        metadataOperations.checkOperationPermission(context, grpId, userGroupRepo);
    }

    @Deprecated
    public void unsetOperation(ServiceContext context, String mdId, String grpId, ReservedOperation opId) throws Exception {
        metadataOperations.unsetOperation(context, mdId, grpId, opId);
    }

    @Deprecated
    public void unsetOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception {
        metadataOperations.unsetOperation(context, mdId, grpId, opId);
    }

    @Deprecated
    public void unsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception {
        metadataOperations.unsetOperation(context, mdId, groupId, operId);
    }

    @Deprecated
    public void forceUnsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception {
        metadataOperations.forceUnsetOperation(context, mdId, groupId, operId);
    }

    @Deprecated
    public void copyDefaultPrivForGroup(ServiceContext context, String id, String groupId, boolean fullRightsForGroup) throws Exception {
        metadataOperations.copyDefaultPrivForGroup(context, id, groupId, fullRightsForGroup);
    }

    @Deprecated
    public boolean isUserMetadataOwner(int userId) throws Exception {
        return metadataOperations.isUserMetadataOwner(userId);
    }

    @Deprecated
    public boolean isUserMetadataStatus(int userId) throws Exception {
        return metadataStatus.isUserMetadataStatus(userId);
    }

    @Deprecated
    public boolean existsUser(ServiceContext context, int id) throws Exception {
        return metadataOperations.existsUser(context, id);
    }

    @Deprecated
    public MetadataStatus getStatus(int metadataId) throws Exception {
        return metadataStatus.getStatus(metadataId);
    }

    @Deprecated
    public String getCurrentStatus(int metadataId) throws Exception {
        return metadataStatus.getCurrentStatus(metadataId);
    }

    @Deprecated
    public MetadataStatus setStatus(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage) throws Exception {
        return metadataStatus.setStatus(context, id, status, changeDate, changeMessage);
    }

    @Deprecated
    public MetadataStatus setStatusExt(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage)
        throws Exception {
        return metadataStatus.setStatusExt(context, id, status, changeDate, changeMessage);
    }

    @Deprecated
    public void activateWorkflowIfConfigured(ServiceContext context, String newId, String groupOwner) throws Exception {
        metadataStatus.activateWorkflowIfConfigured(context, newId, groupOwner);
    }

    @Deprecated
    public void setCategory(ServiceContext context, String mdId, String categId) throws Exception {
        metadataCategory.setCategory(context, mdId, categId);
    }

    @Deprecated
    public boolean isCategorySet(final String mdId, final int categId) throws Exception {
        return metadataCategory.isCategorySet(mdId, categId);
    }

    @Deprecated
    public void unsetCategory(final ServiceContext context, final String mdId, final int categId) throws Exception {
        metadataCategory.unsetCategory(context, mdId, categId);
    }

    @Deprecated
    public Collection<MetadataCategory> getCategories(final String mdId) throws Exception {
        return metadataCategory.getCategories(mdId);
    }

    @Deprecated
    public Element updateFixedInfo(String schema, Optional<Integer> metadataId, String uuid, Element md, String parentUuid,
                                   UpdateDatestamp updateDatestamp, ServiceContext context) throws Exception {
        return metadataManager.updateFixedInfo(schema, metadataId, uuid, md, parentUuid, updateDatestamp, context);
    }

    @Deprecated
    public Set<String> updateChildren(ServiceContext srvContext, String parentUuid, String[] children, Map<String, Object> params)
        throws Exception {
        return metadataManager.updateChildren(srvContext, parentUuid, children, params);
    }

    @Deprecated
    @VisibleForTesting
    public void buildPrivilegesMetadataInfo(ServiceContext context, Map<String, Element> mdIdToInfoMap) throws Exception {
        metadataManager.buildPrivilegesMetadataInfo(context, mdIdToInfoMap);
    }

    @Deprecated
    public void flush() {
        metadataManager.flush();
    }

    @Deprecated
    public int batchDeleteMetadataAndUpdateIndex(Specification<? extends AbstractMetadata> specification) throws Exception {
        return metadataIndexer.batchDeleteMetadataAndUpdateIndex(specification);
    }

    @Deprecated
    public AccessManager getAccessManager() {
        return accessManager;
    }

    @Deprecated
    public EditLib getEditLib() {
        return metadataManager.getEditLib();
    }
}
