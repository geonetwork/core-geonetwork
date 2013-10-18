package org.fao.geonet.kernel.harvest;

import org.fao.geonet.AbstractCoreTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * test base class for testing harvesters.
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:02 PM
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:harvesters-repository-test-context.xml")
public class AbstractHarvesterTest extends AbstractCoreTest {
}
