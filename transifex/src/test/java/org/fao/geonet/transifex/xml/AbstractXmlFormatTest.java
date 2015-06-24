package org.fao.geonet.transifex.xml;

import com.google.common.collect.Maps;
import net.sf.json.JSONObject;
import org.fao.geonet.transifex.TransifexReadyFile;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jesse on 6/19/2015.
 */
public abstract class AbstractXmlFormatTest {

    protected void assertTranslations(Map<String, TransifexReadyFile> filesMap, String fileId, String... properties) {
        TransifexReadyFile labels = filesMap.get(fileId);
        assertNotNull(labels);
        JSONObject labelsJSON = JSONObject.fromObject(labels.data);
        assertEquals(properties.length / 2, labelsJSON.size());
        for (int i = 0; i < properties.length; i+=2) {
            assertEquals(properties[i + 1], labelsJSON.getString(properties[i]));
        }
    }


    protected Map<String, TransifexReadyFile> toMap(List<TransifexReadyFile> files) {
        Map<String, TransifexReadyFile> map = Maps.newHashMap();
        for (TransifexReadyFile file : files) {
            map.put(file.resourceId, file);
        }
        return map;
    }
}
