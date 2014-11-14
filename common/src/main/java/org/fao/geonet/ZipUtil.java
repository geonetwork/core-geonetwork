package org.fao.geonet;

import de.schlichtherle.truezip.nio.file.TPath;
import org.fao.geonet.utils.IO;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * Zip or unzip files
 * 
 */
public class ZipUtil {
	/**
	 * Extracts a zip file to a specified directory.
	 * 
	 * @param zipFile
	 *            the zip file to extract
	 * @param toDir
	 *            the target directory
	 * @throws java.io.IOException
	 */
	public static void extract(FileSystem zipFile, Path toDir) throws IOException {
        Files.createDirectories(toDir);

        for (Path roots : zipFile.getRootDirectories()) {
            IO.copyDirectoryOrFile(roots, toDir.resolve(roots.getFileName()));
        }
	}

    /**
     * FileSystem must be closed when done.  This method should always be called in a try (resource) {} block
     */
    public static FileSystem openZipFs(Path path, boolean create) throws IOException, URISyntaxException {
        if (create) {
            TPath zipFile = new TPath(path);
            return zipFile.getFileSystem();
        } else {
            URI uri = new URI("jar:" + path.toUri());

            return FileSystems.newFileSystem(uri, Collections.singletonMap("create", "false"));
        }
    }
}
