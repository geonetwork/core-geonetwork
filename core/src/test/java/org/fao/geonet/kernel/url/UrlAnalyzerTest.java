package org.fao.geonet.kernel.url;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.LinkStatus;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataLink;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectDeletionSubmitter;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.LinkStatusRepository;
import org.fao.geonet.repository.MetadataLinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.fao.geonet.kernel.UpdateDatestamp.NO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

public class UrlAnalyzerTest extends AbstractCoreIntegrationTest {


    private static final int TEST_OWNER = 42;

    @Autowired
    private IMetadataManager dataManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    protected LinkStatusRepository linkStatusRepository;

    @Autowired
    protected MetadataLinkRepository metadataLinkRepository;

    @Autowired
    private UrlChecker urlChecker;

    @Autowired
    protected SettingManager settingManager;

    @PersistenceContext
    private EntityManager entityManager;

    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
    }

    @Test
    public void encounteringAnUrlForTheFirstTimeAndPersistingIt() throws Exception {
        Element mdAsXml = getMdAsXml();
        AbstractMetadata md = insertMetadataInDb(mdAsXml);
        UrlAnalyzer toTest = createToTest();

        toTest.processMetadata(mdAsXml, md);

        Set<String> urlFromDb = linkRepository.findAll().stream().map(Link::getUrl).collect(Collectors.toSet());
        assertTrue(urlFromDb.contains("HTTPS://acme.de/"));
        assertTrue(urlFromDb.contains("ftp://mon-site.mondomaine/mon-repertoire"));
        assertTrue(urlFromDb.contains("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail_s.gif"));
        assertTrue(urlFromDb.contains("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail.gif"));
        assertTrue(urlFromDb.contains("http://www.fao.org/ag/AGL/aglw/aquastat/watresafrica/index.stm"));
        assertTrue(urlFromDb.contains("http://data.fao.org/maps/wms"));
        assertEquals(6, urlFromDb.size());
        SimpleJpaRepository metadataLinkRepository = new SimpleJpaRepository<MetadataLink, Integer>(MetadataLink.class, entityManager);
        List<MetadataLink> metadataLinkList = metadataLinkRepository.findAll();
        assertEquals(
                metadataLinkList.stream().map(x -> x.getLink().getId()).collect(Collectors.toSet()),
                linkRepository.findAll().stream().map(Link::getId).collect(Collectors.toSet()));
        assertEquals(
                metadataLinkList.stream().map(x -> x.getMetadataId()).collect(Collectors.toSet()),
                Collections.singleton(md.getId()));
    }


    @Test
    public void encounteringSameUrlInVariousMd() throws Exception {
        Element mdAsXml = getMdAsXml();
        AbstractMetadata mdOne = insertMetadataInDb(mdAsXml);
        AbstractMetadata mdTwo = insertMetadataInDb(mdAsXml);
        UrlAnalyzer toTest = createToTest();

        toTest.processMetadata(mdAsXml, mdOne);
        toTest.processMetadata(mdAsXml, mdTwo);

        Set<String> urlFromDb = linkRepository.findAll().stream().map(Link::getUrl).collect(Collectors.toSet());
        assertEquals(6, urlFromDb.size());
        SimpleJpaRepository metadataLinkRepository = new SimpleJpaRepository<MetadataLink, Integer>(MetadataLink.class, entityManager);
        List<MetadataLink> metadataLinkList = metadataLinkRepository.findAll();
        assertEquals(
                metadataLinkList.stream().map(x -> x.getLink().getId()).collect(Collectors.toSet()),
                linkRepository.findAll().stream().map(Link::getId).collect(Collectors.toSet()));
        assertEquals(
                metadataLinkList.stream().map(x -> x.getMetadataId()).collect(Collectors.toSet()),
                Stream.of(mdOne.getId(), mdTwo.getId()).collect(Collectors.toSet()));
        assertEquals(12, metadataLinkList.size());
    }

    @Test
    public void doNotInsertSameLinkOrUrlTwice() throws Exception {
        Element mdAsXml = getMdAsXml();
        AbstractMetadata md = insertMetadataInDb(mdAsXml);
        Xml.selectElement(mdAsXml, ".//gmd:abstract/gco:CharacterString")
                .setText("http://temporary_ressource_when_network_switch.org je répète http://temporary_ressource_when_network_switch.org");
        UrlAnalyzer toTest = createToTest();

        toTest.processMetadata(mdAsXml, md);

        List<Link> urlFromDb = linkRepository.findAll();
        assertEquals(5, urlFromDb.size());
        List<MetadataLink> metadataLinkList = metadataLinkRepository.findAll();
        assertEquals(5, metadataLinkList.size());

    }

    @Test
    public void cascadeDontFordbidMetadalinkDeletion() throws Exception {
        Element mdAsXml = getMdAsXml();
        AbstractMetadata md = insertMetadataInDb(mdAsXml);
        UrlAnalyzer toTest = createToTest();

        toTest.processMetadata(mdAsXml, md);
        toTest.processMetadata(mdAsXml, md);

        List<Link> urlFromDb = linkRepository.findAll();
        assertEquals(6, urlFromDb.size());
        List<MetadataLink> metadataLinkList = metadataLinkRepository.findAll();
        assertEquals(6, metadataLinkList.size());

    }

    @Test
    public void deleteMetadaLinkWhenMdDisappeared() throws Exception {
        // user will have to purge himself orphan link (no metadatalink anymore) using ui:
        //      one can imagine that when network switches for example url have to be kept aside
        // orphan metadatalink (no metadata anymore) purge can be trigered when checking link:
        //      this is toTest.purge method purpose.
        // note that metadata table and medatalink table are loosely coupled:
        //      no constraints beetween them.

        Element mdAsXml = getMdAsXml();
        AbstractMetadata md = insertMetadataInDb(mdAsXml);
        UrlAnalyzer toTest = createToTest();
        toTest.processMetadata(mdAsXml, md);
        SimpleJpaRepository metadataLinkRepository = new SimpleJpaRepository<MetadataLink, Integer>(MetadataLink.class, entityManager);
        List<MetadataLink> metadataLinkList = metadataLinkRepository.findAll();
        assertEquals(6, metadataLinkList.size());
        dataManager.deleteMetadata(context, md.getId() + "", DirectDeletionSubmitter.INSTANCE);

        linkRepository.findAll().stream().forEach(toTest::purgeMetataLink);

        metadataLinkList = metadataLinkRepository.findAll();
        assertEquals(0, metadataLinkList.size());
        Set<String> urlFromDb = linkRepository.findAll().stream().map(Link::getUrl).collect(Collectors.toSet());
        assertEquals(6, urlFromDb.size());
    }

    @Test
    public void updateMdCascade() throws Exception {
        // orphan metadatalink (no link anymore) purge is automatic...

        Element mdAsXml = getMdAsXml();
        AbstractMetadata md = insertMetadataInDb(mdAsXml);
        UrlAnalyzer toTest = createToTest();
        toTest.processMetadata(mdAsXml, md);
        SimpleJpaRepository metadataLinkRepository = new SimpleJpaRepository<MetadataLink, Integer>(MetadataLink.class, entityManager);
        List<MetadataLink> metadataLinkList = metadataLinkRepository.findAll();
        assertEquals(6, metadataLinkList.size());
        Xml.selectElement(mdAsXml, ".//gmd:abstract/gco:CharacterString").setText("http://temporary_ressource_when_network_switch.org");

        toTest.processMetadata(mdAsXml, md);

        metadataLinkList = metadataLinkRepository.findAll();
        assertEquals(5, metadataLinkList.size());
        Set<String> urlFromDb = linkRepository.findAll().stream().map(Link::getUrl).collect(Collectors.toSet());
        assertEquals(7, urlFromDb.size());
    }

    @Test
    public void urlCheckerToSetStatus() throws Exception {
        Element mdAsXml = getMdAsXml();
        AbstractMetadata md = insertMetadataInDb(mdAsXml);
        UrlAnalyzer toTest = createToTest();
        UrlChecker mockUrlChecker = mock(UrlChecker.class);
        toTest.urlChecker = mockUrlChecker;
        Mockito.when(mockUrlChecker.getUrlStatus(eq("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail_s.gif"))).thenReturn(new LinkStatus().setStatusValue("200").setStatusInfo("OK").setFailing(false));
        Mockito.when(mockUrlChecker.getUrlStatus(eq("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail.gif"))).thenReturn(new LinkStatus().setStatusValue("200").setStatusInfo("OK").setFailing(false));
        Mockito.when(mockUrlChecker.getUrlStatus(eq("ftp://mon-site.mondomaine/mon-repertoire"))).thenReturn(new LinkStatus().setStatusValue("406").setStatusInfo("Le serveur HTCPCP ne peut pas infuser du café pour différentes raisons, la réponse devrait indiquer une liste de types de café possibles.").setFailing(true));
        Mockito.when(mockUrlChecker.getUrlStatus(eq("HTTPS://acme.de/"))).thenReturn(new LinkStatus().setStatusValue("418").setStatusInfo("I am teapot").setFailing(true));
        Mockito.when(mockUrlChecker.getUrlStatus(contains("aquastat"))).thenReturn(new LinkStatus().setStatusValue("418").setStatusInfo("I am teapot").setFailing(true));
        Mockito.when(mockUrlChecker.getUrlStatus(contains("data.fao"))).thenReturn(new LinkStatus().setStatusValue("418").setStatusInfo("I am teapot").setFailing(true));
        toTest.processMetadata(mdAsXml, md);
        SimpleJpaRepository statusRepository = new SimpleJpaRepository<LinkStatus, Integer>(LinkStatus.class, entityManager);
        assertEquals(0, statusRepository.findAll().size());

        linkRepository.findAll().stream().forEach(toTest::testLink);
        entityManager.flush();
        entityManager.clear();

        List<LinkStatus> allStatus = statusRepository.findAll();
        assertEquals(6, allStatus.size());
        assertEquals(Stream.of("200", "406", "418").collect(Collectors.toSet()),
                allStatus.stream().map(LinkStatus::getStatusValue).collect(Collectors.toSet()));

        toTest.urlChecker = mockUrlChecker;
        Mockito.when(mockUrlChecker.getUrlStatus(eq("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail_s.gif"))).thenReturn(new LinkStatus().setStatusValue("200").setStatusInfo("OK").setFailing(false));
        Mockito.when(mockUrlChecker.getUrlStatus(eq("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail.gif"))).thenReturn(new LinkStatus().setStatusValue("200").setStatusInfo("OK").setFailing(false));
        Mockito.when(mockUrlChecker.getUrlStatus(eq("ftp://mon-site.mondomaine/mon-repertoire"))).thenReturn(new LinkStatus().setStatusValue("200").setStatusInfo("OK").setFailing(true));
        Mockito.when(mockUrlChecker.getUrlStatus(eq("HTTPS://acme.de/"))).thenReturn(new LinkStatus().setStatusValue("200").setStatusInfo("OK").setFailing(true));
        Mockito.when(mockUrlChecker.getUrlStatus(contains("aquastat"))).thenReturn(new LinkStatus().setStatusValue("418").setStatusInfo("I am teapot").setFailing(true));
        Mockito.when(mockUrlChecker.getUrlStatus(contains("data.fao"))).thenReturn(new LinkStatus().setStatusValue("418").setStatusInfo("I am teapot").setFailing(true));
        linkRepository.findAll().stream().forEach(toTest::testLink);
        entityManager.flush();
        entityManager.clear();

        allStatus = statusRepository.findAll();
        assertEquals(12, allStatus.size());
        Map<String, Long> grouped = allStatus.stream().map(LinkStatus::getStatusValue).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(6L, grouped.get("200").longValue());
        assertEquals(1L,grouped.get("406").longValue());
        assertEquals(5L, grouped.get("418").longValue());
    }

    private UrlAnalyzer createToTest() {
        UrlAnalyzer toTest = new UrlAnalyzer();
        toTest.schemaManager = schemaManager;
        toTest.metadataRepository = metadataRepository;
        toTest.linkRepository = linkRepository;
        toTest.linkStatusRepository = linkStatusRepository;
        toTest.metadataLinkRepository = metadataLinkRepository;
        toTest.entityManager = entityManager;
        toTest.urlChecker = urlChecker;
        toTest.settingManager = settingManager;
        return toTest;
    }

    private AbstractMetadata insertMetadataInDb(Element element) throws Exception {
        loginAsAdmin(context);

        Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(element)
                .setUuid(UUID.randomUUID().toString());
        metadata.getDataInfo()
                .setRoot(element.getQualifiedName())
                .setSchemaId(schemaManager.autodetectSchema(element))
                .setType(MetadataType.METADATA)
                .setPopularity(1000);
        metadata.getSourceInfo()
                .setOwner(TEST_OWNER)
                .setSourceId(sourceRepository.findAll().get(0).getUuid());
        metadata.getHarvestInfo()
                .setHarvested(false);

        AbstractMetadata dbInsertedMetadata = dataManager.insertMetadata(
                context,
                metadata,
                element,
                IndexingMode.full,
                false,
                NO,
                false,
            DirectIndexSubmitter.INSTANCE);

        return dbInsertedMetadata;
    }

    private Element getMdAsXml() throws IOException, JDOMException {
        URL mdResourceUrl = UrlAnalyzerTest.class.getResource("input_with_url.xml");
        return Xml.loadStream(mdResourceUrl.openStream());
    }
}
