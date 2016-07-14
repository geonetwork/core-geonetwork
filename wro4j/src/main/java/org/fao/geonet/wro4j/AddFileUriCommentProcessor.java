package org.fao.geonet.wro4j;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ImportAware;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * This processor is only to be enabled when the maven env variable is in dev mode because it adds
 * debug information to the css and javascript files that assist in debugging the css and
 * javascript.  See dev.properties, prod.properties and inspire.properties and wro.properties.
 * <p/>
 * This class adds a comment to js and css files with the name of the resource so that it is
 * possible to identify in the grouped "views" of wro4j which file contains the code to change.  In
 * the case of js it adds a comment of the form
 * <code><pre>{@value org.fao.geonet.wro4j.AddFileUriCommentProcessor#START_JS_COMMENT}</pre></code>
 * <p/>
 * with a similar tag at the end of the javascript.
 * <p/>
 * In the case of css it is a little trickier because of 2 factors. <ul> <li>imports - css imports
 * are resolved before minification and as such the minification would remove the comment</li>
 * <li>less - the less compiler removes comments as well and again the comment cannot be used</li>
 * </ul> <p></p> The strategy used for css and less us as follows <ol> <li>Add:
 * <code><pre>#start_{resourcename} {text-align:center}</pre></code>
 * to the start of the file. </li> <li>Add:
 * <code><pre>#end_{resourcename} {text-align:center}</pre></code>
 * to the end of the file. </li> <li>Search file for all #start_{xyz} {text-align:center} parts and
 * whitespace is added around the block. This is done because they are added by the import tags and
 * the minified code would have removed all whitespace around the css block White space is added to
 * make it easier to read the minified code.
 * <p/>
 * </li>
 * <p/>
 * </ol> User: Jesse Date: 12/2/13 Time: 3:29 PM
 */
public class AddFileUriCommentProcessor implements ResourcePreProcessor, ImportAware {
    public static final String ALIAS = "addFileUriComment";
    protected static final String START_JS_COMMENT = "/* ---------------  Wro4j Start %s  --------------- */";
    protected static final String END_JS_COMMENT = "/* ---------------  Wro4j End %s --------------- */";
    protected static final String START_CSS_BLOCK = "#______WRO4J_START___ { background-image: url('%s'); }";
    protected static final String END_CSS_BLOCK = "#______WRO4J_END____ { background-image: url('%s'); }";
    private static final int MAX_FILE_LENGTH = 50;

    @Override
    public void process(Resource resource, Reader reader, Writer writer) throws IOException {
        String uri = resource.getUri();
        if (uri.startsWith(ClosureDependencyUriLocator.URI_PREFIX)) {
            IOUtils.copy(reader, writer);
        } else {
            if (resource.getUri().length() > MAX_FILE_LENGTH) {
                uri = "..." + uri.substring(uri.length() - MAX_FILE_LENGTH);
            }
            switch (resource.getType()) {
                case JS:
                    addJavascriptComment(reader, writer, uri);
                    break;
                case CSS:
                    addCssBlock(reader, writer, uri);
                    break;
                default:
                    break;
            }
        }
    }

    private void addCssBlock(Reader reader, Writer writer, String uri) throws IOException {
        writer.write(String.format(START_CSS_BLOCK, uri));
        writer.write("\n\n\n\n");
        IOUtils.copy(reader, writer);
        writer.write("\n\n\n\n");
        writer.write(String.format(END_CSS_BLOCK, uri));
        writer.write("\n");

    }

    public void addJavascriptComment(Reader reader, Writer writer, String uri) throws IOException {
        writer.write(String.format(START_JS_COMMENT, uri));
        writer.write("\n\n\n\n");
        IOUtils.copy(reader, writer);
        writer.write("\n\n\n\n");
        writer.write(String.format(END_JS_COMMENT, uri));
        writer.write("\n");
    }

    @Override
    public boolean isImportAware() {
        return true;
    }
}
