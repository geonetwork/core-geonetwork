package org.fao.geonet.util;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@Ignore(value = "As it depends on third party online app. " +
    "Various number of calls can be required " +
    "to finalize a complete registration.")
public class SpatineoUtilTest extends AbstractCoreIntegrationTest {
    @Test
    public void testRegisterService() {
        String url = "https://www.aquacoope.org/geoserver/ows";
        int calls = 1;
        Map<String, String> response = null;
        try {
            response = SpatineoUtil.registerService(url, calls);
        } catch (TimeoutException e) {
            assertEquals(
                String.format("Registration not finalized for %s after %s calls. Increase number of calls to the API.", url, calls),
                e.getMessage());
        }

        try {
            response = SpatineoUtil.registerService(url, 20);
            assertEquals(
                2,
                response.size());
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

}
