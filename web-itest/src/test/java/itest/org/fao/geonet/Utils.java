package itest.org.fao.geonet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jeeves.utils.Xml;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Ignore;

@Ignore
public class Utils {
	public static final String geonetworkUri = "http://localhost:8080/geonetwork/srv/en/";
	public static final String usernameAdmin = "admin";
	public static final String passwordAdmin = "admin";
	public static final String defaultGroup = "2";
	
	

	/**
	 * Utility method to send a GET request
	 * 
	 * @param urlWithGetParameter
	 * @return
	 * @throws IOException
	 */
	public static String sendRequest(String urlWithGetParameter, boolean loginFirst)
			throws IOException {
		final HttpClient c = new HttpClient();

		return sendRequest(urlWithGetParameter, loginFirst, c);
	}

	public static String sendRequest(String urlWithGetParameter, boolean loginFirst, HttpClient c)
	throws IOException {

		if (loginFirst)
			sendLogin(c);

		final GetMethod m = new GetMethod(Utils.geonetworkUri
				+ urlWithGetParameter);
		int status = c.executeMethod(m);
		if (status != 200) {
			throw new IOException("Error, got status code " + status);
		}
		return m.getResponseBodyAsString();
}
	
	/**
	 * Utility method to send a POST request
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static String sendRequest(String url, HashMap<String, String> params, boolean loginFirst)
			throws IOException, JDOMException {
		final HttpClient c = new HttpClient();
		return sendRequest(url, params, loginFirst, c);
	}
	
	public static String sendRequest(String url, HashMap<String, String> params, boolean loginFirst, HttpClient c)
	throws IOException, JDOMException {

		if (loginFirst)
			sendLogin(c);

		final PostMethod m = new PostMethod(Utils.geonetworkUri + url);
		
		// Add other parameters
		Iterator it = params.keySet().iterator();
		while (it.hasNext()){
		   String paramName = (String) it.next(); 
		   String paramValue = params.get(paramName);
		   m.addParameter(paramName, paramValue);
		}
		
		int status = c.executeMethod(m);
		if (status != 200) {
			throw new IOException("Error, got status code " + status);
		}
		return m.getResponseBodyAsString();
		//return Xml.loadString(m.getResponseBodyAsString(), false);
	}
	
	
	/**
	 * Login to current node
	 * 
	 * @param c
	 * @throws IOException
	 */
	public static void sendLogin(HttpClient c) throws IOException {
		final GetMethod login = new GetMethod(Utils.geonetworkUri
				+ "xml.user.login?username=" + Utils.usernameAdmin + "&password="
				+ Utils.passwordAdmin);
		int status = c.executeMethod(login);
		if (status != 200) {
			throw new IOException("Log in failed, got status code " + status);
		}
	}
	
	
	public static Element XMLOrMEFImport(String fileName, String paramName, String paramValue)
			throws IOException {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(paramName, paramValue);
		return XMLOrMEFImport(fileName, params);
	}
	
	/**
	 * Do an XML or a MEF import.
	 * 
	 * @param fileName
	 * @param params	Map of KVP
	 * @return
	 * @throws IOException
	 */
	public static Element XMLOrMEFImport(String fileName, HashMap<String, String> params)
			throws IOException {
		final HttpClient c = new HttpClient();

		Utils.sendLogin(c);

		// Post MEF
		final PostMethod m = new PostMethod(Utils.geonetworkUri + "mef.import");
		// FIXME File mefFile = new File(TestConfig.getResourcesPath() + "/" + fileName);
		
		
		// File parameters
		ArrayList<Part> parts = new ArrayList<Part>();
		parts.add(new StringPart("file_type", (fileName.endsWith(".zip") || fileName.endsWith(".mef")?"mef":"single")));
		// FIXME : parts.add(new FilePart("mefFile", mefFile));
		
		// Add other parameters
		Iterator it = params.keySet().iterator();
		while (it.hasNext()){
		   String paramName = (String) it.next(); 
		   String paramValue = params.get(paramName);
		   parts.add(new StringPart(paramName, paramValue));
		}
	
		Part[] partsTab = new Part[parts.size()];
		for (int i = 0; i < parts.size(); i++ )
           partsTab[i] = parts.get(i);

		m.setRequestEntity(new MultipartRequestEntity(partsTab, m.getParams()));

		int status = c.executeMethod(m);

		if (status != 200) {
			throw new IOException("got status code " + status);
		} else {
			try {
				String r = m.getResponseBodyAsString();
				Element response = Xml.loadString(r, false);
				return response;
			} catch (JDOMException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	

	/**
	 * Do a MEF export.
	 * 
	 * @param format
	 * @param loginFirst
	 *            TODO
	 * 
	 * @return List of ZIP entries.
	 * @throws IOException
	 */
	public static ArrayList<String> MEFExport(String uuid, String version,
			String format, boolean loginFirst) throws IOException {
		final HttpClient c = new HttpClient();

		if (loginFirst)
			sendLogin(c);

		String uri = Utils.geonetworkUri + "mef.export";
		final PostMethod m = new PostMethod(uri);
		m.setParameter("uuid", uuid);
		m.setParameter("format", format);
		if (version != null)
			m.setParameter("version", version);

		int status = c.executeMethod(m);

		if (status != 200) {
			throw new IOException("got status code " + status);
		} else {

			ArrayList<String> entries = new ArrayList<String>();
			try {
				ZipInputStream zis = new ZipInputStream(m
						.getResponseBodyAsStream());
				ZipEntry entry;

				try {
					while ((entry = zis.getNextEntry()) != null) {
						String fullName = entry.getName();
						entries.add(fullName);
						zis.closeEntry();
					}
				} finally {
					zis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
//			System.out.println("mef.export entries: " + entries.toString());
			return entries;
		}
	}

	/**
	 * Clean all catalogue content base on a default search.
	 * This will not remove templates.
	 * 
	 */
	public static void cleanCatalogue() {
		try {
			// Clean catalogue
			final HttpClient c = new HttpClient();
			Utils.sendRequest("xml.search", true, c);
			Utils.sendRequest("metadata.select?selected=add-all", false, c);
			Utils.sendRequest("metadata.batch.delete", false, c);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Add sample metadata without altering uuid, so test could check against sample's uuid
	 */
	public static void addSamples() {
		try {
			Utils.sendRequest("metadata.samples.add?uuidAction=nothing&file_type=mef&schema=csw-record,dublin-core,fgdc-std,iso19110,iso19115,iso19139", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}