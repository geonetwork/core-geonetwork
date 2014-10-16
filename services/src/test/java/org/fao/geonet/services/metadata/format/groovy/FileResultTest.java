package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Maps;
import org.fao.geonet.services.metadata.format.FormatIntegrationTest;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FileResultTest {

    @Test
    public void testResolve() throws Exception {
        final String file = FormatIntegrationTest.class.getResource("groovy-test-formatter/block.html").getFile();
        Map<String, Object> substitutions = Maps.newHashMap();
        substitutions.put("label", "My Label");
        substitutions.put("childData", "Children");
        final FileResult fileResult = new FileResult(new File(file), substitutions);
        final String data = fileResult.resolve().replaceAll("\\s+", " ");
        assertEquals("<p class=\"block\"> <h1>My Label</h1> Children </p>", data);
    }
}