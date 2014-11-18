package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Maps;
import org.fao.geonet.Constants;
import org.fao.geonet.services.metadata.format.FormatIntegrationTest;
import org.fao.geonet.utils.IO;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FileResultTest {

    @Test
    public void testResolve() throws Exception {
        URI path = FormatIntegrationTest.class.getResource("groovy-test-formatter/block.html").toURI();
        Map<String, Object> substitutions = Maps.newHashMap();
        substitutions.put("label", "My Label");
        substitutions.put("childData", "Children");
        final Path file1 = IO.toPath(path);
        final String template = new String(Files.readAllBytes(file1), Constants.CHARSET);
        final FileResult fileResult = new FileResult(file1, template, substitutions);
        final String data = fileResult.toString().replaceAll("\\s+", " ");
        assertEquals("<p class=\"block\"> <h1>My Label</h1> Children </p>", data);
    }
}