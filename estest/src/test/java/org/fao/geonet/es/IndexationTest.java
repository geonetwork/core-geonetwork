package org.fao.geonet.es;

import org.codehaus.jackson.map.ObjectMapper;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class IndexationTest {

    @Test
    public void nominal() throws Exception {

        Path schemaDir = Paths.get(this.getClass().getResource("/nominal").toURI());
        Element metadata = new Element("root");
        String id = "test-id";
        List< Element > moreFields = Collections.emptyList();
        MetadataType metadataType = MetadataType.METADATA;
        String root = "dummy";
        boolean forceRefreshReaders = true;

        EsSearchManager toTest = new EsSearchManager();
        toTest.settingManager = Mockito.mock(SettingManager.class);
        Mockito.when(toTest.settingManager.getSiteName()).thenReturn("test-scope");
        Mockito.when(toTest.settingManager.getSiteId()).thenReturn("test-harvest-uuid");
        Mockito.when(toTest.settingManager.getNodeURL()).thenReturn("test_harvest-id");

        toTest.index(schemaDir, metadata, id, moreFields, metadataType, root, forceRefreshReaders);

        String docToBeIndexed = toTest.listOfDocumentsToIndex.get(id);
        HashMap<String,Object> result = new ObjectMapper().readValue(docToBeIndexed, HashMap.class);
        Assert.assertEquals(6, result.size());
        Assert.assertEquals("test-id", result.get("id"));
        Assert.assertEquals("metadata", result.get("docType"));
        Assert.assertEquals("source-from-index-xsl", result.get("sourceCatalogue"));
        Assert.assertEquals("test-scope", result.get("scope"));
        Assert.assertEquals("test-harvest-uuid", result.get("harvesterUuid"));
        Assert.assertEquals("test_harvest-id", result.get("harvesterId"));
    }
}
