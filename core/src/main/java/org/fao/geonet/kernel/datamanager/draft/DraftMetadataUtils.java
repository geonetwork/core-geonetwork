package org.fao.geonet.kernel.datamanager.draft;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataHarvestInfo;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataUtils;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.SimpleMetadata;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.reports.MetadataReportsQueries;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

public class DraftMetadataUtils extends BaseMetadataUtils implements IMetadataUtils {

    @Autowired
    private MetadataDraftRepository metadataDraftRepository;

    public void init(ServiceContext context, Boolean force) throws Exception {
        this.metadataDraftRepository = context.getBean(MetadataDraftRepository.class);
        super.init(context, force);
    }

    @Override
    public void setTemplateExt(final int id, final MetadataType metadataType) throws Exception {
        try {
            super.setTemplateExt(id, metadataType);
        } catch (EntityNotFoundException e) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(@Nonnull MetadataDraft metadata) {
                    final MetadataDataInfo dataInfo = metadata.getDataInfo();
                    dataInfo.setType(metadataType);
                }
            });            
        }
    }

    /**
     * Set metadata type to subtemplate and set the title. Only subtemplates need to persist the title as it is used to give a meaningful
     * title for use when offering the subtemplate to users in the editor.
     *
     * @param id Metadata id to set to type subtemplate
     * @param title Title of metadata of subtemplate/fragment
     */
    @Override
    public void setSubtemplateTypeAndTitleExt(final int id, String title) throws Exception {
        try {
            super.setSubtemplateTypeAndTitleExt(id, title);
        } catch (EntityNotFoundException e) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(@Nonnull MetadataDraft metadata) {
                    final MetadataDataInfo dataInfo = metadata.getDataInfo();
                    dataInfo.setType(MetadataType.SUB_TEMPLATE);
                    if (title != null) {
                        dataInfo.setTitle(title);
                    }
                }
            });
        }
    }

    @Override
    public void setHarvestedExt(final int id, final String harvestUuid, final Optional<String> harvestUri) throws Exception {
        try {
            super.setHarvestedExt(id, harvestUuid, harvestUri);
        } catch (EntityNotFoundException e) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(MetadataDraft metadata) {
                    MetadataHarvestInfo harvestInfo = metadata.getHarvestInfo();
                    harvestInfo.setUuid(harvestUuid);
                    harvestInfo.setHarvested(harvestUuid != null);
                    harvestInfo.setUri(harvestUri.orNull());
                }
            });
        }
    }

    /**
     *
     * @param id
     * @param displayOrder
     * @throws Exception
     */
    @Override
    public void updateDisplayOrder(final String id, final String displayOrder) throws Exception {
        try {
            super.updateDisplayOrder(id, displayOrder);
        } catch (EntityNotFoundException e) {
            metadataDraftRepository.update(Integer.valueOf(id), new Updater<MetadataDraft>() {
                @Override
                public void apply(MetadataDraft entity) {
                    entity.getDataInfo().setDisplayOrder(Integer.parseInt(displayOrder));
                }
            });
        }
    }

    /**
     * Rates a metadata.
     *
     * @param ipAddress ipAddress IP address of the submitting client
     * @param rating range should be 1..5
     * @throws Exception hmm
     */
    @Override
    public int rateMetadata(final int metadataId, final String ipAddress, final int rating) throws Exception {
        final int newRating = ratingByIpRepository.averageRating(metadataId);
        try {
            return super.rateMetadata(metadataId, ipAddress, rating);
        } catch (EntityNotFoundException e) {    
            metadataDraftRepository.update(metadataId, new Updater<MetadataDraft>() {
                @Override
                public void apply(MetadataDraft entity) {
                    entity.getDataInfo().setRating(newRating);
                }
            });
        }
        return rating;
    }

    @SuppressWarnings("unchecked")
    @Override
    public long count(Specification<? extends IMetadata> specs) {
        long tmp = 0;
        try {
            tmp += super.count((Specification<Metadata>) specs);
        } catch (Throwable t) {
            // Maybe it is not a Specification<Metadata>
        }
        try {
            tmp += metadataDraftRepository.count((Specification<MetadataDraft>) specs);
        } catch (Throwable t) {
            // Maybe it is not a Specification<MetadataDraft>
        }
        return tmp;
    }

    @Override
    public long count() {
        return super.count() + metadataDraftRepository.count();
    }

    @Override
    public IMetadata findOne(int id) {
        if (super.exists(id)) {
            return super.findOne(id);
        }
        return metadataDraftRepository.findOne(id);
    }

    @Override
    public IMetadata findOneByUuid(String uuid) {
        IMetadata md = super.findOneByUuid(uuid);
        if (md == null) {
            md = metadataDraftRepository.findOneByUuid(uuid);
        }
        return md;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IMetadata findOne(Specification<? extends IMetadata> spec) {
        IMetadata md = super.findOne(spec);

        if (md == null) {
            try {
                md = metadataDraftRepository.findOne((Specification<MetadataDraft>) spec);
            } catch (Throwable t) {
                // Maybe it is not a Specification<MetadataDraft>
            }
        }

        return md;
    }

    @Override
    public IMetadata findOne(String id) {
        IMetadata md = super.findOne(id);
        if (md == null) {
            md = metadataDraftRepository.findOne(id);
        }
        return md;
    }

    @Override
    public List<? extends IMetadata> findAllByHarvestInfo_Uuid(String uuid) {
        List<IMetadata> list = new LinkedList<IMetadata>();
        list.addAll(metadataDraftRepository.findAllByHarvestInfo_Uuid(uuid));
        list.addAll(super.findAllByHarvestInfo_Uuid(uuid));
        return list;
    }

    @Override
    public Iterable<? extends IMetadata> findAll(Set<Integer> keySet) {
        List<IMetadata> list = new LinkedList<IMetadata>();
        for (IMetadata md : super.findAll(keySet)) {
            list.add(md);
        }
        list.addAll(metadataDraftRepository.findAll(keySet));
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends IMetadata> findAll(Specification<? extends IMetadata> specs) {
        List<IMetadata> list = new LinkedList<IMetadata>();
        list.addAll(super.findAll(specs));
        try {
            list.addAll(metadataDraftRepository.findAll((Specification<MetadataDraft>) specs));
        } catch (Throwable t) {
            // Maybe it is not a Specification<MetadataDraft>
        }
        return list;
    }

    @Override
    public List<SimpleMetadata> findAllSimple(String harvestUuid) {
        List<SimpleMetadata> list = super.findAllSimple(harvestUuid);
        list.addAll(metadataDraftRepository.findAllSimple(harvestUuid));
        return list;
    }

    @Override
    public boolean exists(Integer iId) {
        return super.exists(iId) || metadataDraftRepository.exists(iId);
    }

    @Override
    // TODO maybe we should add drafts here too?
    public MetadataReportsQueries getMetadataReports() {
        return super.getMetadataReports();
    }

    @Override
    // TODO maybe we should add drafts here too?
    public Element findAllAsXml(Specification<? extends IMetadata> specs, Sort sortByChangeDateDesc) {
        return super.findAllAsXml(specs, sortByChangeDateDesc);
    }

    @Override
    // TODO maybe we should add drafts here too?
    public Element findAllAsXml(@Nullable Specification<? extends IMetadata> specs, @Nullable Pageable pageable) {
        return super.findAllAsXml(specs, pageable);
    }

    @Override
    public Page<Pair<Integer, ISODate>> findAllIdsAndChangeDates(Pageable pageable) {
        List<Pair<Integer, ISODate>> list = new LinkedList<Pair<Integer, ISODate>>();

        list.addAll(super.findAllIdsAndChangeDates(pageable).getContent());
        list.addAll(metadataDraftRepository.findAllIdsAndChangeDates(pageable).getContent());

        Page<Pair<Integer, ISODate>> res = new PageImpl<Pair<Integer, ISODate>>(list, pageable, list.size());
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<? extends IMetadata> spec) {
        Map<Integer, MetadataSourceInfo> map = super.findAllSourceInfo(spec);
        try {
            map.putAll(metadataDraftRepository.findAllSourceInfo((Specification<MetadataDraft>) spec));
        } catch (Throwable t) {
            // Maybe it is not a Specification<MetadataDraft>
        }
        return map;
    }
}
