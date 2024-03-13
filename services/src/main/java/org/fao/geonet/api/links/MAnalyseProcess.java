//=============================================================================
//===   Copyright (C) 2001-2024 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.links;

import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.url.UrlAnalyzer;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static jeeves.transaction.TransactionManager.CommitBehavior.ALWAYS_COMMIT;
import static jeeves.transaction.TransactionManager.TransactionRequirement.CREATE_NEW;

@ManagedResource()
public class MAnalyseProcess implements SelfNaming {
    private static final String LOGGER = Geonet.GEONETWORK + ".metadatalinks";

    private final LinkRepository linkRepository;
    private final MetadataRepository metadataRepository;
    private final UrlAnalyzer urlAnalyser;
    private final ApplicationContext appContext;

    private ObjectName probeName;
    private final AtomicInteger metadataToAnalyseCount = new AtomicInteger(-1);
    private final AtomicInteger metadataAnalysed = new AtomicInteger(0);
    private final AtomicInteger metadataNotAnalysedInError = new AtomicInteger(0);
    private final AtomicInteger urlToCheckCount = new AtomicInteger(-1);
    private final AtomicInteger urlChecked = new AtomicInteger(0);
    private final AtomicLong deleteAllDate =  new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong analyseMdDate = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong testLinkDate = new AtomicLong(Long.MAX_VALUE);

    @ManagedAttribute
    public int getMetadataToAnalyseCount() {
        return metadataToAnalyseCount.get();
    }

    @ManagedAttribute
    public int getUrlToCheckCount() {
        return urlToCheckCount.get();
    }

    @ManagedAttribute
    public int getMetadataAnalysed() {
        return metadataAnalysed.get();
    }

    @ManagedAttribute
    public int getUrlChecked() {
        return urlChecked.get();
    }

    @ManagedAttribute
    public int getMetadataNotAnalysedInError() {
        return metadataNotAnalysedInError.get();
    }

    @ManagedAttribute
    public long getDeleteAllDate() {
        return deleteAllDate.get();
    }

    @ManagedAttribute
    public long getAnalyseMdDate() {
        return analyseMdDate.get();
    }

    @ManagedAttribute
    public long getTestLinkDate() {
        return testLinkDate.get();
    }

    @ManagedAttribute
    public ObjectName getObjectName() {
        return this.probeName;
    }

    public MAnalyseProcess(String catalogueId,
                                          LinkRepository linkRepository,
                                          MetadataRepository metadataRepository,
                                          UrlAnalyzer urlAnalyser,
                                          ApplicationContext appContext) {
        this.urlAnalyser = urlAnalyser;
        this.linkRepository = linkRepository;
        this.metadataRepository = metadataRepository;
        this.appContext = appContext;
        try {
            this.probeName = new ObjectName(String.format("geonetwork-%s:name=url-check,idx=%s", catalogueId, this.hashCode()));
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    public void deleteAll() {
        runInNewTransaction("manalyseprocess-deleteall", transaction -> {
            deleteAllDate.set(System.currentTimeMillis());
            urlAnalyser.deleteAll();
            return null;
        });
    }

    public void processMetadataAndTestLink(boolean testLink, Set<Integer> ids) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new MAnalyseProcess.LinksCheckCallable(testLink, ids));
        executorService.shutdown();
    }

    public void testLink(List<String> links) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute( new MAnalyseProcess.LinksCheckUrlCallable(links));
        executorService.shutdown();
    }

    /**
     * Test a list of links.
     */
    private final class LinksCheckUrlCallable implements Runnable {
        private List<String> links;
        LinksCheckUrlCallable(List<String> links) {
            this.links = links;
        }

        @Override
        public void run() {
            try {
                testLink(links);
            } catch (Exception ex) {
                Log.error(LOGGER, String.format("Error processing metadata links in process '%s'",
                    probeName), ex);
            }
        }

        private void testLink(List<String> links) {
            List<Link> linkList;
            if (links == null) {
                linkList = linkRepository.findAll();
            } else {
                linkList = linkRepository.findAllByUrlIn(links);
            }
            urlToCheckCount.set(linkList.size());

            for(int i = 0; i < urlToCheckCount.get(); i++) {
                Link link = linkList.get(i);
                runInNewTransaction("manalyseprocess-testlink", transaction -> {
                    testLinkDate.set(System.currentTimeMillis());
                    urlAnalyser.testLink(link);
                    urlChecked.getAndIncrement();

                    return null;
                });
            }
        }
    }

    /**
     * Test a list of links for the metadata provided.
     */
    private final class LinksCheckCallable implements Runnable {
        private boolean testLink;
        private Set<Integer> ids;
        LinksCheckCallable(boolean testLink, Set<Integer> ids) {
            this.testLink = testLink;
            this.ids = ids;
        }

        @Override
        public void run() {
            try {
                processMetadataAndTestLink(testLink, ids);
            } catch (Exception ex) {
                Log.error(LOGGER, String.format("Error processing metadata links in process '%s'",
                    probeName), ex);
            }
        }

        private void processMetadataAndTestLink(boolean testLink, Set<Integer> ids) throws JDOMException, IOException {
            metadataToAnalyseCount.set(ids.size());
            analyseMdDate.set(System.currentTimeMillis());

            for (int i : ids) {
                try {
                    Optional<Metadata> metadataOpt = metadataRepository.findById(i);

                    if (metadataOpt.isPresent()) {
                        Metadata metadata = metadataOpt.get();

                        Element xmlData = metadata.getXmlData(false);

                        runInNewTransaction("manalyseprocess-process-metadata", transaction -> {
                            try {
                                urlAnalyser.processMetadata(xmlData, metadata);
                                metadataAnalysed.incrementAndGet();
                            } catch (Exception e) {
                                metadataNotAnalysedInError.incrementAndGet();
                                Log.error(LOGGER, String.format("Error processing metadata links for metadata '%s' in process '%s'",
                                    metadata.getUuid(), probeName), e);
                            }
                            return null;
                        });
                    } else {
                        metadataNotAnalysedInError.incrementAndGet();
                        Log.warning(LOGGER, String.format("Processing metadata links in process '%s': metadata with id '%d' not found",
                            probeName, i));
                    }
                } catch (Exception e) {
                    metadataNotAnalysedInError.incrementAndGet();
                    Log.error(LOGGER, String.format("Error processing metadata links in process '%s'",
                        probeName), e);
                }
            }

            if (testLink) {
                testLink(null);
            }
        }

        private void testLink(List<String> links) {
            List<Link> linkList;
            if (links == null) {
                linkList = linkRepository.findAll();
            } else {
                linkList = linkRepository.findAllByUrlIn(links);
            }

            urlToCheckCount.set(linkList.size());

            for(int i = 0; i < urlToCheckCount.get(); i++) {
                Link link = linkList.get(i);
                runInNewTransaction("manalyseprocess-testlink", transaction -> {
                    testLinkDate.set(System.currentTimeMillis());
                    urlAnalyser.testLink(link);
                    urlChecked.getAndIncrement();

                    return null;
                });
            }
        }
    }

    private void runInNewTransaction(String name, TransactionTask<Object> transactionTask) {
        TransactionManager.runInTransaction(name, appContext, CREATE_NEW, ALWAYS_COMMIT, false, transactionTask);
    }
}
