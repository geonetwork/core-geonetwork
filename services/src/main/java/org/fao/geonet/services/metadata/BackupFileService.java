package org.fao.geonet.services.metadata;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

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
    protected void backupFile(ServiceContext context, String id, String uuid, Path file) {
        Path outDir = Lib.resource.getRemovedDir(context, id);
        Path outFile;
        try {
            // When metadata records contains character not supported by filesystem
            // it may be an issue. eg. acri-st.fr/96443
            outFile = outDir.resolve(URLEncoder.encode(uuid, Constants.ENCODING) +".mef");
        } catch (UnsupportedEncodingException e1) {
            outFile = outDir.resolve(uuid +".mef");
        }


        try {
            Files.createDirectories(outDir);
            try (InputStream is = IO.newInputStream(file);
                 OutputStream os = Files.newOutputStream(outFile)) {

                BinaryFile.copy(is, os);
            }
        }
        catch(Exception e) {
            context.warning("Cannot backup mef file : "+e.getMessage());
            e.printStackTrace();
        }
        IO.deleteFile(file, false, Geonet.MEF);
    }
}
