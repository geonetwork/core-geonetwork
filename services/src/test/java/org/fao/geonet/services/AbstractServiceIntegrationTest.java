package org.fao.geonet.services;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Adds extra bean required for services tests.
 *
 * User: Jesse
 * Date: 10/17/13
 * Time: 9:53 AM
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:services-repository-test-context.xml")
public abstract class AbstractServiceIntegrationTest extends AbstractCoreIntegrationTest {
}
