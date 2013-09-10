package org.fao.geonet.repository;

import org.fao.geonet.domain.IsoLanguage;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test LocalizedEntityRepository
 * User: Jesse
 * Date: 9/9/13
 * Time: 3:16 PM
 */
public class LocalizedEntityRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    private IsoLanguageRepository _repository;

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testFindAllAsXml() throws Exception {
        IsoLanguage language = IsoLanguageRepositoryTest.createIsoLanguage(_inc);
        language.getLabelTranslations().put("eng", "eng1");
        language.getLabelTranslations().put("fra", "fra1");
        language = _repository.save(language);

        IsoLanguage language2 = IsoLanguageRepositoryTest.createIsoLanguage(_inc);
        language2.getLabelTranslations().put("eng", "eng2");
        language2.getLabelTranslations().put("fra", "fra2");
        language2 = _repository.save(language2);

        Element xml = _repository.findAllAsXml();

        assertEquals(IsoLanguage.class.getSimpleName().toLowerCase(), xml.getName());
        assertEquals(2, xml.getChildren().size());


        for (Element element : (List<Element>) xml.getChildren()) {
            assertEquals(LocalizedEntityRepositoryImpl.RECORD_EL_NAME, element.getName());
            assertNotNull(element.getChild(LocalizedEntityRepositoryImpl.LABEL_EL_NAME));

            IsoLanguage entity = language;
            if (element.getChildText("id").equalsIgnoreCase("" + language2.getId())) {
                entity = language2;
            }

            assertEquals(entity.getId(), Integer.valueOf(element.getChildText("id")).intValue());
            assertEquals(entity.getCode(), element.getChildText("code"));
            assertEquals(entity.getShortCode(), element.getChildText("shortcode"));
            assertEquals(entity.getLabel("eng"), element.getChild(LocalizedEntityRepositoryImpl.LABEL_EL_NAME).getChildText("eng"));
            assertEquals(entity.getLabel("fra"), element.getChild(LocalizedEntityRepositoryImpl.LABEL_EL_NAME).getChildText("fra"));
        }
    }
}
