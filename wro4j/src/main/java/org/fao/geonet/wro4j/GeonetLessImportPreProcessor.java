package org.fao.geonet.wro4j;

import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.support.LessCssImportInspector;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Geonetwork implementation for importing stylesheets.
 *
 * User: Jesse
 * Date: 12/2/13
 * Time: 3:29 PM
 */
public class GeonetLessImportPreProcessor
        extends CssImportPreProcessor {

    public static final String ALIAS = "geonetLessCssImport";
    private static final String LESS_EXT = ".less";
    private static final String CSS_EXT = ".css";

    @Override
    protected List<String> findImports(final String css) {
        final List<String> imports = new LessCssImportInspector(css).findImports();
        return imports;
    }

    @Override
    protected String removeImportStatements(final String cssContent) {
        return new LessCssImportInspector(cssContent).removeImportStatements();
    }
}
