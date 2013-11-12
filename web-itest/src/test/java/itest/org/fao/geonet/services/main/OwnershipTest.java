package itest.org.fao.geonet.services.main;

import itest.org.fao.geonet.Utils;

import java.io.IOException;
import java.util.List;

import org.fao.geonet.utils.Xml;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.JDOMException;

import org.junit.Test;

import org.apache.commons.httpclient.HttpClient;

import junit.framework.TestCase;

public class OwnershipTest extends TestCase {
	public static final String NS_PREFIX = "geonet";
	public static final String NS_URI    = "http://www.fao.org/geonetwork";
	public static final Namespace GEONET_NS = Namespace.getNamespace(NS_PREFIX, NS_URI);

	public OwnershipTest() {
		Utils.setSequential();
	}

	private void init() {
		Utils.cleanCatalogue();
		Utils.addSamples();
	}

	@Test
	public void testOwnershipTransferAll() {

		try {
			init();

			String userId = Utils.addUser("userone", "userone", "2", "Editor");

			final HttpClient c = new HttpClient();

			// set all metadata to owner admin, group 2 (sample group)
			Utils.sendRequest("main.search.embedded", true, c);
			Utils.sendRequest("metadata.select?id=0&selected=add-all", false, c);
			Utils.sendRequest("metadata.batch.newowner?user=1&group=2", false, c);

			// now transfer all metadata belonging to admin to the new user userone
			Utils.sendRequest("xml.ownership.transfer?sourceUser=1&sourceGroup=2&targetUser="+userId+"&targetGroup=2", false, c);
			
			// now login, search and check that the records have ownership set
			Utils.sendLogin(c, "userone", "userone");
			String response = Utils.sendRequest( "xml.search?fast=false", false, c);

			Element resp = Xml.loadString(response,false);
			List<Element> childs = resp.getChildren();
			for (Element child : childs) {
				if (child.getName().equals("metadata")) {
					Element info = child.getChild("info",GEONET_NS);
					assertEquals(info.getChildText("ownername"),"userone");
				}
			}
		} catch (JDOMException je) {
			je.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}

	@Test
	public void testOwnershipAllMetadataRecords() {

		try {
			init();

			String userId = Utils.addUser("userone", "userone", "2", "Editor");

			// search, select all, use batch new owner to set new owner to userone
			final HttpClient c = new HttpClient();
			Utils.sendRequest("main.search.embedded", true, c);
			Utils.sendRequest("metadata.select?id=0&selected=add-all", false, c);
			Utils.sendRequest("metadata.batch.newowner?user="+userId+"&group=2", false, c);

			// now login, search and check that the records have ownership set
			Utils.sendLogin(c, "userone", "userone");
			String response = Utils.sendRequest( "xml.search?fast=false", false, c);
			Element resp = Xml.loadString(response,false);
			List<Element> childs = resp.getChildren();
			for (Element child : childs) {
				if (child.getName().equals("metadata")) {
					Element info = child.getChild("info",GEONET_NS);
					assertEquals(info.getChildText("ownername"),"userone");
				}
			}
		} catch (JDOMException je) {
			je.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}
}
