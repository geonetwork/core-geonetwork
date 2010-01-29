package org.fao.geonet.services.io;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.fao.geonet.constants.Params;
import org.fao.geonet.test.TestConfig;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing MEF version 1 and version 2 import and export.
 * 
 * TODO : Use test suite method to query current node ? Need to add support of
 * multipart request.
 * 
 * @author francois
 * 
 */
public class MEFIOTest extends TestCase {
	private static final String uuid = "123456789";

	@Override
	@Before
	protected void setUp() throws Exception {
		TestConfig.init();
	}

	/**
	 * Load sample data.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_MEF1_load_sampleData() throws IOException {
		String sampleDataDirectory = "../../gast/setup/sample-data";

		Element response = Utils.XMLOrMEFImport(sampleDataDirectory
				+ "/fgdc.mef", Params.UUID_ACTION, Params.NOTHING);
		assertEquals("id", response.getName());
		response = Utils.XMLOrMEFImport(sampleDataDirectory
				+ "/iso-19139-basins-in-africa.mef", Params.UUID_ACTION,
				Params.GENERATE_UUID);
		assertEquals("id", response.getName());
		response = Utils.XMLOrMEFImport(sampleDataDirectory
				+ "/iso-19139-physiographic.mef", Params.UUID_ACTION,
				Params.GENERATE_UUID);
		assertEquals("id", response.getName());
		response = Utils.XMLOrMEFImport(sampleDataDirectory
				+ "/dublin-core.mef", Params.UUID_ACTION, Params.GENERATE_UUID);
		assertEquals("id", response.getName());
		response = Utils.XMLOrMEFImport(sampleDataDirectory + "/iso-19115.mef",
				Params.UUID_ACTION, Params.GENERATE_UUID);
		assertEquals("id", response.getName());
	}

	/**
	 * Check import/export of a MEF 1 file. Test format simple/partial/full
	 * support on export.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_io_MEF1() throws IOException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			Element response = Utils.XMLOrMEFImport("mef1.zip",
					Params.UUID_ACTION, Params.NOTHING);
			assertEquals("id", response.getName());
			String id = response.getText().replace(";", ""); // remove separator

			// Simple/Partial/Full format
			ArrayList<String> entries = Utils.MEFExport(uuid, null, "simple", false);
			assertEquals(true, entries.contains("metadata.xml"));
			assertEquals(true, entries.contains("info.xml"));

			entries = Utils.MEFExport(uuid, null, "partial", false);
			assertEquals(true, entries.contains("metadata.xml"));
			assertEquals(true, entries.contains("info.xml"));
			assertEquals(true, entries.contains("public/"));
			assertEquals(true, entries.contains("/public/thumbnail.gif"));
			assertEquals(true, entries.contains("/public/thumbnail_s.gif"));

			entries = Utils.MEFExport(uuid, null, "full", false);
			assertEquals(true, entries.contains("metadata.xml"));
			assertEquals(true, entries.contains("info.xml"));
			assertEquals(true, entries.contains("public/"));
			assertEquals(true, entries.contains("private/"));
			assertEquals(true, entries.contains("/public/thumbnail.gif"));
			assertEquals(true, entries.contains("/public/thumbnail_s.gif"));
			assertEquals(true, entries.contains("/private/basins.zip"));

		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}
	}

	/**
	 * Try to load an incomplete MEF version 1 file.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_MEF1_with_no_metadata_file() throws IOException {
		Element response = Utils.XMLOrMEFImport("mef1_missing_metadata.zip",
				Params.UUID_ACTION, Params.GENERATE_UUID);
		assertEquals(
				"Missing metadata file : metadata.xml",
				response.getText());
	}

	/**
	 * Import two times the same metadata record and do not change UUID on
	 * import should trigger a unique constraint error.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_MEF1_twice() throws IOException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			Element response = Utils.XMLOrMEFImport("mef1.zip",
					Params.UUID_ACTION, Params.NOTHING);
			assertEquals("id", response.getName());
			response = Utils.XMLOrMEFImport("mef1.zip", Params.UUID_ACTION,
					Params.NOTHING);
			assertEquals("error", response.getName());
			assertEquals(true, response.getText().contains(
					"Immediate unique constraint violation"));
		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}
	}

	/**
	 * 
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_MEF1_and_process_uuid() throws IOException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			// Load a file keeping UUID and get the record
			Utils
					.XMLOrMEFImport("mef1.zip", Params.UUID_ACTION,
							Params.NOTHING);
			String response = Utils.sendRequest("iso19139.xml?uuid=" + uuid,
					true);
			assertEquals(true, response.contains(uuid));
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			
			// Load the same file creating a new UUID and get the record MUST
			// return
			// a metadata not found exception
			Utils.XMLOrMEFImport("mef1.zip", Params.UUID_ACTION,
					Params.GENERATE_UUID);
			response = Utils.sendRequest("iso19139.xml?uuid=" + uuid, true);
			assertEquals(true, response.contains("Metadata not found"));

			// TODO : overwrite

		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}

	}

	/**
	 * Check import/export of a MEF 2 file. Test format simple/partial/full
	 * support on export.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_MEF2() throws IOException {
		String uuid = "4e317870-8a85-11db-8846-0020ed6939a9";

		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			Element response = Utils.XMLOrMEFImport("mef2_clc_sample_fr.zip",
					Params.UUID_ACTION, Params.NOTHING);
			assertEquals("id", response.getName());

			// Simple/Partial/Full format
			ArrayList<String> entries = Utils.MEFExport(uuid, "2.0", "simple", false);
			assertEquals(true, entries.contains(uuid + "/info.xml"));
			assertEquals(true, entries
					.contains(uuid + "/metadata/metadata.xml"));
			assertEquals(true, entries.contains(uuid
					+ "/applschema/metadata.xml"));

			entries = Utils.MEFExport(uuid, "2.0", "partial", false);
			assertEquals(true, entries.contains(uuid + "/info.xml"));
			assertEquals(true, entries
					.contains(uuid + "/metadata/metadata.xml"));
			assertEquals(true, entries.contains(uuid
					+ "/applschema/metadata.xml"));
			assertEquals(true, entries.contains(uuid + "/public/"));
			assertEquals(true, entries.contains(uuid + "/public/corine_s.png"));

			entries = Utils.MEFExport(uuid, "2.0", "full", false);
			assertEquals(true, entries.contains(uuid + "/info.xml"));
			assertEquals(true, entries
					.contains(uuid + "/metadata/metadata.xml"));
			assertEquals(true, entries.contains(uuid
					+ "/applschema/metadata.xml"));
			assertEquals(true, entries.contains(uuid + "/public/"));
			assertEquals(true, entries.contains(uuid + "/public/corine_s.png"));
			assertEquals(true, entries.contains(uuid + "/private/"));
			assertEquals(true, entries.contains(uuid + "/private/basins.zip"));

		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}

	}

	/**
	 * Insert 3 related records.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_MEF2_relations() throws IOException {
		String parentRelatedRecord = "bbb2d457-6751-44af-b493-79a407b88bea";
		String[] uuids = { parentRelatedRecord,
				"2a1eeaf7-6578-4acc-8c81-87b88c37bbbc",
				"ff7834fb-3aa9-4c80-84be-689d2ac7222c" };
		// Clean
		try {
			for (String uuid : uuids)
				Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			Element response = Utils.XMLOrMEFImport(
					"mef2_with_related_records.zip", Params.UUID_ACTION,
					Params.NOTHING);
			assertEquals("id", response.getName());
			assertEquals(true, response.getText().matches(
					"[0-9].*;[0-9].*;[0-9].*;"));

			ArrayList<String> entries = Utils.MEFExport(parentRelatedRecord, "2.0",
					"full", true); // Login first as related records are
			// private.
			for (String uuid : uuids)
				assertEquals(true, entries.contains(uuid + "/"));
		} finally {
			for (String uuid : uuids)
				Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}
	}

	
	/**
	 * ISO Profil record should also export a pure ISO record.
	 */
	@Test
	public void test_export_MEF2_for_iso_profil (){
		// TODO
	}
}