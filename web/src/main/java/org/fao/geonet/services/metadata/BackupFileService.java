package org.fao.geonet.services.metadata;

import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
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

        new File(outDir).mkdirs();

        try {
            FileInputStream is = new FileInputStream(file);
            FileOutputStream os = new FileOutputStream(outFile);

            BinaryFile.copy(is, os, true, true);
        }
        catch(Exception e) {
            context.warning("Cannot backup mef file : "+e.getMessage());
            e.printStackTrace();
        }
        new File(file).delete();
    }
}
