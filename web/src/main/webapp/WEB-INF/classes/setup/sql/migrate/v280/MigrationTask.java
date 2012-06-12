package v280;

import java.sql.SQLException;
import java.util.List;

import jeeves.resources.dbms.Dbms;

import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.lib.DatabaseType;
import org.jdom.Element;

public class MigrationTask implements DatabaseMigrationTask {

	@Override
	public void update(Dbms dbms) throws SQLException {
		@SuppressWarnings("unchecked")
		List<Element> results = dbms.select("SELECT * FROM settings WHERE name='options' AND parentid IN (SELECT id FROM settings WHERE name='node' AND value='webdav') AND NOT id IN (SELECT parentid FROM settings WHERE name='subtype');").getChildren();
		if(!results.isEmpty()) {
			int max = max(dbms);
			for (Element element : results) {
				max += 1;
				String id = element.getChildText("id");
				dbms.execute("INSERT INTO Settings VALUES ("+max+","+id+",'subtype','webdav')");
			}
		}
	}

	private int max(Dbms dbms) throws SQLException {
		Element result = dbms.select("SELECT max(id) FROM Settings");
		Element maxEl = (Element) result.getChildren().get(0);
		String maxText = ((Element) maxEl.getChildren().get(0)).getText();
		return Integer.parseInt(maxText);
	}

}
