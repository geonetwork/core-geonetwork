package org.fao.geonet.services.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;

/**
 * @author heikki doeleman
 */
public abstract class BackupFileService extends NotInReadOnlyModeService {

    /**
     *
     * @param context
     * @param id
     * @param uuid
     * @param file
     */
    protected void backupFile(ServiceContext context, String id, String uuid, String file) {
        String outDir = Lib.resource.getRemovedDir(context, id);
        String outFile;
        try {
            // When metadata records contains character not supported by filesystem
            // it may be an issue. eg. acri-st.fr/96443
            outFile = outDir + URLEncoder.encode(uuid, Constants.ENCODING) +".mef";
        } catch (UnsupportedEncodingException e1) {
            outFile = outDir + uuid +".mef";
        }


        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            IO.mkdirs(new File(outDir), "The backup file directory");
            is = new FileInputStream(file);
            os = new FileOutputStream(outFile);

            BinaryFile.copy(is, os);
        }
        catch(Exception e) {
            context.warning("Cannot backup mef file : "+e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
        IO.delete(new File(file), false, Geonet.MEF);
    }
}
