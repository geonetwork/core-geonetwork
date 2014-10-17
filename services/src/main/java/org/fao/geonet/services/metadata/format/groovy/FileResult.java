package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.io.Files;
import org.fao.geonet.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One of the results the closure of a file handler can return.  It will load a file and replace all ${key} values (where key is in the
 * substitutions map) with the value from the substitutions map.  Keys may not contain { or }.
 *
 * @author Jesse on 10/16/2014.
 */
public class FileResult {
    private static final Pattern KEY_PATTERN = Pattern.compile("\\$\\{([^{}]+)}");
    private final File file;
    private final Map<String, Object> substitutions;

    public FileResult(File file, Map<String, Object> substitutions) {
        this.file = file;
        this.substitutions = substitutions;
    }

    @Override
    public String toString() {
        try {
            final String data = Files.toString(file, Charset.forName(Constants.ENCODING));
            final Matcher matcher = KEY_PATTERN.matcher(data);
            StringBuilder finalData = new StringBuilder();
            int start = 0;
            while (matcher.find()) {
                String key = matcher.group(1);
                Object value = this.substitutions.get(key);
                if (value == null) {
                    throw new IllegalArgumentException("A substitution ${" + key + "} was found in " + this.file
                                                       + " but no replacement was found in the substitutions map");
                }
                finalData.append(data.substring(start, matcher.start()));
                finalData.append(value);
                start = matcher.end();
            }
            finalData.append(data.substring(start, data.length()));
            return finalData.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
