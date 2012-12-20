package v110;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.GeonetworkDataDirectory;
import org.junit.Test;

public class UpdateRDFTest {

	@Test
	public void test() throws SQLException, IOException {
		
		File file = new File(getClass().getResource("/corrupt-keywords.rdf").getFile());
		File fileToUpdate = new File(file.getParentFile(), "config/codelist/local/_none_/corrupt-keywords.rdf");
		fileToUpdate.getParentFile().mkdirs();
		FileUtils.copyFile(file, fileToUpdate);
		System.setProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY, file.getParent() );
		new UpdateRDF().update(null, null);
		
		String updatedData = FileUtils.readFileToString(fileToUpdate, "UTF-8");
		
		assertFalse(updatedData.contains("xml:lang=\"ita\""));
		assertFalse(updatedData.contains("xml:lang=\"deu\""));
		assertFalse(updatedData.contains("xml:lang=\"fra\""));
		assertFalse(updatedData.contains("xml:lang=\"eng\""));
		assertFalse(updatedData.contains("xml:lang=\"roh\""));

		assertTrue(updatedData.contains("xml:lang=\"rm\""));
		assertTrue(updatedData.contains("xml:lang=\"it\""));
		assertTrue(updatedData.contains("xml:lang=\"en\""));
		assertTrue(updatedData.contains("xml:lang=\"de\""));
		assertTrue(updatedData.contains("xml:lang=\"fr\""));

	}

}
