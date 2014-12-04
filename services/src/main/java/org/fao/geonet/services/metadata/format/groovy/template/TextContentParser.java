package org.fao.geonet.services.metadata.format.groovy.template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses text to extract sections that need to be replaced with a value from the model.
 *
 * @author Jesse on 11/29/2014.
 */
public class TextContentParser {
    final static Pattern INTERPOLATION_PATTERN = Pattern.compile("\\{\\{(\\s*[^}]+\\s*)\\}\\}");

    public TextBlock parse(String text) {
        TextBlock textBlock = new TextBlock();

        final Matcher matcher = INTERPOLATION_PATTERN.matcher(text);

        int start = 0;
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            textBlock.add(new TextContentStatic(text.substring(start, matcher.start())));
            textBlock.add(new TextContentReplacement(key));

            start = matcher.end();
        }

        textBlock.add(new TextContentStatic(text.substring(start, text.length())));

        return textBlock;
    }
}
