//==============================================================================
//===
//===   CopyFiles
//===
//==============================================================================
//===	Lifted from the net  - 
//=== http://forum.java.sun.com/thread.jspa?threadID=328293&messageID=1334818
//==============================================================================

package org.fao.geonet.util;

import java.io.File;
import java.io.IOException;

import jeeves.utils.BinaryFile;
import jeeves.utils.Log;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.constants.Geonet;

public class FileCopyMgr {
    public static void copyFiles(String strPath, String dstPath) throws IOException {

        File src = new File(strPath);
        File dest = new File(dstPath);
        BinaryFile.copy(src, dest);
    }

    /**
     * Remove directory or file recursively
     * 
     * @param dir
     * @throws IOException
     */
    public static void removeDirectoryOrFile(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            Log.warning(Geonet.GEONETWORK, "A failure occured while trying to delete: " + dir.getAbsolutePath(), e);
        }
    }
}
// ==============================================================================
