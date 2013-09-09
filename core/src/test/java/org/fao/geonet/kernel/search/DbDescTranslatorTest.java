package org.fao.geonet.kernel.search;

import org.fao.geonet.domain.Localized;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.fail;

/**
 * Test DbDescTranslator
 * User: Jesse
 * Date: 9/9/13
 * Time: 9:07 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/fao/geonet/kernel/search/translator-test-application-context.xml" })
public class DbDescTranslatorTest {
    @Autowired
    ApplicationContext _appContext;

    @Test
    public void testTranslateStringKey() throws Exception {

       fail("needs implementation");
    }
    @Test
    public void testTranslateIntKey() throws Exception {
       fail("needs implementation");
    }

    private static interface TestRepositoryInt extends JpaRepository<Localized, Integer> {}
    private static interface TestRepositoryString extends JpaRepository<Localized, String> {}
}
