package org.fao.geonet.services.metadata.format;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
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
abstract class AbstractFormatService {
    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    protected void checkLegalId(String paramName, String xslid) throws BadParameterEx {
        if(!FormatterConstants.ID_XSL_REGEX.matcher(xslid).matches()) {
            throw new BadParameterEx(paramName, "Only the following are permitted in the id"+ FormatterConstants.ID_XSL_REGEX);
        }
    }
	protected String getMetadataSchema(Element params, ServiceContext context) throws Exception {
		String metadataId = Utils.getIdentifierFromParameters(params, context);
		DataManager dm = context.getBean(DataManager.class);
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
    protected File getAndVerifyFormatDir(GeonetworkDataDirectory dataDirectory, String paramName, String xslid, File schemaDir) throws BadParameterEx, IOException {
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

        File userXslDir = dataDirectory.getFormatterDir();
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
        
        if (!containsFile(userXslDir, formatDir)) {
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
            final boolean xslViewExists = new File(file, VIEW_XSL_FILENAME).exists();
            final boolean groovyViewExists = new File(file, VIEW_GROOVY_FILENAME).exists();
            boolean viewFileExists = xslViewExists || groovyViewExists;
            return file.isDirectory() && viewFileExists;
        }
    }
    
}
