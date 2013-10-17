package org.fao.geonet.repository;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {AbstractSpringDataTest.CLASSPATH_CONFIG_SPRING_GEONETWORK_XML, AbstractSpringDataTest
        .CLASSPATH_REPOSITORY_TEST_CONTEXT_XML})
@Transactional
public abstract class AbstractSpringDataTest {

    public static final String CLASSPATH_CONFIG_SPRING_GEONETWORK_XML = "classpath*:config-spring-geonetwork.xml";
    public static final String CLASSPATH_REPOSITORY_TEST_CONTEXT_XML = "classpath:domain-repository-test-context.xml";
}
