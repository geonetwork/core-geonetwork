package org.fao.geonet.services.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.test.TestConfig;
import org.jdom.Element;
import org.jdom.JDOMException;

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

		if (loginFirst)
			sendLogin(c);

		final GetMethod m = new GetMethod(Utils.geonetworkUri + urlWithGetParameter);
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
		File mefFile = new File(TestConfig.getResourcesPath() + "/" + fileName);
		
		
		// File parameters
		ArrayList<Part> parts = new ArrayList<Part>();
		parts.add(new StringPart("file_type", (fileName.endsWith(".zip") || fileName.endsWith(".mef")?"mef":"single")));
		parts.add(new FilePart("mefFile", mefFile));
		
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
//				System.out.println("mef.import file: " + fileName
//						+ " - response: " + r.trim());

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
}
