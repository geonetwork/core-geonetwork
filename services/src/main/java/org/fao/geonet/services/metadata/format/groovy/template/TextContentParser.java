package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses text to extract sections that need to be replaced with a value from the model.
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TextContentParser {
    final static Pattern INTERPOLATION_PATTERN = Pattern.compile("\\{\\{(\\s*[^}]+\\s*)\\}\\}");

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @VisibleForTesting
    Map<String, TextContentFilter> filters = Maps.newHashMap();

    public TextBlock parse(String text) {
        TextBlock textBlock = new TextBlock();

        final Matcher matcher = INTERPOLATION_PATTERN.matcher(text);

        int start = 0;
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            textBlock.add(new TextContentStatic(text.substring(start, matcher.start())));

            List<TextContentFilter> applicableFilters = Lists.newArrayList();
            final String[] parts = key.split("\\|");
            key = parts[0];
            for (int i = 1; i < parts.length; i++) {
                String filterKey = parts[i].trim();
                TextContentFilter filter = this.filters.get(filterKey);
                if (filter == null) {
                    throw new TemplateException("There is no TextContentFilter with the id: '" + filterKey + "'");
                }

                applicableFilters.add(filter);
            }
            textBlock.add(new TextContentReplacement(key, applicableFilters));

            start = matcher.end();
        }

        textBlock.add(new TextContentStatic(text.substring(start, text.length())));

        return textBlock;
    }
}
