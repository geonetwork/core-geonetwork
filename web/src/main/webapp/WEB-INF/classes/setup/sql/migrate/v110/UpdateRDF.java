package v110;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import jeeves.resources.dbms.Dbms;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;

public class UpdateRDF implements DatabaseMigrationTask {

	@Override
	public void update(SettingManager settings, Dbms dbms) throws SQLException {
		String dir = System.getProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY)+"/config/codelist/local/";
		Collection<File> files = FileUtils.listFiles(new File(dir), new String[] {"rdf"}, true);
		for(File f : files) {
			try {
				String updated = FileUtils.readFileToString(f, "UTF-8").
					replace("xml:lang=\"deu\"", "xml:lang=\"de\"").
					replace("xml:lang=\"fra\"", "xml:lang=\"fr\"").
					replace("xml:lang=\"ita\"", "xml:lang=\"it\"").
					replace("xml:lang=\"roh\"", "xml:lang=\"rm\"").
					replace("xml:lang=\"eng\"", "xml:lang=\"en\"");
				FileUtils.write(f, updated);
			} catch (IOException e) {
				throw new RuntimeException("Failed rdf update task", e);
			}
		}
	}

}
