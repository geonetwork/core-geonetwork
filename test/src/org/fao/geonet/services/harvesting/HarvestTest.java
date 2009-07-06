package org.fao.geonet.services.harvesting;

import org.junit.Test;
import org.fao.geonet.test.ProtocolTestCase;


/**
 * Tests for harvesting Jeeves-services.
 */
public class HarvestTest extends ProtocolTestCase
{
	public static final String URL_FAO="www.fao.org";

	@Test
	public void testGNHarvester() throws Exception
	{
		setVariable("name", "gn.fao");
		setVariable("host", URL_FAO);
		doTest("gn-harvester-add.xml");
		doTest("gn-harvester-get-inactive.xml");
//		doTest("harvester-run-inactive.xml");
		doTest("harvester-start.xml");
		doTest("gn-harvester-get-active.xml");

		doTest("harvester-stop.xml");

		// Inactive status
		doTest("gn-harvester-get-inactive.xml");
		doTest("harvester-remove.xml");
	}

/*	@Test
	public void testCGPHarvesterLifecycle() throws Exception
	{
		setVariable("url", URL_GEOTASKSERVER);
		doTest("cgp-harvester-add.xml");
		doTest("cgp-harvester-get-inactive.xml");
		doTest("harvester-run-inactive.xml");
		doTest("harvester-start.xml");
		doTest("cgp-harvester-get-active.xml");

		doTest("harvester-stop.xml");

		// Inactive status
		doTest("cgp-harvester-get-inactive.xml");
		doTest("harvester-remove.xml");
	}

	@Test
	public void testCGPHarvesterInvoke() throws Exception
	{
		setVariable("url", URL_GEOTASKSERVER);
		doTest("cgp-harvester-add.xml");
		// may take long time (22 recs) use with search
		doTest("harvester-invoke.xml");
		doTest("harvester-remove.xml");
	}

	@Test
	public void testCGPHarvesterInvokeWithSearch() throws Exception
	{
		setVariable("url", URL_GEOTASKSERVER);
		setVariable("searchtext", "ee");
		doTest("cgp-harvester-add-search-text.xml");
		doTest("harvester-invoke.xml");
		// Second invoke should delete harvested MD from the first
		doTest("harvester-invoke.xml");
		doTest("harvester-remove.xml");
	}

	@Test
	public void testCGPHarvesterTextSearch() throws Exception
	{
		setVariable("url", URL_GEOTASKSERVER);
		setVariable("searchtext", "ee");
		doTest("cgp-harvester-add-search-text.xml");
		doTest("cgp-harvester-get-inactive-search-text.xml");
		doTest("harvester-remove.xml");
	}

	
	@Test
	public void testCGPHarvesterBboxSearch() throws Exception
	{
		// This BBox delivers only one record
		// TODO support use of "." like 47.1234 (fails since regexp token)
		setVariable("url", URL_GEOTASKSERVER);
		setVariable("latNorth", "48");
		setVariable("latSouth", "44");
		setVariable("lonEast", "11");
		setVariable("lonWest", "4");

		doTest("cgp-harvester-add-search-bbox.xml");
		doTest("harvester-invoke.xml");
		doTest("harvester-remove.xml");
	}

	@Test
	public void testCGPHarvesterDebug() throws Exception
	{
		setVariable("url", URL_GEOTASKSERVER);
		doTest("cgp-harvester-add.xml");
		doTest("cgp-harvester-get-inactive.xml");

//		doTest("cgp-harvester-add-search-text.xml");
//		doTest("harvester-invoke.xml");
		doTest("harvester-remove.xml");
	}   */
}
