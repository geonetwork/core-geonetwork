package org.fao.geonet.services.harvesting;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.ObjectNotFoundEx;
import org.fao.geonet.kernel.harvest.AbstractHarvesterServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test Harvester Get Service.
 *
 * User: Jesse
 * Date: 10/22/13
 * Time: 4:04 PM
 */
public class GetIntegrationTest extends AbstractHarvesterServiceIntegrationTest {
    @Test(expected = ObjectNotFoundEx.class)
    public void testExecNoSuchHarvester() throws Exception {
        final Get get = new Get();

        Element params = createParams(Pair.read("id", "912"));
        ServiceContext context = createServiceContext();
        get.exec(params, context);
    }

    @Test
    public void testExecFindAllHarvesterWhenThereAreNone() throws Exception {
        final Get get = new Get();

        Element params = createParams();
        ServiceContext context = createServiceContext();
        final Element result = get.exec(params, context);

        Assert.assertEquals(0, result.getChildren().size());
    }

    @Test
    public void testExecFindAllHarvesterWhenThereAreTwo() throws Exception {
        final Get get = new Get();
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String added = _harvestManager.addHarvesterReturnId(createHarvesterParams("csw"), context.getUserSession().getUserId());
        final String added2 = _harvestManager.addHarvesterReturnId(createHarvesterParams("geonetwork20"), context.getUserSession()
                .getUserId());
        Element params = createParams();
        final Element result = get.exec(params, context);

        Assert.assertEquals(2, result.getChildren().size());

        List<String> ids = Arrays.asList(added, added2);
        List<String> types = Arrays.asList("csw", "geonetwork20");

        Assert.assertTrue(ids.contains(((Element)result.getChildren().get(0)).getAttributeValue("id")));
        Assert.assertTrue(ids.contains(((Element)result.getChildren().get(1)).getAttributeValue("id")));

        Assert.assertTrue(types.contains(((Element)result.getChildren().get(0)).getAttributeValue("type")));
        Assert.assertTrue(types.contains(((Element)result.getChildren().get(1)).getAttributeValue("type")));
    }

    @Test
    public void testExecFindOne() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String added = _harvestManager.addHarvesterReturnId(createHarvesterParams("csw"), context.getUserSession().getUserId());

        final Get get = new Get();

        Element params = createParams(Pair.read("id", added));
        final Element result = get.exec(params, context);

        Assert.assertEquals(1, result.getChildren().size());
        Element node = result.getChild("node");
        Assert.assertEquals(Xml.getString(result)+"\n", added, node.getAttributeValue("id"));
        Assert.assertEquals(Xml.getString(result)+"\n", "csw", node.getAttributeValue("type"));
        Assert.assertFalse(Xml.getString(result) + "\n", node.getChildren().isEmpty());
    }


    @Test
    public void testExecOnlyInfoWithTwoHarvesters() throws Exception {
        final Get get = new Get();
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String added = _harvestManager.addHarvesterReturnId(createHarvesterParams("csw"), context.getUserSession().getUserId());
        final String added2 = _harvestManager.addHarvesterReturnId(createHarvesterParams("geonetwork20"), context.getUserSession()
                .getUserId());
        Element params = createParams(Pair.read("onlyInfo", "true"));
        Element result = get.exec(params, context);

        Assert.assertEquals(2, result.getChildren().size());

        List<String> ids = Arrays.asList(added, added2);

        Assert.assertTrue(ids.contains(((Element)result.getChildren().get(0)).getAttributeValue("id")));
        Assert.assertTrue(ids.contains(((Element)result.getChildren().get(1)).getAttributeValue("id")));
        assertOnlyInfo(result);

        params.addContent(new Element("id").setText(ids.get(0)));
        result = get.exec(params, context);

        Assert.assertTrue(ids.contains(((Element)result.getChildren().get(0)).getAttributeValue("id")));
        assertOnlyInfo(result);

        params.addContent(new Element("id").setText(ids.get(1)));
        result = get.exec(params, context);

        Assert.assertTrue(ids.contains(((Element)result.getChildren().get(0)).getAttributeValue("id")));
        Assert.assertTrue(ids.contains(((Element)result.getChildren().get(1)).getAttributeValue("id")));
        assertOnlyInfo(result);
    }

    private void assertOnlyInfo(Element result) {
        for (Object o : result.getChildren()) {
            if (o instanceof Element) {
                Element el = (Element) o;
                assertNotNull(el.getChild("info"));
                assertNull(el.getChild("site"));
            }
        }
    }

}
