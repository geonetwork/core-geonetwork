package org.fao.geonet.wro4j;

import org.apache.commons.io.IOUtils;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.UriLocator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.servlet.ServletContext;

/**
 * Converts a resource to a closure goog.addDependency statement.
 * <p/>
 * Created by fgravin on 2/17/14.
 */
public class TemplatesUriLocator implements UriLocator {

	public static final String URI_PREFIX = "template://";
	public static final String URI_PREFIX_HEADER = "template://header";
	public static final String URI_PREFIX_FOOTER = "template://footer";
	public static final String URI_LOCATOR_ID = "templateURILocator";

	@Override
    public InputStream locate(String uri) throws IOException {
	    StringBuilder javascript;
    	if(uri.startsWith(URI_PREFIX_HEADER)) {
    		javascript = getHeader();
    	}
    	else if(uri.startsWith(URI_PREFIX_FOOTER)) {
    		javascript = getFooter();
    	} else {
    		javascript = new StringBuilder();
    		final String realPath;
        	final String path = uri.substring(URI_PREFIX.length());
        	final ServletContext servletContext = Context.get().getServletContext();
            if(servletContext != null) {
            	realPath = servletContext.getRealPath(path);
            }
            else {
            	realPath = path;
            }

            // Check to avoid NullPointerException
            if (realPath == null) {
                return new ByteArrayInputStream(javascript.toString().getBytes("UTF-8"));
            }

            File folder = new File(realPath);
            File[] files = folder.listFiles();
            if(files != null) {
                for(int i=0;i<files.length;++i) {

                    if(files[i].isDirectory()) {
                        break;
                    }
                    BufferedReader br = null;
                    StringBuilder template = null;

                    String sCurrentLine;
                    template = new StringBuilder();
                    final Reader reader = new InputStreamReader(new FileInputStream(files[i]), "UTF-8");
                    try {
                        br = new BufferedReader(reader);
                        while ((sCurrentLine = br.readLine()) != null) {
                            template.append(sCurrentLine);
                        }

                        String sTemplate = template.toString();
                        sTemplate = sTemplate.replaceAll(">\\s*<", "><");
                        sTemplate = sTemplate.replaceAll("\\s\\s+", " ");
                        sTemplate = sTemplate.replaceAll("\n", "");
                        sTemplate = sTemplate.replace("'", "\\'");

                        javascript.append(
                                String.format("$templateCache.put('%s', '%s');",
                                        "../.." + path.replace('\\','/') + '/' + files[i].getName(),
                                        sTemplate));
                    } finally {
                        IOUtils.closeQuietly(reader);
                    }
                }

            }
    	}
        return new ByteArrayInputStream(javascript.toString().getBytes("UTF-8"));
    }

	private StringBuilder getHeader() {
		StringBuilder javascript = new StringBuilder();
		javascript.append("(function() {")
				.append("angular.module('gn').run(['$templateCache', function($templateCache) {");
		return javascript;
	}

	private StringBuilder getFooter() {
		StringBuilder javascript = new StringBuilder();
		javascript.append("}]);").append("})();");
		return javascript;
	}

	@Override
	public boolean accept(String uri) {
		return uri.startsWith(URI_PREFIX);
	}
}
