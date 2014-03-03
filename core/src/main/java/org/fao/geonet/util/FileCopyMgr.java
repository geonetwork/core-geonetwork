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

import com.google.common.collect.FluentIterable;
import com.google.common.io.Files;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.Log;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.constants.Geonet;

public class FileCopyMgr {
    /**
     * Remove directory or file recursively
     * 
     * @param dir
     * @throws IOException
     */
    public static void removeDirectoryOrFile(File dir) {
        final FluentIterable<File> files = Files.fileTreeTraverser().postOrderTraversal(dir);
        for (File file : files) {
            if (!file.delete()) {
                Log.warning(Geonet.MEF, "Unable to delete file: "+file);
            }
        }
    }
}
// ==============================================================================
