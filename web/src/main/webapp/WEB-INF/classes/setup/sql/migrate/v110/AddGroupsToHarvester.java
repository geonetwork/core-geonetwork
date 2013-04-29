package v110;

import java.sql.SQLException;
import java.util.List;

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
		update(element, settings, "owner", dbms);
		dbms.commit();
	}

	@SuppressWarnings("unchecked")
	private void update(Element element, SettingManager settings,
			String setting, Dbms dbms) throws SQLException {
		try {
			List<Element> sites = (List<Element>) jeeves.utils.Xml.selectNodes(element, "*//site[not(//"+setting+")]");

			for (Element site : sites) {
				String id = site.getAttributeValue("id");
				Log.info(Geonet.HARVESTER, "Added owner to harvester id:"+id);
				settings.add(dbms, "id:"+id, setting, "1");
			}

		} catch (JDOMException e) {
			throw new RuntimeException(e);
		}
	}

}
