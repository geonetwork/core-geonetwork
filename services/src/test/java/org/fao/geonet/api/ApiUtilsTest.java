package org.fao.geonet.api;

import org.junit.Assert;
import org.junit.Test;

public class ApiUtilsTest {

    @Test
    public void testFixURIFragment() throws Exception {
        String result;

        //http://www.thesaurus.gc.ca/concept/#Offshore area        -->   http://www.thesaurus.gc.ca/concept/#Offshore%20area
        result = ApiUtils.fixURIFragment("http://www.thesaurus.gc.ca/concept/#Offshore area");
        Assert.assertEquals("http://www.thesaurus.gc.ca/concept/#Offshore%20area",result);

        //http://www.thesaurus.gc.ca/concept/#AIDS (disease)       -->   http://www.thesaurus.gc.ca/concept/#AIDS%20%28disease%29
        result = ApiUtils.fixURIFragment("http://www.thesaurus.gc.ca/concept/#AIDS (disease)");
        Assert.assertEquals("http://www.thesaurus.gc.ca/concept/#AIDS%20%28disease%29",result);

        //http://www.thesaurus.gc.ca/concept/#Alzheimer's disease  -->   http://www.thesaurus.gc.ca/concept/#Alzheimer%27s%20disease
        result = ApiUtils.fixURIFragment("http://www.thesaurus.gc.ca/concept/#Alzheimer's disease");
        Assert.assertEquals("http://www.thesaurus.gc.ca/concept/#Alzheimer%27s%20disease",result);

        result = ApiUtils.fixURIFragment("http://www.thesaurus.gc.ca/concept/#simple");
        Assert.assertEquals("http://www.thesaurus.gc.ca/concept/#simple",result);

        //special case for "+"
        result = ApiUtils.fixURIFragment("http://www.thesaurus.gc.ca/concept/#simple+space");
        Assert.assertEquals("http://www.thesaurus.gc.ca/concept/#simple%20space",result);
    }
}
