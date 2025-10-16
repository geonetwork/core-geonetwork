//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.datamanager.base;

import com.google.common.base.Optional;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.*;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SavedQuery;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.index.IndexingList;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.reports.MetadataReportsQueries;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.web.DefaultLanguage;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static org.fao.geonet.kernel.setting.Settings.*;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataUuid;

public class BaseMetadataUtils implements IMetadataUtils {
    @Autowired
    private MetadataRepository metadataRepository;

    // FIXME Remove when get rid of Jeeves
    private ServiceContext servContext;
    @Autowired
    protected IMetadataSchemaUtils metadataSchemaUtils;
    @Autowired
    protected IMetadataIndexer metadataIndexer;
    @Autowired
    protected SchemaManager schemaManager;
    @Autowired
    protected MetadataRatingByIpRepository ratingByIpRepository;

    @Autowired
    protected LanguageRepository languageRepository;

    @Autowired
    @Lazy
    protected SettingManager settingManager;

    @Autowired
    private IndexingList indexingList;

    @Autowired
    private EsSearchManager searchManager;

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    @Autowired(required = false)
    protected XmlSerializer xmlSerializer;

    @Autowired
    private DefaultLanguage defaultLanguage;

    private Path stylePath;

    protected IMetadataManager metadataManager;

    @Override
    public void setMetadataManager(IMetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public void init(ServiceContext context, Boolean force) throws Exception {
        servContext = context;
        stylePath = dataDirectory.resolveWebResource(Geonet.Path.STYLESHEETS);
    }

    /**
     * Needed to avoid circular dependency injection
     */
    @PostConstruct
    public void init() {
        this.metadataIndexer.setMetadataUtils(this);
    }

    /**
     * @param id
     * @return
     * @throws Exception
     */
    public @Nullable
    @Override
    String getMetadataUuid(@Nonnull String id) throws Exception {
        AbstractMetadata metadata = findOne(id);

        if (metadata == null)
            return null;

        return metadata.getUuid();
    }

    protected ServiceContext getServiceContext() {
        ServiceContext context = ServiceContext.get();
        return context == null ? servContext : context;
    }

    /**
     * Start an editing session. This will record the original metadata record in
     * the session under the
     * {@link org.fao.geonet.constants.Geonet.Session#METADATA_BEFORE_ANY_CHANGES} +
     * id session property.
     * <p>
     * The record contains geonet:info element.
     * <p>
     * Note: Only the metadata record is stored in session. If the editing session
     * upload new documents or thumbnails, those documents will not be cancelled.
     * This needs improvements.
     */
    @Override
    public Integer startEditingSession(ServiceContext context, String id) throws Exception {
        if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
            Log.debug(Geonet.EDITOR_SESSION, "Editing session starts for record " + id);
        }

        boolean keepXlinkAttributes = true;
        boolean forEditing = false;
        boolean withValidationErrors = false;
        Element metadataBeforeAnyChanges = metadataManager.getMetadata(context, id, forEditing, false,
            withValidationErrors, keepXlinkAttributes);
        context.getUserSession().setProperty(Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id, metadataBeforeAnyChanges);
        return Integer.valueOf(id);
    }

    /**
     * Rollback to the record in the state it was when the editing session started
     * (See {@link #startEditingSession(ServiceContext, String)}).
     */
    @Override
    public void cancelEditingSession(ServiceContext context, String id) throws Exception {
        UserSession session = context.getUserSession();
        Element metadataBeforeAnyChanges = (Element) session
            .getProperty(Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id);

        if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
            Log.debug(Geonet.EDITOR_SESSION, "Editing session end. Cancel changes. Restore record " + id
                + ". Replace by original record which was: ");
        }

        if (metadataBeforeAnyChanges != null) {
            if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
                Log.debug(Geonet.EDITOR_SESSION, " > restoring record: ");
                Log.debug(Geonet.EDITOR_SESSION, Xml.getString(metadataBeforeAnyChanges));
            }
            Element info = metadataBeforeAnyChanges.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
            boolean validate = false;
            boolean ufo = false;
            metadataBeforeAnyChanges.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);
            metadataManager.updateMetadata(context, id, metadataBeforeAnyChanges, validate, ufo,
                context.getLanguage(), info.getChildText(Edit.Info.Elem.CHANGE_DATE),
                true, IndexingMode.full);
            endEditingSession(id, session);
        } else {
            if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
                Log.debug(Geonet.EDITOR_SESSION, " > nothing to cancel for record " + id
                    + ". Original record was null. Use starteditingsession to.");
            }
        }
    }

    /**
     * Remove the original record stored in session.
     */
    @Override
    public void endEditingSession(String id, UserSession session) {
        if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
            Log.debug(Geonet.EDITOR_SESSION, "Editing session end.");
        }
        session.removeProperty(Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id);

        metadataManager.getEditLib().clearVersion(id);
    }

    /**
     * @param md
     * @return
     * @throws Exception
     */
    @Override
    public Element enumerateTree(Element md) throws Exception {
        metadataManager.getEditLib().enumerateTree(md);
        return md;
    }

    /**
     * Extract UUID from the metadata record using the schema XSL for UUID
     * extraction)
     */
    @Override
    public String extractUUID(String schema, Element md) throws Exception {
        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.EXTRACT_UUID);
        String uuid = Xml.transform(md, styleSheet).getText().trim();

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted UUID '" + uuid + "' for schema '" + schema + "'");

        // --- needed to detach md from the document
        md.detach();

        return uuid;
    }

    /**
     * Extract metadata default language from the metadata record using the schema XSL for default language extraction)
     */
    @Override
    public String extractDefaultLanguage(String schema, Element md) throws Exception {
        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.EXTRACT_DEFAULT_LANGUAGE);
        String defaultLanguageValue = Xml.transform(md, styleSheet).getText().trim();

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted default language '" + defaultLanguageValue + "' for schema '" + schema + "'");

        // --- needed to detach md from the document
        md.detach();

        return defaultLanguageValue;
    }

    /**
     * Extract metadata default language from the metadata record using the schema XSL for default language extraction)
     */
    @Override
    public LinkedHashMap<String, String> extractTitles(String schema, Element md) throws Exception {
        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.EXTRACT_TITLES);
        Element root = Xml.transform(md, styleSheet);

        LinkedHashMap<String, String> titles = new LinkedHashMap<>();
        root.getChildren("title").forEach(o -> {
            if (o instanceof Element) {
                Element e = (Element) o;
                String lang = e.getAttributeValue("lang");
                if (lang != null) {
                    titles.put(lang, e.getTextNormalize());
                }
            }
        });

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted title '" + titles + "' for schema '" + schema + "'");

        // --- needed to detach md from the document
        md.detach();

        return titles;
    }


    /**
     * Extract metadata default language from the metadata record using the schema XSL for default language extraction)
     */
    @Override
    public LinkedHashMap<String, String> extractTitles(@Nonnull String id) throws Exception {
        AbstractMetadata metadata = findOne(id);

        // If metadata exists, and it is metadata or template then extract the titles otherwise we will return null
        if (metadata != null &&
            (metadata.getDataInfo().getType() == MetadataType.METADATA ||
            metadata.getDataInfo().getType() == MetadataType.TEMPLATE)) {

            Element md = Xml.loadString(metadata.getData(), false);

            return extractTitles(metadata.getDataInfo().getSchemaId(), md);
        }
        return null;
    }

    @Override
    public String getPermalink(String uuid, String language) {
        Boolean doiIsFirst = settingManager.getValueAsBool(METADATA_URL_SITEMAPDOIFIRST, false);

        if (Boolean.TRUE.equals(doiIsFirst)) {
            String doi = null;
            try {
                doi = getDoi(uuid);
            } catch (Exception e) {
                // DOI not supported for schema
            }
            if (StringUtils.isNotEmpty(doi)) {
                return doi;
            }
        }


        String sitemapLinkUrl = settingManager.getValue(METADATA_URL_SITEMAPLINKURL);
        String defaultLink = settingManager.getNodeURL() + "api/records/" + uuid + "?language=all";
        String permalink = buildUrl(uuid, language, sitemapLinkUrl);

        // If the permalink template has been modified use the configured permalink template
        // otherwise use the defaultLink
        UriComponentsBuilder uriComponentsBuilder;
        if (StringUtils.isNotEmpty(permalink) && !defaultLink.equals(permalink)) {
            uriComponentsBuilder = UriComponentsBuilder.fromUriString(permalink);
        } else {
            uriComponentsBuilder = UriComponentsBuilder.fromUriString(defaultLink);

            // Get the recordLinkFormatter from the UI configuration
            String recordLinkFormatter = XslUtil.getUiConfigurationJsonProperty(null, "mods.search.formatter.recordLinkFormatter");

            // If recordLinkFormatter is configured add the recordViewFormatter query parameter
            if (StringUtils.isNotBlank(recordLinkFormatter)) {
                uriComponentsBuilder.queryParam("recordViewFormatter", recordLinkFormatter);
            }
        }

        return uriComponentsBuilder.build().toString();
    }

    @Override
    public String getDefaultUrl(String uuid, String language) {
        String dynamicAppLinkUrl = settingManager.getValue(METADATA_URL_DYNAMICAPPLINKURL);
        if ("all".equals(language)) {
            language = defaultLanguage.getLanguage();
        }
        String defaultLink = settingManager.getNodeURL() + language + "/catalog.search#/metadata/" + uuid;
        String url = buildUrl(uuid, language, dynamicAppLinkUrl);
        return StringUtils.isNotEmpty(url) ? url : defaultLink;
    }

    private String buildUrl(String uuid, String language, String url) {
        if (StringUtils.isNotEmpty(url)) {
            String upperCaseUrl = url.toUpperCase();
            Map<String, String> substitutions = new HashMap<>();
            substitutions.put("{{UUID}}", uuid);
            substitutions.put("{{LANG}}", StringUtils.isEmpty(language) ? "" : language);
            if (upperCaseUrl.contains("{{RESOURCEID}}")) {
                try {
                    String resourceId = getResourceIdentifier(uuid);
                    substitutions.put("{{RESOURCEID}}", StringUtils.isEmpty(resourceId) ? "" : resourceId);
                } catch (Exception e) {
                    // No resource identifier xpath defined in schema
                }
            }
            for (Map.Entry<String, String> s : substitutions.entrySet()) {
                if (upperCaseUrl.contains(s.getKey())) {
                    url = url.replaceAll("(?i)" + Pattern.quote(s.getKey()), s.getValue());
                }
            }
        }
        return url;
    }


    @Override
    public String getDoi(String uuid) throws ResourceNotFoundException, IOException, JDOMException {
        AbstractMetadata metadata = findOneByUuid(uuid);
        final MetadataSchema schema = metadataSchemaUtils
            .getSchema(metadata.getDataInfo().getSchemaId());
        Element xml = metadata.getXmlData(false);
        return schema.queryString(SavedQuery.DOI_GET, xml);
    }

    @Override
    public String getResourceIdentifier(String uuid) throws ResourceNotFoundException, JDOMException, IOException {
        AbstractMetadata metadata = findOneByUuid(uuid);
        final MetadataSchema schema = metadataSchemaUtils
            .getSchema(metadata.getDataInfo().getSchemaId());
        Element xml = metadata.getXmlData(false);
        return schema.queryString(SavedQuery.RESOURCEID_GET, xml);
    }

    /**
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    @Override
    public String extractDateModified(String schema, Element md) throws Exception {
        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.EXTRACT_DATE_MODIFIED);
        String dateMod = Xml.transform(md, styleSheet).getText().trim();

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted Date Modified '" + dateMod + "' for schema '" + schema + "'");

        // --- needed to detach md from the document
        md.detach();

        return dateMod;
    }

    /**
     * @param schema
     * @param uuid
     * @param md
     * @return
     * @throws Exception
     */
    @Override
    public Element setUUID(String schema, String uuid, Element md) throws Exception {
        // --- setup environment

        Element env = new Element("env");
        env.addContent(new Element("uuid").setText(uuid));

        // --- setup root element

        Element root = new Element("root");
        root.addContent(md.detach());
        root.addContent(env.detach());

        // --- do an XSL transformation

        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.SET_UUID);

        return Xml.transform(root, styleSheet);
    }

    /**
     * @param md
     * @return
     * @throws Exception
     */
    @Override
    public Element extractSummary(Element md) throws Exception {
        Path styleSheet = stylePath.resolve(Geonet.File.METADATA_BRIEF);
        Element summary = Xml.transform(md, styleSheet);
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted summary '\n" + Xml.getString(summary));

        // --- needed to detach md from the document
        md.detach();

        return summary;
    }

    /**
     * @param uuid
     * @return
     * @throws Exception
     */
    @Override
    public @Nullable
    String getMetadataId(@Nonnull String uuid) throws Exception {
        final List<Integer> idList = findAllIdsBy(hasMetadataUuid(uuid));
        if (idList.isEmpty()) {
            return null;
        }
        return String.valueOf(idList.get(0));
    }

    @Override
    public List<Integer> findAllIdsBy(Specification<? extends AbstractMetadata> specs) {
        try {
            return metadataRepository.findIdsBy((Specification<Metadata>) specs);
        } catch (ClassCastException t) {
            // Maybe it is not a Specification<Metadata>
        }

        return Collections.emptyList();
    }

    /**
     * @param id
     * @return
     */
    @Override
    public String getVersion(String id) {
        return metadataManager.getEditLib().getVersion(id);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public String getNewVersion(String id) {
        return metadataManager.getEditLib().getNewVersion(id);
    }

    @Override
    public void setTemplate(final int id, final MetadataType type, final String title) throws Exception {
        setTemplateExt(id, type);
        metadataIndexer.indexMetadata(Integer.toString(id), true, IndexingMode.full);
    }

    @Override
    public void setTemplateExt(final int id, final MetadataType metadataType) throws Exception {
        metadataRepository.update(id, metadata -> {
            final MetadataDataInfo dataInfo = metadata.getDataInfo();
            dataInfo.setType(metadataType);
        });
    }

    @Override
    public void setHarvested(int id, String harvestUuid) throws Exception {
        setHarvestedExt(id, harvestUuid);
        metadataIndexer.indexMetadata(Integer.toString(id), true, IndexingMode.full);
    }

    /**
     * Set metadata type to subtemplate and set the title. Only subtemplates need to
     * persist the title as it is used to give a meaningful title for use when
     * offering the subtemplate to users in the editor.
     *
     * @param id    Metadata id to set to type subtemplate
     * @param title Title of metadata of subtemplate/fragment
     */
    @Override
    public void setSubtemplateTypeAndTitleExt(final int id, String title) throws Exception {
        metadataRepository.update(id, metadata -> {
            final MetadataDataInfo dataInfo = metadata.getDataInfo();
            dataInfo.setType(MetadataType.SUB_TEMPLATE);
            if (title != null) {
                dataInfo.setTitle(title);
            }
        });
    }

    @Override
    public void setHarvestedExt(int id, String harvestUuid) throws Exception {
        setHarvestedExt(id, harvestUuid, Optional.<String>absent());
    }

    @Override
    public void setHarvestedExt(final int id, final String harvestUuid, final Optional<String> harvestUri)
        throws Exception {
        metadataRepository.update(id, metadata -> {
            MetadataHarvestInfo harvestInfo = metadata.getHarvestInfo();
            harvestInfo.setUuid(harvestUuid);
            harvestInfo.setHarvested(harvestUuid != null);
            harvestInfo.setUri(harvestUri.orNull());
        });
    }

    /**
     * @param id
     * @param displayOrder
     * @throws Exception
     */
    @Override
    public void updateDisplayOrder(final String id, final String displayOrder) throws Exception {
        metadataRepository.update(Integer.valueOf(id), entity -> entity.getDataInfo().setDisplayOrder(Integer.parseInt(displayOrder)));
    }

    /**
     * @throws Exception hmm
     */
    @Override
    public void increasePopularity(ServiceContext srvContext, String id) throws Exception {
        // READONLYMODE
        if (!srvContext.getBean(NodeInfo.class).isReadOnly()) {
            int iId = Integer.parseInt(id);
            metadataRepository.update(iId, entity -> entity.getDataInfo().setPopularity(
                entity.getDataInfo().getPopularity() + 1
            ));
            final java.util.Optional<Metadata> metadata = metadataRepository.findById(iId);

            if (metadata.isPresent()) {
                searchManager.updateFieldAsynch(
                    metadata.get().getUuid(),
                    Geonet.IndexFieldNames.POPULARITY,
                    metadata.get().getDataInfo().getPopularity());

            }
        } else {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER,
                    "GeoNetwork is operating in read-only mode. IncreasePopularity is skipped.");
            }
        }
    }

    /**
     * Rates a metadata.
     *
     * @param ipAddress ipAddress IP address of the submitting client
     * @param rating    range should be 1..5
     * @throws Exception hmm
     */
    @Override
    public int rateMetadata(final int metadataId, final String ipAddress, final int rating) throws Exception {
        // Save rating for this IP
        MetadataRatingByIp ratingEntity = new MetadataRatingByIp();
        ratingEntity.setRating(rating);
        ratingEntity.setId(new MetadataRatingByIpId(metadataId, ipAddress));

        ratingByIpRepository.save(ratingEntity);

        // calculate new rating
        final int newRating = ratingByIpRepository.averageRating(metadataId);

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Setting rating for id:" + metadataId + " --> rating is:" + newRating);

        metadataRepository.update(metadataId, entity -> entity.getDataInfo().setRating(newRating));
        // And register the metadata to be indexed in the near future
        indexingList.add(metadataId);

        return rating;
    }

    /**
     * Retrieves a metadata (in xml) given its id with no geonet:info.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Element getMetadataNoInfo(ServiceContext srvContext, String id) throws Exception {
        Element md = metadataManager.getMetadata(srvContext, id, false, true, false, false);
        return removeMetadataInfo(md);
    }

    /**
     *  remove the geonet:info element from the supplied metadata.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Element removeMetadataInfo(Element md) throws Exception {
        md.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);

        // Drop Geonet namespace declaration. It may be contained
        // multiple times, so loop on all.
        final List<Namespace> additionalNamespaces = new ArrayList<>(md.getAdditionalNamespaces());
        for (Namespace n : additionalNamespaces) {
            if (Edit.NAMESPACE.getURI().equals(n.getURI())) {
                md.removeNamespaceDeclaration(Edit.NAMESPACE);
            }
        }
        return md;
    }

    /**
     * Retrieves a metadata element given it's ref.
     */
    @Override
    public Element getElementByRef(Element md, String ref) {
        return metadataManager.getEditLib().findElement(md, ref);
    }

    /**
     * Returns true if the metadata exists in the database.
     */
    @Override
    public boolean existsMetadata(int id) throws Exception {
        return exists(id);
    }

    /**
     * Returns true if the metadata uuid exists in the database.
     */
    @Override
    public boolean existsMetadataUuid(String uuid) throws Exception {
        return !findAllIdsBy(hasMetadataUuid(uuid)).isEmpty();
    }

    @Override
    public boolean isMetadataPublished(int metadataId) throws Exception {
        // For metadata records)
        // It will be a draft if the record is not viewable by public
        final Specification<OperationAllowed> isMdPublished = OperationAllowedSpecs.isPublic(ReservedOperation.view);
        final Specification<OperationAllowed> hasMdId = OperationAllowedSpecs.hasMetadataId(metadataId);
        final java.util.Optional<OperationAllowed> one = ApplicationContextHolder.get()
            .getBean(OperationAllowedRepository.class)
            .findOne(Specification.where(hasMdId).and(isMdPublished));
        return one.isPresent();
    }

    @Override
    public boolean isMetadataApproved(int metadataId) throws Exception {
        boolean isApproved = false;
        MetadataStatus metadataStatus = ApplicationContextHolder.get().getBean(IMetadataStatus.class).getStatus(metadataId);
        if (metadataStatus != null) {
            String statusId = metadataStatus.getStatusValue().getId() + "";
            isApproved = statusId.equals(StatusValue.Status.APPROVED);
        }
        return isApproved;
    }

    @Override
    public boolean isMetadataDraft(int metadataId) throws Exception {
        // It will be a draft if the record is not viewable by public and it is not approved record.
        return !(isMetadataApproved(metadataId) || isMetadataPublished(metadataId));
    }

    /**
     * Returns all the keywords in the system.
     */
    @Override
    public Element getKeywords() throws Exception {
        // TODO ES
        // Collection<String> keywords = getSearchManager().getTerms("keyword");
        Element el = new Element("keywords");

        // for (Object keyword : keywords) {
        // el.addContent(new Element("keyword").setText((String) keyword));
        // }
        return el;
    }


    /**
     * @param context
     * @param metadataId
     * @param md
     * @param env
     * @param schema
     * @param styleSheet
     * @param indexAfterChange
     * @throws Exception
     */
    private void transformMd(ServiceContext context, String metadataId, Element md, Element env, String schema,
                             String styleSheet, boolean indexAfterChange) throws Exception {

        if (env.getChild("host") == null) {
            String host = getSettingManager().getValue(Settings.SYSTEM_SERVER_HOST);
            String port = getSettingManager().getValue(Settings.SYSTEM_SERVER_PORT);
            env.addContent(new Element("host").setText(host));
            env.addContent(new Element("port").setText(port));
        }

        // --- setup root element
        Element root = new Element("root");
        root.addContent(md);
        root.addContent(env);

        // --- do an XSL transformation
        Path styleSheetPath = metadataSchemaUtils.getSchemaDir(schema).resolve(styleSheet);

        md = Xml.transform(root, styleSheetPath);
        String changeDate = null;
        String uuid = null;
        if (schemaManager.getSchema(schema).isReadwriteUUID()) {
            uuid = extractUUID(schema, md);
        }

        getXmlSerializer().update(metadataId, md, changeDate, true, uuid, context);

        if (indexAfterChange) {
            metadataIndexer.indexMetadata(metadataId, true, IndexingMode.full);
        }
    }

    /**
     * @param context
     * @param id
     * @param licenseurl
     * @param imageurl
     * @param jurisdiction
     * @param licensename
     * @param type
     * @throws Exception
     */
    @Override
    public void setDataCommons(ServiceContext context, String id, String licenseurl, String imageurl,
                               String jurisdiction, String licensename, String type) throws Exception {
        Element env = prepareCommonsEnv(licenseurl, imageurl, jurisdiction, licensename, type);
        manageCommons(context, id, env, Geonet.File.SET_DATACOMMONS);
    }

    private Element prepareCommonsEnv(String licenseurl, String imageurl, String jurisdiction, String licensename,
                                      String type) {
        Element env = new Element("env");
        env.addContent(new Element("imageurl").setText(imageurl));
        env.addContent(new Element("licenseurl").setText(licenseurl));
        env.addContent(new Element("jurisdiction").setText(jurisdiction));
        env.addContent(new Element("licensename").setText(licensename));
        env.addContent(new Element("type").setText(type));
        return env;
    }

    /**
     * @param context
     * @param id
     * @param licenseurl
     * @param imageurl
     * @param jurisdiction
     * @param licensename
     * @param type
     * @throws Exception
     */
    @Override
    public void setCreativeCommons(ServiceContext context, String id, String licenseurl, String imageurl,
                                   String jurisdiction, String licensename, String type) throws Exception {
        Element env = prepareCommonsEnv(licenseurl, imageurl, jurisdiction, licensename, type);
        manageCommons(context, id, env, Geonet.File.SET_CREATIVECOMMONS);
    }

    /**
     * Extract the title field from the Metadata Repository. This is only valid for
     * subtemplates as the title can be stored with the subtemplate (since
     * subtemplates don't have a title) - metadata records don't store the title
     * here as this is part of the metadata.
     *
     * @param id metadata id to retrieve
     */
    @Override
    public String getMetadataTitle(String id) throws Exception {
        AbstractMetadata md = findOne(id);

        if (md == null) {
            throw new IllegalArgumentException("Metadata not found for id : " + id);
        } else {
            // get metadata title
            return md.getDataInfo().getTitle();
        }
    }

    /**
     * @param context
     * @param id
     * @param env
     * @param styleSheet
     * @throws Exception
     */
    private void manageCommons(ServiceContext context, String id, Element env, String styleSheet) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);
        Element md = getXmlSerializer().select(context, id);

        if (md == null)
            return;

        md.detach();

        String schema = metadataSchemaUtils.getMetadataSchema(id);
        transformMd(context, id, md, env, schema, styleSheet, true);
    }

    private XmlSerializer getXmlSerializer() {
        return xmlSerializer;
    }

    private SettingManager getSettingManager() {
        return this.settingManager;
    }

    @Override
    public long count(Specification<? extends AbstractMetadata> specs) {
        try {
            return metadataRepository.count((Specification<Metadata>) specs);
        } catch (Throwable t) {
            // Maybe it is not a Specification<Metadata>
        }
        throw new NotImplementedException("Unknown IMetadata subtype: " + specs.getClass().getName());
    }

    @Override
    public long count() {
        return metadataRepository.count();
    }

    @Override
    public AbstractMetadata findOne(int id) {
        java.util.Optional<Metadata> metadata = metadataRepository.findById(id);

        return metadata.isPresent()?metadata.get():null;
    }

    @Override
    public AbstractMetadata findOneByUuid(String uuid) {
    	AbstractMetadata metadata = null;
        try {
            metadata = metadataRepository.findOneByUuid(uuid);
        } catch (IncorrectResultSizeDataAccessException e){
            Log.warning(Geonet.GEONETWORK, String.format(
                "More than one record found with UUID '%s'. Error is '%s'.",
                uuid, e.getMessage()));
        }
        return metadata;
    }

    @Override
    public List<? extends AbstractMetadata> findAllByUuid(String uuid) {
        return metadataRepository.findAllByUuid(uuid);
    }

    @Override
    public AbstractMetadata findOne(Specification<? extends AbstractMetadata> spec) {
        try {
            java.util.Optional<Metadata> metadata = metadataRepository.findOne((Specification<Metadata>) spec);

            return metadata.isPresent()?metadata.get():null;
        } catch (ClassCastException t) {
            throw new ClassCastException("Unknown AbstractMetadata subtype: " + spec.getClass().getName());
        }
    }

    @Override
    public AbstractMetadata findOne(String id) {
        java.util.Optional<Metadata> metadata = metadataRepository.findById(Integer.parseInt(id));
        return metadata.isPresent() ? metadata.get() : null;
    }

    @Override
    public List<? extends AbstractMetadata> findAllByHarvestInfo_Uuid(String uuid) {
        return metadataRepository.findAllByHarvestInfo_Uuid(uuid);
    }

    @Override
    public Iterable<? extends AbstractMetadata> findAll(Set<Integer> keySet) {
        return metadataRepository.findAllById(keySet);
    }

    @Override
    public List<? extends AbstractMetadata> findAll(Specification<? extends AbstractMetadata> spec, Sort order) {
        return metadataRepository.findAll((Specification<Metadata>) spec, order);
    }

    @Override
    public List<? extends AbstractMetadata> findAll(Specification<? extends AbstractMetadata> specs) {
        try {
            return metadataRepository.findAll((Specification<Metadata>) specs);
        } catch (Throwable t) {
            // Maybe it is not a Specification<Metadata>
        }
        throw new NotImplementedException("Unknown AbstractMetadata subtype: " + specs.getClass().getName());
    }

    @Override
    public List<SimpleMetadata> findAllSimple(String harvestUuid) {
        return metadataRepository.findSimple(harvestUuid);
    }

    @Override
    public boolean exists(Integer iId) {
        return metadataRepository.existsById(iId);
    }

    @Override
    public Element findAllAsXml(Specification<? extends AbstractMetadata> specs, Sort sortByChangeDateDesc) {
        try {
            return metadataRepository.findAllAsXml((Specification<Metadata>) specs, sortByChangeDateDesc);

        } catch (Throwable t) {
            // Maybe it is not a Specification<Metadata>
        }
        throw new NotImplementedException("Unknown AbstractMetadata subtype: " + specs.getClass().getName());
    }

    @Override
    public MetadataReportsQueries getMetadataReports() {
        return metadataRepository.getMetadataReports();
    }

    @Override
    public Element findAllAsXml(@Nullable Specification<? extends AbstractMetadata> specs,
                                @Nullable Pageable pageable) {
        try {
            return metadataRepository.findAllAsXml((Specification<Metadata>) specs, pageable);
        } catch (Throwable t) {
            // Maybe it is not a Specification<Metadata>
        }
        throw new NotImplementedException("Unknown AbstractMetadata subtype: " + specs.getClass().getName());
    }

    protected MetadataRepository getMetadataRepository() {
        return metadataRepository;
    }

    protected SchemaManager getSchemaManager() {
        return schemaManager;
    }

    @Override
    public boolean checkMetadataWithSameUuidExist(String uuid, int id) {
        // Check if another record exist with that UUID
        Metadata recordWithThatUuid = getMetadataRepository().findOneByUuid(uuid);
        if (recordWithThatUuid != null && recordWithThatUuid.getId() != id) {
            // If yes, this would have triggered a DataIntegrityViolationException
            throw new IllegalArgumentException(String.format(
                "Another record exist with UUID '%s'. This record as internal id '%d'. The record you're trying to update with id '%d' can not be saved.",
                uuid, recordWithThatUuid.getId(), id));
        }
        return false;
    }

    @Override
    public Page<Pair<Integer, ISODate>> findAllIdsAndChangeDates(Pageable pageable) {
        return metadataRepository.findIdsAndChangeDates(pageable);
    }

    @Override
    public Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<? extends AbstractMetadata> spec) {
        try {
            return metadataRepository.findSourceInfo((Specification<Metadata>) spec);
        } catch (Throwable t) {
            // Maybe it is not a Specification<Metadata>
        }
        throw new NotImplementedException("Unknown AbstractMetadata subtype: " + spec.getClass().getName());
    }

    @Override
    public void cloneFiles(AbstractMetadata original, AbstractMetadata dest) {
        // Empty implementation for non-draft mode as not used
    }

    @Override
    public void replaceFiles(AbstractMetadata original, AbstractMetadata dest) {
        // Empty implementation for non-draft mode as not used
    }

    @Override
    public String selectOneWithSearchAndReplace(String uuid, String search, String replace) {
        return metadataRepository.selectOneWithSearchAndReplace(uuid, search, replace);
    }

    @Override
    public String selectOneWithRegexSearchAndReplaceWithFlags(String uuid, String search, String replace, String flags) {
        return metadataRepository.selectOneWithRegexSearchAndReplaceWithFlags(uuid, search, replace, flags);
    }

    @Override
    public String selectOneWithRegexSearchAndReplace(String uuid, String search, String replace) {
        return metadataRepository.selectOneWithRegexSearchAndReplace(uuid, search, replace);
    }
}
