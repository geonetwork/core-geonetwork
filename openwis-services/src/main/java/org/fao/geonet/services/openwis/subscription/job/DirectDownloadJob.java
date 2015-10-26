/**
 * 
 */
package org.fao.geonet.services.openwis.subscription.job;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.OpenwisDownload;
import org.fao.geonet.repository.OpenwisDownloadRepository;
import org.openwis.processedrequest.client.ProcessedRequestClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * openwis4-openwis-services
 * 
 * @author delawen
 * 
 * 
 */
public class DirectDownloadJob extends QuartzJobBean {

    @Autowired
    private ProcessedRequestClient processedRequestClient;

    @Autowired
    private OpenwisDownloadRepository openwisRepository;

    /**
     * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
     * @param context
     * @throws JobExecutionException
     */
    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        Specification<OpenwisDownload> spec = new Specification<OpenwisDownload>() {
            public Predicate toPredicate(Root<OpenwisDownload> root,
                    CriteriaQuery<?> query, CriteriaBuilder builder) {
                Path<?> path = root.get("url");
                return path.isNull();
            }
        };

        List<OpenwisDownload> list = openwisRepository.findAll(spec);
        for (OpenwisDownload od : list) {
            if (processedRequestClient.isFinished((long) od.getRequestId())) {
                od.setUrl(processedRequestClient
                        .getURL((long) od.getRequestId()));
                openwisRepository.save(od);
            }
        }
        openwisRepository.flush();
    }
}
