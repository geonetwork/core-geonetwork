package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;

public class EditFileIntegrationTest extends AbstractServiceIntegrationTest {

    @Test(expected=BadParameterEx.class)
    public void testExecBadFnameParameter() throws Exception {
        EditFile ef = new EditFile();
        ServiceContext ctx = createServiceContext();

        Element params = Xml.loadString(
                "<request><id>test</id><_content_type>json</_content_type>"
                        + "<fname>/etc/passwd</fname></request>", false);

            ef.exec(params, ctx);
    }

}
