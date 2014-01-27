package org.fao.geonet.kernel.harvest;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Super class to use when implementing Harvester related Service integration tests.
 * User: Jesse
 * Date: 10/22/13
 * Time: 4:17 PM
 */
public abstract class AbstractHarvesterServiceIntegrationTest
        extends AbstractCoreIntegrationTest {

    @Autowired
    protected HarvestManagerImpl _harvestManager;

    @Before
    public void configureHarvesterBeans() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        _harvestManager.init(serviceContext, false);
    }


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
                        .addContent(new Element("ownerId").setText("1"))
                        .addContent(new Element("ownerGroupId").setText("2"))
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
