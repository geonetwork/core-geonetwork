package org.fao.geonet.wro4j;

import com.google.common.annotations.VisibleForTesting;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.LessCssImportPreProcessor;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extends the importer so that block quotes are converted to // because those quotes break the importer.
 *
 * @author Jesse on 11/20/2014.
 */
@SupportedResourceType(ResourceType.CSS)
public class GeonetLessCssImport extends LessCssImportPreProcessor {
    public static final String ALIAS = "geonetLessCssImport";
    private final Pattern BLOCK_QUOTE = Pattern.compile("([^/]|^)/\\*.*?\\*/", Pattern.MULTILINE|Pattern.DOTALL);
    @Override
    protected String doTransform(String cssContent, List<Resource> foundImports) throws IOException {
        return super.doTransform(removeBlockComments(cssContent), foundImports);
    }

    @VisibleForTesting
    String removeBlockComments(String cssContent) {
        final Matcher matcher = BLOCK_QUOTE.matcher(cssContent);
        StringBuilder finalData = new StringBuilder();
        int start = 0;
        while (matcher.find()) {
            finalData.append(cssContent.substring(start, matcher.start()));
            finalData.append(matcher.group(1));
            start = matcher.end();
        }
        finalData.append(cssContent.substring(start, cssContent.length()));
        return finalData.toString();
    }
}
