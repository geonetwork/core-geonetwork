package org.fao.geonet.kernel.search;

import org.fao.geonet.kernel.SingleThesaurusFinder;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.junit.Test;
import org.openrdf.sesame.config.ConfigurationException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class TermUriTranslatorTest {

    private final IsoLanguagesMapper isoLangMapper = new IsoLanguagesMapper() {
        {
            _isoLanguagesMap639.put("en", "eng");
            _isoLanguagesMap639.put("de", "ger");
        }
    };

    @Test
    public void testTermWithPreferredLabelForLanguage() throws IOException, ConfigurationException {
        ThesaurusFinder finder = createThesaurusFinderFor(isoLangMapper, "TermUriTranslatorTest.rdf");
        Translator translator = new TermUriTranslator(finder, "eng", "http://www.my.com/test");
        String label = translator.translate("http://www.my.com/test#ocean_temperature");
        assertEquals("ocean temperature", label);
    }

    @Test
    public void testTermWithNoPreferredLabelForLanguage() throws IOException, ConfigurationException {
        ThesaurusFinder finder = createThesaurusFinderFor(isoLangMapper, "TermUriTranslatorTest.rdf");
        Translator translator = new TermUriTranslator(finder, "ger", "http://www.my.com/test");
        String label = translator.translate("http://www.my.com/test#ocean_temperature");
        assertEquals("http://www.my.com/test#ocean_temperature", label);
    }

    @Test
    public void testMissingTerm() throws IOException, ConfigurationException {
        ThesaurusFinder finder = createThesaurusFinderFor(isoLangMapper, "TermUriTranslatorTest.rdf");
        Translator translator = new TermUriTranslator(finder, "ger", "http://www.my.com/test");
        String label = translator.translate("http://www.my.com/test#unknown_term");
        assertEquals("http://www.my.com/test#unknown_term", label);
    }

    private ThesaurusFinder createThesaurusFinderFor(IsoLanguagesMapper isoLangMapper, String fileName) throws IOException, ConfigurationException {
        //TODO: Load from in memory data directory?
        Path thesaurusFile = new ClassPathResource(fileName, this.getClass()).getFile().toPath();
        Thesaurus thesaurus = loadThesaurusFile(isoLangMapper, thesaurusFile);
        return new SingleThesaurusFinder(thesaurus);
    }

    private Thesaurus loadThesaurusFile(IsoLanguagesMapper isoLanguagesMapper, Path thesaurusFile)
            throws ConfigurationException, IOException {
        Thesaurus thesaurus = new Thesaurus(isoLanguagesMapper, thesaurusFile.getFileName().toString(), "external", "theme", thesaurusFile, "http://dummy.org/geonetwork");
        thesaurus.initRepository();
        return thesaurus;
    }
}
