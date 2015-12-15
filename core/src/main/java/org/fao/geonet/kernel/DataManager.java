//==============================================================================
//===
//=== DataManager
//===
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

package org.fao.geonet.kernel;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.kernel.metadata.IMetadataCategory;
import org.fao.geonet.kernel.metadata.IMetadataIndexer;
import org.fao.geonet.kernel.metadata.IMetadataManager;
import org.fao.geonet.kernel.metadata.IMetadataOperations;
import org.fao.geonet.kernel.metadata.IMetadataSchemaUtils;
import org.fao.geonet.kernel.metadata.IMetadataStatus;
import org.fao.geonet.kernel.metadata.IMetadataUtils;
import org.fao.geonet.kernel.metadata.IMetadataValidator;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.utils.Xml.ErrorHandler;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Handles all operations on metadata (select,insert,update,delete etc...).
 *
 * @Deprecated in favor of shorter utility classes that can be replaced to
 *             implement different behaviours.
 * 
 * 
 *             Use {@link IMetadataUtils} directly
 * 
 *             Use {@link IMetadataSchemaUtils} directly
 * 
 *             Use {@link IMetadataValidator} directly
 * 
 *             Use {@link IMetadataIndexer} directly
 */
// @Transactional(propagation = Propagation.REQUIRED, noRollbackFor =
// {XSDValidationErrorEx.class, NoSchemaMatchesException.class})
public class DataManager {


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

    // --------------------------------------------------------------------------
    // ---
    // --- Constructor
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public void init(ServiceContext context, Boolean force) throws Exception {
        //FIXME remove all the inits when autowiring works fine
        
        this.metadataManager = context.getBean(IMetadataManager.class);
        this.metadataManager.init(context);
        this.metadataUtils = context.getBean(IMetadataUtils.class);
        this.metadataUtils.init(context);
        this.metadataIndexer = context.getBean(IMetadataIndexer.class);
        this.metadataIndexer.init(context);
        this.metadataValidator = context.getBean(IMetadataValidator.class);
        this.metadataValidator.init(context);
        this.metadataOperations = context.getBean(IMetadataOperations.class);
        this.metadataOperations.init(context);
        this.metadataStatus = context.getBean(IMetadataStatus.class);
        this.metadataStatus.init(context);
        this.metadataSchemaUtils = context.getBean(IMetadataSchemaUtils.class);
        this.metadataSchemaUtils.init(context);
        this.metadataCategory = context.getBean(IMetadataCategory.class);
        this.metadataCategory.init(context);
        
        metadataManager.init(context, force);
    }

    @Deprecated
    public void rebuildIndexXLinkedMetadata(final ServiceContext context)
            throws Exception {
        metadataIndexer.rebuildIndexXLinkedMetadata(context);
    }

    @Deprecated
    public void rebuildIndexForSelection(final ServiceContext context,
            boolean clearXlink) throws Exception {
        metadataIndexer.rebuildIndexForSelection(context, clearXlink);
    }

    @Deprecated
    public void batchIndexInThreadPool(ServiceContext context,
            List<?> metadataIds) {
        metadataIndexer.batchIndexInThreadPool(context, metadataIds);
    }

    @Deprecated
    public boolean isIndexing() {
        return metadataIndexer.isIndexing();
    }

    @Deprecated
    public void indexMetadata(final List<String> metadataIds) throws Exception {
        metadataIndexer.indexMetadata(metadataIds);
    }

    @Deprecated
    public void indexMetadata(final String metadataId,
            boolean forceRefreshReaders) throws Exception {
        metadataIndexer.indexMetadata(metadataId, forceRefreshReaders);
    }

    @Deprecated
    public void rescheduleOptimizer(Calendar beginAt, int interval)
            throws Exception {
        metadataIndexer.rescheduleOptimizer(beginAt, interval);
    }

    @Deprecated
    public void disableOptimizer() throws Exception {
        metadataIndexer.disableOptimizer();
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Schema management API
    // ---
    // --------------------------------------------------------------------------

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
    public void validate(String schema, Document doc) throws Exception {
        metadataValidator.validate(schema, doc);
    }

    @Deprecated
    public void validate(String schema, Element md) throws Exception {
        metadataValidator.validate(schema, md);
    }

    @Deprecated
    public Element validateInfo(String schema, Element md, ErrorHandler eh)
            throws Exception {
        return metadataValidator.validateInfo(schema, md, eh);
    }

    @Deprecated
    public Element doSchemaTronForEditor(String schema, Element md, String lang)
            throws Exception {
        return metadataValidator.doSchemaTronForEditor(schema, md, lang);
    }

    @Deprecated
    public String getMetadataSchema(String id) throws Exception {
        return metadataSchemaUtils.getMetadataSchema(id);
    }

    @Deprecated
    public void versionMetadata(ServiceContext context, String id, Element md)
            throws Exception {
        metadataUtils.versionMetadata(context, id, md);
    }

    @Deprecated
    public void startEditingSession(ServiceContext context, String id)
            throws Exception {
        metadataManager.startEditingSession(context, id);
    }

    @Deprecated
    public void cancelEditingSession(ServiceContext context, String id)
            throws Exception {
        metadataManager.cancelEditingSession(context, id);
    }

    @Deprecated
    public void endEditingSession(String id, UserSession session) {
        metadataManager.endEditingSession(id, session);
    }

    @Deprecated
    public Element enumerateTree(Element md) throws Exception {
        return metadataUtils.enumerateTree(md);
    }

    @Deprecated
    public void validateMetadata(String schema, Element xml,
            ServiceContext context) throws Exception {
        metadataValidator.validateMetadata(schema, xml, context);
    }

    @Deprecated
    public void validateMetadata(String schema, Element xml,
            ServiceContext context, String fileName) throws Exception {
        metadataValidator.validateMetadata(schema, xml, context, fileName);

    }

    // --------------------------------------------------------------------------
    // ---
    // --- General purpose API
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public String extractUUID(String schema, Element md) throws Exception {
        return metadataUtils.extractUUID(schema, md);
    }

    @Deprecated
    public String extractDateModified(String schema, Element md)
            throws Exception {
        return metadataUtils.extractDateModified(schema, md);
    }

    @Deprecated
    public Element setUUID(String schema, String uuid, Element md)
            throws Exception {
        return metadataUtils.setUUID(schema, uuid, md);
    }

    @Deprecated
    public Element extractSummary(Element md) throws Exception {
        return metadataUtils.extractSummary(md);
    }

    @Deprecated
    public @Nullable String getMetadataId(@Nonnull String uuid)
            throws Exception {
        return metadataUtils.getMetadataId(uuid);
    }

    @Deprecated
    public @Nullable String getMetadataUuid(@Nonnull String id)
            throws Exception {
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
    public void setTemplate(final int id, final MetadataType type,
            final String title) throws Exception {
        metadataUtils.setTemplate(id, type, title);
    }

    @Deprecated
    public void setTemplateExt(final int id, final MetadataType metadataType)
            throws Exception {
        metadataUtils.setTemplateExt(id, metadataType);
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
    public void setHarvestedExt(final int id, final String harvestUuid,
            final Optional<String> harvestUri) throws Exception {
        metadataUtils.setHarvestedExt(id, harvestUuid, harvestUri);
    }

    @Deprecated
    public @CheckForNull String autodetectSchema(Element md)
            throws SchemaMatchConflictException, NoSchemaMatchesException {
        return metadataSchemaUtils.autodetectSchema(md);
    }

    @Deprecated
    public @CheckForNull String autodetectSchema(Element md,
            String defaultSchema) throws SchemaMatchConflictException,
                    NoSchemaMatchesException {
        return metadataSchemaUtils.autodetectSchema(md, defaultSchema);
    }

    @Deprecated
    public void updateDisplayOrder(final String id, final String displayOrder)
            throws Exception {
        metadataUtils.updateDisplayOrder(id, displayOrder);
        ;
    }

    @Deprecated
    public void increasePopularity(ServiceContext srvContext, String id)
            throws Exception {
        metadataUtils.increasePopularity(srvContext, id);
    }

    @Deprecated
    public int rateMetadata(final int metadataId, final String ipAddress,
            final int rating) throws Exception {
        return metadataUtils.rateMetadata(metadataId, ipAddress, rating);
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Metadata Insert API
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public String createMetadata(ServiceContext context, String templateId,
            String groupOwner, String source, int owner, String parentUuid,
            String isTemplate, boolean fullRightsForGroup) throws Exception {

        return metadataManager.createMetadata(context, templateId, groupOwner,
                source, owner, parentUuid, isTemplate, fullRightsForGroup,
                UUID.randomUUID().toString());
    }

    @Deprecated
    public String createMetadata(ServiceContext context, String templateId,
            String groupOwner, String source, int owner, String parentUuid,
            String isTemplate, boolean fullRightsForGroup, String uuid)
                    throws Exception {
        return metadataManager.createMetadata(context, templateId, groupOwner,
                source, owner, parentUuid, isTemplate, fullRightsForGroup,
                uuid);
    }

    @Deprecated
    public String insertMetadata(ServiceContext context, String schema,
            Element metadataXml, String uuid, int owner, String groupOwner,
            String source, String metadataType, String docType, String category,
            String createDate, String changeDate, boolean ufo, boolean index)
                    throws Exception {

        return metadataManager.insertMetadata(context, schema, metadataXml,
                uuid, owner, groupOwner, source, metadataType, docType,
                category, createDate, changeDate, ufo, index);
    }

    @Deprecated
    public Metadata insertMetadata(ServiceContext context, Metadata newMetadata,
            Element metadataXml, boolean notifyChange, boolean index,
            boolean updateFixedInfo, UpdateDatestamp updateDatestamp,
            boolean fullRightsForGroup, boolean forceRefreshReaders)
                    throws Exception {

        return metadataManager.insertMetadata(context, newMetadata, metadataXml,
                notifyChange, index, updateFixedInfo, updateDatestamp,
                fullRightsForGroup, forceRefreshReaders);
    }
    // --------------------------------------------------------------------------
    // ---
    // --- Metadata Get API
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public Element getMetadataNoInfo(ServiceContext srvContext, String id)
            throws Exception {
        return metadataManager.getMetadataNoInfo(srvContext, id);
    }

    @Deprecated
    public Element getMetadata(String id) throws Exception {
        return metadataManager.getMetadata(id);
    }

    @Deprecated
    public Element getMetadata(ServiceContext srvContext, String id,
            boolean forEditing, boolean withEditorValidationErrors,
            boolean keepXlinkAttributes) throws Exception {
        return metadataManager.getMetadata(srvContext, id, forEditing,
                withEditorValidationErrors, keepXlinkAttributes);
    }

    @Deprecated
    public Element getElementByRef(Element md, String ref) {
        return metadataManager.getElementByRef(md, ref);
    }

    @Deprecated
    public boolean existsMetadata(int id) throws Exception {
        return metadataManager.existsMetadata(id);
    }

    @Deprecated
    public boolean existsMetadataUuid(String uuid) throws Exception {
        return metadataManager.existsMetadataUuid(uuid);
    }

    @Deprecated
    public Element getKeywords() throws Exception {
        return metadataUtils.getKeywords();
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Metadata Update API
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public synchronized void updateMetadataOwner(final int id,
            final String owner, final String groupOwner) throws Exception {
        metadataManager.updateMetadataOwner(id, owner, groupOwner);
    }

    @Deprecated
    public synchronized Metadata updateMetadata(final ServiceContext context,
            final String metadataId, final Element md, final boolean validate,
            final boolean ufo, final boolean index, final String lang,
            final String changeDate, final boolean updateDateStamp)
                    throws Exception {
        return metadataManager.updateMetadata(context, metadataId, md, validate,
                ufo, index, lang, changeDate, updateDateStamp);
    }

    @Deprecated
    public boolean validate(Element xml) {
        return metadataValidator.validate(xml);
    }

    @Deprecated
    public boolean doValidate(String schema, String metadataId, Document doc,
            String lang) {
        return metadataValidator.doValidate(schema, metadataId, doc, lang);
    }

    @Deprecated
    public Pair<Element, String> doValidate(UserSession session, String schema,
            String metadataId, Element md, String lang, boolean forEditing)
                    throws Exception {
        return metadataValidator.doValidate(session, schema, metadataId, md,
                lang, forEditing);
    }

    @Deprecated
    public Element applyCustomSchematronRules(String schema, int metadataId,
            Element md, String lang, List<MetadataValidation> validations) {
        return metadataValidator.applyCustomSchematronRules(schema, metadataId,
                md, lang, validations);
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Metadata Delete API
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public void deleteMetadata(ServiceContext context, String metadataId)
            throws Exception {
        metadataManager.deleteMetadata(context, metadataId);
    }

    @Deprecated
    public void deleteMetadataGroup(ServiceContext context, String metadataId)
            throws Exception {
        metadataManager.deleteMetadataGroup(context, metadataId);
    }

    @Deprecated
    public void deleteMetadataOper(ServiceContext context, String metadataId,
            boolean skipAllIntranet) throws Exception {
        metadataManager.deleteMetadataOper(context, metadataId,
                skipAllIntranet);
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Metadata thumbnail API
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public Element getThumbnails(ServiceContext context, String metadataId)
            throws Exception {
        return metadataUtils.getThumbnails(context, metadataId);
    }

    @Deprecated
    public void setThumbnail(ServiceContext context, String id, boolean small,
            String file, boolean indexAfterChange) throws Exception {
        metadataUtils.setThumbnail(context, id, small, file, indexAfterChange);
    }

    @Deprecated
    public void unsetThumbnail(ServiceContext context, String id, boolean small,
            boolean indexAfterChange) throws Exception {
        metadataUtils.unsetThumbnail(context, id, small, indexAfterChange);
    }

    @Deprecated
    public void setDataCommons(ServiceContext context, String id,
            String licenseurl, String imageurl, String jurisdiction,
            String licensename, String type) throws Exception {
        metadataUtils.setDataCommons(context, id, licenseurl, imageurl,
                jurisdiction, licensename, type);
    }

    @Deprecated
    public void setCreativeCommons(ServiceContext context, String id,
            String licenseurl, String imageurl, String jurisdiction,
            String licensename, String type) throws Exception {
        metadataUtils.setCreativeCommons(context, id, licenseurl, imageurl,
                jurisdiction, licensename, type);
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Privileges API
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public void setOperation(ServiceContext context, String mdId, String grpId,
            ReservedOperation op) throws Exception {
        metadataOperations.setOperation(context, mdId, grpId, op);
    }

    @Deprecated
    public void setOperation(ServiceContext context, String mdId, String grpId,
            String opId) throws Exception {
        metadataOperations.setOperation(context, mdId, grpId, opId);
    }

    @Deprecated
    public boolean setOperation(ServiceContext context, int mdId, int grpId,
            int opId) throws Exception {
        return metadataOperations.setOperation(context, mdId, grpId, opId);
    }

    @Deprecated
    public Optional<OperationAllowed> getOperationAllowedToAdd(
            final ServiceContext context, final int mdId, final int grpId,
            final int opId) {
        return metadataOperations.getOperationAllowedToAdd(context, mdId, grpId,
                opId);
    }

    @Deprecated
    public void checkOperationPermission(ServiceContext context, int grpId,
            UserGroupRepository userGroupRepo) {
        metadataOperations.checkOperationPermission(context, grpId,
                userGroupRepo);
    }

    @Deprecated
    public void unsetOperation(ServiceContext context, String mdId,
            String grpId, ReservedOperation opId) throws Exception {
        metadataOperations.unsetOperation(context, mdId, grpId, opId);
    }

    @Deprecated
    public void unsetOperation(ServiceContext context, String mdId,
            String grpId, String opId) throws Exception {
        metadataOperations.unsetOperation(context, mdId, grpId, opId);
    }

    @Deprecated
    public void unsetOperation(ServiceContext context, int mdId, int groupId,
            int operId) throws Exception {
        metadataOperations.unsetOperation(context, mdId, groupId, operId);
    }

    @Deprecated
    public void forceUnsetOperation(ServiceContext context, int mdId,
            int groupId, int operId) throws Exception {
        metadataOperations.forceUnsetOperation(context, mdId, groupId, operId);
    }

    @Deprecated
    public void copyDefaultPrivForGroup(ServiceContext context, String id,
            String groupId, boolean fullRightsForGroup) throws Exception {
        metadataOperations.copyDefaultPrivForGroup(context, id, groupId,
                fullRightsForGroup);
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Check User Id to avoid foreign key problems
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public boolean isUserMetadataOwner(int userId) throws Exception {
        return metadataUtils.isUserMetadataOwner(userId);
    }

    @Deprecated
    public boolean isUserMetadataStatus(int userId) throws Exception {
        return metadataUtils.isUserMetadataStatus(userId);
    }

    @Deprecated
    public boolean existsUser(ServiceContext context, int id) throws Exception {
        return metadataUtils.existsUser(context, id);
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Status API
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public MetadataStatus getStatus(int metadataId) throws Exception {
        return metadataStatus.getStatus(metadataId);
    }

    @Deprecated
    public String getCurrentStatus(int metadataId) throws Exception {
        return metadataStatus.getCurrentStatus(metadataId);
    }

    @Deprecated
    public MetadataStatus setStatus(ServiceContext context, int id, int status,
            ISODate changeDate, String changeMessage) throws Exception {
        return metadataStatus.setStatus(context, id, status, changeDate,
                changeMessage);
    }

    @Deprecated
    public MetadataStatus setStatusExt(ServiceContext context, int id,
            int status, ISODate changeDate, String changeMessage)
                    throws Exception {
        return metadataStatus.setStatusExt(context, id, status, changeDate,
                changeMessage);
    }

    @Deprecated
    public void activateWorkflowIfConfigured(ServiceContext context,
            String newId, String groupOwner) throws Exception {
        metadataStatus.activateWorkflowIfConfigured(context, newId, groupOwner);
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Categories API
    // ---
    // --------------------------------------------------------------------------

    @Deprecated
    public void setCategory(ServiceContext context, String mdId, String categId)
            throws Exception {
        metadataCategory.setCategory(context, mdId, categId);
    }

    @Deprecated
    public boolean isCategorySet(final String mdId, final int categId)
            throws Exception {
        return metadataCategory.isCategorySet(mdId, categId);
    }

    @Deprecated
    public void unsetCategory(final ServiceContext context, final String mdId,
            final int categId) throws Exception {
        metadataCategory.unsetCategory(context, mdId, categId);
    }

    @Deprecated
    public Collection<MetadataCategory> getCategories(final String mdId)
            throws Exception {
        return metadataCategory.getCategories(mdId);
    }

    @Deprecated
    public Element updateFixedInfo(String schema, Optional<Integer> metadataId,
            String uuid, Element md, String parentUuid,
            UpdateDatestamp updateDatestamp, ServiceContext context)
                    throws Exception {
        return metadataManager.updateFixedInfo(schema, metadataId, uuid, md,
                parentUuid, updateDatestamp, context);
    }

    @Deprecated
    public Set<String> updateChildren(ServiceContext srvContext,
            String parentUuid, String[] children, Map<String, Object> params)
                    throws Exception {
        return metadataUtils.updateChildren(srvContext, parentUuid, children,
                params);
    }

    @Deprecated
    @VisibleForTesting
    void buildPrivilegesMetadataInfo(ServiceContext context,
            Map<String, Element> mdIdToInfoMap) throws Exception {
        metadataUtils.buildPrivilegesMetadataInfo(context, mdIdToInfoMap);
    }

    // ---------------------------------------------------------------------------
    // ---
    // --- Static methods are for external modules like GAST to be able to use
    // --- them.
    // ---
    // ---------------------------------------------------------------------------

    /**
     * 
     * TODO move somewhere else! Static is baaaad!
     *
     * @param md
     */
    public static void setNamespacePrefix(final Element md) {
        // --- if the metadata has no namespace or already has a namespace then
        // --- we must skip this phase

        Namespace ns = md.getNamespace();
        if (ns != Namespace.NO_NAMESPACE
                && (md.getNamespacePrefix().equals(""))) {
            // --- set prefix for iso19139 metadata

            ns = Namespace.getNamespace("gmd", md.getNamespace().getURI());
            setNamespacePrefix(md, ns);
        }
    }

    /**
     *
     * TODO move somewhere else! Static is baaaad!
     * 
     * @param md
     * @param ns
     */
    private static void setNamespacePrefix(final Element md,
            final Namespace ns) {
        if (md.getNamespaceURI().equals(ns.getURI())) {
            md.setNamespace(ns);
        }

        Attribute xsiType = md.getAttribute("type", Namespaces.XSI);
        if (xsiType != null) {
            String xsiTypeValue = xsiType.getValue();

            if (StringUtils.isNotEmpty(xsiTypeValue)
                    && !xsiTypeValue.contains(":")) {
                xsiType.setValue(ns.getPrefix() + ":" + xsiType.getValue());
            }
        }

        for (Object o : md.getChildren()) {
            setNamespacePrefix((Element) o, ns);
        }
    }

    @Deprecated
    public void notifyMetadataChange(Element md, String metadataId)
            throws Exception {
        metadataUtils.notifyMetadataChange(md, metadataId);
    }

    @Deprecated
    public void flush() {
        metadataUtils.flush();
    }

    @Deprecated
    public int batchDeleteMetadataAndUpdateIndex(
            Specification<Metadata> specification) throws Exception {
        return metadataIndexer.batchDeleteMetadataAndUpdateIndex(specification);
    }
}
