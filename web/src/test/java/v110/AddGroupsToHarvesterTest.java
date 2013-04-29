package v110;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Xml;

import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import org.junit.Test;

public class AddGroupsToHarvesterTest {

	@Test
	public void test() throws Exception {
		AddGroupsToHarvester task = new AddGroupsToHarvester();
		Dbms dbms = mock(Dbms.class);
		SettingManager settings = mock(SettingManager.class);
		Element settingsXml = Xml.loadFile(AddGroupsToHarvesterTest.class.getResource("sampleSettings.xml"));
		when(settings.get(anyString(), anyInt())).thenReturn(settingsXml);

		task.update(settings , dbms);
		
		verify(settings).add(dbms, "id:1003", "owner", "1");

	}

}
