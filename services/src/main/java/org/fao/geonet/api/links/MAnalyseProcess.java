package org.fao.geonet.api.links;

import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.url.UrlAnalyzer;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.transaction.TransactionStatus;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static jeeves.transaction.TransactionManager.CommitBehavior.ALWAYS_COMMIT;
import static jeeves.transaction.TransactionManager.TransactionRequirement.CREATE_NEW;

@ManagedResource()
public class MAnalyseProcess implements SelfNaming {

    private final ApplicationContext appContext;
    private final LinkRepository linkRepository;

    private final MetadataRepository metadataRepository;

    private final UrlAnalyzer urlAnalyser;

    private ObjectName probeName;
    private int metadataToAnalyseCount = -1;
    private int metadataAnalysed = 0;
    private int metadataNotAnalysedInError = 0;
    private int urlToCheckCount = -1;
    private final AtomicInteger urlChecked = new AtomicInteger(0);
    private long deleteAllDate = Long.MAX_VALUE;
    private long analyseMdDate = Long.MAX_VALUE;
    private long testLinkDate = Long.MAX_VALUE;


    public MAnalyseProcess(String catalogueId,
                           LinkRepository linkRepository,
                           MetadataRepository metadataRepository,
                           UrlAnalyzer urlAnalyser,
                           ApplicationContext appContext) {
        this.linkRepository = linkRepository;
        this.metadataRepository = metadataRepository;
        this.urlAnalyser = urlAnalyser;
        this.appContext = appContext;
        try {
            this.probeName = new ObjectName(String.format("geonetwork-%s:name=url-check,idx=%s", catalogueId, this.hashCode()));
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    @ManagedAttribute
    public int getMetadataToAnalyseCount() {
        return metadataToAnalyseCount;
    }

    @ManagedAttribute
    public int getUrlToCheckCount() {
        return urlToCheckCount;
    }

    @ManagedAttribute
    public int getMetadataAnalysed() {
        return metadataAnalysed;
    }

    @ManagedAttribute
    public int getUrlChecked() {
        return urlChecked.get();
    }

    @ManagedAttribute
    public int getMetadataNotAnalysedInError() {
        return metadataNotAnalysedInError;
    }

    @ManagedAttribute
    public long getDeleteAllDate() {
        return deleteAllDate;
    }

    @ManagedAttribute
    public long getAnalyseMdDate() {
        return analyseMdDate;
    }

    @ManagedAttribute
    public long getTestLinkDate() {
        return testLinkDate;
    }

    @ManagedAttribute
    public ObjectName getObjectName() {
        return this.probeName;
    }

    public void deleteAll() {
        runInNewTransaction("manalyseprocess-deleteall", new TransactionTask<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                deleteAllDate = System.currentTimeMillis();
                urlAnalyser.deleteAll();
                return null;
            }
        });
    }

    public void processMetadataAndTestLink(boolean testLink, Set<Integer> ids) throws JDOMException, IOException {
        metadataToAnalyseCount = ids.size();
        analyseMdDate = System.currentTimeMillis();
        for (int i : ids) {
            try {
                Metadata metadata = metadataRepository.findById(i).get();
                Element xmlData = metadata.getXmlData(false);
                runInNewTransaction("manalyseprocess-process-metadata", new TransactionTask<Object>() {
                    @Override
                    public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                        try {
                            urlAnalyser.processMetadata(xmlData, metadata);
                            metadataAnalysed++;
                        } catch (Exception e) {
                            metadataNotAnalysedInError++;
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                metadataNotAnalysedInError++;
                e.printStackTrace();
            }
        }
        if (testLink) {
            testLink(null);
        }
    }


    public void testLink(List<String> links) throws JDOMException, IOException {
        List<Link> linkList;
        if (links == null) {
            linkList = linkRepository.findAll();
        } else {
            linkList = linkRepository.findAllByUrlIn(links);
        }
        urlToCheckCount = linkList.size();
        runInNewTransaction("manalyseprocess-testlink", new TransactionTask<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                testLinkDate = System.currentTimeMillis();
                urlToCheckCount = linkList.size();
                linkList
                    .parallelStream()
                    .peek(urlAnalyser::testLink)
                    .forEach(x -> urlChecked.getAndIncrement());
                return null;
            }
        });
    }

    private final void runInNewTransaction(String name, TransactionTask<Object> transactionTask) {
        TransactionManager.runInTransaction(name, appContext, CREATE_NEW, ALWAYS_COMMIT, false, transactionTask);
    }
}
