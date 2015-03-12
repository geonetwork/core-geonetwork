package org.fao.geonet.services.metadata.format.cache;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest.ImportMetadata;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.metadata.Publish;
import org.fao.geonet.services.metadata.format.FormatType;
import org.fao.geonet.services.metadata.format.FormatterWidth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.data.jpa.domain.Specifications.where;

@ContextConfiguration(inheritLocations = true, locations = "classpath:formatter-cache-test-context.xml")
public class FormatterCacheIntegrationTest extends AbstractServiceIntegrationTest {

    public static final MockHttpServletRequest SERVLET_REQUEST = new MockHttpServletRequest("GET", "requesturi");
    @Autowired
    private Publish publish;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private SystemInfo systemInfo;
    @PersistenceContext
    EntityManager entityManager;
    private FilesystemStore fsStore;
    private FormatterCache formatterCache;

    private String metadataId;
    private String stagingProfile;

    @Before
    public void setUp() throws Exception {
        ServiceContext context = createServiceContext();
        ImportMetadata importer = new ImportMetadata(this, context);
        importer.invoke();

        this.metadataId = importer.getMetadataIds().get(0);

        this.fsStore = _applicationContext.getBean(FilesystemStore.class);
        this.formatterCache = _applicationContext.getBean(FormatterCache.class);
        this.stagingProfile = systemInfo.getStagingProfile();
        systemInfo.setStagingProfile(SystemInfo.STAGE_PRODUCTION);
    }

    @After
    public void resetStagingProfile() throws Exception {
        systemInfo.setStagingProfile(stagingProfile);
    }

    @Test
    public void testUpdatesAfterMetadataUnpublished() throws Exception {
        final Specification<OperationAllowed> isPublished = OperationAllowedSpecs.isPublic(ReservedOperation.view);
        final Specification<OperationAllowed> hasMdId = OperationAllowedSpecs.hasMetadataId(metadataId);
        final OperationAllowed one = operationAllowedRepository.findOne(where(hasMdId).and(isPublished));
        final long changeDate = new Date().getTime();
        final Key key = new Key(Integer.parseInt(metadataId), "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, one != null), true);

        if (one != null) {
            publish.unpublish("eng", SERVLET_REQUEST, metadataId, false);
            assertPublished(key, false);
        }

        publish.publish("eng", new MockHttpServletRequest("GET", "requesturi"), metadataId, false);
        assertPublished(key, true);

        publish.unpublish("eng", new MockHttpServletRequest("GET", "requesturi"), metadataId, false);
        assertPublished(key, false);

        publish.publish("eng", new MockHttpServletRequest("GET", "requesturi"), metadataId, false);
        assertPublished(key, true);
    }

    private void assertPublished(Key key, boolean published) throws IOException, SQLException {
        assertEquals(published, formatterCache.getPublished(key) != null);
        assertEquals(published, fsStore.getPublished(key) != null);
    }

    @Test
    public void testUpdatesAfterMetadataDeleted() throws Exception {
        final long changeDate = new Date().getTime();
        final Key key = new Key(Integer.parseInt(metadataId), "eng", FormatType.html, "full_view", true, FormatterWidth._100);
        formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("result", changeDate, true), true);

        dataManager.deleteMetadata(createServiceContext(), metadataId);
        entityManager.flush();

        assertNull(formatterCache.getPublished(key));
        assertNull(fsStore.getPublished(key));
        assertNull(fsStore.get(key));
        final byte[] result = formatterCache.get(key, new ChangeDateValidator(changeDate), new TestLoader("newValue", changeDate,
                true), true);
        assertEquals("newValue", new String(result, Constants.CHARSET));
    }
}