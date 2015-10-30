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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.OpenwisDownload;
import org.fao.geonet.repository.OpenwisDownloadRepository;
import org.openwis.processedrequest.client.ProcessedRequestClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ConfigurableApplicationContext;
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

    private ConfigurableApplicationContext applicationContext;

    /**
     * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
     * @param context
     * @throws JobExecutionException
     */
    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {

        // Retrieve application context. A default SpringBeanJobFactory
        // will not provide the application context to the job. Use
        // AutowiringSpringBeanJobFactory.
        applicationContext = (ConfigurableApplicationContext) context
                .getJobDetail().getJobDataMap().get("applicationContext");

        ApplicationContextHolder.set(applicationContext);

        OpenwisDownloadRepository openwisRepository = this.applicationContext
                .getBean(OpenwisDownloadRepository.class);
        ProcessedRequestClient processedRequestClient = this.applicationContext
                .getBean(ProcessedRequestClient.class);

        Specification<OpenwisDownload> spec = new Specification<OpenwisDownload>() {
            public Predicate toPredicate(Root<OpenwisDownload> root,
                    CriteriaQuery<?> query, CriteriaBuilder builder) {
                Path<?> path = root.get("url");
                return path.isNull();
            }
        };

        List<OpenwisDownload> list = openwisRepository.findAll(spec);
        for (OpenwisDownload od : list) {
            if (od.getRequestId() == null) {
                openwisRepository.delete(od);
            } else if (processedRequestClient
                    .isFinished((long) od.getRequestId())) {
                od.setUrl(processedRequestClient
                        .getURL((long) od.getRequestId()));
                openwisRepository.save(od);
            }
        }
        
        //TODO what to do with downloads that are no longer available?
        // need the specific getRequests method from SOAP to cleanup
        
        openwisRepository.flush();
    }
}
