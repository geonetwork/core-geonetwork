package org.fao.geonet.kernel.search;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.io.ByteArrayInputStream;

public class LuceneSearcherGeomTest extends AbstractCoreIntegrationTest {
    @Test
    public void testGeomSearch() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final Element sampleMetadataXml = getSampleMetadataXml();
        byte[] bytes = Xml.getString(sampleMetadataXml).getBytes(Constants.ENCODING);
        importMetadataXML(serviceContext, "uuid:" + System.currentTimeMillis(), new ByteArrayInputStream(bytes),
                MetadataType.METADATA, ReservedGroup.intranet.getId(), Params.GENERATE_UUID);
    }
}