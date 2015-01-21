package org.fao.geonet.repository;


import org.fao.geonet.domain.IsoLanguage;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class IsoLanguageRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    IsoLanguageRepository _repo;

    @Before
    public void setUp() throws Exception {
        _inc.set(0);
    }

    @Test
    public void testFindOne() {
        IsoLanguage isolang1 = newIsoLanguage();
        isolang1 = _repo.save(isolang1);

        IsoLanguage isolang2 = newIsoLanguage();
        isolang2 = _repo.save(isolang2);


        assertEquals(isolang2, _repo.findOne(isolang2.getId()));
        assertEquals(isolang1, _repo.findOne(isolang1.getId()));
    }

    @Test
    public void testFindByCode() {
        IsoLanguage isolang1 = newIsoLanguage();
        isolang1 = _repo.save(isolang1);

        IsoLanguage isolang2 = newIsoLanguage();
        isolang2 = _repo.save(isolang2);

        List<IsoLanguage> langs = _repo.findAllByCode(isolang1.getCode());

        assertEquals(isolang1.getCode(), langs.get(0).getCode());

        langs = _repo.findAllByCode(isolang2.getCode());

        assertEquals(isolang2.getCode(), langs.get(0).getCode());
    }

    @Test
    public void testFindByShortCode() {
        IsoLanguage isolang1 = newIsoLanguage();
        isolang1 = _repo.save(isolang1);

        IsoLanguage isolang2 = newIsoLanguage();
        isolang2 = _repo.save(isolang2);

        List<IsoLanguage> langs = _repo.findAllByShortCode(isolang1.getShortCode());

        assertEquals(isolang1.getShortCode(), langs.get(0).getShortCode());

        langs = _repo.findAllByShortCode(isolang2.getShortCode());

        assertEquals(isolang2.getShortCode(), langs.get(0).getShortCode());
    }

    private IsoLanguage newIsoLanguage() {
        final AtomicInteger inc = _inc;
        return createIsoLanguage(inc);
    }

    public static IsoLanguage createIsoLanguage(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        IsoLanguage lang = new IsoLanguage();
        lang.setCode("cd" + val);
        lang.setShortCode("c" + val);
        return lang;
    }

}
