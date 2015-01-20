package org.fao.geonet.utils;

import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Holder the information required by {@link org.fao.geonet.utils.NioPathAwareCatalogResolver}
 * and {@link org.fao.geonet.utils.NioPathAwareEntityResolver} for resolving paths.
 *
 * @author Jesse on 11/4/2014.
 */
public class NioPathHolder {
    private static final ThreadLocal<Path> ACTUAL_RELATIVE_TO = new InheritableThreadLocal<>();
    private static final ThreadLocal<Path> SYS_ID_RELATIVE_TO = new InheritableThreadLocal<>();

    static void setBase(Path base) {
        if (base != null) {
            ACTUAL_RELATIVE_TO.set(base.getParent());
            SYS_ID_RELATIVE_TO.set(new File(".").getAbsoluteFile().toPath().getParent());
        } else {
            ACTUAL_RELATIVE_TO.set(null);
            SYS_ID_RELATIVE_TO.set(null);
        }
    }

    static InputSource resolveEntity(String publicId, String systemId) throws IOException {
        Path resource = resolveResource(publicId, systemId);
        if (resource != null) {
            return new PathInputSource(resource);
        }
        return null;
    }

    public static Path resolveResource(String publicId, String systemId) {
        if (ACTUAL_RELATIVE_TO.get() != null) {
            if (systemId.startsWith("file:/") || systemId.startsWith(ACTUAL_RELATIVE_TO.get().toUri().getScheme())) {
                try {
                    Path srcPath = Paths.get(new URI(systemId));
                    final Path relativePath = SYS_ID_RELATIVE_TO.get().relativize(srcPath);
                    Path finalPath = ACTUAL_RELATIVE_TO.get().resolve(relativePath.toString());
                    if (Files.isRegularFile(finalPath)) {
                        return finalPath;
                    }
                } catch (URISyntaxException e) {
                    // failed
                }
            } else {
                Path srcPath = ACTUAL_RELATIVE_TO.get().resolve(systemId);
                if (Files.isRegularFile(srcPath)) {
                    return srcPath;
                }
            }
        }

        return null;
    }
}
