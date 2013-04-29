package v110;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;

import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import org.jdom.JDOMException;

public class AddGroupsToHarvester implements DatabaseMigrationTask {

	@Override
	public void update(SettingManager settings, Dbms dbms) throws SQLException {
		Element element = settings.get("harvesting", -1);
		Map<String, Object> values = new HashMap<String, Object>();
		update(element, values, "owner");
		Log.info(Geonet.HARVESTER, "Added owners to settings: \n"+values);
		settings.setValues(dbms, values);
	}

	@SuppressWarnings("unchecked")
	private void update(Element element, Map<String, Object> values,
			String setting) {
		try {
			List<Element> sites = (List<Element>) jeeves.utils.Xml.selectNodes(element, "*//site[not(//"+setting+")]");

			for (Element site : sites) {
				String id = site.getAttributeValue("id");
				values.put("id:"+id+"/"+setting, "1");
			}

		} catch (JDOMException e) {
			throw new RuntimeException(e);
		}
	}

}
