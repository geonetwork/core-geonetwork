package org.fao.geonet.services.metadata.format.groovy.template;

import org.fao.geonet.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * One of the results the closure of a file handler can return.  It will load a file and replace all ${key} values (where key is in the
 * substitutions map) with the value from the substitutions map.  Keys may not contain { or }.
 *
 * @author Jesse on 10/16/2014.
 */
public class FileResult {
    private final TNode template;
    private final Map<String, Object> substitutions;

    public FileResult(TNode template, Map<String, Object> substitutions) {
        this.template = template;
        this.substitutions = substitutions;
    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(out, substitutions);
        try {
            template.render(context);
            return out.toString(Constants.ENCODING);
        } catch (IOException e) {
            throw new TemplateException(e);
        }
    }
}
