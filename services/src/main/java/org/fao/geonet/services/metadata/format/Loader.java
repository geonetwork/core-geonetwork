package org.fao.geonet.services.metadata.format;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.utils.Xml;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.Show;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Enumeration for loader type of the service metadata.formatter
 * You can specify the loader with the param loader=show in the url.
 * By default, the loader is SHOW which will use internal metadata.show service to retrieve the xml of the metadata.
 * You can specify the HTTP one, with the param "url" which will call an HTTP request to retrieve external xml of a metadata.
 *  
 * @author fgravin
 *
 */
public enum Loader {
		
		/**
		 * HTTP Enumeration
		 * 
		 * Used to retrieve the xml of the metadata through an external http request.
		 * Needs the url parameter in the formatter url (should be encoded):
		 * http://localhost:8080/geonetwork/srv/fre/metadata.formatter.html?xsl=default&loader=HTTP&url=http%3A%2F%2Flocalhost%3A8080%2Fgeonetwork%2Fsrv%2Ffre%2Fcsw%3FSERVICE%3DCSW%26VERSION%3D2.0.2%26REQUEST%3DGetRecordById%26ID%3D43484350-02cf-11e0-abec-005056987263%26outputschema%3Dcsw%3AIsoRecord%26ElementSetName%3Dfull
		 * 
		 * The url we be called and should return a valid metadata XML. You can specify which element from this result that will be inserted
		 * in the formatter output with the config properties loader.http.
		 * For example, GetRecordById response will gives <csw:GetRecordByIdResponse><gmd:MD_Metadata> while a metadata.show will return directly the <gmd:MD_Metadata> element as root.
		 * loader.http=* value will outpass the <csw:GetRecordByIdResponse> and insert <gmd:MD_Metadata> under the root element of the formatter output.
		 */
		HTTP {
			
			/**
			 * Basic namespace needed for the Xpath search (xpath is given by config property loader.http)
			 */
			private transient List<Namespace> theNSs;
			
			/**
			 * The value of the parameter "url" in the formatter url
			 */
			private String urlParam;
			
			public void init(Path appPath, ServiceConfig params) {
				theNSs = new ArrayList<Namespace>();
				theNSs.add(Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"));
				theNSs.add(Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork"));
				theNSs.add(Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd"));
				theNSs.add(Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco"));
			}
			
			public void validateParams(Element params) {
				urlParam = Util.getParam(params, Params.URL, null);
				if (urlParam == null) {
					throw new IllegalArgumentException(Params.URL + " is a required parameter if you use "+this.name()+" loader");
				}
			}
			
			public Element load(Element params, ConfigFile config, ServiceContext context) throws Exception {
				Element element = Xml.loadFile(new URL(urlParam));
				String xpath = config.loadLoaderRootPath(this.name());

				Element resElt = Xml.selectElement(element, xpath, theNSs);
				return (Element)resElt.detach();
			}
			
			public boolean isCompatibleXml(Element params, ConfigFile config,
					ServiceContext context) throws Exception { 
				List<String> applicable = config.listOfApplicableSchemas();
				return applicable.contains("all");
			}
			public Loader getValidLoader(final String url, Element params) throws Exception {
				validateParams(params);
				String xmlUrl = URLDecoder.decode(urlParam, "UTF-8").toLowerCase();
				
				URI baseUrl = new URI(url);
				URI paramUrl = new URI(xmlUrl);
				
				boolean sameserver = InetAddress.getByName(baseUrl.getHost()).equals(InetAddress.getByName(paramUrl.getHost()));
				boolean sameport = baseUrl.getPort() == paramUrl.getPort();
				boolean sameapp = paramUrl.getPath().startsWith(baseUrl.getPath());
				
				if(sameserver && sameport && sameapp) {
					String uuid = getParamsFromUrl(xmlUrl, Params.UUID);
					if (uuid == null) {
						uuid = getParamsFromUrl(xmlUrl, Params.ID);
					}
					params.addContent(new Element(Params.UUID).setText(uuid));
					return Loader.SHOW;
				} else {
					return this;
				}
			}
			
			private String getParamsFromUrl(final String url, final String param) {
				
				if (url.indexOf("?") > -1) {
					String paramaters = url.substring(url.indexOf("?") + 1);
					StringTokenizer paramGroup = new StringTokenizer(paramaters, "&");
	
					while (paramGroup.hasMoreTokens()) {
						StringTokenizer value = new StringTokenizer(paramGroup.nextToken(), "=");
						if(param.equals(value.nextToken().toLowerCase())) {
							return value.nextToken();
						}
					}
				}
				return null;
			}
		},
		
		/**
		 * SHOW enumeration
		 * Will be called by default if no or not valid loader param is specified.
		 * Show will call internal metadata.show service to retrieve the xmlo of the metadata.
		 * Need uuid or id param in the url.
		 */
		SHOW {
			
			private transient Show showService;
			
			public void init(Path appPath, ServiceConfig params) throws Exception {
				showService = new Show();
				showService.init(appPath, params);
			}
			
			public void validateParams(Element params) {
				
				String uuid = Util.getParam(params, Params.UUID, null);
				String id = Util.getParam(params, Params.ID, null);

				if (uuid == null && id == null) {
					throw new IllegalArgumentException("Either '" + Params.UUID
							+ "' or '" + Params.ID + " is a required parameter");
				}
			}
			
			public Element load(Element params, ConfigFile config, ServiceContext context) throws Exception {
				return showService.exec(params, context);
			}
			
			public boolean isCompatibleXml(Element params, ConfigFile config,
					ServiceContext context) throws Exception { 
				String schema = getMetadataSchema(params, context);
				List<String> applicable = config.listOfApplicableSchemas();
				return applicable.contains(schema) || applicable.contains("all");
			}
			
			protected String getMetadataSchema(Element params, ServiceContext context)
					throws Exception {
				String metadataId = Utils.getIdentifierFromParameters(params, context);
				GeonetContext gc = (GeonetContext) context
						.getHandlerContext(Geonet.CONTEXT_NAME);
				DataManager dm = gc.getBean(DataManager.class);
				String schema = dm.getMetadataSchema(metadataId);
				return schema;
			}
			public Loader getValidLoader(final String url, Element params) throws Exception {
				return this;
			}
		};
		

		public abstract void init(Path appPath, ServiceConfig params) throws Exception;
		public abstract void validateParams(Element params);
		
		/**
		 * Will retrieve the XML of the metadata depending on the loader
		 */
		public abstract Element load(Element params, ConfigFile config, ServiceContext context) throws Exception;
		
		/**
		 * Will check if the returned XML is a valid MD file
		 */
		public abstract boolean isCompatibleXml(Element params, ConfigFile config,
				ServiceContext context) throws Exception;
		
		/**
		 * Will check if the loader specified in the formatter url is the most designed one to be use.
		 * Loader.HTTP will check if the http url is not on the same host as the formatter, in this case, it will use
		 * internal service through Loader.SHOW
		 * This process can be activated or not with the config property loader.redirect
		 * @param url : the service url
		 */
		public abstract Loader getValidLoader(final String url, Element params) throws Exception;
		
		public static Loader fromString(final String name) {
			Loader l = null;
			try {
				l = Loader.valueOf(name.toUpperCase());
			}
			// if loader parameter is null or doesn't match with any enumeration
			catch(Exception e) {
				throw new IllegalArgumentException(Params.LOADER+" "+name+" is not a valid loader for the formatter");
			}
			return l;
		}
		
		public static Loader fromParam(Element params) {
			String urlParam = Util.getParam(params, Params.URL, null);
			Loader l = null;
			if(urlParam == null) {
				l = Loader.SHOW;
			}
			else {
				l = Loader.HTTP;
			}
			return l;
		}
	}