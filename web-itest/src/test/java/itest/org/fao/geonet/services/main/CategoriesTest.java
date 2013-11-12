package itest.org.fao.geonet.services.main;

import itest.org.fao.geonet.Utils;

import org.fao.geonet.utils.Xml;

import org.junit.Test;

import org.jdom.Element;
import org.jdom.JDOMException;

import org.apache.commons.httpclient.HttpClient;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.List;

public class CategoriesTest extends TestCase {
	public CategoriesTest() {
		Utils.setSequential();
	}

	private void init() {
		Utils.cleanCatalogue();
		Utils.addSamples();
		String userId = Utils.addUser("userone", "userone", "2", "Editor");
		Utils.transferOwner(userId); // make the new user the owner of the samples
	}

	private List<Element> getCategories(HttpClient c) throws JDOMException, IOException {
		String cats = Utils.sendRequest("xml.info?type=categories", false, c);
		Element info = Xml.loadString(cats, false);
		return info.getChild("categories").getChildren();
	}

	@Test
	public void testCategoriesSetOneMetadataRecord() {

		String uuid = "da165110-88fd-11da-a88f-000d939bc5d8";

		try {
			init();

			final HttpClient c = new HttpClient();
			Utils.sendLogin(c, "userone", "userone");
			List<Element> categories = getCategories(c);
			for (Element cat : categories) {
				String catId = cat.getAttributeValue("id");
				Utils.sendRequest("metadata.category?uuid="+uuid+"&_"+catId+"=on", false, c);
				String req = "xml.search?_cat="+cat.getChildText("name");
				String response = Utils.sendRequest(req, false, c);
				assertTrue(response.contains(uuid));
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (JDOMException je) {
			je.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}

	@Test
	public void testCategoriesSetAllMetadataRecords() {

		String uuid = "da165110-88fd-11da-a88f-000d939bc5d8";

		try {
			init();

			final HttpClient c = new HttpClient();
			Utils.sendLogin(c, "userone", "userone");
			List<Element> categories = getCategories(c);
			for (Element cat : categories) {
				String catId = cat.getAttributeValue("id");
				Utils.sendRequest("main.search.embedded", false, c);
				Utils.sendRequest("metadata.select?selected=add-all&id=0", false, c);
				Utils.sendRequest("metadata.batch.update.categories?&_"+catId+"=on", false, c);
				String response = Utils.sendRequest( "xml.search?_cat="+cat.getChildText("name"), false, c);
				assertTrue(response.contains(uuid));
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (JDOMException je) {
			je.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}

	@Test
	public void testCategoriesAddUpdateDeleteCategory() {
		try {
			init();

			final HttpClient c = new HttpClient();
			Utils.sendLogin(c);

			// add a category
			Utils.sendRequest("category.update?name=codswallop", false, c);

			List<Element> categories = getCategories(c);
			int catId = -1;
			for (Element cat : categories) {
				if (cat.getChildText("name").equals("codswallop")) {
					catId = Integer.parseInt(cat.getAttributeValue("id"));
				}
			}
			assertTrue(catId != -1);

			// update a category
			Utils.sendRequest("category.update?id="+catId+"&name=cobblers", false, c);
			categories = getCategories(c);
			boolean found = false;
			for (Element cat : categories) {
				if (cat.getChildText("name").equals("cobblers") && 
						Integer.parseInt(cat.getAttributeValue("id")) == catId) {
							found = true;
							break;
				}
			}
			assertTrue(found);

			// delete a category
			Utils.sendRequest("category.remove?id="+catId, false, c);
			categories = getCategories(c);
			boolean notFound = true;
			for (Element cat : categories) {
				if (cat.getChildText("name").equals("cobblers") && 
						Integer.parseInt(cat.getAttributeValue("id")) == catId) {
							notFound = false;
							break;
				}
			}
			assertTrue(notFound);

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (JDOMException je) {
			je.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}

}
