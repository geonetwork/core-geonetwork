package itest.org.fao.geonet.services.main;

import itest.org.fao.geonet.Utils;

import java.io.IOException;

import org.junit.Test;

import junit.framework.TestCase;

public class SearchTest extends TestCase {
	public SearchTest() {
		Utils.setSequential();
		Utils.cleanCatalogue();
	}
	
	@Test
	public void testDenominator() {
		Utils.addSamples();

		try {
			// Equals
			String response = Utils
					.sendRequest(
							"xml.search?denominator=5000000",
							false);
			assertTrue(response
					.contains("da165110-88fd-11da-a88f-000d939bc5d8"));

			
			// Range
			response = Utils
			.sendRequest(
					"xml.search?denominatorFrom=5000001&denominatorTo=5000002",
					false);
			assertFalse(response
					.contains("da165110-88fd-11da-a88f-000d939bc5d8"));
			
			response = Utils
			.sendRequest(
					"xml.search?denominatorFrom=4999999&denominatorTo=5000002",
					false);
			assertTrue(response
					.contains("da165110-88fd-11da-a88f-000d939bc5d8"));

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}

	@Test
	public void testBboxOverlaps() {
		Utils.addSamples();

		try {
			// Overlaps is default relation operator
			String response = Utils
					.sendRequest(
							"xml.search?westBL=-17.3&eastBL=51.1&southBL=-34.6&northBL=38.2",
							false);
			assertTrue(response
					.contains("da165110-88fd-11da-a88f-000d939bc5d8"));

			response = Utils
					.sendRequest(
							"xml.search?westBL=-17.3&eastBL=51.1&southBL=-34.6&northBL=38.2&relation=overlaps",
							false);
			assertTrue(response
					.contains("da165110-88fd-11da-a88f-000d939bc5d8"));

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}

	@Test
	public void testBboxEquals() {
		Utils.addSamples();

		try {
			String response = Utils
					.sendRequest(
							"xml.search?westBL=-17.3&eastBL=51.1&southBL=-34.6&northBL=38.2&relation=equal",
							false);
			assertTrue(response
					.contains("da165110-88fd-11da-a88f-000d939bc5d8"));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}

}
