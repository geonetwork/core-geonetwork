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

package org.fao.geonet.kernel.harvest.harvester.geonet20;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.AbstractAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmittor;
import org.fao.geonet.kernel.search.submission.batch.BatchingDeletionSubmittor;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.specification.MetadataCategorySpecs;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Aligner extends AbstractAligner<GeonetParams> {

    private final AtomicBoolean cancelMonitor;

    private Logger log;

    private XmlRequest req;

    private DataManager dataMan;

    private IMetadataManager metadataManager;

    private ServiceContext context;

    private CategoryMapper localCateg;

    private UUIDMapper localUuids;

    private HarvestResult result;

    public Aligner(AtomicBoolean cancelMonitor, Logger log, XmlRequest req, GeonetParams params, DataManager dm,
                   IMetadataManager metadataManager, ServiceContext sc, CategoryMapper cm) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.req = req;
        this.params = params;
        this.dataMan = dm;
        this.metadataManager = metadataManager;
        this.context = sc;
        this.localCateg = cm;
    }

    //--------------------------------------------------------------------------

    public HarvestResult align(Element result, String siteId) throws Exception {
        log.info("Start of alignment for site-id=" + siteId);

        this.result = new HarvestResult();
        this.result.siteId = siteId;

        @SuppressWarnings("unchecked")
        List<Element> mdList = result.getChildren("metadata");

        //-----------------------------------------------------------------------
        //--- retrieve local uuids for given site-id

        localUuids = new UUIDMapper(context.getBean(IMetadataUtils.class), siteId);

        //-----------------------------------------------------------------------
        //--- remove old metadata

        try (BatchingDeletionSubmittor submittor = new BatchingDeletionSubmittor()) {
            for (String uuid : localUuids.getUUIDs()) {
                if (cancelMonitor.get()) {
                    return this.result;
                }

                if (!exists(mdList, uuid)) {
                    String id = localUuids.getID(uuid);

                    if (log.isDebugEnabled()) log.debug("  - Removing old metadata with id=" + id);
                    metadataManager.deleteMetadata(context, id, submittor);

                    metadataManager.flush();
                    this.result.locallyRemoved++;
                }
            }
        }
        //-----------------------------------------------------------------------
        //--- insert/update new metadata

        for (Element aMdList : mdList) {
            if (cancelMonitor.get()) {
                return this.result;
            }

            Element info = aMdList.getChild("info", Edit.NAMESPACE);

            String remoteId = info.getChildText("id");
            String remoteUuid = info.getChildText("uuid");
            String schema = info.getChildText("schema");
            String changeDate = info.getChildText("changeDate");

            this.result.totalMetadata++;

            if (log.isDebugEnabled())
                log.debug("Obtained remote id=" + remoteId + ", changeDate=" + changeDate);

            if (!dataMan.existsSchema(schema)) {
                if (log.isDebugEnabled()) log.debug("  - Skipping unsupported schema : " + schema);
                this.result.schemaSkipped++;
            } else {
                String id = dataMan.getMetadataId(remoteUuid);

                if (id == null) {
                    id = addMetadata(info);
                } else {
                    updateMetadata(siteId, info, id);
                }

                metadataManager.flush();


                //--- maybe the metadata was unretrievable

                if (id != null) {
                    dataMan.indexMetadata(id, DirectIndexSubmittor.INSTANCE);
                }
            }
        }

        log.info("End of alignment for site-id=" + siteId);

        return this.result;
    }

    //--------------------------------------------------------------------------

    private String addMetadata(Element info) throws Exception {
        String remoteId = info.getChildText("id");
        String remoteUuid = info.getChildText("uuid");
        String schema = info.getChildText("schema");
        String createDate = info.getChildText("createDate");
        String changeDate = info.getChildText("changeDate");

        if (log.isDebugEnabled()) log.debug("  - Adding metadata with remote id=" + remoteId);

        Element md = getRemoteMetadata(req, remoteId);

        if (md == null) {
            log.warning("  - Cannot get metadata (possibly bad XML) with remote id=" + remoteId);
            return null;
        }

        //
        //  insert metadata
        //
        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(remoteUuid);
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(md.getQualifiedName()).
            setType(MetadataType.METADATA).
            setChangeDate(new ISODate(changeDate)).
            setCreateDate(new ISODate(createDate));
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(getOwner());
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid());

        try {
            metadata.getSourceInfo().setGroupOwner(Integer.valueOf(params.getOwnerIdGroup()));
        } catch (NumberFormatException e) {
        }

        @SuppressWarnings("unchecked")
        List<Element> categories = info.getChildren("category");
        addCategories(metadata, categories);

        metadata = metadataManager.insertMetadata(context, metadata, md, IndexingMode.none, false, UpdateDatestamp.NO, false, DirectIndexSubmittor.INSTANCE);

        String id = String.valueOf(metadata.getId());

        result.addedMetadata++;

        addPrivileges(id);

        return id;
    }

    private void addCategories(AbstractMetadata metadata, List<Element> categ) throws Exception {
        final MetadataCategoryRepository categoryRepository = context.getBean(MetadataCategoryRepository.class);
        Collection<String> catNames = Lists.transform(categ, new Function<Element, String>() {
            @Nullable
            @Override
            public String apply(@Nonnull Element input) {
                String catName = input.getText();
                return localCateg.getID(catName);
            }
        });
        catNames = Collections2.filter(catNames, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return input != null;
            }
        });
        final List<MetadataCategory> categories = categoryRepository.findAll(MetadataCategorySpecs.hasCategoryNameIn(catNames));

        if (log.isDebugEnabled()) {
            log.debug("    - Setting categories : " + categories);
        }

        metadata.getCategories().addAll(categories);
    }

    private void addPrivileges(String id) throws Exception {
        //--- set view privilege for both groups 'intranet' and 'all'
        dataMan.setOperation(context, id, "0", "0");
        dataMan.setOperation(context, id, "1", "0");
    }

    private void updateMetadata(String siteId, Element info, String id) throws Exception {
        String remoteId = info.getChildText("id");
        String remoteUuid = info.getChildText("uuid");
        String changeDate = info.getChildText("changeDate");

        if (localUuids.getID(remoteUuid) == null) {
            log.error("  - Warning! The remote uuid '" + remoteUuid + "' does not belong to site '" + siteId + "'");
            log.error("     - The site id of this metadata has been changed.");
            log.error("     - The metadata update will be skipped.");

            result.uuidSkipped++;
        } else {
            updateMetadata(id, remoteId, remoteUuid, changeDate);
            updateCategories(id, info);
        }
    }

    private void updateMetadata(String id, String remoteId, String remoteUuid, String changeDate) throws Exception {
        String date = localUuids.getChangeDate(remoteUuid);

        if (!updateCondition(date, changeDate)) {
            if (log.isDebugEnabled())
                log.debug("  - XML not changed to local metadata with id=" + id);
            result.unchangedMetadata++;
        } else {
            if (log.isDebugEnabled()) log.debug("  - Updating local metadata with id=" + id);

            Element md = getRemoteMetadata(req, remoteId);

            if (md == null) {
                log.warning("  - Cannot get metadata (possibly bad XML) with remote id=" + remoteId);
            } else {
                //
                // update metadata
                //
                boolean validate = false;
                boolean ufo = false;
                String language = context.getLanguage();
                metadataManager.updateMetadata(context, id, md, validate, ufo, language, changeDate, false, IndexingMode.none);

                result.updatedMetadata++;
            }
        }
    }

    private void updateCategories(String id, Element info) throws Exception {
        @SuppressWarnings("unchecked")
        List<Element> catList = info.getChildren("category");

        //--- remove old categories

        Collection<MetadataCategory> locCateg = dataMan.getCategories(id);

        for (MetadataCategory el : locCateg) {
            int catId = el.getId();
            String catName = el.getName();

            if (!existsCategory(catList, catName)) {
                if (log.isDebugEnabled()) {
                    log.debug("  - Unsetting category : " + catName);
                }
                dataMan.unsetCategory(context, id, catId);
            }
        }

        //--- add new categories

        for (Element categ : catList) {
            String catName = categ.getAttributeValue("name");
            String catId = localCateg.getID(catName);

            if (catId != null) {
                //--- it is not necessary to query the db. Anyway...
                if (!dataMan.isCategorySet(id, Integer.valueOf(catId))) {
                    if (log.isDebugEnabled()) {
                        log.debug("  - Setting category : " + catName);
                    }
                    dataMan.setCategory(context, id, catId);
                }
            }
        }
    }

    private boolean existsCategory(List<Element> catList, String name) {
        for (Object aCatList : catList) {
            Element categ = (Element) aCatList;
            String catName = categ.getText();

            if (catName.equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieves remote metadata. If validation is requested and the metadata does not validate,
     * returns null.
     */
    private Element getRemoteMetadata(XmlRequest req, String id) throws Exception {
        req.setAddress(params.getServletPath() + "/srv/en/" + Geonet.Service.XML_METADATA_GET);
        req.clearParams();
        req.addParam("id", id);

        try {
            Element md = req.execute();
            Element info = md.getChild("info", Edit.NAMESPACE);

            if (info != null)
                info.detach();

            try {
                Integer groupIdVal = null;
                if (StringUtils.isNotEmpty(params.getOwnerIdGroup())) {
                    groupIdVal = getGroupOwner();
                }

                params.getValidate().validate(dataMan, context, md, groupIdVal);
                return (Element) md.detach();
            } catch (Exception e) {
                log.info("Ignoring invalid metadata: " + id);
                result.doesNotValidate++;
            }

            return md;
        } catch (Exception e) {
            log.warning("Cannot retrieve remote metadata with id:" + id);
            log.warning(" (C) Error is : " + e.getMessage());

            return null;
        }
    }

    /**
     * Return true if the sourceId is present in the remote site
     */

    private boolean exists(List<Element> mdList, String uuid) {
        for (Element aMdList : mdList) {
            Element elInfo = aMdList.getChild("info", Edit.NAMESPACE);

            if (uuid.equals(elInfo.getChildText("uuid"))) {
                return true;
            }
        }

        return false;
    }

    private boolean updateCondition(String localDate, String remoteDate) {
        ISODate local = new ISODate(localDate);
        ISODate remote = new ISODate(remoteDate);

        //--- accept if remote date is greater than local date

        return (remote.timeDifferenceInSeconds(local) > 0);
    }
}
