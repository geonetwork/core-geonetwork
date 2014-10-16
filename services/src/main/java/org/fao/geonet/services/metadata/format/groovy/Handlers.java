package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Sets;
import groovy.lang.Closure;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Used for registering Element handlers and in many ways configuring the view.
 *
 * @author Jesse on 10/15/2014.
 */
public class Handlers {
    PriorityQueue<Handler> handlers = new PriorityQueue<Handler>();
    Set<String> roots = Sets.newHashSet();
    StartEndHandler startHandler = new StartEndHandler(null);
    StartEndHandler endHandler = new StartEndHandler(null);

    /**
     * Set the xpaths for selecting the roots for processing the metadata.
     *
     * Each xpath should be the type of xpath used when calling {@link org.fao.geonet.utils.Xml#selectNodes(org.jdom.Element, String, java.util.List)}
     */
    public void roots(String... xpaths) {
        this.roots.clear();
        this.roots.addAll(Arrays.asList(xpaths));
    }

    /**
     * Add a root xpath selector to the set of roots.
     *
     * The xpath should be the type of xpath used when calling {@link org.fao.geonet.utils.Xml#selectNodes(org.jdom.Element, String, java.util.List)}
     */
    public void root(String xpath) {
        this.roots.add(xpath);
    }

    /**
     * Add a handler with the priority 1 which will exactly match element name and prefix.
     *
     * @param elementName the qualified element name to match
     * @param function the handler closure/function
     */
    public Handler add(String elementName, Closure function) {
        final ByNameHandler handler = new ByNameHandler(Pattern.compile(Pattern.quote(elementName)), 1, function);
        handlers.add(handler);
        return handler;
    }

    /**
     * Add a handler with priority 0 which will do a regular expression match against the qualified element name to see if it applies
     * to the element.
     *
     * @param nameMatcher the regular expression to match.
     * @param function the handler closure/function
     */
    public Handler add(Pattern nameMatcher, Closure function) {
        final ByNameHandler handler = new ByNameHandler(nameMatcher, 0, function);
        handlers.add(handler);
        return handler;
    }
    /**
     * Add a handler with priority 0 which will do a regular expression match against full path from root to see if it applies
     * to the element.  Each segment of path will be separated by >
     *
     * @param pathMatcher the regular expression to match.
     * @param function the handler closure/function
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
     * @param matcher the regular expression to match.
     * @param function the handler closure/function
     */
    public Handler add(Closure matcher, Closure function) {
        final Handler handler = new FunctionMatchingHandler(matcher, 0, function);
        handlers.add(handler);
        return handler;
    }
    public StartEndHandler start(Closure function) {
        this.startHandler = new StartEndHandler(function);
        return this.startHandler;
    }
    public StartEndHandler end(Closure function) {
        this.endHandler = new StartEndHandler(function);
        return this.endHandler;
    }
}
