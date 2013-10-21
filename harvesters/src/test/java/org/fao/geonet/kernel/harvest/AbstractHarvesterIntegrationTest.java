package org.fao.geonet.kernel.harvest;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.junit.Test;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * test base class for testing harvesters.
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:02 PM
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:harvesters-repository-test-context.xml")
public abstract class AbstractHarvesterIntegrationTest extends AbstractCoreIntegrationTest {
    @Autowired
    MockRequestFactoryGeonet _requestFactory;

    @Before
    public void clearRequestFactory() {
        _requestFactory.clear();
    }

    @Test
    public void testHarvest() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        mockHttpRequests(_requestFactory);

        Element params = createHarvesterParams("csw");
        customizeParams(params);
        AbstractHarvester _harvester = getHarvesterUnderTest();
        _harvester.setContext(context);
        _harvester.init(params);

        Logger log = Log.createLogger(Geonet.CSW_HARVEST);
        _harvester.doHarvest(log);
    }
    protected abstract void mockHttpRequests(MockRequestFactoryGeonet bean);
    protected abstract void customizeParams(Element params);

    protected abstract AbstractHarvester getHarvesterUnderTest();

    /**
     * Create a harvester configuration that contains the common elements of a configuration.  After calling this method the
     * harvester specific elements need to be added.
     *
     * @param type the type of the harvester (for the type attributes and elements).
     *
     * @return a basic configuration.
     */
    protected Element createHarvesterParams(String type) {
        return new Element("node")
                .setAttribute("id", "1")
                .setAttribute("type", type)
                .setAttribute("owner", "1")
                .addContent(new Element("site")
                        .addContent(new Element("name").setText("testHarvester"))
                        .addContent(new Element("uuid").setText("testHarvesetUuid"))
                        .addContent(new Element("icon").setText("icon.png")))
                .addContent(new Element("content")
                        .addContent(new Element("validate").setText("true"))
                        .addContent(new Element("importxsl").setText("none")))
                .addContent(new Element("options")
                        .addContent(new Element("every").setText("0 0 5 ? * MON"))
                        .addContent(new Element("oneRunOnly").setText("false"))
                        .addContent(new Element("status").setText("inactive")))
                .addContent(new Element("privileges")
                        .addContent(new Element("group")
                                .setAttribute("id", "" + ReservedGroup.all.getId())
                                .addContent(new Element("operation").setAttribute("name", ReservedOperation.view.name()))));
    }
}
