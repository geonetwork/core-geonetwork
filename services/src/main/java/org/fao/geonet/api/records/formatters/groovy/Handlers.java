/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.formatters.groovy;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.util.ClosureComparator;
import groovy.util.slurpersupport.GPathResult;

import org.fao.geonet.api.records.formatters.FormatterParams;
import org.fao.geonet.api.records.formatters.groovy.template.FileResult;
import org.fao.geonet.api.records.formatters.groovy.template.TemplateCache;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Used for registering Element handlers and in many ways configuring the view.
 *
 * @author Jesse on 10/15/2014.
 */
public class Handlers {
    static final String HANDLER_SELECT = "select";

    private final Path formatterDir;
    private final Path schemaDir;
    private final Path rootFormatterDir;
    private final TemplateCache templateCache;
    TransformEngine transformationEngine = new TransformEngine(this);
    StartEndHandler startHandler = new StartEndHandler(null);
    StartEndHandler endHandler = new StartEndHandler(null);
    private Set<String> roots = Sets.newHashSet();
    private List<SkipElement> skipElements = Lists.newArrayList();
    private Callable<Iterable<Object>> functionRoots = null;
    /**
     * ModeId -> Mode mapping
     */
    private Map<String, Mode> modes = Maps.newHashMap();
    /**
     * ModeId -> Sorter mapping
     */
    private ListMultimap<String, Sorter> sorters = ArrayListMultimap.create();
    /**
     * ModeId -> Handler mapping
     */
    private ListMultimap<String, Handler> handlers = ArrayListMultimap.create();

    public Handlers(FormatterParams fparams, Path schemaDir, Path rootFormatterDir,
                    TemplateCache templateCache) {
        this.formatterDir = fparams.formatDir;
        this.schemaDir = schemaDir;
        this.rootFormatterDir = rootFormatterDir;
        this.templateCache = templateCache;
    }

    /**
     * Executed once when transformer is created.  This locks down the handler configuration so that
     * it is not modifiable any more.
     */
    public void prepareForTransformer() {
        for (String id : this.handlers.keySet()) {
            Collections.sort(this.handlers.get(id));
        }
        this.handlers = Multimaps.unmodifiableListMultimap(this.handlers);
        for (String id : this.sorters.keySet()) {
            Collections.sort(this.sorters.get(id));
        }
        this.sorters = Multimaps.unmodifiableListMultimap(this.sorters);
        this.modes = Collections.unmodifiableMap(this.modes);
        this.roots = Collections.unmodifiableSet(this.roots);
    }

    /**
     * Set the xpaths for selecting the roots for processing the metadata.
     * <p/>
     * Each xpath should be the type of xpath used when calling {@link
     * org.fao.geonet.utils.Xml#selectNodes(org.jdom.Element, String, java.util.List)}
     */
    public void roots(Object... xpaths) {
        this.roots.clear();
        this.functionRoots = null;
        for (Object xpath : xpaths) {
            this.roots.add(xpath.toString());
        }
    }


    /**
     * Set the xpaths for selecting the roots for processing the metadata.
     * <p/>
     * Each xpath should be the type of xpath used when calling {@link
     * org.fao.geonet.utils.Xml#selectNodes(org.jdom.Element, String, java.util.List)}
     */
    @SuppressWarnings("unchecked")
    public void roots(Closure rootFunction) {
        this.roots.clear();
        this.functionRoots = rootFunction;
    }

    public Set<String> getRoots() throws Exception {
        final HashSet<String> allRoots = Sets.newHashSet(this.roots);
        if (this.functionRoots != null) {
            Iterable<Object> fromFunc = this.functionRoots.call();
            for (Object root : fromFunc) {
                allRoots.add(root.toString());
            }
        }
        return allRoots;
    }

    /**
     * Add a root xpath selector to the set of roots.
     * <p/>
     * The xpath should be the type of xpath used when calling {@link org.fao.geonet.utils.Xml#selectNodes(org.jdom.Element,
     * String, java.util.List)}
     */
    public void root(Object xpath) {
        this.roots.add(xpath.toString());
    }

    /**
     * Add an "skip element".  A skip element is an element that shouldn't be processed but some of
     * its children should be.  For example a formatter for iso19139 may not want to show the
     * informationInfo elements but does want to display its children. The match can be
     * gmd:informationInfo and the childSelector can be the closure {it.children()}
     *
     * @param select        a string which is the element name of the element to skip or a closure
     *                      that takes an element and returns boolean
     * @param childSelector a closure that returns the children to process.  The element selected by
     *                      'select' will be passed to the closure
     */
    public SkipElement skip(Object select, Closure childSelector) {
        return skip(select, childSelector, 0);
    }

    /**
     * Add an "skip element".  A skip element is an element that shouldn't be processed but some of
     * its children should be.  For example a formatter for iso19139 may not want to show the
     * informationInfo elements but does want to display its children. The match can be
     * gmd:informationInfo and the childSelector can be the closure {it.children()}
     *
     * @param select        a string which is the element name of the element to skip or a closure
     *                      that takes an element and returns boolean
     * @param childSelector a closure that returns the children to process.  The element selected by
     *                      'select' will be passed to the closure
     * @param priority      priority of the skipElement
     */
    public SkipElement skip(Object select, Closure childSelector, int priority) {
        final SkipElement skipElement;
        if (select instanceof String) {
            final Pattern pattern = Pattern.compile(Pattern.quote((String) select));
            skipElement = new SkipElementPattern(pattern, priority, childSelector);
        } else if (select instanceof GString) {
            final GString gString = (GString) select;
            final Pattern pattern = Pattern.compile(Pattern.quote(gString.toString()));
            skipElement = new SkipElementPattern(pattern, priority, childSelector);
        } else if (select instanceof Closure) {
            Closure closure = (Closure) select;
            skipElement = new SkipElementClosure(closure, priority, childSelector);
        } else {
            throw new IllegalArgumentException("select must either be a string or a closure but was a: " + select.getClass());
        }

        this.skipElements.add(skipElement);
        return skipElement;
    }

    /**
     * Create a handler from a map of properties to values.  There must be a 'select' attribute
     * which can be: <ul> <li>a function for determining if this handler should be applied</li>
     * <li>a string for matching against the name</li> <li>a regular expression for matching against
     * the path</li> </ul>
     * <p/>
     * In addition any JavaBean properties on the handler maybe set using the correct JavaBean
     * semantics. For example: priority, processChildren
     */
    public SkipElement skip(Map<String, Object> properties, Closure handlerFunction) {
        Object select = properties.get(HANDLER_SELECT);

        if (select == null) {
            throw new IllegalArgumentException("A property " + HANDLER_SELECT + " must be present in the properties map");
        }

        final SkipElement handler;
        if (select instanceof Closure) {
            handler = skip(select, handlerFunction);
        } else if (select instanceof String || select instanceof GString) {
            handler = skip(select.toString(), handlerFunction);
        } else if (select instanceof Pattern) {
            handler = skip(select, handlerFunction);
        } else {
            throw new IllegalArgumentException(
                "The property " + HANDLER_SELECT + " is not a legal type.  Legal types are: Closure/function or String or " +
                    "Regular Expression(Pattern) but was " + select.getClass());
        }
        handler.configure(properties);

        return handler;
    }

    /**
     * Add a handler with the priority 1 which will exactly match element name and prefix.
     *
     * @param elementName the qualified element name to match
     * @param function    the handler closure/function
     */
    public Handler add(String elementName, Closure function) {
        final HandlerNameSelect handler = new HandlerNameSelect(Pattern.compile(Pattern.quote(elementName)), 1, function);
        addHandler(handler);
        return handler;
    }

    /**
     * Add a handler with priority 0 which will do a regular expression match against the qualified
     * element name to see if it applies to the element.
     *
     * @param namePattern the regular expression to match.
     * @param function    the handler closure/function
     */
    public Handler add(Pattern namePattern, Closure function) {
        final HandlerNameSelect handler = new HandlerNameSelect(namePattern, 0, function);
        addHandler(handler);
        return handler;
    }

    /**
     * Add a handler with priority 0 which will do a regular expression match against full path from
     * root to see if it applies to the element.  Each segment of path will be separated by >
     *
     * @param pathPattern the regular expression to match.
     * @param function    the handler closure/function
     */
    public Handler withPath(Pattern pathPattern, Closure function) {
        final HandlerPathSelect handler = new HandlerPathSelect(pathPattern, 0, function);
        addHandler(handler);
        return handler;
    }

    /**
     * Add a handler with priority 0 which will do a execute the matcher function with the current
     * element to see if it the handler should be applied to the element.
     *
     * @param select   the regular expression to match.
     * @param function the handler closure/function
     */
    public Handler add(Closure select, Closure function) {
        final Handler handler = new HandlerFunctionSelect(select, 0, function);
        addHandler(handler);
        return handler;
    }

    /**
     * Create a handler from a map of properties to values.  There must be a 'select' attribute
     * which can be: <ul> <li>a function for determining if this handler should be applied</li>
     * <li>a string for matching against the name</li> <li>a regular expression for matching against
     * the path</li> </ul>
     * <p/>
     * In addition any JavaBean properties on the handler maybe set using the correct JavaBean
     * semantics. For example: priority, processChildren
     */
    public Handler add(Map<String, Object> properties, Closure handlerFunction) {
        Object select = properties.get(HANDLER_SELECT);

        if (select == null) {
            throw new IllegalArgumentException("A property " + HANDLER_SELECT + " must be present in the properties map");
        }

        final Handler handler;
        if (select instanceof Closure) {
            handler = add((Closure) select, handlerFunction);
        } else if (select instanceof String || select instanceof GString) {
            handler = add(select.toString(), handlerFunction);
        } else if (select instanceof Pattern) {
            handler = withPath((Pattern) select, handlerFunction);
        } else {
            throw new IllegalArgumentException(
                "The property " + HANDLER_SELECT + " is not a legal type.  Legal types are: Closure/function or String or " +
                    "Regular Expression(Pattern) but was " + select.getClass());
        }
        handler.configure(properties);
        if (!handler.mode.equals(Mode.DEFAULT)) {
            this.handlers.get(Mode.DEFAULT).remove(handler);
            addHandler(handler);
        }

        return handler;
    }

    private void addHandler(Handler handler) {
        handlers.put(handler.mode, handler);
        addMode(handler);
    }

    private void addMode(Selectable selectable) {
        if (!this.modes.containsKey(selectable.mode)) {
            this.modes.put(selectable.mode, new Mode(selectable.mode));
        }
    }

    /**
     * See if the default mode has a handler for the given element.
     */
    public boolean hasHandlerFor(GPathResult elem) {
        return hasHandlerFor(Mode.DEFAULT, elem);
    }

    /**
     * See if the mode has a handler for the given element.
     */
    public boolean hasHandlerFor(String mode, GPathResult elem) {
        return findHandlerFor(mode, elem) != null;
    }

    public Handler findHandlerFor(GPathResult elem) {
        return findHandlerFor(Mode.DEFAULT, elem);
    }

    public Handler findHandlerFor(String mode, GPathResult elem) {
        for (Handler handler : this.handlers.get(mode)) {
            if (handler.select(TransformationContext.getContext(), elem)) {
                Logging.debug("Handler %2$s found for element %1$s", elem, handler);
                return handler;
            }
        }
        Logging.debug("No handler found for element %s", elem);

        return null;
    }

    /**
     * Create a FileResult object as a result from a handler function. See {@link
     * org.fao.geonet.api.records.formatters.groovy.template.FileResult}
     * <p/>
     * File resolution is done by searching: <ul> <li>formatterDir/path</li>
     * <li>schemaFormatterDir/path</li> <li>rootFormatterDir/path</li> </ul>
     *
     * @param path  The relative path to the file to load.
     * @param model A map representing the data model to use when rendering the fileResult file.
     */
    public FileResult fileResult(String path, Map<String, Object> model) throws IOException {
        return this.templateCache.createFileResult(this.formatterDir, this.schemaDir, this.rootFormatterDir, path, model);
    }

    /**
     * Set the start handler.
     *
     * @param function the function executed at the start of the transformation.
     */
    public StartEndHandler start(Closure function) {
        this.startHandler = new StartEndHandler(function);
        return this.startHandler;
    }

    /**
     * Set the end handler.
     *
     * @param function the function executed at the end of the transformation.
     */
    public StartEndHandler end(Closure function) {
        this.endHandler = new StartEndHandler(function);
        return this.endHandler;
    }

    /**
     * Add a strategy for sorting the children of a node.
     *
     * @param select       a closure for selecting if the sorter should be applied to a given node.
     *                     If there is a match then the sorter will sort the children of the node
     *                     and add the data resulting in processing the children in the sorted
     *                     order.
     * @param sortFunction a function that compares two elements and the data produced by
     *                     processing/transforming them.  The function must return a negative, 0 or
     *                     positive number in the same way as a Comparator class.
     */
    public Sorter sort(Closure select, Closure sortFunction) {
        final SorterFunctionSelect sorter = new SorterFunctionSelect(0, select, new ClosureComparator(sortFunction));
        addSorter(sorter);
        return sorter;
    }

    /**
     * Add a strategy for sorting the children of a node.
     *
     * @param elemName     The name of the element this applies to
     * @param sortFunction a function that compares two elements and the data produced by
     *                     processing/transforming them.  The function must return a negative, 0 or
     *                     positive number in the same way as a Comparator class.
     */
    public Sorter sort(String elemName, Closure sortFunction) {
        final Sorter sorter = new SorterNameSelect(1, Pattern.compile(Pattern.quote(elemName)),
            new ClosureComparator(sortFunction));
        addSorter(sorter);
        return sorter;
    }

    /**
     * Add a strategy for sorting the children of a node.
     *
     * @param namePattern  A pattern to use for matching the name of the element to apply the sorter
     *                     to.
     * @param sortFunction a function that compares two elements and the data produced by
     *                     processing/transforming them.  The function must return a negative, 0 or
     *                     positive number in the same way as a Comparator class.
     */
    public Sorter sort(Pattern namePattern, Closure sortFunction) {
        final Sorter sorter = new SorterNameSelect(0, namePattern,
            new ClosureComparator(sortFunction));
        addSorter(sorter);
        return sorter;
    }

    /**
     * Add a strategy for sorting the children of a node.
     *
     * @param pathSelect   A pattern to use for matching the path of the element to apply the sorter
     *                     to.
     * @param sortFunction a function that compares two elements and the data produced by
     *                     processing/transforming them.  The function must return a negative, 0 or
     *                     positive number in the same way as a Comparator class.
     */
    public Sorter sortPathMatcher(Pattern pathSelect, Closure sortFunction) {
        final Sorter sorter = new SorterPathSelect(0, pathSelect,
            new ClosureComparator(sortFunction));
        addSorter(sorter);
        return sorter;
    }

    public Sorter sort(Map<String, Object> properties, Closure handlerFunction) {
        Object select = properties.get(HANDLER_SELECT);

        if (select == null) {
            throw new IllegalArgumentException("A property " + HANDLER_SELECT + " must be present in the properties map");
        }

        final Sorter sorter;
        if (select instanceof Closure) {
            sorter = sort((Closure) select, handlerFunction);
        } else if (select instanceof String || select instanceof GString) {
            sorter = sort(select.toString(), handlerFunction);
        } else if (select instanceof Pattern) {
            sorter = sortPathMatcher((Pattern) select, handlerFunction);
        } else {
            throw new IllegalArgumentException(
                "The property " + HANDLER_SELECT + " is not a legal type.  Legal types are: Closure/function or String or " +
                    "Regular Expression(Pattern) but was " + select.getClass());
        }
        sorter.configure(properties);

        if (!sorter.mode.equals(Mode.DEFAULT)) {
            this.sorters.get(Mode.DEFAULT).remove(sorter);
            addSorter(sorter);
        }
        return sorter;
    }

    private void addSorter(Sorter sorter) {
        this.sorters.put(sorter.mode, sorter);
        addMode(sorter);
    }

    Sorter findSorter(TransformationContext context, GPathResult md) {
        for (Sorter sorter : this.sorters.get(context.getCurrentMode())) {
            if (sorter.select(context, md)) {
                Logging.debug("Sorter %2$s found for element %1$s.", md, sorter);
                return sorter;
            }
        }
        Logging.debug("No handler found for element %s", md);
        return null;
    }

    /**
     * Add a new mode with the given id.  If the mode already exists and has no fallback this is a
     * no-op.  if the mode exists and has a fallback then an exception is thrown.  To change the
     * fallback to null the 2 argument mode method must be called.
     * <p/>
     * Note: This methods is not normally required because you can simply configure the mode on a
     * handler or a sorter and a mode will automatically be created.
     *
     * @param id the id of the mode to create
     */
    public Mode mode(String id) {
        Mode mode = this.modes.get(id);
        if (mode == null) {
            mode = new Mode(id);
            this.modes.put(id, mode);
        } else {
            if (mode.getFallback() != null) {
                throw new IllegalArgumentException("The mode with id: " + id + " already exists and has a non-null fallback id.  " +
                    "If you want to replace the fallback to null then explicitly call the 2 parameter " +
                    "mode method");
            }
        }
        return mode;
    }

    /**
     * Add a new mode with the given id and fallback.  If the mode exists then the fallback of the
     * mode will be updated with the new fallback id.
     *
     * @param id the id of the mode to create
     */
    public Mode mode(String id, String fallback) {
        Mode mode = this.modes.get(id);
        if (mode == null) {
            mode = new Mode(id, fallback);
            this.modes.put(id, mode);
        } else {
            mode.setFallback(fallback);
        }

        return mode;
    }

    /**
     * Find the mode with the given id or return null.
     */
    public Mode findMode(String id) {
        return this.modes.get(id);
    }

    /**
     * Process/Transform all GPathResult objects in the selection.  Since a GPathResult can contain
     * multiple elements, the selection will be iterated over and each element in each GPathResult
     * will be iterated over.
     * <p/>
     * This version will not sort the element and will use the current transformation mode.
     *
     * @param selection a iterable of GPathResult objects
     */
    public String processElements(Iterable selection) throws IOException {
        final TransformationContext context = TransformationContext.getContext();
        return processElementsInMode(context.getCurrentMode(), selection, null);
    }

    /**
     * Process/Transform all GPathResult objects in the selection.  Since a GPathResult can contain
     * multiple elements, the selection will be iterated over and each element in each GPathResult
     * will be iterated over.
     * <p/>
     * This version will not sort the element and will use the current transformation mode.
     *
     * @param selection     a iterable of GPathResult objects
     * @param sortByElement a GPathResult representing a single element which is used to look up a
     *                      sorter. If null no sorting will be done
     */
    public String processElements(Iterable selection, GPathResult sortByElement) throws IOException {
        final TransformationContext context = TransformationContext.getContext();
        return processElementsInMode(context.getCurrentMode(), selection, sortByElement);
    }

    /**
     * Process/Transform all GPathResult objects in the selection.  Since a GPathResult can contain
     * multiple elements, the selection will be iterated over and each element in each GPathResult
     * will be iterated over.
     * <p/>
     * This version will not sort the element and will use the declared mode transformation mode.
     *
     * @param mode      the transformation mode to use
     * @param selection a iterable of GPathResult objects
     */
    public String processElementsInMode(String mode, Iterable selection) throws IOException {
        return processElementsInMode(mode, selection, null);
    }

    /**
     * Process/Transform all GPathResult objects in the selection.  Since a GPathResult can contain
     * multiple elements, the selection will be iterated over and each element in each GPathResult
     * will be iterated over.
     * <p/>
     * This version will sort the element using the sorter that applies to the sortByElement. will
     * use the declared mode transformation mode.
     *
     * @param mode          the transformation mode to use
     * @param selection     a iterable of GPathResult objects
     * @param sortByElement a GPathResult representing a single element which is used to look up a
     *                      sorter. If null no sorting will be done
     */
    public String processElementsInMode(String mode, Iterable selection, GPathResult sortByElement) throws IOException {
        return transformationEngine.processElementsInMode(mode, selection, sortByElement);
    }

    ListMultimap<String, Handler> getHandlers() {
        return handlers;
    }

    public List<SkipElement> getSkipElements() {
        return skipElements;
    }
}
