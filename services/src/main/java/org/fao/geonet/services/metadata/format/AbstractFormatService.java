package org.fao.geonet.services.metadata.format;

import org.fao.geonet.exceptions.BadParameterEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Common constants and methods for Metadata formatter classes
 * 
 * @author jeichar
 */
abstract class AbstractFormatService implements Service {
    protected static final String USER_XSL_DIR = "user_xsl_dir";
    protected static final Pattern ID_XSL_REGEX = Pattern.compile("[\\w0-9\\-_]+");
    protected static final String VIEW_XSL_FILENAME = "view.xsl";

    protected volatile String userXslDir;
    protected volatile boolean initializedDir;

    public void init(String appPath, ServiceConfig params) throws Exception
    {
        userXslDir = params.getMandatoryValue(USER_XSL_DIR);
        if(!userXslDir.endsWith(File.separator)) {
            userXslDir = userXslDir + File.separator;
        }
        
        Log.info(Geonet.DATA_DIRECTORY, "Custom Metadata format XSL directory set to initial value of: "+userXslDir);
    }

    protected void ensureInitializedDir(ServiceContext context) throws IOException {
        if (!initializedDir) {
            synchronized (this) {
                if (!initializedDir) {
                    if (!new File(userXslDir).isAbsolute()) {
                        String systemDataDir = context.getBean(GeonetworkDataDirectory.class).getSystemDataDir();

                        if (!systemDataDir.endsWith(File.separator)) {
                            systemDataDir = systemDataDir + File.separator;
                        }
                        userXslDir = systemDataDir + "data" + File.separator + userXslDir;
                    }
                    IO.mkdirs(new File(userXslDir), "Formatter directory");

                    Log.info(Geonet.DATA_DIRECTORY, "Final Custom Metadata format XSL directory set to: " + userXslDir);

                    initializedDir = true;
                }
            }
        }
    }

    protected void checkLegalId(String paramName, String xslid) throws BadParameterEx {
        if(!ID_XSL_REGEX.matcher(xslid).matches()) {
            throw new BadParameterEx(paramName, "Only the following are permitted in the id"+ID_XSL_REGEX);
        }
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
    protected static boolean containsFile(File container, File desiredFile) throws IOException {
        String canonicalDesired = desiredFile.getCanonicalPath();
        String canonicalContainer = container.getCanonicalPath();
        return canonicalDesired.startsWith(canonicalContainer);
    }
    protected File getAndVerifyFormatDir(String paramName, String xslid) throws BadParameterEx, IOException {
        if (xslid == null) {
            throw new BadParameterEx(paramName, "missing "+paramName+" param");
        }
        
        checkLegalId(paramName, xslid);
        File formatDir = new File(userXslDir + xslid);
        
        if(!formatDir.exists()) {
            throw new BadParameterEx(paramName, "Format bundle "+xslid+" does not exist");
        }
        
        if(!formatDir.isDirectory()) {
            throw new BadParameterEx(paramName, "Format bundle "+xslid+" is not a directory");
        }
        
        if(!new File(formatDir, VIEW_XSL_FILENAME).exists()) {
            throw new BadParameterEx(paramName, "Format bundle "+xslid+" is not a valid format bundle because it does not have a "+VIEW_XSL_FILENAME+" file");
        }
        
        if (!containsFile(new File(userXslDir), formatDir)) {
            throw new BadParameterEx(paramName, "Format bundle "+xslid+" is not a format bundle id because it does not reference a file contained within the userXslDir");
        }
        return formatDir;
    }

    protected static class FormatterFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() && new File(file, VIEW_XSL_FILENAME).exists();
        }
    }
    
}
