package org.fao.geonet.services.main;

import org.junit.Test;
import org.fao.geonet.test.ProtocolTestCase;

public class CswTest extends ProtocolTestCase {

	@Test
	public void testGNCswGetCapabilities() throws Exception {
		doTest("gn-csw-GetCapabilities.xml");
	}

	@Test
	public void testGNCswDescribeRecord() throws Exception {
		doTest("gn-csw-DescribeRecord.xml");
	}
	
	@Test
	public void testGNCswGetRecords() throws Exception {
		doTest("gn-csw-GetRecords.xml");
	} 
}
