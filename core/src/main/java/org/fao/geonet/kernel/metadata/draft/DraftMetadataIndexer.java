/**
 * 
 */
package org.fao.geonet.kernel.metadata.draft;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.events.md.MetadataIndexCompleted;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.metadata.DefaultMetadataIndexer;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.specification.MetadataDraftSpecs;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import jeeves.server.context.ServiceContext;

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
            if(md != null) {
                MetadataDraft mdD = mdDraftRepository.findOneByUuid(md.getUuid());
                if(mdD != null) {
                    indexMetadata(Integer.toString(mdD.getId()), false);
                }
            }
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
        Metadata metaData = mdRepository.findOne(metadataId);
        if (metaData != null) {
            //It is a normal metadata record
            MetadataDraft mdD = mdDraftRepository
                    .findOneByUuid(metaData.getUuid());
            
            superIndexMetadata(metadataId, forceRefreshReaders, (mdD != null));
        } else {
            //It is a draft
            try {
                if (waitForIndexing.contains(metadataId)) {
                    return;
                }
                while (indexing.contains(metadataId)) {
                    try {
                        waitForIndexing.add(metadataId);
                        // don't index the same metadata 2x
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        return;
                    } finally {
                        waitForIndexing.remove(metadataId);
                    }
                }
                indexing.add(metadataId);
            } finally {
            }
            MetadataDraft fullMd;

            try {
                Vector<Element> moreFields = new Vector<Element>();
                int id$ = Integer.parseInt(metadataId);

                // get metadata, extracting and indexing any xlinks
                Element md = processXLinks(metadataId, moreFields);

                fullMd = mdDraftRepository.findOne(id$);
                
                if(fullMd == null)
                    return;

                final String schema = fullMd.getDataInfo().getSchemaId();
                final String createDate = fullMd.getDataInfo().getCreateDate().getDateAndTime();
                final String changeDate = fullMd.getDataInfo().getChangeDate().getDateAndTime();
                final String source = fullMd.getSourceInfo().getSourceId();
                final MetadataType metadataType = fullMd.getDataInfo().getType();
                final String root = fullMd.getDataInfo().getRoot();
                final String uuid = fullMd.getUuid();
                final String extra = fullMd.getDataInfo().getExtra();
                final String isHarvested = String.valueOf(Constants.toYN_EnabledChar(fullMd.getHarvestInfo().isHarvested()));
                final String owner = String.valueOf(fullMd.getSourceInfo().getOwner());
                final Integer groupOwner = fullMd.getSourceInfo().getGroupOwner();
                final String popularity = String.valueOf(fullMd.getDataInfo().getPopularity());
                final String rating = String.valueOf(fullMd.getDataInfo().getRating());
                final String displayOrder = fullMd.getDataInfo().getDisplayOrder() == null ? null : String.valueOf(fullMd.getDataInfo().getDisplayOrder());
                final String draft = "Y";
                
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "record schema (" + schema + ")"); //DEBUG
                    Log.debug(Geonet.DATA_MANAGER, "record createDate (" + createDate + ")"); //DEBUG
                }


                addMoreFields(fullMd, moreFields, id$, schema, createDate,
                        changeDate, source, metadataType, root, uuid, extra,
                        isHarvested, owner, groupOwner, popularity, rating,
                        displayOrder, draft);
                
                getSearchManager().index(getSchemaManager().getSchemaDir(schema), md, metadataId, moreFields, metadataType, root, forceRefreshReaders);
            } catch (Exception x) {
                Log.error(Geonet.DATA_MANAGER, "The metadata document index with id=" + metadataId + " is corrupt/invalid - ignoring it. Error: " + x.getMessage(), x);
                fullMd = null;
            } finally {
                try {
                    indexing.remove(metadataId);
                } finally {
                }
            }
            if (fullMd != null) {
                applicationEventPublisher.publishEvent(new MetadataIndexCompleted(fullMd));
            }
        }
    }
    
    /**
     * Should be same as super.indexMetadata but with information about drafts
     * @param metadataId
     * @param forceRefreshReaders
     * @param hasDraft
     * @throws Exception
     */
    public void superIndexMetadata(final String metadataId,
            boolean forceRefreshReaders, boolean hasDraft) throws Exception {
        try {
            if (waitForIndexing.contains(metadataId)) {
                return;
            }
            if (!indexing.contains(metadataId)) {
                try {
                    waitForIndexing.add(metadataId);
                    // don't index the same metadata 2x
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                  return;
                } finally {
                    waitForIndexing.remove(metadataId);
                }
            }
            indexing.add(metadataId);
        } finally {
        }
        Metadata fullMd;

        try {
            Vector<Element> moreFields = new Vector<Element>();
            int id$ = Integer.parseInt(metadataId);

            // get metadata, extracting and indexing any xlinks
            Element md = processXLinks(metadataId, moreFields);

            fullMd = mdRepository.findOne(id$);

            final String schema = fullMd.getDataInfo().getSchemaId();
            final String createDate = fullMd.getDataInfo().getCreateDate().getDateAndTime();
            final String changeDate = fullMd.getDataInfo().getChangeDate().getDateAndTime();
            final String source = fullMd.getSourceInfo().getSourceId();
            final MetadataType metadataType = fullMd.getDataInfo().getType();
            final String root = fullMd.getDataInfo().getRoot();
            final String uuid = fullMd.getUuid();
            final String extra = fullMd.getDataInfo().getExtra();
            final String isHarvested = String.valueOf(Constants.toYN_EnabledChar(fullMd.getHarvestInfo().isHarvested()));
            final String owner = String.valueOf(fullMd.getSourceInfo().getOwner());
            final Integer groupOwner = fullMd.getSourceInfo().getGroupOwner();
            final String popularity = String.valueOf(fullMd.getDataInfo().getPopularity());
            final String rating = String.valueOf(fullMd.getDataInfo().getRating());
            final String displayOrder = fullMd.getDataInfo().getDisplayOrder() == null ? null : String.valueOf(fullMd.getDataInfo().getDisplayOrder());
            final String draft = (hasDraft? "E" : "N");
            
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "record schema (" + schema + ")"); //DEBUG
                Log.debug(Geonet.DATA_MANAGER, "record createDate (" + createDate + ")"); //DEBUG
            }

            addMoreFields(fullMd, moreFields, id$, schema, createDate,
                    changeDate, source, metadataType, root, uuid, extra,
                    isHarvested, owner, groupOwner, popularity, rating,
                    displayOrder, draft);
            
            getSearchManager().index(getSchemaManager().getSchemaDir(schema), md, metadataId, moreFields, metadataType, root, forceRefreshReaders);
        } catch (Exception x) {
            Log.error(Geonet.DATA_MANAGER, "The metadata document index with id=" + metadataId + " is corrupt/invalid - ignoring it. Error: " + x.getMessage(), x);
            fullMd = null;
        } finally {
            try {
                indexing.remove(metadataId);
            } finally {
            }
        }
        if (fullMd != null) {
            applicationEventPublisher.publishEvent(new MetadataIndexCompleted(fullMd));
        }
    }
}
