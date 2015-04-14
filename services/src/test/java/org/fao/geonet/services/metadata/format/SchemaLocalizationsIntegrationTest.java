package org.fao.geonet.services.metadata.format;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.format.groovy.CurrentLanguageHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertTrue;

public class SchemaLocalizationsIntegrationTest extends AbstractCoreIntegrationTest {


    @Autowired
    private ApplicationContext context;

    @Test
    public void testNodeTranslation() throws Exception {
        CurrentLanguageHolder currentLanguageHolder = new TestLanguageHolder("fre");
        final SchemaLocalizations freIso19139 = new SchemaLocalizations(context, currentLanguageHolder, "iso19139", null);

        final String keywordLabel = freIso19139.nodeTranslation("gmd:descriptiveKeywords", "srv:SV_ServiceIdentification", "label");
        assertTrue("'" + keywordLabel + "' should contain 'mots'", keywordLabel.toLowerCase().contains("mots"));
        assertTrue("'" + keywordLabel + "' should contain 'clés'", keywordLabel.toLowerCase().contains("clés"));

        String identifierLabel = freIso19139.nodeTranslation("gmd:identifier", "gmd:CI_Citation", "label");
        assertTrue("'" + identifierLabel + "' should contain 'identificateur'", identifierLabel.toLowerCase().contains("identificateur"));
    }

    private final class TestLanguageHolder implements CurrentLanguageHolder {
        private final String currentLang;

        public TestLanguageHolder(String currentLang) {
            this.currentLang = currentLang;
        }

        @Override
        public String getLang3() {
            return currentLang;
        }

        @Override
        public String getLang2() {
            return context.getBean(IsoLanguagesMapper.class).iso639_2_to_iso639_1(currentLang);
        }
    }
}