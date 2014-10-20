package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.util.slurpersupport.GPathResult;
import org.fao.geonet.services.metadata.format.FormatterParams;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Used for registering Element handlers and in many ways configuring the view.
 *
 * @author Jesse on 10/15/2014.
 */
public class Handlers {
    private static final String HANDLER_SELECT = "select";
    private static final String HANDLER_PRIORITY = "priority";
    private static final String HANDLER_PROCESS_CHILDREN = "processChildren";

    private final File formatterDir;
    private final File schemaDir;
    private final File rootFormatterDir;
    private final FormatterParams fparams;
    private final TemplateCache templateCache;
    final List<Handler> handlers = Lists.newArrayList();
    final Set<String> roots = Sets.newHashSet();
    StartEndHandler startHandler = new StartEndHandler(null);
    StartEndHandler endHandler = new StartEndHandler(null);

    public Handlers(FormatterParams fparams, File schemaDir, File rootFormatterDir,
                    TemplateCache templateCache) {
        this.fparams = fparams;
        this.formatterDir = fparams.formatDir;
        this.schemaDir = schemaDir;
        this.rootFormatterDir = rootFormatterDir;
        this.templateCache = templateCache;
    }

    /**
     * Set the xpaths for selecting the roots for processing the metadata.
     * <p/>
     * Each xpath should be the type of xpath used when calling {@link org.fao.geonet.utils.Xml#selectNodes(org.jdom.Element, String,
     * java.util.List)}
     */
    public void roots(String... xpaths) {
        this.roots.clear();
        this.roots.addAll(Arrays.asList(xpaths));
    }

    /**
     * Add a root xpath selector to the set of roots.
     * <p/>
     * The xpath should be the type of xpath used when calling {@link org.fao.geonet.utils.Xml#selectNodes(org.jdom.Element, String,
     * java.util.List)}
     */
    public void root(String xpath) {
        this.roots.add(xpath);
    }

    /**
     * Add a handler with the priority 1 which will exactly match element name and prefix.
     *
     * @param elementName the qualified element name to match
     * @param function    the handler closure/function
     */
    public Handler add(String elementName, Closure function) {
        final HandlerNameMatch handler = new HandlerNameMatch(Pattern.compile(Pattern.quote(elementName)), 1, function);
        handlers.add(handler);
        return handler;
    }

    public boolean canHandle(GPathResult elem) {
        for (Handler handler : handlers) {
            if (handler.canHandle(TransformationContext.getContext(), elem)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add a handler with priority 0 which will do a regular expression match against the qualified element name to see if it applies
     * to the element.
     *
     * @param nameMatcher the regular expression to match.
     * @param function    the handler closure/function
     */
    public Handler add(Pattern nameMatcher, Closure function) {
        final HandlerNameMatch handler = new HandlerNameMatch(nameMatcher, 0, function);
        handlers.add(handler);
        return handler;
    }

    /**
     * Add a handler with priority 0 which will do a regular expression match against full path from root to see if it applies
     * to the element.  Each segment of path will be separated by >
     *
     * @param pathMatcher the regular expression to match.
     * @param function    the handler closure/function
     */
    public Handler withPath(Pattern pathMatcher, Closure function) {
        final PathMatchingHandler handler = new PathMatchingHandler(pathMatcher, 0, function);
        handlers.add(handler);
        return handler;
    }

    /**
     * Add a handler with priority 0 which will do a execute the matcher function with the current element to see if it the handler
     * should be applied to the element.
     *
     * @param matcher  the regular expression to match.
     * @param function the handler closure/function
     */
    public Handler add(Closure matcher, Closure function) {
        final Handler handler = new FunctionMatchingHandler(matcher, 0, function);
        handlers.add(handler);
        return handler;
    }

    /**
     * Create a handler from a map of properties to values.  Allowed properties are:
     * <ul>
     *   <li>{@link #HANDLER_SELECT} - <strong>(Required)</strong> One of:
     *   <ul>
     *      <li>a function for determining if this handler should be applied</li>
     *      <li>a string for matching against the name</li>
     *      <li>a regular expression for matching against the path</li>
     *   </ul>
     *   </li>
     *   <li>
     *       {@link #HANDLER_PRIORITY} - <strong>(Optional)</strong> handlers with a higher priority will be evaluated before
     *       handlers with a lower priority
     *   </li>
     *   <li>
     *     {@link #HANDLER_PROCESS_CHILDREN} - <strong>(Optional)</strong> if true the handler function takes at least 3 parameters
     *     then all children of this node will be processed and that data passed to the function for use by the handler
     *   </li>
     * </ul>
     */
    public Handler add(Map<String, Object> properties, Closure handlerFunction) {
        Object matcher = null;
        int priority = 0;
        boolean processChildren = false;
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(HANDLER_SELECT)) {
                matcher = entry.getValue();
            } else if (entry.getKey().equalsIgnoreCase(HANDLER_PRIORITY)) {
                priority = Integer.parseInt(entry.getValue().toString());
            } else if (entry.getKey().equalsIgnoreCase(HANDLER_PROCESS_CHILDREN)) {
                processChildren = Boolean.parseBoolean(entry.getValue().toString());
            } else {
                throw new IllegalArgumentException("Handler's do not have a configurable property: " + entry.getKey() + " value = " +
                                                   entry.getValue());
            }
        }

        if (matcher == null) {
            throw new IllegalArgumentException("A property " + HANDLER_SELECT + " must be present in the properties map");
        }

        final Handler handler;
        if (matcher instanceof Closure) {
            handler = add((Closure) matcher, handlerFunction);
        } else if (matcher instanceof String || matcher instanceof GString) {
            handler = add(matcher.toString(), handlerFunction);
        } else if (matcher instanceof Pattern) {
            handler = withPath((Pattern) matcher, handlerFunction);
        } else {
            throw new IllegalArgumentException(
                    "The property " + HANDLER_SELECT + " is not a legal type.  Legal types are: Closure/function or String or " +
                    "Regular Expression(Pattern) but was " + matcher.getClass());
        }
        handler.setPriority(priority);
        handler.setProcessChildren(processChildren);

        return handler;
    }

    /**
     * Create a FileResult object as a result from a handler function.
     * See {@link org.fao.geonet.services.metadata.format.groovy.FileResult}
     *
     * File resolution is done by searching:
     * <ul>
     *     <li>formatterDir/path</li>
     *     <li>schemaFormatterDir/path</li>
     *     <li>rootFormatterDir/path</li>
     * </ul>
     * @param path The relative path to the file to load.
     * @param substitutions the key -> substitution String/GString map of substitutions.
     */
    public FileResult fileResult (String path, Map<String, Object> substitutions) throws IOException {
        return this.templateCache.createFileResult(this.formatterDir, this.schemaDir, this.rootFormatterDir, path, substitutions);
    }

    /**
     * Set the start handler.
     * @param function the function executed at the start of the transformation.
     */
    public StartEndHandler start(Closure function) {
        this.startHandler = new StartEndHandler(function);
        return this.startHandler;
    }

    /**
     * Set the end handler.
     * @param function the function executed at the end of the transformation.
     */
    public StartEndHandler end(Closure function) {
        this.endHandler = new StartEndHandler(function);
        return this.endHandler;
    }
}
