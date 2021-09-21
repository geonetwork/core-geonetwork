package org.fao.geonet.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoteHarvesterConfigurationTest {
    @Test
    public void testGsonExport()  {
        RemoteHarvesterConfiguration harvesterConfiguration = new RemoteHarvesterConfiguration();
        harvesterConfiguration.setUrl("http://harvester.com");
        harvesterConfiguration.setNumberOfRecordsPerRequest(20);
        harvesterConfiguration.setLongTermTag("MT");
        harvesterConfiguration.setLookForNestedDiscoveryService(false);
        harvesterConfiguration.setErrorConfigDuplicatedUuids(false);
        harvesterConfiguration.setDoNotSort(true);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String body = gson.toJson(harvesterConfiguration);

        RemoteHarvesterConfiguration harvesterConfiguration2 = gson.fromJson(body, RemoteHarvesterConfiguration.class);
        assertEquals(harvesterConfiguration, harvesterConfiguration2);
    }
}
