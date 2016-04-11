package org.fao.geonet.api.sld;

import org.fao.geonet.domain.TextFile;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.TextFileRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test File service
 */
public class TextFileTest extends AbstractServiceIntegrationTest {

    @Autowired
    private TextFileRepository textFileRepository;

    @Test
    public void testStoreLoadFile() throws IOException {

        TextFile sld = new TextFile();
        sld.setContent(this.getRessourceAsString("test-sld-merged.xml"));
        sld.setMimeType("application/xml");
        textFileRepository.save(sld);
        int id = sld.getId();
        assertTrue(id > 0);

        TextFile file = textFileRepository.findOne(id);
        file.getContent();

        assertEquals(this.getRessourceAsString("test-sld-merged.xml"),file.getContent());

    }

    private String getRessourceAsString(String name) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(classloader.getResourceAsStream(name)));
        String line = "";
        StringBuilder res = new StringBuilder();
        while ((line = buffer.readLine()) != null)
            res.append(line);

        return res.toString();
    }

}
