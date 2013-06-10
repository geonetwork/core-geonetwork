package org.fao.geonet.repository;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-test-context.xml", "classpath:spring-data-jpa.xml" })
@Transactional
public abstract class AbstractSpringDataTest {

}
