package org.fao.geonet.services.metadata;

import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.IO;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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
        String outFile= outDir + uuid +".mef";


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
