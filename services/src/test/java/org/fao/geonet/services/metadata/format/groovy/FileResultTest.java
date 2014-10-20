package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.fao.geonet.Constants;
import org.fao.geonet.services.metadata.format.FormatIntegrationTest;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FileResultTest {

    @Test
    public void testResolve() throws Exception {
        final String path = FormatIntegrationTest.class.getResource("groovy-test-formatter/block.html").getFile();
        Map<String, Object> substitutions = Maps.newHashMap();
        substitutions.put("label", "My Label");
        substitutions.put("childData", "Children");
        final File file1 = new File(path);
        final String template = Files.toString(file1, Constants.CHARSET);
        final FileResult fileResult = new FileResult(file1, template, substitutions);
        final String data = fileResult.toString().replaceAll("\\s+", " ");
        assertEquals("<p class=\"block\"> <h1>My Label</h1> Children </p>", data);
    }
}