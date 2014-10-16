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

import static org.fao.geonet.services.metadata.format.FormatterConstants.VIEW_GROOVY_FILENAME;
import static org.fao.geonet.services.metadata.format.FormatterConstants.VIEW_XSL_FILENAME;

/**
 * Common constants and methods for Metadata formatter classes
 * 
 * @author jeichar
 */
abstract class AbstractFormatService implements Service {

    protected volatile String userXslDir;
    protected volatile boolean initializedDir;

    public void init(String appPath, ServiceConfig params) throws Exception
    {
        userXslDir = params.getMandatoryValue(FormatterConstants.USER_XSL_DIR);
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
        if(!FormatterConstants.ID_XSL_REGEX.matcher(xslid).matches()) {
            throw new BadParameterEx(paramName, "Only the following are permitted in the id"+ FormatterConstants.ID_XSL_REGEX);
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
        File canonicalDesired = desiredFile.getCanonicalFile();
        final File canonicalContainer = container.getCanonicalFile();
        while (canonicalDesired.getParentFile()!= null && !canonicalDesired.getParentFile().equals(canonicalContainer)) {
            canonicalDesired = canonicalDesired.getParentFile();
        }

        return canonicalContainer.equals(canonicalDesired.getParentFile());
    }
    protected File getAndVerifyFormatDir(String paramName, String xslid, File schemaDir) throws BadParameterEx, IOException {
        if (xslid == null) {
            throw new BadParameterEx(paramName, "missing "+paramName+" param");
        }
        
        checkLegalId(paramName, xslid);
        File formatDir = null;
        if (schemaDir != null && schemaDir.exists()) {
            formatDir = new File(new File(schemaDir, FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR), xslid);
            if (!formatDir.exists()) {
                formatDir = null;
            }
        }

        if (formatDir == null) {
            formatDir = new File(userXslDir, xslid);
        }

        if(!formatDir.exists()) {
            throw new BadParameterEx(paramName, "Format bundle "+xslid+" does not exist");
        }
        
        if(!formatDir.isDirectory()) {
            throw new BadParameterEx(paramName, "Format bundle "+xslid+" is not a directory");
        }
        
        if(!new File(formatDir, VIEW_XSL_FILENAME).exists() &&
                !new File(formatDir, VIEW_GROOVY_FILENAME).exists()) {
            throw new BadParameterEx(paramName,
                    "Format bundle "+xslid+" is not a valid format bundle because it does not have a '"+
                    VIEW_XSL_FILENAME+"' file or a '" + VIEW_GROOVY_FILENAME + "' file.");
        }
        
        if (!containsFile(new File(userXslDir), formatDir)) {
            if (schemaDir == null || !containsFile(new File(schemaDir, FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR), formatDir)) {
                throw new BadParameterEx(paramName,
                        "Format bundle " + xslid + " is not a format bundle id because it does not reference a " +
                        "file contained within the userXslDir");
            }
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
