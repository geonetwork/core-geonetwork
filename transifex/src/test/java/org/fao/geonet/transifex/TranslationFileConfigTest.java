package org.fao.geonet.transifex;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Jesse on 6/25/2015.
 */
public class TranslationFileConfigTest {
    @Test
    public void testFormatSupport() throws Exception {
        assertNotNull(TranslationFileConfig.getFormatInstance("JsonFormat"));
        assertNotNull(TranslationFileConfig.getFormatInstance("JSONFormat"));
        assertNotNull(TranslationFileConfig.getFormatInstance("XmlFormat"));
        assertNotNull(TranslationFileConfig.getFormatInstance("SchemaPluginLabelsFormat"));
        assertNotNull(TranslationFileConfig.getFormatInstance("SchemaPluginCodelistFormat"));
        assertNotNull(TranslationFileConfig.getFormatInstance("SimpleElementFormat"));
        assertNotNull(TranslationFileConfig.getFormatInstance("LeafElementFormat"));
    }
}