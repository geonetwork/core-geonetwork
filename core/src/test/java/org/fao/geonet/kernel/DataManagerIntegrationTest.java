package org.fao.geonet.kernel;

import static org.junit.Assert.*;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for the DataManager.
 * <p/>
 * User: Jesse
 * Date: 10/24/13
 * Time: 5:30 PM
 */
public class DataManagerIntegrationTest extends AbstractCoreIntegrationTest {
    @Autowired
    DataManager _dataManager;
    @Autowired
    MetadataRepository _metadataRepository;

    @Test
    public void testDeleteMetadata() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final UserSession userSession = serviceContext.getUserSession();
        final String mdId = _dataManager.insertMetadata(serviceContext, "iso19193", new Element("MD_Metadata"), "uuid",
                userSession.getUserIdAsInt(),
                "" + ReservedGroup.all.getId(), "sourceid", "n", "doctype", "Title", null, new ISODate().getDateAndTime(),
                new ISODate().getDateAndTime(), false, false);

        assertEquals(1, _metadataRepository.count());

        _dataManager.deleteMetadata(serviceContext, mdId);

        assertEquals(0, _metadataRepository.count());

    }
}
