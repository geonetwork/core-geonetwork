/**
 * 
 */
package org.fao.geonet.kernel.metadata.draft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId_;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.events.md.MetadataIndexCompleted;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.metadata.DefaultMetadataIndexer;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.specification.MetadataDraftSpecs;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public class DraftMetadataIndexer extends DefaultMetadataIndexer {
    @Autowired
    private MetadataDraftRepository mdDraftRepository;

    /**
     * @param context
     */
    @Override
    public void init(ServiceContext context) {
        super.init(context);
        this.mdDraftRepository = context.getBean(MetadataDraftRepository.class);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataIndexer#batchDeleteMetadataAndUpdateIndex(org.springframework.data.jpa.domain.Specification)
     * @param specification
     * @return
     * @throws Exception
     */
    @Override
    public int batchDeleteMetadataAndUpdateIndex(
            Specification<Metadata> specification) throws Exception {
        // Search for the ID of the drafts (if any) associated to that metadata
        final List<Integer> idsOfMetadataToDelete = mdRepository
                .findAllIdsBy(specification);

        List<Integer> mdDraftIds = new LinkedList<Integer>();

        // And remove all drafts associated to this metadatas
        for (Integer id : idsOfMetadataToDelete) {
            // --- remove metadata directory for each record
            final Path metadataDataDir = ApplicationContextHolder.get()
                    .getBean(GeonetworkDataDirectory.class)
                    .getMetadataDataDir();
            Path pb = Lib.resource.getMetadataDir(metadataDataDir, id + "");
            IO.deleteFileOrDirectory(pb);

            Metadata md = mdRepository.findOne(id);
            MetadataDraft mdD = mdDraftRepository.findOneByUuid(md.getUuid());
            if (mdD != null) {
                mdDraftIds.add(mdD.getId());
            }
        }

        mdDraftRepository
                .deleteAll(MetadataDraftSpecs.hasMetadataIdIn(mdDraftIds));

        // Remove draft records from the index
        searchManager.delete("_id",
                Lists.transform(mdDraftIds, new Function<Integer, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nonnull Integer input) {
                        return input.toString();
                    }
                }));

        // Finally deal with non-draft metadata
        return super.batchDeleteMetadataAndUpdateIndex(specification);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataIndexer#batchIndexInThreadPool(jeeves.server.context.ServiceContext,
     *      java.util.List)
     * @param context
     * @param metadataIds
     */
    @Override
    public void batchIndexInThreadPool(ServiceContext context,
            List<?> metadataIds) {

        List<Integer> ids = new LinkedList<Integer>();

        for (Object id : metadataIds) {
            Metadata md = mdRepository.findOne(id.toString());
            if (md != null) {
                ids.add(md.getId());
                MetadataDraft mdD = mdDraftRepository
                        .findOneByUuid(md.getUuid());
                if (mdD != null && !ids.contains(mdD.getId())) {
                    ids.add(mdD.getId());
                }
            } else {
                int id2 = mdDraftRepository.findOne(id.toString()).getId();
                if (!ids.contains(id2)) {
                    ids.add(id2);
                }
            }
        }

        super.batchIndexInThreadPool(context, ids);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataIndexer#indexMetadata(java.util.List)
     * @param metadataIds
     * @throws Exception
     */
    @Override
    public void indexMetadata(List<String> metadataIds) throws Exception {

        // Just in case, do the same for the related drafts
        for (String metadataId : metadataIds) {
            Metadata md = mdRepository.findOne(metadataId);
            MetadataDraft mdD = mdDraftRepository.findOneByUuid(md.getUuid());
            indexMetadata(Integer.toString(mdD.getId()), false);
        }

        super.indexMetadata(metadataIds);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataIndexer#indexMetadata(java.lang.String,
     *      boolean)
     * @param metadataId
     * @param forceRefreshReaders
     * @throws Exception
     */
    @Override
    public void indexMetadata(String metadataId, boolean forceRefreshReaders)
            throws Exception {
        // Just in case, do the same for the related drafts
        Metadata metaData = mdRepository.findOne(metadataId);
        if (metaData != null) {
            MetadataDraft mdD = mdDraftRepository
                    .findOneByUuid(metaData.getUuid());
            if (mdD != null) {
                indexMetadata(Integer.toString(mdD.getId()),
                        forceRefreshReaders);
                superIndexMetadata(metadataId, forceRefreshReaders, true);
            } else {
                superIndexMetadata(metadataId, forceRefreshReaders, false);
            }
        } else {
            indexLock.lock();
            try {
                if (waitForIndexing.contains(metadataId)) {
                    return;
                }
                while (indexing.contains(metadataId)) {
                    try {
                        waitForIndexing.add(metadataId);
                        // don't index the same metadata 2x
                        wait(200);
                    } catch (InterruptedException e) {
                        return;
                    } finally {
                        waitForIndexing.remove(metadataId);
                    }
                }
                indexing.add(metadataId);
            } finally {
                indexLock.unlock();
            }
            MetadataDraft fullMd;

            try {
                Vector<Element> moreFields = new Vector<Element>();
                int id$ = Integer.parseInt(metadataId);

                // get metadata, extracting and indexing any xlinks
                Element md = ApplicationContextHolder.get()
                        .getBean(XmlSerializer.class)
                        .selectNoXLinkResolver(metadataId, true);
                if (ApplicationContextHolder.get().getBean(XmlSerializer.class)
                        .resolveXLinks()) {
                    List<Attribute> xlinks = Processor.getXLinks(md);
                    if (xlinks.size() > 0) {
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.HASXLINKS, "1", true,
                                true));
                        StringBuilder sb = new StringBuilder();
                        for (Attribute xlink : xlinks) {
                            sb.append(xlink.getValue());
                            sb.append(" ");
                        }
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.XLINK, sb.toString(),
                                true, true));
                        Processor.detachXLink(md, getServiceContext());
                    } else {
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.HASXLINKS, "0", true,
                                true));
                    }
                } else {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
                }

                fullMd = mdDraftRepository.findOne(id$);
                
                if(fullMd == null)
                    return;

                final String schema = fullMd.getDataInfo().getSchemaId();
                final String createDate = fullMd.getDataInfo().getCreateDate()
                        .getDateAndTime();
                final String changeDate = fullMd.getDataInfo().getChangeDate()
                        .getDateAndTime();
                final String source = fullMd.getSourceInfo().getSourceId();
                final MetadataType metadataType = fullMd.getDataInfo()
                        .getType();
                final String root = fullMd.getDataInfo().getRoot();
                final String uuid = fullMd.getUuid();
                final String extra = fullMd.getDataInfo().getExtra();
                final String isHarvested = String
                        .valueOf(Constants.toYN_EnabledChar(
                                fullMd.getHarvestInfo().isHarvested()));
                final String owner = String
                        .valueOf(fullMd.getSourceInfo().getOwner());
                final Integer groupOwner = fullMd.getSourceInfo()
                        .getGroupOwner();
                final String popularity = String
                        .valueOf(fullMd.getDataInfo().getPopularity());
                final String rating = String
                        .valueOf(fullMd.getDataInfo().getRating());
                final String displayOrder = fullMd.getDataInfo()
                        .getDisplayOrder() == null ? null
                                : String.valueOf(
                                        fullMd.getDataInfo().getDisplayOrder());

                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER,
                            "record schema (" + schema + ")"); // DEBUG
                    Log.debug(Geonet.DATA_MANAGER,
                            "record createDate (" + createDate + ")"); // DEBUG
                }

                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.ROOT, root, true, true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.SCHEMA, schema, true, true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.DATABASE_CREATE_DATE, createDate,
                        true, true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.DATABASE_CHANGE_DATE, changeDate,
                        true, true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.SOURCE, source, true, true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.IS_TEMPLATE,
                        metadataType.codeString, true, true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.UUID, uuid, true, true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.IS_HARVESTED, isHarvested, true,
                        true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.OWNER, owner, true, true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.DUMMY, "0", false, true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.POPULARITY, popularity, true,
                        true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.RATING, rating, true, true));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.DISPLAY_ORDER, displayOrder,
                        true, false));
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.EXTRA, extra, false, true));
                
                //Mark as draft
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.DRAFT, "Y", true, true));

                // If the metadata has an atom document, index related
                // information
                InspireAtomFeed feed = inspireAtomFeedRepository
                        .findByMetadataId(id$);

                if ((feed != null) && StringUtils.isNotEmpty(feed.getAtom())) {
                    moreFields.add(SearchManager.makeField("has_atom", "y",
                            true, true));
                    moreFields.add(SearchManager.makeField("any",
                            feed.getAtom(), false, true));
                }

                if (owner != null) {
                    User user = userRepository
                            .findOne(fullMd.getSourceInfo().getOwner());
                    if (user != null) {
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.USERINFO,
                                user.getUsername() + "|" + user.getSurname()
                                        + "|" + user.getName() + "|"
                                        + user.getProfile(),
                                true, false));
                    }
                }

                String logoUUID = null;
                if (groupOwner != null) {
                    final Group group = groupRepository.findOne(groupOwner);
                    if (group != null) {
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.GROUP_OWNER,
                                String.valueOf(groupOwner), true, true));
                        final boolean preferGroup = ApplicationContextHolder
                                .get().getBean(SettingManager.class)
                                .getValueAsBool(
                                        SettingManager.SYSTEM_PREFER_GROUP_LOGO,
                                        true);
                        if (group.getWebsite() != null
                                && !group.getWebsite().isEmpty()
                                && preferGroup) {
                            moreFields.add(SearchManager.makeField(
                                    Geonet.IndexFieldNames.GROUP_WEBSITE,
                                    group.getWebsite(), true, false));
                        }
                        if (group.getLogo() != null && preferGroup) {
                            logoUUID = group.getLogo();
                        }
                    }
                }
                if (logoUUID == null) {
                    logoUUID = source;
                }

                if (logoUUID != null) {
                    final Path logosDir = Resources
                            .locateLogosDir(getServiceContext());
                    final String[] logosExt = { "png", "PNG", "gif", "GIF",
                            "jpg", "JPG", "jpeg", "JPEG", "bmp", "BMP", "tif",
                            "TIF", "tiff", "TIFF" };
                    boolean added = false;
                    for (String ext : logosExt) {
                        final Path logoPath = logosDir
                                .resolve(logoUUID + "." + ext);
                        if (Files.exists(logoPath)) {
                            added = true;
                            moreFields.add(SearchManager
                                    .makeField(Geonet.IndexFieldNames.LOGO,
                                            "/images/logos/"
                                                    + logoPath.getFileName(),
                                            true, false));
                            break;
                        }
                    }

                    if (!added) {
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.LOGO,
                                "/images/logos/" + logoUUID + ".png", true,
                                false));
                    }
                }

                // get privileges
                List<OperationAllowed> operationsAllowed = operationAllowedRepository
                        .findAllById_MetadataId(id$);

                for (OperationAllowed operationAllowed : operationsAllowed) {
                    OperationAllowedId operationAllowedId = operationAllowed
                            .getId();
                    int groupId = operationAllowedId.getGroupId();
                    int operationId = operationAllowedId.getOperationId();

                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.OP_PREFIX + operationId,
                            String.valueOf(groupId), true, true));
                    if (operationId == ReservedOperation.view.getId()) {
                        Group g = groupRepository.findOne(groupId);
                        if (g != null) {
                            moreFields.add(SearchManager.makeField(
                                    Geonet.IndexFieldNames.GROUP_PUBLISHED,
                                    g.getName(), true, true));
                        }
                    }
                }

                for (MetadataCategory category : fullMd.getCategories()) {
                    moreFields.add(
                            SearchManager.makeField(Geonet.IndexFieldNames.CAT,
                                    category.getName(), true, true));
                }

                // get status
                Sort statusSort = new Sort(Sort.Direction.DESC,
                        MetadataStatus_.id.getName() + "."
                                + MetadataStatusId_.changeDate.getName());
                List<MetadataStatus> statuses = mdStatusRepository
                        .findAllById_MetadataId(id$, statusSort);
                if (!statuses.isEmpty()) {
                    MetadataStatus stat = statuses.get(0);
                    String status = String.valueOf(stat.getId().getStatusId());
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.STATUS, status, true, true));
                    String statusChangeDate = stat.getId().getChangeDate()
                            .getDateAndTime();
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.STATUS_CHANGE_DATE,
                            statusChangeDate, true, true));
                }

                // getValidationInfo
                // -1 : not evaluated
                // 0 : invalid
                // 1 : valid
                List<MetadataValidation> validationInfo = mdValidationRepository
                        .findAllById_MetadataId(id$);
                if (validationInfo.isEmpty()) {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.VALID, "-1", true, true));
                } else {
                    String isValid = "1";
                    for (MetadataValidation vi : validationInfo) {
                        String type = vi.getId().getValidationType();
                        MetadataValidationStatus status = vi.getStatus();
                        if (status == MetadataValidationStatus.INVALID
                                && vi.isRequired()) {
                            isValid = "0";
                        }
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.VALID + "_" + type,
                                status.getCode(), true, true));
                    }
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.VALID, isValid, true, true));
                }
                searchManager.index(schemaManager.getSchemaDir(schema), md,
                        metadataId, moreFields, metadataType, root,
                        forceRefreshReaders);
            } catch (Exception x) {
                Log.error(Geonet.DATA_MANAGER,
                        "The metadata document index with id=" + metadataId
                                + " is corrupt/invalid - ignoring it. Error: "
                                + x.getMessage(),
                        x);
                fullMd = null;
            } finally {
                indexLock.lock();
                try {
                    indexing.remove(metadataId);
                } finally {
                    indexLock.unlock();
                }
            }
            if (fullMd != null) {
                applicationEventPublisher
                        .publishEvent(new MetadataIndexCompleted(fullMd));
            }

        }
    }
    
    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataIndexer#indexMetadata(java.lang.String,
     *      boolean)
     * @param metadataId
     * @param forceRefreshReaders
     * @throws Exception
     */
    public void superIndexMetadata(final String metadataId,
            boolean forceRefreshReaders, boolean hasDraft) throws Exception {
        indexLock.lock();
        try {
            if (waitForIndexing.contains(metadataId)) {
                return;
            }
            while (indexing.contains(metadataId)) {
                try {
                    waitForIndexing.add(metadataId);
                    // don't index the same metadata 2x
                    wait(200);
                } catch (InterruptedException e) {
                    return;
                } finally {
                    waitForIndexing.remove(metadataId);
                }
            }
            indexing.add(metadataId);
        } finally {
            indexLock.unlock();
        }
        Metadata fullMd;

        try {
            Vector<Element> moreFields = new Vector<Element>();
            int id$ = Integer.parseInt(metadataId);

            // get metadata, extracting and indexing any xlinks
            Element md = ApplicationContextHolder.get()
                    .getBean(XmlSerializer.class)
                    .selectNoXLinkResolver(metadataId, true);
            if (ApplicationContextHolder.get().getBean(XmlSerializer.class)
                    .resolveXLinks()) {
                List<Attribute> xlinks = Processor.getXLinks(md);
                if (xlinks.size() > 0) {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.HASXLINKS, "1", true, true));
                    StringBuilder sb = new StringBuilder();
                    for (Attribute xlink : xlinks) {
                        sb.append(xlink.getValue());
                        sb.append(" ");
                    }
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.XLINK, sb.toString(), true,
                            true));
                    Processor.detachXLink(md, getServiceContext());
                } else {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
                }
            } else {
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
            }

            fullMd = mdRepository.findOne(id$);

            final String schema = fullMd.getDataInfo().getSchemaId();
            final String createDate = fullMd.getDataInfo().getCreateDate()
                    .getDateAndTime();
            final String changeDate = fullMd.getDataInfo().getChangeDate()
                    .getDateAndTime();
            final String source = fullMd.getSourceInfo().getSourceId();
            final MetadataType metadataType = fullMd.getDataInfo().getType();
            final String root = fullMd.getDataInfo().getRoot();
            final String uuid = fullMd.getUuid();
            final String extra = fullMd.getDataInfo().getExtra();
            final String isHarvested = String.valueOf(Constants
                    .toYN_EnabledChar(fullMd.getHarvestInfo().isHarvested()));
            final String owner = String
                    .valueOf(fullMd.getSourceInfo().getOwner());
            final Integer groupOwner = fullMd.getSourceInfo().getGroupOwner();
            final String popularity = String
                    .valueOf(fullMd.getDataInfo().getPopularity());
            final String rating = String
                    .valueOf(fullMd.getDataInfo().getRating());
            final String displayOrder = fullMd.getDataInfo()
                    .getDisplayOrder() == null ? null
                            : String.valueOf(
                                    fullMd.getDataInfo().getDisplayOrder());

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER,
                        "record schema (" + schema + ")"); // DEBUG
                Log.debug(Geonet.DATA_MANAGER,
                        "record createDate (" + createDate + ")"); // DEBUG
            }

            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.ROOT,
                    root, true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.SCHEMA, schema, true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.DATABASE_CREATE_DATE, createDate,
                    true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.DATABASE_CHANGE_DATE, changeDate,
                    true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.SOURCE, source, true, true));
            moreFields.add(
                    SearchManager.makeField(Geonet.IndexFieldNames.IS_TEMPLATE,
                            metadataType.codeString, true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.UUID,
                    uuid, true, true));
            moreFields.add(
                    SearchManager.makeField(Geonet.IndexFieldNames.IS_HARVESTED,
                            isHarvested, true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.OWNER,
                    owner, true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DUMMY,
                    "0", false, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.POPULARITY, popularity, true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.RATING, rating, true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.DISPLAY_ORDER, displayOrder, true,
                    false));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.EXTRA,
                    extra, false, true));
            
            //Mark if draft exists (E) or not (N)
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.DRAFT, hasDraft? "E" : "N", true, true));

            // If the metadata has an atom document, index related information
            InspireAtomFeed feed = inspireAtomFeedRepository
                    .findByMetadataId(id$);

            if ((feed != null) && StringUtils.isNotEmpty(feed.getAtom())) {
                moreFields.add(
                        SearchManager.makeField("has_atom", "y", true, true));
                moreFields.add(SearchManager.makeField("any", feed.getAtom(),
                        false, true));
            }

            if (owner != null) {
                User user = userRepository
                        .findOne(fullMd.getSourceInfo().getOwner());
                if (user != null) {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.USERINFO,
                            user.getUsername() + "|" + user.getSurname() + "|"
                                    + user.getName() + "|" + user.getProfile(),
                            true, false));
                }
            }

            String logoUUID = null;
            if (groupOwner != null) {
                final Group group = groupRepository.findOne(groupOwner);
                if (group != null) {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.GROUP_OWNER,
                            String.valueOf(groupOwner), true, true));
                    final boolean preferGroup = ApplicationContextHolder.get()
                            .getBean(SettingManager.class).getValueAsBool(
                                    SettingManager.SYSTEM_PREFER_GROUP_LOGO,
                                    true);
                    if (group.getWebsite() != null
                            && !group.getWebsite().isEmpty() && preferGroup) {
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.GROUP_WEBSITE,
                                group.getWebsite(), true, false));
                    }
                    if (group.getLogo() != null && preferGroup) {
                        logoUUID = group.getLogo();
                    }
                }
            }
            if (logoUUID == null) {
                logoUUID = source;
            }

            if (logoUUID != null) {
                final Path logosDir = Resources
                        .locateLogosDir(getServiceContext());
                final String[] logosExt = { "png", "PNG", "gif", "GIF", "jpg",
                        "JPG", "jpeg", "JPEG", "bmp", "BMP", "tif", "TIF",
                        "tiff", "TIFF" };
                boolean added = false;
                for (String ext : logosExt) {
                    final Path logoPath = logosDir
                            .resolve(logoUUID + "." + ext);
                    if (Files.exists(logoPath)) {
                        added = true;
                        moreFields.add(SearchManager
                                .makeField(Geonet.IndexFieldNames.LOGO,
                                        "/images/logos/"
                                                + logoPath.getFileName(),
                                        true, false));
                        break;
                    }
                }

                if (!added) {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.LOGO,
                            "/images/logos/" + logoUUID + ".png", true, false));
                }
            }

            // get privileges
            List<OperationAllowed> operationsAllowed = operationAllowedRepository
                    .findAllById_MetadataId(id$);

            for (OperationAllowed operationAllowed : operationsAllowed) {
                OperationAllowedId operationAllowedId = operationAllowed
                        .getId();
                int groupId = operationAllowedId.getGroupId();
                int operationId = operationAllowedId.getOperationId();

                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.OP_PREFIX + operationId,
                        String.valueOf(groupId), true, true));
                if (operationId == ReservedOperation.view.getId()) {
                    Group g = groupRepository.findOne(groupId);
                    if (g != null) {
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.GROUP_PUBLISHED,
                                g.getName(), true, true));
                    }
                }
            }

            for (MetadataCategory category : fullMd.getCategories()) {
                moreFields
                        .add(SearchManager.makeField(Geonet.IndexFieldNames.CAT,
                                category.getName(), true, true));
            }

            // get status
            Sort statusSort = new Sort(Sort.Direction.DESC,
                    MetadataStatus_.id.getName() + "."
                            + MetadataStatusId_.changeDate.getName());
            List<MetadataStatus> statuses = mdStatusRepository
                    .findAllById_MetadataId(id$, statusSort);
            if (!statuses.isEmpty()) {
                MetadataStatus stat = statuses.get(0);
                String status = String.valueOf(stat.getId().getStatusId());
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.STATUS, status, true, true));
                String statusChangeDate = stat.getId().getChangeDate()
                        .getDateAndTime();
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.STATUS_CHANGE_DATE,
                        statusChangeDate, true, true));
            }

            // getValidationInfo
            // -1 : not evaluated
            // 0 : invalid
            // 1 : valid
            List<MetadataValidation> validationInfo = mdValidationRepository
                    .findAllById_MetadataId(id$);
            if (validationInfo.isEmpty()) {
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.VALID, "-1", true, true));
            } else {
                String isValid = "1";
                for (MetadataValidation vi : validationInfo) {
                    String type = vi.getId().getValidationType();
                    MetadataValidationStatus status = vi.getStatus();
                    if (status == MetadataValidationStatus.INVALID
                            && vi.isRequired()) {
                        isValid = "0";
                    }
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.VALID + "_" + type,
                            status.getCode(), true, true));
                }
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.VALID, isValid, true, true));
            }
            searchManager.index(schemaManager.getSchemaDir(schema), md,
                    metadataId, moreFields, metadataType, root,
                    forceRefreshReaders);
        } catch (Exception x) {
            Log.error(Geonet.DATA_MANAGER,
                    "The metadata document index with id=" + metadataId
                            + " is corrupt/invalid - ignoring it. Error: "
                            + x.getMessage(),
                    x);
            fullMd = null;
        } finally {
            indexLock.lock();
            try {
                indexing.remove(metadataId);
            } finally {
                indexLock.unlock();
            }
        }
        if (fullMd != null) {
            applicationEventPublisher
                    .publishEvent(new MetadataIndexCompleted(fullMd));
        }
    }
}
