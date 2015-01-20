package org.fao.geonet.services.metadata;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.ServiceNotAllowedEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;

/**
 * Service to publish and unpublish one or more metadata.  This service only modifies guest, all and intranet privileges all others
 * are left unmodified.
 *
 * @author Jesse on 1/16/2015.
 */
@Controller("md.publish")
public class Publish {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;
    @Autowired
    private ServiceManager serviceManager;
    @VisibleForTesting
    boolean testing = false;


    @RequestMapping(value = "/{lang}/md.publish", produces = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public PublishReport publish(
            @PathVariable String lang,
            HttpServletRequest request,
            @RequestParam(value = "ids", required = false) String commaSeparatedIds,
            @RequestParam(value = "skipIntranet", defaultValue = "false") boolean skipIntranet) throws Exception {
        final ServiceContext serviceContext = serviceManager.createServiceContext("md.publish", lang, request);

        return exec(commaSeparatedIds, true, skipIntranet, serviceContext);
    }


    @RequestMapping(value = "/{lang}/md.unpublish", produces = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseBody
    public PublishReport unpublish(
            @PathVariable String lang,
            HttpServletRequest request,
            @RequestParam(value = "ids", required = false) String commaSeparatedIds,
            @RequestParam(value = "skipIntranet", defaultValue = "false") boolean skipIntranet) throws Exception {
        final ServiceContext serviceContext = serviceManager.createServiceContext("md.publish", lang, request);

        return exec(commaSeparatedIds, false, skipIntranet, serviceContext);
    }

    /**
     * publish or unpublish the metadata identified by the commaSeparatedIds or the selection if ids is empty.
     *
     * @param commaSeparatedIds the ids of the metadata to publish/unpublish.
     * @param publish if true the metadata will be published otherwise unpublished
     * @param skipIntranet if true then metadata only the all group will be affected
     */
    private PublishReport exec(String commaSeparatedIds, boolean publish, boolean skipIntranet, ServiceContext serviceContext) throws
            Exception {
        final PublishReport report = new PublishReport();

        Iterator<String> iter = getIds(serviceContext.getUserSession(), commaSeparatedIds);

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
                doPublish(serviceContext, report, groupIds, toIndex, operationIds, mdId, allOpsSpec, operationAllowed);
            } else {
                doUnpublish(serviceContext, report, groupIds, toIndex, operationIds, mdId, allOpsSpec, operationAllowed);
            }
        }

        BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataManager, toIndex);
        r.process(testing || toIndex.size() < 5);


        return report;
    }

    private Iterator<String> getIds(UserSession userSession, final String commaSeparatedIds) {
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
        for (Integer groupId : groupIds) {
            for (Integer operationId : operationIds) {
                try {
                    if (publish) {
                        if (!dataManager.setOperation(serviceContext, metadataId, groupId, operationId)) {
                            return false;
                        }
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
        private int published, unpublished, unmodified, disallowed;

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

        @Override
        public String toString() {
            return "PublishReport{" +
                   "published=" + published +
                   ", unpublished=" + unpublished +
                   ", unmodified=" + unmodified +
                   ", disallowed=" + disallowed +
                   '}';
        }
    }
}
