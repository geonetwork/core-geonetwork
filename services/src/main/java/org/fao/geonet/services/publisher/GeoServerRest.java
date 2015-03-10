//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.services.publisher;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.CheckReturnValue;

/**
 * This class uses GeoServer's management REST APIs for creating, updating and
 * deleting data or coverage store.
 * http://docs.geoserver.org/stable/en/user/extensions/rest/rest-config-api.html
 * 
 * 
 * Similar development have been discovered at the end of that proposal patch:
 * http://code.google.com/p/gsrcj/
 * http://code.google.com/p/toolboxenvironment/source
 * /browse/trunk/ArchivingServer
 * /src/java/it/intecs/pisa/archivingserver/chain/commands
 * /PublishToGeoServer.java
 * 
 */
public class GeoServerRest {

	public final static String METHOD_POST = "POST";
	public final static String METHOD_GET = "GET";
	public final static String METHOD_PUT = "PUT";
	public final static String METHOD_DELETE = "DELETE";
	public final static String LOGGER_NAME = "geonetwork.GeoServerRest";

	private String password;
	private String username;
	private String restUrl;
	private String baseCatalogueUrl;
	private String defaultWorkspace;
	private String response;
	private int status;

	private GeonetHttpRequestFactory factory;

	/**
	 * Create a GeoServerRest instance to communicate with a GeoServer node and
	 * a default namespace
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @param defaultns
	 * @param baseCatalogueUrl
	 *            TODO
	 */
	public GeoServerRest(GeonetHttpRequestFactory factory, String url, String username, String password,
			String defaultns, String baseCatalogueUrl) {
        this.restUrl = url;
        this.username = username;
        this.password = password;
        this.baseCatalogueUrl = baseCatalogueUrl;
        this.factory = factory;
        Log.createLogger(LOGGER_NAME);
        this.defaultWorkspace = defaultns;
	}


	/**
	 * @return Return last transaction response information if set.
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @return Last status information
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return The default workspace used
	 */
	public String getDefaultWorkspace() {
		return defaultWorkspace;
	}

	/**
	 * Retrieve layer (feature type or coverage) information. Use @see
	 * #getResponse() to get the message returned.
	 * 
	 * TODO : Add format ?
	 * 
	 * @param layer
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean getLayer(String layer) throws IOException {
		int status = sendREST(GeoServerRest.METHOD_GET, "/layers/" + layer
				+ ".xml", null, null, null, true);
		return status == 200;
	}

	public String getLayerInfo(String layer) throws IOException {
		if (getLayer(layer))
			return getResponse();
		else
			return null;
	}

	/**
	 * If the layer does not exist, return <code>false</code>
	 *
	 * @param layer
	 *            Name of the layer to delete
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean deleteLayer(String layer) throws IOException {
		int status = sendREST(GeoServerRest.METHOD_DELETE, "/layers/" + layer,
				null, null, null, true);
		// TODO : add force to remove ft, ds
		return status == 200;
	}

	/**
	 * Create a coverage from file
	 *
	 * @param ws
	 *            Name of the workspace to add the coverage in
	 * @param cs
	 *            Name of the coverage
	 * @param f
	 *            A zip or a geotiff {@link java.io.File} to updload.
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean createCoverage(String ws, String cs, Path f, String metadataUuid, String metadataTitle, String metadataAbstract)
			throws IOException {
		String contentType = "image/tiff";
		if (f.getFileName().toString().toLowerCase().endsWith(".zip")) {
			contentType = "application/zip";
		}
		int status = sendREST(GeoServerRest.METHOD_PUT, "/workspaces/" + ws
				+ "/coveragestores/" + cs + "/file.geotiff", null, f,
				contentType, false);

		createCoverageForStore(ws, cs, null, metadataUuid, metadataTitle, metadataAbstract);

		return status == 201;
	}

	/**
	 * Create a coverage from an external file or URL.
	 *
	 * @param ws
	 * @param cs
	 * @param file
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean createCoverage(String ws, String cs, String file, String metadataUuid, String metadataTitle, String metadataAbstract)
			throws IOException {
		String contentType = "image/tiff";
		String extension = "geotiff";

		if (GeoFile.fileIsECW(file)) {
			contentType = "image/ecw";
			extension = "ecw";
		}

		String type = "file";
		if (file.toLowerCase().endsWith(".zip")) {
			contentType = "application/zip";
		}
		// String extension = file.substring(file.lastIndexOf('.'),
		// file.length());
		if (file.startsWith("http://")) {
			type = "url";
		} else if (file.startsWith("file://")) {
			type = "external";
		}

		int status = sendREST(GeoServerRest.METHOD_PUT, "/workspaces/" + ws
				+ "/coveragestores/" + cs + "/" + type + "." + extension, file,
				null, contentType, false);

		createCoverageForStore(ws, cs, file, metadataUuid, metadataTitle, metadataAbstract);
		return status == 201;
	}

	private void createCoverageForStore(String ws, String cs, String file,
			String metadataUuid, String metadataTitle, String metadataAbstract)
			throws IOException {
		String xml = "<coverage><name>" + cs + "</name><title>"
			+ (metadataTitle != null ? metadataTitle : cs)
			+ "</title><enabled>true</enabled>"
			+ (file != null ? "<file>" + file + "</file>" : "")
			+ "<metadataLinks>"
				+ "<metadataLink>"
					+ "<type>text/xml</type>"
					+ "<metadataType>ISO19115:2003</metadataType>"
					+ "<content>"
						+ this.baseCatalogueUrl
						+ "/csw?SERVICE=CSW&amp;VERSION=2.0.2&amp;REQUEST=GetRecordById"
						+ "&amp;outputSchema=http://www.isotc211.org/2005/gmd"
						+ "&amp;ID=" + metadataUuid
					+ "</content>"
				+ "</metadataLink>"
				+ "<metadataLink>"
					+ "<type>text/html</type>"
					+ "<metadataType>TC211</metadataType>"
					+ "<content>"
						+ this.baseCatalogueUrl
						+ "/csw?SERVICE=CSW&amp;VERSION=2.0.2&amp;REQUEST=GetRecordById"
						+ "&amp;outputSchema=http://www.isotc211.org/2005/gmd"
						+ "&amp;ID=" + metadataUuid
					+ "</content>"
				+ "</metadataLink>"
				+ "<metadataLink>"
					+ "<type>text/html</type>"
					+ "<metadataType>TC211</metadataType>"
					+ "<content>"
						+ this.baseCatalogueUrl + "/home?uuid=" + metadataUuid
					+ "</content>"
				+ "</metadataLink>"
			+ "</metadataLinks>"
		+ "</coverage>";

		int statusCoverage = sendREST(GeoServerRest.METHOD_POST, "/workspaces/" + ws
				+ "/coveragestores/" + cs + "/coverages.xml", xml,
				null, "text/xml", false);

		checkResponseCode(statusCoverage);
	}

	/**
	 * Create a coverage from file in default workspace
	 *
	 * @param cs
	 * @param f
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean createCoverage(String cs, Path f, String metadataUuid, String metadataTitle, String metadataAbstract) throws IOException {
		// TODO : check default workspace is not null ?
		return createCoverage(getDefaultWorkspace(), cs, f, metadataUuid, metadataTitle, metadataAbstract);
	}

	/**
	 * Create a coverage from external file or URL in default workspace
	 *
	 * @param cs
	 * @param f
	 * @param metadataUuid TODO
	 * @param metadataTitle TODO
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean createCoverage(String cs, String f, String metadataUuid, String metadataTitle, String metadataAbstract) throws IOException {
		return createCoverage(getDefaultWorkspace(), cs, f, metadataUuid, metadataTitle, metadataAbstract);
	}

	/**
	 * @param ws
	 * @param ds
	 * @param f
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean updateCoverage(String ws, String ds, Path f, String metadataUuid, String metadataTitle, String metadataAbstract)
			throws IOException {
		return createCoverage(ws, ds, f, metadataUuid, metadataTitle, metadataAbstract);
	}

	/**
	 * @param ds
	 * @param f
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean updateCoverage(String ds, Path f, String metadataUuid, String metadataTitle, String metadataAbstract) throws IOException {
		return createCoverage(getDefaultWorkspace(), ds, f, metadataUuid, metadataTitle, metadataAbstract);
	}

	/**
	 *
	 * @param ws
	 *            Name of the workspace the coverage is in
	 * @param cs
	 *            Name of the coverage store
	 * @param c
	 *            Name of the coverage
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean deleteCoverage(String ws, String cs, String c)
			throws IOException {
		int status = sendREST(GeoServerRest.METHOD_DELETE, "/workspaces/" + ws
				+ "/coveragestores/" + cs + "/coverages/" + c, null, null,
				null, true);
		return status == 200;
	}

	/**
	 * Delete a coverage in default workspace
	 *
	 * @param cs
	 * @param c
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean deleteCoverage(String cs, String c) throws IOException {
		return deleteCoverage(getDefaultWorkspace(), cs, c);
	}

	/**
	 *
	 * @param ws
	 *            Name of the workspace to put the datastore in
	 * @param ds
	 *            Name of the datastore
	 * @param f
	 *            Zip {@link java.io.File} to upload containing a shapefile
	 * @param createStyle
	 *            True to create a default style @see
	 *            {@link #createStyle(String)}
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean createDatastore(String ws, String ds, Path f,
			boolean createStyle) throws IOException {
		int status = sendREST(GeoServerRest.METHOD_PUT, "/workspaces/" + ws
				+ "/datastores/" + ds + "/file.shp", null, f,
				"application/zip", false);

		if (createStyle) {
			createStyle(ds);
		}

		return status == 201;
	}

	public boolean createDatastore(String ws, String ds, String file,
			boolean createStyle) throws IOException {
		String type = "";
		String extension = file.substring(file.lastIndexOf('.'), file.length());
		if (file.startsWith("http://")) {
			type = "url";
		} else if (file.startsWith("file://")) {
			type = "external";
		}

		int status = sendREST(GeoServerRest.METHOD_PUT, "/workspaces/" + ws
				+ "/datastores/" + ds + "/" + type + extension, file, null,
				"text/plain", false);

		if (createStyle) {
			createStyle(ds);
		}

		return status == 201;
	}

	/**
	 * Create datastore in default workspace
	 *
	 * @param ds
	 * @param f
	 * @param createStyle
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean createDatastore(String ds, Path f, boolean createStyle)
			throws IOException {
		return createDatastore(getDefaultWorkspace(), ds, f, createStyle);
	}

	public boolean createDatastore(String ds, String file, boolean createStyle)
			throws IOException {
		return createDatastore(getDefaultWorkspace(), ds, file, createStyle);
	}

	/**
	 * Delete a datastore
	 *
	 * @param ws
	 * @param ds
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean deleteDatastore(String ws, String ds) throws IOException {
		int status = sendREST(GeoServerRest.METHOD_DELETE, "/workspaces/" + ws
				+ "/datastores/" + ds, null, null, null, true);
		return status == 200;
	}

	/**
	 * Delete a datastore in default workspace
	 *
	 * @param ds
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean deleteDatastore(String ds) throws IOException {
		return deleteDatastore(getDefaultWorkspace(), ds);
	}

	/**
	 * Delete a coverage store
	 *
	 * @param ws
	 * @param cs
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean deleteCoverageStore(String ws, String cs) throws IOException {
		int status = sendREST(GeoServerRest.METHOD_DELETE, "/workspaces/" + ws
				+ "/coveragestores/" + cs, null, null, null, true);
		return status == 200;
	}

/**
	 * Delete a coverage store in default workspace @see {@link #deleteCoverageStore(String, String)
	 * @param ds
	 * @return
	 * @throws IOException
	 */
	public boolean deleteCoverageStore(String ds) throws IOException {
		return deleteCoverageStore(getDefaultWorkspace(), ds);
	}

	public boolean deleteFeatureType(String ws, String ds, String ft)
			throws IOException {
		int status = sendREST(GeoServerRest.METHOD_DELETE, "/workspaces/" + ws
				+ "/datastores/" + ds + "/featuretypes/" + ft, null, null,
				null, true);
		return status == 200;
	}

	public boolean deleteFeatureType(String ds, String ft) throws IOException {
		return deleteFeatureType(getDefaultWorkspace(), ds, ft);
	}

	/**
	 * Create a default style for the layer named {layer}_style copied from the
	 * default style set by GeoServer (eg. polygon.sld for polygon).
	 *
	 * @param layer
	 * @return
	 */
	public int createStyle(String layer) {
		try {
			int status = sendREST(GeoServerRest.METHOD_GET, "/layers/" + layer
					+ ".xml", null, null, null, true);

			checkResponseCode(status);
			Element layerProperties = Xml.loadString(getResponse(), false);
			String styleName = layerProperties.getChild("defaultStyle")
					.getChild("name").getText();

			status = sendREST(GeoServerRest.METHOD_GET, "/styles/" + styleName
					+ ".sld", null, null, null, true);
            checkResponseCode(status);

			String currentStyle = getResponse();

			String body = "<style><name>" + layer + "_style</name><filename>"
					+ layer + ".sld</filename></style>";
			status = sendREST(GeoServerRest.METHOD_POST, "/styles", body, null,
					"text/xml", true);
            checkResponseCode(status);

			status = sendREST(GeoServerRest.METHOD_PUT, "/styles/" + layer
					+ "_style", currentStyle, null,
					"application/vnd.ogc.sld+xml", true);
            checkResponseCode(status);

			body = "<layer><defaultStyle><name>"
					+ layer
					+ "_style</name></defaultStyle><enabled>true</enabled></layer>";

			// Add the enable flag due to GeoServer bug
			// http://jira.codehaus.org/browse/GEOS-3964
			status = sendREST(GeoServerRest.METHOD_PUT, "/layers/" + layer,
					body, null, "text/xml", true);
            checkResponseCode(status);

		} catch (Exception e) {
			if(Log.isDebugEnabled(LOGGER_NAME))
				Log.debug(LOGGER_NAME, "Failed to create style for layer: "
					+ layer + ", error is: " + e.getMessage());
		}

		return status;
	}

	private void checkResponseCode(int status2) {
	    if(status2 > 399) {
	        Log.warning(Geonet.GEOPUBLISH, "Warning a bad response code to message was returned:"+status2);
	    }
    }

    public boolean createDatabaseDatastore(String ds, String host, String port,
			String db, String user, String pwd, String dbType, String ns)
			throws IOException {
		return createDatabaseDatastore(getDefaultWorkspace(), ds, host, port,
				db, user, pwd, dbType, ns);

	}

	public boolean createDatabaseDatastore(String ws, String ds, String host,
			String port, String db, String user, String pwd, String dbType,
			String ns) throws IOException {

		String xml = "<dataStore><name>" + ds
				+ "</name><enabled>true</enabled><connectionParameters><host>"
				+ host + "</host><port>" + port + "</port><database>" + db
				+ "</database><user>" + user + "</user><passwd>" + pwd
				+ "</passwd><dbtype>" + dbType + "</dbtype><namespace>" + ns
				+ "</namespace></connectionParameters></dataStore>";

		status = sendREST(GeoServerRest.METHOD_POST, "/workspaces/" + ws
				+ "/datastores", xml, null, "text/xml", true);

		return 201 == status;
	}

	public boolean createFeatureType(String ds, String ft, boolean createStyle,
			String metadataUuid, String metadataTitle, String metadataAbstract) throws IOException {
		return createFeatureType(getDefaultWorkspace(), ds, ft, createStyle,
				metadataUuid, metadataTitle, metadataAbstract);
	}

	public boolean createFeatureType(String ws, String ds, String ft,
			boolean createStyle, String metadataUuid, String metadataTitle, String metadataAbstract)
			throws IOException {
		String xml = "<featureType><name>" + ft + "</name><title>" + ft
				+ "</title>" + "</featureType>";

		status = sendREST(GeoServerRest.METHOD_POST, "/workspaces/" + ws
				+ "/datastores/" + ds + "/featuretypes", xml, null, "text/xml",
				true);

		xml = "<featureType><title>"
				+ (metadataTitle != null ? metadataTitle : ft)
				+ "</title><abstract>"
				+ (metadataAbstract != null ? metadataAbstract : ft)
				+ "</abstract><enabled>true</enabled>"
				+ "<metadataLinks>"
					+ "<metadataLink>"
						+ "<type>text/xml</type>"
						+ "<metadataType>ISO19115:2003</metadataType>"
						+ "<content>"
							+ this.baseCatalogueUrl
							+ "/csw?SERVICE=CSW&amp;VERSION=2.0.2&amp;REQUEST=GetRecordById"
							+ "&amp;outputSchema=http://www.isotc211.org/2005/gmd"
							+ "&amp;ID=" + metadataUuid
						+ "</content>"
					+ "</metadataLink>"
					+ "<metadataLink>"
						+ "<type>text/xml</type>"
						+ "<metadataType>TC211</metadataType>"
						+ "<content>"
							+ this.baseCatalogueUrl
							+ "/csw?SERVICE=CSW&amp;VERSION=2.0.2&amp;REQUEST=GetRecordById"
							+ "&amp;outputSchema=http://www.isotc211.org/2005/gmd"
							+ "&amp;ID=" + metadataUuid
						+ "</content>"
					+ "</metadataLink>"
					+ "<metadataLink>"
						+ "<type>text/html</type>"
						+ "<metadataType>TC211</metadataType>"
						+ "<content>"
							+ this.baseCatalogueUrl + "/home?uuid=" + metadataUuid
						+ "</content>"
					+ "</metadataLink>"
				+ "</metadataLinks>"
			+ "</featureType>";
		status = sendREST(GeoServerRest.METHOD_PUT, "/workspaces/" + ws
				+ "/datastores/" + ds + "/featuretypes/" + ft, xml, null,
				"text/xml", true);

		// Create layer for feature type (require for MapServer REST API)
		status = sendREST(GeoServerRest.METHOD_PUT, "/layers/" + ft, null, null,
				"text/xml", false);

		checkResponseCode(status);

		if (createStyle) {
			createStyle(ft);
		}

		return 201 == status;
	}

	/**
	 *
	 * @param method
	 *            e.g. 'POST', 'GET', 'PUT' or 'DELETE'
	 * @param urlParams
	 *            REST API parameter
	 * @param postData
	 *            XML data
	 * @param file
	 *            File to upload
	 * @param contentType
	 *            type of content in case of post data or file updload.
	 * @param saveResponse
	 * @return
	 * @throws java.io.IOException
	 */
	public @CheckReturnValue int sendREST(String method, String urlParams, String postData,
			Path file, String contentType, Boolean saveResponse)
			throws IOException {

		response = "";
		String url = this.restUrl + urlParams;
        if(Log.isDebugEnabled(LOGGER_NAME)) {
            Log.debug(LOGGER_NAME, "url:" + url);
            Log.debug(LOGGER_NAME, "method:" + method);
            Log.debug(LOGGER_NAME, "postData:" + postData);
        }

		HttpRequestBase m;
		if (method.equals(METHOD_PUT)) {
			m = new HttpPut(url);
			if (file != null) {
                ((HttpPut) m).setEntity(new InputStreamEntity(IO.newInputStream(file), Files.size(file),
                        ContentType.create(contentType, Constants.ENCODING)));
			}

			if (postData != null) {
                final StringEntity entity = new StringEntity(postData, ContentType.create(contentType, Constants.ENCODING));
                ((HttpPut) m).setEntity(entity);
			}
		} else if (method.equals(METHOD_DELETE)) {
			m = new HttpDelete(url);
		} else if (method.equals(METHOD_POST)) {
			m = new HttpPost(url);
			if (postData != null) {
                final StringEntity entity = new StringEntity(postData, ContentType.create(contentType, Constants.ENCODING));
				((HttpPost) m).setEntity(entity);
			}
		} else {
			m = new HttpGet(url);
		}

		if (contentType != null && !"".equals(contentType)) {
			m.setHeader("Content-type", contentType);
		}


		m.setConfig(RequestConfig.custom().setAuthenticationEnabled(true).build());

		// apparently this is needed to preemptively send the auth, for servers that dont require it but
		// dont send the same data if you're authenticated or not.
		try {
			m.addHeader(new BasicScheme().authenticate(new UsernamePasswordCredentials(username, password), m));
		} catch (AuthenticationException a) {
			Log.warning(LOGGER_NAME, "Failed to add the authentication Header, error is: " + a.getMessage());
		};

        final ClientHttpResponse httpResponse = factory.execute(m, new UsernamePasswordCredentials(username, password), AuthScope.ANY);

        try {
            status = httpResponse.getRawStatusCode();
            if(Log.isDebugEnabled(LOGGER_NAME)) {
                Log.debug(LOGGER_NAME, "status:" + status);
            }
            if (saveResponse) {
                this.response = IOUtils.toString(httpResponse.getBody());
            }
        }finally {
            httpResponse.close();
        }


		return status;
	}

}
