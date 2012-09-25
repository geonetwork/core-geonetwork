package v280;

import java.sql.SQLException;
import java.util.Iterator;

import jeeves.resources.dbms.Dbms;

import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

public class MigrationTask implements DatabaseMigrationTask {

	@Override
	public void update(SettingManager settings, Dbms dbms) throws SQLException {
		Element harvesters = settings.get("harvesting", -1);
		@SuppressWarnings("unchecked")
		Iterator<Object> iter = harvesters.getDescendants();
		while(iter.hasNext()) {
			Object next = iter.next();
			if(next instanceof Element) {
				Element el = (Element) next;
				if(el.getName().equals("options")) {
					handleOption(settings, dbms, el);
				} else if(el.getName().equals("site")) {
					handleSite(settings, dbms, el);
				}
			}
		}
	}

	private void handleSite(SettingManager settings, Dbms dbms, Element el) throws SQLException {
		if((parentValue(el, "geonetwork") || parentValue(el, "geonetwork20")) && child(el,"host") != null) {
			Element host = child(el,"host");
			Element port = child(el, "port");
			Element servlet = child(el, "servlet");
			
			String value = "http://"+value(host)+":"+value(port)+"/"+value(servlet);
			settings.setValue(dbms, idPath(host), value);
			
			settings.remove(dbms, idPath(port));
			settings.remove(dbms, idPath(servlet));
		}
	}

	private String value(Element el) {
		return el.getChildText("value");
	}

	private Element child(Element el, String name) {
		return el.getChild("children").getChild(name);
	}

	private boolean parentValue(Element el, String string) {
		return el.getParentElement().getParentElement().getChildText("value").equals(string);
	}
	private void handleOption(SettingManager settings, Dbms dbms, Element el) throws SQLException {
		if(parentValue(el, "webdav") && child(el,"subtype") == null) {
			settings.add(dbms, idPath(el), "subtype", "webdav");
		}
	}

	private String idPath(Element el) {
		return "id:"+el.getAttributeValue("id");
	}
}
