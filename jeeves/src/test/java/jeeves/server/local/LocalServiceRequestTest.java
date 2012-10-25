package jeeves.server.local;

import static org.junit.Assert.*;

import org.junit.Test;

public class LocalServiceRequestTest {

	@Test
	public void testParseLocalURLNoLangNoParams() throws Exception {
		LocalServiceRequest req = LocalServiceRequest.create("local://csw");
		assertNoLangNoParams(req);
	}
	@Test
    public void testParseLocalURLNoLangURLWithLangInParams() throws Exception {
        LocalServiceRequest req = LocalServiceRequest
                .create("local://csw?thesaurus=external._none_.gemet&id=http%3A//www.eionet.europa.eu/gemet/concept/13275&locales=DE,FR,IT,EN");
        assertNull("Expected Null but got: " + req.getLanguage(), req.getLanguage());
        assertEquals("csw", req.getService());
        assertEquals("request", req.getParams().getName());
        assertEquals(3, req.getParams().getChildren().size());
    }
	@Test
	public void testParseRelativeURLNoLangNoParams() throws Exception {
		assertNoLangNoParams(LocalServiceRequest.create("/csw"));
	}
	@Test
	public void testParseRelative2URLNoLangNoParams() throws Exception {
		assertNoLangNoParams(LocalServiceRequest.create("csw"));
	}
	private void assertNoLangNoParams(LocalServiceRequest req) {
		assertNull("Expected Null but got: " +req.getLanguage(), req.getLanguage());
		assertEquals("csw", req.getService());
		assertEquals("request", req.getParams().getName());
		assertEquals(0, req.getParams().getChildren().size());
	}
	
	
	@Test
	public void testParseLocalURLLangNoParams() throws Exception {
		LocalServiceRequest req = LocalServiceRequest.create("local://eng/csw");
		assertLangNoParams(req);
	}
	@Test
	public void testParseRelativeURLLangNoParams() throws Exception {
		assertLangNoParams(LocalServiceRequest.create("/eng/csw"));
	}
	@Test
	public void testParseRelative2URLLangNoParams() throws Exception {
		assertLangNoParams(LocalServiceRequest.create("eng/csw"));
	}
	private void assertLangNoParams(LocalServiceRequest req) {
		assertEquals("eng",req.getLanguage());
		assertEquals("csw", req.getService());
		assertEquals("request", req.getParams().getName());
		assertEquals(0, req.getParams().getChildren().size());
	}

	@Test
	public void testParseLocalURLLangParams() throws Exception {
		assertLangParams(LocalServiceRequest.create("local://deu/csw?version=1.0.0"));
	}
	@Test
	public void testParseRelativeURLLangParams() throws Exception {
		assertLangParams(LocalServiceRequest.create("/deu/csw?version=1.0.0"));
	}
	@Test
	public void testParseRelative2URLLangParams() throws Exception {
		assertLangParams(LocalServiceRequest.create("deu/csw?version=1.0.0"));
	}
	private void assertLangParams(LocalServiceRequest req) {
		assertEquals("deu",req.getLanguage());
		assertEquals("csw", req.getService());
		assertEquals("request", req.getParams().getName());
		assertEquals(1, req.getParams().getChildren().size());
		assertEquals("1.0.0", req.getParams().getChildText("version"));
	}

}
