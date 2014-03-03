package org.fao.geonet.services.io;

import java.io.IOException;
import java.util.HashMap;

import org.fao.geonet.utils.Xml;
import junit.framework.TestCase;

import org.fao.geonet.constants.Params;
import org.fao.geonet.test.TestConfig;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing XML import and export.
 * 
 * @author francois
 * 
 */
public class XMLIOTest extends TestCase {
	private static final String uuid = "123456789";
	private static final String testFile = "sample.xml";

	@Override
	@Before
	protected void setUp() throws Exception {
		TestConfig.init();
	}

	/**
	 * Load a simple XML file in sample group.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_XML() throws IOException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
			Element response = Utils.XMLOrMEFImport(testFile, Params.GROUP,
					Utils.defaultGroup);
			assertEquals("id", response.getName());

			// Default behaviour is keep UUID.
			String xml = Utils.sendRequest("iso19139.xml?uuid=" + uuid, true);
			assertEquals(true, xml.contains(uuid));

		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}
	}

	/**
	 * Group is mandatory.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_XML_group() throws IOException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			Element response = Utils.XMLOrMEFImport(testFile,
					Params.UUID_ACTION, Params.NOTHING);
			assertEquals("error", response.getName());
			assertEquals("missing-parameter", response.getAttributeValue("id"));
			assertEquals("group", response.getText());

			response = Utils.XMLOrMEFImport(testFile, Params.GROUP,
					"NONEXISTINGGROUP");
			assertEquals("error", response.getName());
			assertEquals("error", response.getAttributeValue("id"));
			assertEquals("For input string: \"NONEXISTINGGROUP\"", response
					.getText());
			// TODO : here we should probably return a "non existing group"
			// exception instead ?

			response = Utils.XMLOrMEFImport(testFile, Params.GROUP, "999999");
			assertEquals("error", response.getName());
			assertEquals("error", response.getAttributeValue("id"));
			assertEquals(true, response.getText().contains(
					"Immediate foreign key constraint violation"));
			// TODO : here we should probably return a "non existing group"
			// exception instead ?

			// check group is correct
			response = Utils.XMLOrMEFImport(testFile, Params.GROUP,
					Utils.defaultGroup);
			String xml = Utils.sendRequest("xml.metadata.get?uuid=" + uuid,
					true);
			// TODO : How to check group is ok ? assertEquals(true,
			// xml.contains("<group>2</group>"));

		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}
	}

	/**
	 * Category is optional.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_XML_category() throws IOException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			// Insert and check no category assigned
			Element response = Utils
					.XMLOrMEFImport(testFile, Params.GROUP, "2");
			assertEquals("id", response.getName());
			String xml = Utils.sendRequest("xml.metadata.get?uuid=" + uuid,
					true);
			assertEquals(false, xml.contains("<category>"));

			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			// check category is assigned
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(Params.GROUP, Utils.defaultGroup);
			params.put(Params.CATEGORY, "2"); // 2 = datasets

			response = Utils.XMLOrMEFImport(testFile, params);
			xml = Utils.sendRequest("xml.metadata.get?uuid=" + uuid, true);
			assertEquals(true, xml.contains("<category>datasets</category>"));
		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}
	}

	/**
	 * Insert as template.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_XML_istemplate() throws IOException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put(Params.GROUP, Utils.defaultGroup);
			params.put(Params.TEMPLATE, "y");

			Element response = Utils.XMLOrMEFImport(testFile, params);
			String id = response.getText().replace(";", ""); // Quick unique id
			// cleaner

			String xml = Utils.sendRequest("xml.metadata.get?id=" + id, true);
			assertEquals(true, xml.contains("<isTemplate>y</isTemplate>"));
		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}
	}

	/**
	 * Check generate UUID actions
	 * FIXME : Trigger Transaction error in mckoi sometimes.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_XML_and_process_uuid() throws IOException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			// Load a file keeping UUID and get the record
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(Params.GROUP, Utils.defaultGroup);
			params.put(Params.UUID_ACTION, Params.NOTHING);
			Utils.XMLOrMEFImport(testFile, params);
			String response = Utils.sendRequest("iso19139.xml?uuid=" + uuid,
					true);
			assertEquals(true, response.contains(uuid));

			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			// Load the same file creating a new UUID and get the record MUST
			// return
			// a metadata not found exception
			params.put(Params.UUID_ACTION, Params.GENERATE_UUID);
			Utils.XMLOrMEFImport(testFile, params);
			response = Utils.sendRequest("iso19139.xml?uuid=" + uuid, true);
			assertEquals(true, response.contains("Metadata not found"));

			// TODO : check overwrite
		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
			// TODO : clean the generated uuid
		}
	}

	/**
	 * Import two times the same metadata record and do not change UUID on
	 * import should trigger a unique constraint error.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test_import_XML_twice() throws IOException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put(Params.GROUP, Utils.defaultGroup);
			params.put(Params.UUID_ACTION, Params.NOTHING);

			Element response = Utils.XMLOrMEFImport(testFile, params);
			assertEquals("id", response.getName());
			response = Utils.XMLOrMEFImport(testFile, params);
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
	 * @throws IOException
	 */
	@Test
	public void test_import_XML_load_templates() throws IOException {
		String sampleDataDirectory = "../../gast/setup/templates";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(Params.GROUP, Utils.defaultGroup);
		params.put(Params.TEMPLATE, "y");

		Element response = Utils.XMLOrMEFImport(sampleDataDirectory
				+ "/iso19139/raster.xml", params);
		assertEquals("id", response.getName());
		response = Utils.XMLOrMEFImport(sampleDataDirectory
				+ "/iso19139/service.xml", params);
		assertEquals("id", response.getName());
		response = Utils.XMLOrMEFImport(sampleDataDirectory
				+ "/iso19139/vector.xml", params);
		assertEquals("id", response.getName());

		response = Utils.XMLOrMEFImport(sampleDataDirectory
				+ "/iso19110/metadata.xml", params);
		assertEquals("id", response.getName());

		response = Utils.XMLOrMEFImport(sampleDataDirectory
				+ "/fgdc-std/metadata.xml", params);
		assertEquals("id", response.getName());

		response = Utils.XMLOrMEFImport(sampleDataDirectory
				+ "/dublin-core/metadata.xml", params);
		assertEquals("id", response.getName());
	}

	@Test
	public void test_import_XML_copyPaste() throws IOException, JDOMException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			Element md = Xml.loadFile(TestConfig.getResourcesPath() + "/"
					+ testFile);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put(Params.GROUP, Utils.defaultGroup);
			params.put(Params.UUID_ACTION, Params.NOTHING);
			params.put(Params.DATA, Xml.getString(md));

			String response = Utils.sendRequest("metadata.insert.paste",
					params, true);
			assertEquals(true, response.contains("Metadata added with ID")); // Quick
			// check
		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}
	}

	@Test
	public void test_import_XML_copyPaste_twice() throws IOException,
			JDOMException {
		try {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
			
			Element md = Xml.loadFile(TestConfig.getResourcesPath() + "/"
					+ testFile);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put(Params.GROUP, Utils.defaultGroup);
			params.put(Params.UUID_ACTION, Params.NOTHING);
			params.put(Params.DATA, Xml.getString(md));

			String response = Utils.sendRequest("metadata.insert.paste",
					params, true);
			assertEquals(true, response.contains("Metadata added with ID")); // Quick
			// check

			response = Utils.sendRequest("metadata.insert.paste", params, true);
			assertEquals(true, response.contains("error"));
			assertEquals(true, response
					.contains("Immediate unique constraint violation"));
		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}
	}

	@Test
	public void test_import_XML_copyPaste_and_process_uuid()
			throws IOException, JDOMException {
		try {
			// Sample metadata record
			Element md = Xml.loadFile(TestConfig.getResourcesPath() + "/"
					+ testFile);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(Params.GROUP, Utils.defaultGroup);
			params.put(Params.DATA, Xml.getString(md));

			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			// Load a file keeping UUID and get the record
			params.put(Params.UUID_ACTION, Params.NOTHING);
			String response = Utils.sendRequest("metadata.insert.paste",
					params, true);
			response = Utils.sendRequest("iso19139.xml?uuid=" + uuid, true);
			assertEquals(true, response.contains(uuid));

			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			// Load the same file creating a new UUID and get the record MUST
			// return
			// a metadata not found exception
			params.put(Params.UUID_ACTION, Params.GENERATE_UUID);
			response = Utils.sendRequest("metadata.insert.paste", params, true);
			response = Utils.sendRequest("iso19139.xml?uuid=" + uuid, true);
			assertEquals(true, response.contains("Metadata not found"));
			// TODO : check the loaded one is a good one ?

			// Clean - do nothing - clean the generated uuid
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);

			// Check overwrite
			// Load once
			params.put(Params.UUID_ACTION, Params.OVERWRITE);
			response = Utils.sendRequest("metadata.insert.paste", params, true);
			response = Utils.sendRequest("iso19139.xml?uuid=" + uuid, true);
			assertEquals(true, response.contains(uuid));

			// Upade metadata record and load with overwrite option
			md.addContent(new Element("addedNode"));
			params.put(Params.DATA, Xml.getString(md));
			response = Utils.sendRequest("metadata.insert.paste", params, true);

			// Check changes
			response = Utils.sendRequest("iso19139.xml?uuid=" + uuid, true);
			assertEquals(true, response.contains("addedNode"));

		} finally {
			// Clean
			Utils.sendRequest("metadata.delete?uuid=" + uuid, true);
		}
	}
}