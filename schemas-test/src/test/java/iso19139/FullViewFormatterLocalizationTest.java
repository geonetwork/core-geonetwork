package iso19139;

import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.format.FormatType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 10/17/2014.
 */
public class FullViewFormatterLocalizationTest extends AbstractFullViewFormatterTest {

    @Autowired
    private IsoLanguagesMapper mapper;

    @Test @DirtiesContext
    @SuppressWarnings("unchecked")
    public void testPrintFormatLocales() throws Exception {
        final FormatType formatType = FormatType.testpdf;

        Format format = new Format(formatType);
        assertCorrectTranslation(format, "eng");
        assertCorrectTranslation(format, "fre");
        assertCorrectTranslation(format, "ger");

    }

    public void assertCorrectTranslation(Format format, String lang) throws Exception {
        format.setRequestLanguage(lang);
        format.invoke();
        String view = format.getView();

        assertEquals(view, lang.equalsIgnoreCase("eng"), view.contains("Identification EN Title"));
        assertEquals(view, lang.equalsIgnoreCase("fre"), view.contains("Identification FR Title"));
        assertEquals(view, lang.equalsIgnoreCase("ger"), view.contains("Identification DE Title"));

    }

}
