package org.fao.geonet.repository;


import org.fao.geonet.domain.Language;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class LanguageRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    LanguageRepository _repo;

    @Test
    public void testFindOne() {
        Language language = newLanguage();
        language = _repo.save(language);

        Language language1 = newLanguage();
        language1 = _repo.save(language1);


        assertEquals(language1, _repo.findOne(language1.getId()));
        assertEquals(language, _repo.findOne(language.getId()));
    }

    @Test
    public void testFindOneByDefaultLanguage() {
        Language language = newLanguage();
        language.setDefaultLanguage(true);
        language = _repo.save(language);

        Language language1 = newLanguage();
        language1.setDefaultLanguage(false);
        _repo.save(language1);

        assertEquals(language, _repo.findOneByDefaultLanguage());
    }

    @Test
    public void testFindByInspireFlag() {
        Language language1 = newLanguage();
        language1 = _repo.save(language1);

        Language language2 = newLanguage();
        language2 = _repo.save(language2);

        List<Language> histories = _repo.findAllByInspireFlag(language1.isInspire());
        assertEquals(language1.isInspire(), histories.get(0).isInspire());

        histories = _repo.findAllByInspireFlag(language2.isInspire());
        assertEquals(language2.isInspire(), histories.get(0).isInspire());
    }

    private Language newLanguage() {
        return newLanguage(_inc);
    }

    public static Language newLanguage(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        Language lang = new Language();
        lang.setId("l-" + val);
        lang.setDefaultLanguage(val % 2 == 0);
        lang.setInspire(val % 2 == 1);
        lang.setName("name" + val);
        return lang;
    }

}
