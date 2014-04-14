package org.fao.geonet.domain;

import org.junit.Assert;
import org.junit.Test;

import static org.fao.geonet.domain.SchematronRequirement.*;

/**
 * Created by Jesse on 2/6/14.
 */
public class SchematronRequirementTest {
    @Test
    public void testHighestRequirement() throws Exception {
        Assert.assertEquals(REQUIRED, REQUIRED.highestRequirement(DISABLED));
        Assert.assertEquals(REQUIRED, REQUIRED.highestRequirement(REPORT_ONLY));
        Assert.assertEquals(REQUIRED, REQUIRED.highestRequirement(REQUIRED));

        Assert.assertEquals(REQUIRED, REPORT_ONLY.highestRequirement(REQUIRED));
        Assert.assertEquals(REPORT_ONLY, REPORT_ONLY.highestRequirement(DISABLED));
        Assert.assertEquals(REPORT_ONLY, REPORT_ONLY.highestRequirement(REPORT_ONLY));

        Assert.assertEquals(DISABLED, DISABLED.highestRequirement(DISABLED));
        Assert.assertEquals(REPORT_ONLY, DISABLED.highestRequirement(REPORT_ONLY));
        Assert.assertEquals(REQUIRED, DISABLED.highestRequirement(REQUIRED));
    }
}
