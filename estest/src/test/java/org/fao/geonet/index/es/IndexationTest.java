package org.fao.geonet.index.es;

import org.codehaus.jackson.map.ObjectMapper;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexationTest {

    @Ignore
    @Test
    public void nominal() throws Exception {

        Path schemaDir = Paths.get(this.getClass().getResource("/nominal").toURI());
        Element metadata = new Element("root");
        String id = "test-id";
        Map<String, Object> moreFields = new HashMap<>();
        MetadataType metadataType = MetadataType.METADATA;
        String root = "dummy";
        boolean forceRefreshReaders = true;

        EsSearchManager toTest = new EsSearchManager();

//        toTest.index(schemaDir, metadata, id, moreFields, metadataType, root, forceRefreshReaders);
//
//        String docToBeIndexed = toTest.listOfDocumentsToIndex.get(id);
//        Map<String,Object> result = new ObjectMapper().readValue(docToBeIndexed, HashMap.class);
//        Assert.assertEquals(3, result.size());
//        Assert.assertEquals("test-id", result.get("id"));
//        Assert.assertEquals("metadata", result.get("docType"));
//        Assert.assertEquals("source-from-index-xsl", result.get("sourceCatalogue"));
    }
}
