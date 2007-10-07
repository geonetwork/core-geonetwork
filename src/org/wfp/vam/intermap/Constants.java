package org.wfp.vam.intermap;

public class Constants {
	// Request elements
	public static final String MAP_SERVICE_ID = "id";
	public static final String CONTEXT_ID = "id";
	public static final String MAP_X = "map.x";
	public static final String MAP_Y = "map.y";
	public static final String ZOOM_FACTOR = "factor";
	public static final String ACTIVE_LAYER = "activeLayer";
	public static final String IDENTIFY_FORMAT = "format";
	public static final String TRANSPARENCY = "transparency";

	// response Elements
	public static final String URL = "url";

	// www map servers configuration file
	public static final String MAP_SERVERS_CONFIG = "mapServers";
	public static final String MAP_SERVER_TYPES = "types";
	public static final String MAP_CONTEXT_ID = "id";
	public static final String MAP_SERVER_ID = "id";
	public static final String MAP_SERVERS = "mapServers";
	public static final String MAP_CONTEXTS = "mapContexts";
	public static final String MAP_DEFAULTCONTEXT = "default";
	public static final String MAP_LAYER = "layer";
	public static final String MAP_SERVER_TYPE = "type";
	public static final String MAP_SERVER_URL = "url";
	public static final String MAP_SERVICE = "service";
	public static final String MAP_CONTEXT = "context";
	public static final String MAP_TOOL = "tool";

	// AXL Requests configuration
	public static final String AXL_CONFIG = "axlRequests";

	// WmsService configuration
//	public static final String WMS_CONFIG = "wmsTransform";

	// Proxy server configuration
	public static final String USE_PROXY = "useProxy";
	public static final String PROXY_HOST = "proxyHost";
	public static final String PROXY_PORT = "proxyPort";

	// Temporary files
	public static final String TEMP_DIR = "tempDir";
	public static final String TEMP_DELETE = "tempDeleteMinutes";
	public static final String TEMP_URL = "tempUrl";

	// Cache files
	public static final String CACHE_DIR = "httpCacheDir";
	public static final String CACHE_DELETE = "httpCacheDeleteEvery";
	public static final String USE_CACHE = "useCache";

	// Screen DPI
	public static final String DPI = "screenDpi";

	// Output format for map images
	public static final String FILE_FORMAT = "fileFormat";

	// Output format for GetFeatureInfo
	public static final String FORMAT_GML="application/vnd.ogc.gml";
	public static final String FORMAT_XHTML="text/xhtml";
	public static final String FORMAT_HTML="text/html";
	public static final String FORMAT_PLAIN="text/plain";

	// Session
	public static final String SESSION_MAP = "map";
	public static final String SESSION_TOOL = "tool";
	public static final String SESSION_SIZE = "size";

	// Temporary files
	public static final String TMP_DIR = "tempDir";
	public static final String TMP_URL = "tempUrl";

	// Map parameters
	public static final int DEFAULT_WIDTH = 300;
	public static final int DEFAULT_HEIGHT = 300;
	public static final int BIGGER_WIDTH = 450;
	public static final int BIGGER_HEIGHT = 450;
	public static final int PRINT_WIDTH = 600;
	public static final int PRINT_HEIGHT = 600;
	public static final int PIXEL_TOLERANCE = 4;

	// Map tools
	public static final String DEFAULT_TOOL = "zoomIn";

	// Banner urls

	public static final String BANNER_URL_LOGIN    = "user.login.form";
	public static final String BANNER_URL_ADMIN    = "admin";
	public static final String BANNER_URL_TOOLS    = "tools.html";
	public static final String BANNER_URL_LINKS    = "links.html";
	public static final String BANNER_URL_FEEDBACK = "feedback.html";
	public static final String BANNER_URL_HELP     = "help.html";
}
