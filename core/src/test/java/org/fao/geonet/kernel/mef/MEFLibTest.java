package org.fao.geonet.kernel.mef;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreTest;
import org.jdom.Element;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * Test MEF.
 *
 * User: Jesse
 * Date: 10/15/13
 * Time: 8:53 PM
 */
public class MEFLibTest extends AbstractCoreTest {
    @Test
    public void testDoImport() throws Exception {
        ServiceContext context = createServiceContext();

        final File resource = new File(MEFLibTest.class.getResource("dublin-core.mef").getFile());

        Element params = new Element("request");
        MEFLib.doImport(params, context, resource, getStyleSheets());
    }

    @Test
    public void testDoExport() throws Exception {

    }

    @Test
    public void testDoMEF2Export() throws Exception {

    }
}
