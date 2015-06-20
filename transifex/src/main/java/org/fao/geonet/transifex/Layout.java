package org.fao.geonet.transifex;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The layout of the translation files.
 * @author Jesse on 6/18/2015.
 */
public enum Layout {
    /**
     * The translation files are all in the same directory and have a 2 letter language code as the prefix of the translation file name.
     * EG en-core.json.
     *
     * The path for this layout should be the path to the directory that contains all the prefixed file.
     */
    PREFIX {
        @Override
        public Path getFile(String path, String filename, String lang) {
            String stem = filename;
            if (filename.startsWith("en-" + filename)) {
                stem = filename.substring(3);
            }
            return Paths.get(path + "/" + lang + "-" + stem);
        }
    },
    /**
     * The translation files are in a directory with the 3 character language code.
     *
     * The path for this layout should be the path to the directory that contains all the language directories.
     */
    DIR {
        @Override
        public Path getFile(String path, String filename, String lang) {
            return Paths.get(path + "/" + lang + "/" + filename);
        }
    };

    public abstract Path getFile(String path, String filename, String lang);
}
