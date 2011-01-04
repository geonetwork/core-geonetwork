package itest.org.fao.geonet.guiservices.sampledata;

import itest.org.fao.geonet.Utils;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

public class AddTest extends TestCase {
	
	@Test
	public void testAddSampleData (){
		Utils.cleanCatalogue();
		
		try {
			String response = Utils.sendRequest("metadata.samples.add?uuidAction=nothing&file_type=mef&schema=csw-record,dublin-core,fgdc-std,iso19110,iso19115,iso19139", true);
			assertTrue(response.contains("<response status=\"true\""));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			Utils.cleanCatalogue();
		}
	}
}
