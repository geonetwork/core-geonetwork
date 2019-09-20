package org.fao.geonet.kernel;

import static org.fao.geonet.domain.MetadataType.SUB_TEMPLATE;
import static org.fao.geonet.domain.MetadataType.TEMPLATE;
import static org.fao.geonet.kernel.UpdateDatestamp.NO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.IndexingTask;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

public class LocalXLinksUpdateDeleteTest extends AbstractIntegrationTestWithMockedSingletons {

    private static final int TEST_OWNER = 42;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SearchManager searchManager;

    @Autowired
    private SettingManager settingManager;

    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        settingManager.setValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE, true);
    }

    class MyQuartzJob extends IndexingTask {

        public MyQuartzJob() {
            this._dataManager = dataManager;
            this.serviceManager = context.getBean(ServiceManager.class);
            this.applicationContext = context.getApplicationContext();
        }

        public void execute() throws JobExecutionException {
            super.executeInternal(null);
        }
    }

    @Test
    @Ignore(value = "Doesn't run along the others in core")
    public void updateHasToTriggerIndexation() throws Exception {
        URL contactResource = AbstractCoreIntegrationTest.class.getResource("kernel/babarContact.xml");
        Element contactElement = Xml.loadStream(contactResource.openStream());
        AbstractMetadata contactMetadata = insertContact(contactElement);
        AbstractMetadata vicinityMapMetadata = insertVicinityMap(contactMetadata);

        Document document = searchForMetadataTagged("babar");
        assertEquals(vicinityMapMetadata.getUuid(), document.getField("_uuid").stringValue());

        Xml.selectElement(contactElement, "gmd:individualName/gco:CharacterString", Arrays.asList(GMD, GCO)).setText("momo");
        metadataManager.updateMetadata(context,
                Integer.toString(contactMetadata.getId()),
                contactElement,
                false,
                false,
                true,
                null,
                null,
                false);

        new MyQuartzJob().execute();
        searchManager.forceIndexChanges();

        document = searchForMetadataTagged("momo");
        assertEquals(vicinityMapMetadata.getUuid(), document.getField("_uuid").stringValue());
    }

    @Test
    public void deleteAllowedWhenRefNotExists() throws Exception {
        settingManager.setValue(Settings.SYSTEM_XLINK_ALLOW_REFERENCED_DELETION, false);
        AbstractMetadata contactMetadata = insertContact();
        AbstractMetadata vicinityMapMetadata = insertVicinityMap(contactMetadata);

        metadataManager.deleteMetadata(context, Integer.toString(vicinityMapMetadata.getId()));
        metadataManager.deleteMetadata(context, Integer.toString(contactMetadata.getId()));
        assertNull(metadataManager.getMetadata(Integer.toString(contactMetadata.getId())));
    }

    @Test
    public void deleteHasToBeForbiddenWhenRefExistsAndSettingsSaySo() throws Exception {
        settingManager.setValue(Settings.SYSTEM_XLINK_ALLOW_REFERENCED_DELETION, false);
        AbstractMetadata contactMetadata = insertContact();
        insertVicinityMap(contactMetadata);

        try {
            metadataManager.deleteMetadata(context,
                    Integer.toString(contactMetadata.getId()));
        } catch (Exception e) {

        }
        assertNotNull(metadataManager.getMetadata(Integer.toString(contactMetadata.getId())));
    }

    @Test
    public void deleteHasToBeAllowedWhenRefExistsAndSettingsSaySo() throws Exception {
        settingManager.setValue(Settings.SYSTEM_XLINK_ALLOW_REFERENCED_DELETION, true);
        AbstractMetadata contactMetadata = insertContact();
        insertVicinityMap(contactMetadata);

        metadataManager.deleteMetadata(context, Integer.toString(contactMetadata.getId()));
        assertNull(metadataManager.getMetadata(Integer.toString(contactMetadata.getId())));
    }

    private AbstractMetadata insertTemplateResourceInDb(Element element, MetadataType type) throws Exception {
        loginAsAdmin(context);

        Metadata metadata = new Metadata();
        metadata
                .setDataAndFixCR(element)
                .setUuid(UUID.randomUUID().toString());
        metadata.getDataInfo()
                .setRoot(element.getQualifiedName())
                .setSchemaId(schemaManager.autodetectSchema(element))
                .setType(type)
                .setPopularity(1000);
        metadata.getSourceInfo()
                .setOwner(TEST_OWNER)
                .setSourceId(sourceRepository.findAll().get(0).getUuid());
        metadata.getHarvestInfo()
                .setHarvested(false);

        AbstractMetadata dbInsertedMetadata = metadataManager.insertMetadata(
                context,
                metadata,
                element,
                false,
                true,
                false,
                NO,
                false,
                false);

        return dbInsertedMetadata;
    }

    private AbstractMetadata insertVicinityMap(AbstractMetadata contactMetadata) throws Exception {
        URL vicinityMapResource = AbstractCoreIntegrationTest.class.getResource("kernel/vicinityMap.xml");
        Element vicinityMapElement = Xml.loadStream(vicinityMapResource.openStream());
        Attribute href = (Attribute) Xml.selectElement(vicinityMapElement, "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact").getAttributes().get(0);
        href.setValue(href.getValue().replace("@contact_uuid@", contactMetadata.getUuid()));
        return insertTemplateResourceInDb(vicinityMapElement, TEMPLATE);
    }

    private AbstractMetadata insertContact() throws Exception {
        URL contactResource = AbstractCoreIntegrationTest.class.getResource("kernel/babarContact.xml");
        Element contactElement = Xml.loadStream(contactResource.openStream());
        return insertContact(contactElement);
    }

    private AbstractMetadata insertContact( Element contactElement) throws Exception {
        AbstractMetadata contactMetadata = insertTemplateResourceInDb(contactElement, SUB_TEMPLATE);

        SpringLocalServiceInvoker mockInvoker = resetAndGetMockInvoker();
        when(mockInvoker.invoke(any(String.class))).thenReturn(contactElement);
        return contactMetadata;
    }

    private Document searchForMetadataTagged(String contactName) throws IOException {
        IndexAndTaxonomy indexReader = searchManager.getIndexReader(null, -1);
        IndexSearcher searcher = new IndexSearcher(indexReader.indexReader);
        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(Geonet.IndexFieldNames.ANY, contactName)), BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term(Geonet.IndexFieldNames.IS_TEMPLATE, "s")), BooleanClause.Occur.MUST_NOT);
        TopDocs docs = searcher.search(query, 1);
        return indexReader.indexReader.document(docs.scoreDocs[0].doc);
    }
}
