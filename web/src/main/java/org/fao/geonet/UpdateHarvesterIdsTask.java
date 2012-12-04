package org.fao.geonet;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jeeves.resources.dbms.Dbms;

import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

public class UpdateHarvesterIdsTask implements DatabaseMigrationTask {

	@Override
	public void update(SettingManager settings, Dbms dbms) throws SQLException {

		final int MIN_HARVEST_ID = 100000;
		Element result = dbms
				.select("select * from Settings where parentid = 2 and id < "
						+ MIN_HARVEST_ID);

		if (result.getChildren().size() > 0) {
			dbms.execute("SELECT * INTO Harvester FROM Settings WHERE parentid = 2");
			int changed = result.getChildren().size();
			while(changed > 0) {
				changed = dbms.execute("INSERT INTO Harvester SELECT * FROM Settings WHERE parentid IN (SELECT id FROM Harvester) AND NOT id IN (SELECT id FROM Harvester)");
			}

			dbms.execute("DELETE FROM Settings WHERE id IN (SELECT id FROM Harvester)");
			int newHarvesterBaseIndex = Math.max(max(dbms), 100000);
			@SuppressWarnings("unchecked")
			List<Element> harvesterValues = dbms.select(
					"SELECT * from Harvester").getChildren();

			Map<Integer, Integer> updates = new HashMap<Integer, Integer>();
			updates.put(2,2);  //Add harvesting node parentid to id map
			for (Element element : harvesterValues) {
				int id = Integer.parseInt(element.getChildText("id"));
				newHarvesterBaseIndex++;
				updates.put(id, newHarvesterBaseIndex);
			}
			StringBuilder builder = new StringBuilder("INSERT INTO Settings VALUES ");
			for (Element element : harvesterValues) {
				int id = Integer.parseInt(element.getChildText("id"));
				int parentid = Integer.parseInt(element.getChildText("parentid"));
				String name = element.getChildText("name");
				String value = element.getChildText("value");
				builder.append("(")
					.append(updates.get(id))
					.append(',')
					.append(updates.get(parentid))
					.append(",'")
					.append(name)
					.append("','")
					.append(value)
					.append("'),");
			}
			builder.deleteCharAt(builder.length()-1);
			dbms.execute(builder.toString());
		}

	}

	private int max(Dbms dbms) throws SQLException {
		Element result = dbms.select("SELECT max(id) FROM Settings");
		Element maxEl = (Element) result.getChildren().get(0);
		String maxText = ((Element) maxEl.getChildren().get(0)).getText();
		return Integer.parseInt(maxText);
	}

}
