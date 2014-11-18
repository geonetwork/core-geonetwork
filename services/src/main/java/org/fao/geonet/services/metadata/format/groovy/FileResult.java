package org.fao.geonet.services.metadata.format.groovy;

import java.nio.file.Path;
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
    private final String template;
    private final Map<String, Object> substitutions;
    private final Path file;

    public FileResult(Path file, String template, Map<String, Object> substitutions) {
        this.file = file;
        this.template = template;
        this.substitutions = substitutions;
    }

    @Override
    public String toString() {
        final Matcher matcher = KEY_PATTERN.matcher(template);
        StringBuilder finalData = new StringBuilder();
        int start = 0;
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = this.substitutions.get(key);
            if (value == null) {
                value = "";
            }
            finalData.append(template.substring(start, matcher.start()));
            finalData.append(value);
            start = matcher.end();
        }
        finalData.append(template.substring(start, template.length()));
        return finalData.toString();
    }
}
