package itest.org.fao.geonet.services.main;

import itest.org.fao.geonet.Utils;

import java.io.IOException;

import org.junit.Test;

import org.apache.commons.httpclient.HttpClient;

import junit.framework.TestCase;

public class StatusTest extends TestCase {
	public StatusTest() {
	}

	private void init() {
		Utils.cleanCatalogue();
		Utils.addSamples();
		Utils.addUser("userone", "userone");
		Utils.transferOwner("2"); // make the new user the owner of the samples
	}

	@Test
	public void testStatusSetOneMetadataRecord() {

		String uuid = "da165110-88fd-11da-a88f-000d939bc5d8";

		try {
			init();

			final HttpClient c = new HttpClient();
			Utils.sendLogin(c, "userone", "userone");
			Utils.sendRequest("xml.metadata.status.set?uuid="+uuid+"&status=4&changeMessage=submitted", false, c);
			String response = Utils
					.sendRequest( "xml.search?_status=4", false, c);
			assertTrue(response
					.contains(uuid));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}

	@Test
	public void testStatusSetAllMetadataRecords() {

		String uuid = "da165110-88fd-11da-a88f-000d939bc5d8";

		try {
			init();

			final HttpClient c = new HttpClient();
			Utils.sendLogin(c, "userone", "userone");
			Utils.sendRequest("main.search.embedded", false, c);
			Utils.sendRequest( "metadata.select?selected=add-all&id=0", false, c);
			Utils.sendRequest("metadata.batch.update.status?&status=4&changeMessage=submitted", false, c);
			String response = Utils
					.sendRequest( "xml.search?_status=4", false, c);
			assertTrue(response
					.contains(uuid));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}
}
