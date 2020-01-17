/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.services.metadata;

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.records.MetadataUtils;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.ServiceNotAllowedEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.util.WorkflowUtil;
import org.jdom.Document;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

/**
 * Service to publish and unpublish one or more metadata.  This service only modifies guest, all and
 * intranet privileges all others are left unmodified.
 *
 * @author Jesse on 1/16/2015.
 */
@Deprecated
@Controller("md.publish")
public class Publish {

    @VisibleForTesting
    boolean testing = false;


    @RequestMapping(value = "/{portal}/{lang}/md.publish", produces = {
        MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public PublishReport publish(
        @PathVariable String lang,
        HttpServletRequest request,
        @RequestParam(value = "ids", required = false) String commaSeparatedIds,
        @RequestParam(value = "skipIntranet", defaultValue = "false") boolean skipIntranet) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);
        final ServiceContext serviceContext = serviceManager.createServiceContext("md.publish", lang, request);

        return exec(commaSeparatedIds, true, skipIntranet, serviceContext);
    }


    @RequestMapping(value = "/{portal}/{lang}/md.unpublish", produces = {
        MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public PublishReport unpublish(
        @PathVariable String lang,
        HttpServletRequest request,
        @RequestParam(value = "ids", required = false) String commaSeparatedIds,
        @RequestParam(value = "skipIntranet", defaultValue = "false") boolean skipIntranet) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);
        final ServiceContext serviceContext = serviceManager.createServiceContext("md.publish", lang, request);

        return exec(commaSeparatedIds, false, skipIntranet, serviceContext);
    }

    /**
     * publish or unpublish the metadata identified by the commaSeparatedIds or the selection if ids
     * is empty.
     *
     * @param commaSeparatedIds the ids of the metadata to publish/unpublish.
     * @param publish           if true the metadata will be published otherwise unpublished
     * @param skipIntranet      if true then metadata only the all group will be affected
     */
    private PublishReport exec(String commaSeparatedIds, boolean publish, boolean skipIntranet, ServiceContext serviceContext) throws
        Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        DataManager dataManager = appContext.getBean(DataManager.class);
        OperationAllowedRepository operationAllowedRepository = appContext.getBean(OperationAllowedRepository.class);
        IMetadataUtils metadataRepository = appContext.getBean(IMetadataUtils.class);
        MetadataValidationRepository metadataValidationRepository = appContext.getBean(MetadataValidationRepository.class);
        IMetadataStatus metadataStatusRepository = appContext.getBean(IMetadataStatus.class);
        SettingManager sm = appContext.getBean(SettingManager.class);
        IMetadataValidator validator = appContext.getBean(IMetadataValidator.class);
        GroupRepository groupRepository = appContext.getBean(GroupRepository.class);

        boolean allowPublishInvalidMd = sm.getValueAsBool(Settings.METADATA_WORKFLOW_ALLOW_PUBLISH_INVALID_MD);
        boolean allowPublishNonApprovedMd = sm.getValueAsBool(Settings.METADATA_WORKFLOW_ALLOW_PUBLISH_NON_APPROVED_MD);

        final PublishReport report = new PublishReport();

        Iterator<String> iter = getIds(appContext, serviceContext.getUserSession(), commaSeparatedIds);

        final ArrayList<Integer> groupIds = Lists.newArrayList(ReservedGroup.all.getId());
        if (!skipIntranet) {
            groupIds.add(ReservedGroup.intranet.getId());
        }

        Set<Integer> toIndex = Sets.newHashSet();

        final Specification<OperationAllowed> hasGroupIdIn = OperationAllowedSpecs.hasGroupIdIn(groupIds);
        Collection<Integer> operationIds = Lists.newArrayList(ReservedOperation.download.getId(), ReservedOperation.view.getId(),
            ReservedOperation.dynamic.getId(), ReservedOperation.featured.getId(), ReservedOperation.notify.getId());
        final Specification<OperationAllowed> hasOperationIdIn = OperationAllowedSpecs.hasOperationIdIn(operationIds);
        while (iter.hasNext()) {
            String nextId = iter.next();
            if (nextId == null) {
                continue;
            }

            int mdId = Integer.parseInt(nextId);
            final Specifications<OperationAllowed> allOpsSpec = Specifications.where(hasMetadataId(nextId)).and
                (hasGroupIdIn).and(hasOperationIdIn);

            List<OperationAllowed> operationAllowed = operationAllowedRepository.findAll(allOpsSpec);
            if (publish) {
                AbstractMetadata metadata = metadataRepository.findOne(mdId);

                if (!allowPublishInvalidMd) {
                    boolean isInvalid = MetadataUtils.retrieveMetadataValidationStatus(metadata, serviceContext);

                    if (isInvalid) {
                        report.incNoValid();
                        continue;
                    }
                }

                if (!allowPublishNonApprovedMd) {
                    MetadataStatus metadataStatus = metadataStatusRepository.getStatus(metadata.getId());

                    String statusId = metadataStatus.getId().getStatusId() + "";
                    if (!statusId.equals(StatusValue.Status.APPROVED)) {
                        report.incNoApproved();
                        continue;
                    }
                }

                doPublish(serviceContext, report, groupIds, toIndex, operationIds, mdId, allOpsSpec, operationAllowed);
            } else {
                doUnpublish(serviceContext, report, groupIds, toIndex, operationIds, mdId, allOpsSpec, operationAllowed);
            }
        }

        BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataManager, toIndex);
        r.process(testing || toIndex.size() < 5);


        return report;
    }

    private Iterator<String> getIds(ConfigurableApplicationContext appContext, UserSession userSession, final String commaSeparatedIds) {
        final DataManager dataManager = appContext.getBean(DataManager.class);

        if (commaSeparatedIds == null) {
            if (userSession != null) {
                SelectionManager sm = SelectionManager.getManager(userSession);
                final Iterator<String> selectionIter = sm.getSelection(SelectionManager.SELECTION_METADATA).iterator();
                return Iterators.transform(selectionIter, new Function<String, String>() {
                    @Nullable
                    @Override
                    public String apply(String uuid) {
                        try {
                            return dataManager.getMetadataId(uuid);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                });
            } else {
                return Iterators.emptyIterator();
            }
        } else {
            return new Iterator<String>() {
                final StringTokenizer tokenizer = new StringTokenizer(commaSeparatedIds, ",", false);

                @Override
                public boolean hasNext() {
                    return tokenizer.hasMoreElements();
                }

                @Override
                public String next() {
                    return tokenizer.nextToken();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private void doUnpublish(ServiceContext serviceContext, PublishReport report, ArrayList<Integer> groupIds, Set<Integer> toIndex,
                             Collection<Integer> operationIds, int mdId, Specifications<OperationAllowed> allOpsSpec,
                             List<OperationAllowed> operationAllowed) throws Exception {

        OperationAllowedRepository operationAllowedRepository = serviceContext.getBean(OperationAllowedRepository.class);
        final long count = operationAllowedRepository.count(allOpsSpec);
        if (count == 0) {
            report.incUnmodified();
        } else {
            final boolean succeeded = updateOps(serviceContext, false, groupIds, operationIds, mdId);
            if (!succeeded) {
                operationAllowedRepository.deleteAll(allOpsSpec);
                operationAllowedRepository.save(operationAllowed);
                report.incDisallowed();
            } else {
                report.incUnpublished();
                toIndex.add(mdId);
            }
        }
    }

    private void doPublish(ServiceContext serviceContext, PublishReport report, ArrayList<Integer> groupIds, Set<Integer> toIndex,
                           Collection<Integer> operationIds, int mdId, Specifications<OperationAllowed> allOpsSpec,
                           List<OperationAllowed> operationAllowed) throws Exception {
        OperationAllowedRepository operationAllowedRepository = serviceContext.getBean(OperationAllowedRepository.class);
        long count = operationAllowedRepository.count(Specifications.where(hasMetadataId(mdId)).and
            (OperationAllowedSpecs.isPublic(ReservedOperation.view)));
        if (count == 1) {
            report.incUnmodified();
        } else {
            final boolean succeeded = updateOps(serviceContext, true, groupIds, operationIds, mdId);
            if (!succeeded) {
                operationAllowedRepository.deleteAll(allOpsSpec);
                operationAllowedRepository.save(operationAllowed);
                report.incDisallowed();
            } else {
                toIndex.add(mdId);
                report.incPublished();
            }
        }
    }

    private boolean updateOps(ServiceContext serviceContext, boolean publish, ArrayList<Integer> groupIds, Collection<Integer>
        operationIds, int metadataId) throws Exception {
        final DataManager dataManager = serviceContext.getBean(DataManager.class);

        for (Integer groupId : groupIds) {
            for (Integer operationId : operationIds) {
                try {
                    if (publish) {
                        dataManager.setOperation(serviceContext, metadataId, groupId, operationId);
                    } else {
                        dataManager.unsetOperation(serviceContext, metadataId, groupId, operationId);
                    }
                } catch (ServiceNotAllowedEx e) {
                    return false;
                }
            }
        }

        return true;
    }




    @XmlRootElement(name = "publishReport")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class PublishReport implements Serializable {
        private int published, unpublished, unmodified, disallowed, novalid, noapproved;

        public void incPublished() {
            published++;
        }

        public void incUnpublished() {
            unpublished++;
        }

        public void incUnmodified() {
            unmodified++;
        }

        public void incDisallowed() {
            disallowed++;
        }

        public void incNoValid() {
            novalid++;
        }

        public void incNoApproved() {
            noapproved++;
        }

        public int getPublished() {
            return published;
        }

        public int getUnpublished() {
            return unpublished;
        }

        public int getUnmodified() {
            return unmodified;
        }

        public int getDisallowed() {
            return disallowed;
        }

        public int getNovalid() {
            return novalid;
        }

        public int getNoapproved() {
            return noapproved;
        }

        @Override
        public String toString() {
            return "PublishReport{" +
                   "published=" + published +
                   ", unpublished=" + unpublished +
                   ", unmodified=" + unmodified +
                   ", disallowed=" + disallowed +
                   ", novalid=" + novalid +
                   ", noapproved=" + noapproved +
                   '}';
        }
    }
}
