package org.fao.geonet.kernel.harvest.harvester.csw;

import com.google.common.base.Predicate;
import jeeves.server.context.ServiceContext;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.harvest.AbstractHarvesterIntegrationTest;
import org.fao.geonet.kernel.harvest.Common;
import org.fao.geonet.kernel.harvest.MockRequestFactoryGeonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.utils.MockXmlRequest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Integration Test for the Csw Harvester class.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:01 PM
 */
public class CswHarvesterIntegrationNoOwnerTest extends CswHarvesterIntegrationTest {

    @Autowired
    private CswHarvester _harvester;

    protected void customizeParams(Element params) {
        addCswSpecificParams(params);
        params.getChild("site").getChild("ownerId").detach();
    }

    @Override
    protected void assertExpectedResult(Element result) {
        assertEquals(0, result.getChildren().size());
    }

    @Override
    protected void mockHttpRequests(MockRequestFactoryGeonet bean) {
        // no requests will be made
    }

    @Override
    protected void assertExpectedErrors(List errors) {
        assertFalse(errors.isEmpty());
    }
}
