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

public class UserGroupsTest extends TestCase {
	public UserGroupsTest() {
		Utils.setSequential();
	}

	private void init() {
		Utils.cleanCatalogue();
		Utils.addSamples();
	}

	@Test
	public void testUserGroups() {

		String uuid = "da165110-88fd-11da-a88f-000d939bc5d8";

		try {
			init();

			final HttpClient c = new HttpClient();
			Utils.dropAllNonAdminUsers(true, c);
			
			// get groups using xml.info and then add a new user to each one
			String resp = Utils.sendRequest("xml.info?type=groups", false, c);
			Element info = Xml.loadString(resp, false);
			List<Element> grps = info.getChild("groups").getChildren();
			for (Element grp : grps) {
				String grpId = grp.getAttributeValue("id");
				int iGrpId = Integer.parseInt(grpId);
				if (iGrpId > 1) continue; // skip all, intranet

				// add a new UserAdmin user with the current group
				String userId = Utils.addUser("userone", "userone", grpId, "UserAdmin");
				Utils.sendLogin(c, "userone", "userone");

				// try to get user groups of some other user - naughty
				resp = Utils.sendRequestToFail("xml.usergroups.list?id=1", false, c);
				assertTrue(resp.contains("OperationNotAllowedEx"));

				// try to get user groups of this user
				resp = Utils.sendRequest("xml.usergroups.list?id="+userId, false, c);
				info = Xml.loadString(resp, false);
				List<Element> groups = info.getChildren();
				boolean found = false;
				for (Element group : groups) {
					if (group.getChildText("id").equals(grpId)) {
						found = true;
						break;
					}
				}
				assertTrue(found);

				Utils.dropAllNonAdminUsers(true, c);
			}
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			fail();
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
