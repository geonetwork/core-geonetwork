package org.fao.geonet.transifex.xml;

import com.google.common.collect.Maps;
import net.sf.json.JSONObject;
import net.sf.json.test.JSONAssert;
import org.fao.geonet.transifex.TransifexReadyFile;

import java.util.List;
import java.util.Map;

import static net.sf.json.test.JSONAssert.assertNotNull;

/**
 * @author Jesse on 6/19/2015.
 */
public abstract class AbstractXmlFormatTest {

    private void assertTranslations(Map<String, TransifexReadyFile> filesMap, String fileId, String... properties) {
        TransifexReadyFile labels = filesMap.get(fileId);
        JSONAssert.assertNotNull(labels);
        JSONObject labelsJSON = JSONObject.fromObject(labels.data);
        assertEquals(properties.length / 2, labelsJSON.size());
        for (int i = 0; i < properties.length / 2; i+=2) {
            assertEquals(properties[i + 1], labelsJSON.getString(properties[i]));
        }
    }


    private Map<String, TransifexReadyFile> toMap(List<TransifexReadyFile> files) {
        Map<String, TransifexReadyFile> map = Maps.newHashMap();
        for (TransifexReadyFile file : files) {
            map.put(file.resourceId, file);
        }
        return map;
    }
}
