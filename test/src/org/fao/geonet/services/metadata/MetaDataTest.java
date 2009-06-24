package org.fao.geonet.services.metadata;

import org.junit.Test;
import org.fao.geonet.test.ProtocolTestCase;


/**
 * Tests for "metadata" Jeeves-service.
 */
public class MetaDataTest extends ProtocolTestCase
{

	@Test
	public void testCreateWithTemplate() throws Exception
	{
		doTest("md-create-template.xml");
	}

}
