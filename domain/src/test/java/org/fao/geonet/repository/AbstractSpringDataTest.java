package org.fao.geonet.repository;

import org.fao.geonet.ApplicationContextHolder;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.io.InputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        AbstractSpringDataTest.CLASSPATH_CONFIG_SPRING_GEONETWORK_PARENT_XML,
        AbstractSpringDataTest.CLASSPATH_CONFIG_SPRING_GEONETWORK_XML,
        AbstractSpringDataTest.CLASSPATH_REPOSITORY_TEST_CONTEXT_XML})
@Transactional(propagation = Propagation.REQUIRED)
public abstract class AbstractSpringDataTest {

    public static final String CLASSPATH_CONFIG_SPRING_GEONETWORK_XML = "classpath*:config-spring-geonetwork.xml";
    public static final String CLASSPATH_CONFIG_SPRING_GEONETWORK_PARENT_XML = "classpath*:config-spring-geonetwork-parent.xml";
    public static final String CLASSPATH_REPOSITORY_TEST_CONTEXT_XML = "classpath:domain-repository-test-context.xml";

    @Autowired
    private ConfigurableApplicationContext _appContext;

    @Before
    public void setApplicationContextInApplicationHolder() {
        ApplicationContextHolder.set(_appContext);
    }

    /**
     * Find a file on the classpath in the same directory (or root) as the current class and return a stream to that file.
     *
     * @param fileName the name of the file.
     */
    @Nonnull
    protected InputStream fileStream(@Nonnull String fileName) {
        final InputStream stream = getClass().getResourceAsStream(fileName);
        if (stream != null) {
            return stream;
        } else {
            final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
            if (resourceAsStream != null) {
                return resourceAsStream;
            }
        }

        throw new IllegalArgumentException(fileName + " not found");

    }

}
