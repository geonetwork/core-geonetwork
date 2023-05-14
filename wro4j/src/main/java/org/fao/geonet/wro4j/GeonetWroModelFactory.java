/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
package org.fao.geonet.wro4j;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.logging.log4j.Level;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.StopWatch;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.fao.geonet.wro4j.GeonetworkWrojManagerFactory.WRO4J_LOG;

/**
 * Creates model of views to javascript and css.
 * <p/>
 * User: Jesse Date: 11/22/13 Time: 8:28 AM
 */
public class GeonetWroModelFactory implements WroModelFactory {
    public static final String WRO_SOURCES_KEY = "wroSources";
    public static final String JS_SOURCE_EL = "jsSource";
    public static final String INCLUDE_EL = "include";
    public static final String FILE_ATT = "file";
    public static final String FILE_EL = "file";
    public static final String DECLARATIVE_EL = "declarative";
    public static final String DECLARATIVE_NAME_ATT = "name";
    public static final String MINIMIZED_ATT = "minimize";
    public static final String REQUIRE_EL = "require";
    public static final String CSS_SOURCE_EL = "cssSource";
    public static final String WEBAPP_ATT = "webappPath";
    public static final String PATH_ON_DISK_ATT = "pathOnDisk";
    public static final String CLASSPATH_PREFIX = "classpath:";
    public static final String GROUP_NAME_CLOSURE_DEPS = "closure_deps";
    public static final String TEMPLATE_PATTERN = "directive.js";
    private static final Pattern PATH_REPLACEMENT_MATCHER = Pattern.compile("\\{\\{(.+?)\\}\\}");
    private static final String NOT_MINIMIZED_EL = "notMinimized";
    @Inject
    private ReadOnlyContext _context;
    private Collection<Throwable> errors = Lists.newArrayList();

    private String _geonetworkRootDirectory = "";

    static String findGeonetworkRootDirectory(String sourcesXmlFile) {
        File currentFile = new File(sourcesXmlFile);
        while (currentFile != null && !new File(currentFile, "web/src/main/webResources/WEB-INF/web.xml").exists()) {
            currentFile = currentFile.getParentFile();
        }

        if (currentFile == null) {
            throw new AssertionError("Unable to find root geonetwork directory using '" + sourcesXmlFile + "' as a starting point");
        }

        return currentFile.getAbsolutePath() + "/";
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public WroModel create() {
        final StopWatch stopWatch = new StopWatch("Create Wro Model using Geonetwork");
        try {
            stopWatch.start("createModel");
            final String sourcesXmlFile = getSourcesXmlFile();

            if (isMavenBuild() && _geonetworkRootDirectory.isEmpty()) {
                _geonetworkRootDirectory = findGeonetworkRootDirectory(sourcesXmlFile);
            }
            FileInputStream sourcesInputStream = null;
            try {
                sourcesInputStream = new FileInputStream(sourcesXmlFile);
                final WroModel model = createModel(sourcesXmlFile, sourcesInputStream);
                logModel(model);
                return model;
            } finally {
                if (sourcesInputStream != null) {
                    IOUtils.closeQuietly(sourcesInputStream);
                }
            }

        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            stopWatch.stop();
            Log.info(WRO4J_LOG, stopWatch.prettyPrint());
        }
    }

    private WroModel createModel(String parentSourcesXmlFile, InputStream sourcesXmlFile) throws ParserConfigurationException,
        SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(sourcesXmlFile);

        final WroModel model = new WroModel();

        Group closureDepsGroup = new Group(GROUP_NAME_CLOSURE_DEPS);

        final NodeList includeNodes = doc.getElementsByTagName(INCLUDE_EL);

        for (int i = 0; i < includeNodes.getLength(); i++) {
            Element include = (Element) includeNodes.item(i);
            if (!include.hasAttribute(FILE_ATT)) {
                throw new AssertionError("include elements must have a " + FILE_ATT + " attribute");
            }

            Collection<IncludesStream> streams = null;
            try {
                streams = openIncludesStream(parentSourcesXmlFile, include.getAttribute(FILE_ATT));
                for (IncludesStream is : streams) {
                    InputStream stream = is.stream;
                    WroModel includedModel = createModel(is.locationLoadedFrom, stream);
                    for (Group group : includedModel.getGroups()) {
                        if (GROUP_NAME_CLOSURE_DEPS.equals(group.getName())) {
                            for (Resource resource : group.getResources()) {
                                closureDepsGroup.addResource(resource);
                            }
                        } else {
                            model.addGroup(group);
                        }
                    }
                }
            } catch (Exception e) {
                errors.add(e);
                Log.error(WRO4J_LOG, "Error while loading wro4j model", e);
            } finally {
                if (streams != null) {
                    for (IncludesStream stream : streams) {
                        IOUtils.closeQuietly(stream.stream);
                    }
                }
            }
        }

        final NodeList requireNodes = doc.getElementsByTagName(REQUIRE_EL);

        for (int i = 0; i < requireNodes.getLength(); i++) {
            Element require = (Element) requireNodes.item(i);
            loadGroupsUsingRequireDependencyManagement(require, model, closureDepsGroup);
        }
        if (!closureDepsGroup.getResources().isEmpty()) {
            model.addGroup(closureDepsGroup);
        }
        final NodeList declareNodes = doc.getElementsByTagName(DECLARATIVE_EL);

        for (int i = 0; i < declareNodes.getLength(); i++) {
            Element declared = (Element) declareNodes.item(i);
            addExplicitlyDeclarativeGroups(declared, model);
        }
        return model;
    }

    public void setContext(ReadOnlyContext context) {
        this._context = context;
    }

    public void setGeonetworkRootDirectory(String geonetworkRootDirectory) {
        this._geonetworkRootDirectory = geonetworkRootDirectory;
    }

    private Collection<IncludesStream> openIncludesStream(String parentSourcesXmlFile, String includeFile) throws IOException {

        if (includeFile.startsWith(CLASSPATH_PREFIX)) {
            Collection<IncludesStream> includesStreams = loadFromClasspath(includeFile);
            if (includesStreams.isEmpty()) {
                final String actualPath = includeFile.substring(CLASSPATH_PREFIX.length());
                includesStreams = loadFromClasspath(CLASSPATH_PREFIX + "WEB-INF/classes/" + actualPath);
            }
            if (!includesStreams.isEmpty()) {
                return includesStreams;
            } else {
                throw new AssertionError("Unable to load: " + includeFile);
            }
        }
        String relativePath = toRelativePath(parentSourcesXmlFile);
        if (relativePath.startsWith(CLASSPATH_PREFIX)) {
            final Collection<IncludesStream> includesStreams;
            if (relativePath.equals(CLASSPATH_PREFIX)) {
                includesStreams = loadFromClasspath(relativePath + includeFile);
            } else {
                includesStreams = loadFromClasspath(relativePath + "/" + includeFile);
            }
            if (!includesStreams.isEmpty()) {
                return includesStreams;
            }
        }

        IncludesStream stream = tryToLoadAsURL(includeFile);
        if (stream != null) {
            return Collections.singleton(stream);
        }

        final String pathWithRelativePortion = relativePath + "/" + includeFile;
        stream = tryToLoadAsURL(pathWithRelativePortion);
        if (stream != null) {
            return Collections.singleton(stream);
        }


        if (new File(includeFile).exists()) {
            return Collections.singleton(new IncludesStream(new FileInputStream(includeFile), includeFile));
        }
        final File file = new File(relativePath, includeFile);
        if (file.exists()) {
            return Collections.singleton(new IncludesStream(new FileInputStream(file), file.getAbsolutePath()));
        }
        if (!isMavenBuild()) {
            final ServletContext servletContext = _context.getServletContext();
            try {
                File absolute = new File(servletContext.getRealPath(includeFile));
                if (absolute.exists()) {
                    return Collections.singleton(new IncludesStream(new FileInputStream(absolute), absolute.getAbsolutePath()));
                }
            } catch (Throwable t) {
                // try relative file then...
            }
            try {
                File relative = new File(servletContext.getRealPath(pathWithRelativePortion));
                if (relative.exists()) {
                    return Collections.singleton(new IncludesStream(new FileInputStream(relative), relative.getAbsolutePath()));
                }
            } catch (Throwable t) {
                throw new RuntimeException("Error trying to load: '" + includeFile + "' with parent:'" + parentSourcesXmlFile, t);
            }
        }

        throw new AssertionError("Unable to locate include xml file. \n\trelativePath: " + relativePath +
            "\n\tinclude file: " + includeFile);
    }

    private String toRelativePath(String relativeToWithName) {
        final int i = relativeToWithName.replace('\\', '/').lastIndexOf('/');
        String relativeTo;
        if (i > -1) {
            relativeTo = relativeToWithName.substring(0, i);
        } else {
            relativeTo = "";
            if (relativeToWithName.startsWith(CLASSPATH_PREFIX)) {
                relativeTo = CLASSPATH_PREFIX;
            }
        }
        return relativeTo;
    }

    private Collection<IncludesStream> loadFromClasspath(String includeFile) throws IOException {
        final String actualPath = includeFile.substring(CLASSPATH_PREFIX.length());
        final Enumeration<URL> resources = GeonetWroModelFactory.class.getClassLoader().getResources(actualPath);
        Collection<IncludesStream> results = new ArrayList<>();
        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            String file = url.getFile();
            if (file.matches("/.:/.*")) {
                file = file.substring(1);
            }
            String relativeFile = includeFile;
            if (new File(file).exists()) {
                relativeFile = file;
            }
            results.add(new IncludesStream(url.openStream(), relativeFile));
        }

        if (results.isEmpty() && !isMavenBuild()) {
            // this is for jetty:run where the file is actually not on the classpath yet.
            final String path = _context.getServletContext().getRealPath(actualPath);
            if (path != null && new File(path).exists()) {
                results.add(new IncludesStream(new FileInputStream(path), path));
            }
        }
        return results;
    }

    private IncludesStream tryToLoadAsURL(String includeFile) {
        try {
            return new IncludesStream(new URL(includeFile).openStream(), includeFile);
        } catch (MalformedURLException e) {
            // try another way of opening the stream
        } catch (IOException e) {
            // try another way of opening the stream
        }
        return null;
    }

    private void addExplicitlyDeclarativeGroups(Element declareEl, WroModel model) {
        String defaultPathOnDisk = declareEl.getAttribute(PATH_ON_DISK_ATT);
        final NodeList jsSources = declareEl.getElementsByTagName(JS_SOURCE_EL);

        if (!declareEl.hasAttribute(DECLARATIVE_NAME_ATT)) {
            throw new AssertionError(DECLARATIVE_EL + " elements require a " + DECLARATIVE_NAME_ATT + " attribute.");
        }
        String name = declareEl.getAttribute(DECLARATIVE_NAME_ATT);

        Group group = new Group(name);
        for (int i = 0; i < jsSources.getLength(); i++) {
            final Element item = (Element) jsSources.item(i);
            final ResourceDesc desc = parseSource(item, defaultPathOnDisk);
            boolean isMinimized = !item.hasAttribute(MINIMIZED_ATT) || Boolean.parseBoolean(item.getAttribute(MINIMIZED_ATT));
            Resource resource = createResource(isMinimized, desc, ResourceType.JS);
            group.addResource(resource);
        }

        final NodeList cssSources = declareEl.getElementsByTagName(CSS_SOURCE_EL);

        for (int i = 0; i < cssSources.getLength(); i++) {
            final Element item = (Element) cssSources.item(i);
            final ResourceDesc desc = parseSource(item, defaultPathOnDisk);
            boolean isMinimized = !item.hasAttribute(MINIMIZED_ATT) || Boolean.parseBoolean(item.getAttribute(MINIMIZED_ATT));
            Resource resource = createResource(isMinimized, desc, ResourceType.CSS);
            group.addResource(resource);
        }

        model.addGroup(group);

    }

    private Resource createResource(boolean isMinimized, ResourceDesc desc, ResourceType type) {
        File file = desc.root;
        Resource resource = new Resource();
        resource.setMinimize(isMinimized);
        resource.setType(type);
        resource.setUri(file.toURI().toString());
        return resource;
    }

    private void loadGroupsUsingRequireDependencyManagement(Element doc, WroModel model, Group closureDepsGroup) throws IOException {
        String defaultPathOnDisk = makePathReplacements(doc.getAttribute(PATH_ON_DISK_ATT));
        final NodeList jsSources = doc.getElementsByTagName(JS_SOURCE_EL);

        final ClosureRequireDependencyManager dependencyManager = configureJavascripDependencyManager(jsSources, defaultPathOnDisk);

        Map<String, Group> groups = addJavascriptGroupsByRequireDependencies(model, dependencyManager, closureDepsGroup);

        final NodeList cssSources = doc.getElementsByTagName(CSS_SOURCE_EL);
        addCssGroupsByRequireDependencies(model, groups, cssSources, defaultPathOnDisk);

    }

    private String makePathReplacements(String path) {
        if (ApplicationContextHolder.get() == null) {
            // testing
            return path;
        }

        final GeonetworkDataDirectory dataDirectory = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
        final Matcher matcher = PATH_REPLACEMENT_MATCHER.matcher(path);

        StringBuffer builder = new StringBuffer();
        while (matcher.find()) {
            final String group = matcher.group(1);
            try {
                final Method method = dataDirectory.getClass().getMethod("get" + Character.toUpperCase(group.charAt(0)) + group.substring(1));
                final Object invoke = method.invoke(dataDirectory);
                matcher.appendReplacement(builder, invoke.toString().replace("\\", "\\\\"));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        matcher.appendTail(builder);
        return builder.toString();
    }

    private void logModel(WroModel model) {
        if (Log.isEnabledFor(WRO4J_LOG, Level.INFO)) {
            StringBuilder builder = new StringBuilder();
            final int uriLength = 60;
            for (Group group : model.getGroups()) {
                builder.append("Group " + group.getName() + " contains:\n");
                for (Resource resource : group.getResources()) {
                    String uri = resource.getUri();
                    int min = Math.max(0, uri.length() - uriLength);
                    int max = resource.getUri().length();
                    uri = uri.substring(min, max);
                    if (min > 0) {
                        uri = "..." + uri;
                    }
                    builder.append("\t" + uri + "\n");
                }
                builder.append("\n\n");
            }
            Log.info(WRO4J_LOG, builder.toString());
        }

        if (!errors.isEmpty()) {
            Log.error(WRO4J_LOG, "Errors were encountered");
            for (Throwable error : errors) {
                Log.error(WRO4J_LOG, "error", error);
            }
        }
    }

    private void addCssGroupsByRequireDependencies(final WroModel model, final Map<String, Group> groups, final NodeList cssSources,
                                                   String defaultPathOnDisk) {

        HashSet<String> cssGroups = new HashSet<String>();

        for (int i = 0; i < cssSources.getLength(); i++) {
            Node cssSource = cssSources.item(i);
            final Set<String> notMinimized = parseSetOfNotMinimized((Element) cssSource);
            ResourceDesc desc = parseSource((Element) cssSource, defaultPathOnDisk);

            final Iterable<File> css = desc.files("css", "less");
            for (File file : css) {
                final String name = file.getName();
                final String groupId = name.substring(0, name.lastIndexOf('.'));

                if (cssGroups.contains(groupId)) {
                    Log.warning(WRO4J_LOG, "There are at least two css file with the name: " + name + ". " +
                        "Only the first one will be used, ignoring: " + file.getPath());
                    continue;
                }

                cssGroups.add(groupId);

                final Group group;
                if (groups.containsKey(groupId)) {
                    group = groups.get(groupId);
                } else {
                    group = new Group(groupId);
                }

                boolean isMinimized = true;
                for (String s : notMinimized) {
                    if (file.getAbsolutePath().endsWith(s)) {
                        isMinimized = false;
                        break;
                    }
                }
                Resource resource = new Resource();
                resource.setMinimize(isMinimized);
                resource.setType(ResourceType.CSS);
                resource.setUri(file.toURI().toString());

                group.addResource(resource);
                if (!groups.containsKey(groupId)) {
                    model.addGroup(group);
                }
            }
        }
        //To change body of created methods use File | Settings | File Templates.
    }

    private Map<String, Group> addJavascriptGroupsByRequireDependencies(final WroModel model, final ClosureRequireDependencyManager
        dependencyManager, Group closureDepsGroup) {
        final Set<String> moduleIds = dependencyManager.getAllModuleIds();
        Map<String, Group> groupMap = new HashMap<>((int) (moduleIds.size() * 1.5));

        for (String moduleId : moduleIds) {
            Group group = new Group(moduleId);
            groupMap.put(moduleId, group);

            closureDepsGroup.addResource(ClosureDependencyUriLocator.createClosureDepResource(dependencyManager.getNode(moduleId)));
            final Collection<ClosureRequireDependencyManager.Node> deps = dependencyManager.getTransitiveDependenciesFor(moduleId, true);

            List<Resource> resources = new ArrayList<>();
            for (ClosureRequireDependencyManager.Node dep : deps) {
                group.addResource(createResourceFrom(dep));
                addTemplateResourceFrom(resources, dep);
                closureDepsGroup.addResource(ClosureDependencyUriLocator.createClosureDepResource(dep));
            }

            group.addResource(createResourceFrom(dependencyManager.getNode(moduleId)));
            if (resources.size() > 0) {
                group.addResource(getTemplateResource(TemplatesUriLocator.URI_PREFIX_HEADER));
                for (Resource resource : resources) {
                    group.addResource(resource);
                }
                group.addResource(getTemplateResource(TemplatesUriLocator.URI_PREFIX_FOOTER));
            }
            model.addGroup(group);
        }

        return groupMap;
    }

    private Resource createResourceFrom(ClosureRequireDependencyManager.Node dep) {
        Resource resource = new Resource();
        resource.setMinimize(dep.isMinimized);
        resource.setType(ResourceType.JS);
        final Path path = IO.toPath(dep.path.replace("file:/D:", "/D"));
        if (Files.exists(path)) {
            resource.setUri(path.toUri().toString());
        } else {
            resource.setUri(dep.path);
        }
        return resource;
    }

    private Resource getTemplateResource(final String prefix) {
        Resource resource = new Resource();
        resource.setMinimize(false);
        resource.setType(ResourceType.JS);
        resource.setUri(prefix);
        return resource;
    }

    /**
     * If a JS file ends with {@See TEMPLATE_PATTERN} or is a view template
     * then check all HTML files which are templates.
     * <p>
     * TODO: Add for all views ?
     */
    private void addTemplateResourceFrom(List<Resource> group, ClosureRequireDependencyManager.Node dep) {
        boolean isViewTemplate = dep.path.startsWith("/catalog/views/default/config.js");
        if (dep.path.toLowerCase().endsWith(TEMPLATE_PATTERN) ||
            isViewTemplate) {
            Path dir;
            if (dep.path.startsWith("file:/")) {
                try {
                    dir = Paths.get(new URI(dep.path)).getParent();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else {
                dir = Paths.get(dep.path).getParent();
            }
            if (dir == null) {
                throw new RuntimeException("Directory folder is missing!!");
            }
            Path resolve = dir;
            String dirPath = resolve.toString().replace('\\', '/');
            String prefix = TemplatesUriLocator.URI_PREFIX + dirPath;
            group.add(getTemplateResource(prefix));
        }
    }

    private ClosureRequireDependencyManager configureJavascripDependencyManager(final NodeList jsSources,
                                                                                String defaultPathOnDisk) throws IOException {
        ClosureRequireDependencyManager depManager = new ClosureRequireDependencyManager();

        for (int i = 0; i < jsSources.getLength(); i++) {
            Node jsSource = jsSources.item(i);
            ResourceDesc desc = parseSource((Element) jsSource, defaultPathOnDisk);
            Set<String> notMinimized = parseSetOfNotMinimized((Element) jsSource);
            for (File file : desc.files("js")) {
                String path;
                // if servlet context is null then the build is the
                // maven build. so the path has to be the full path because
                // servletcontext is not used to locate the file.
                if (isMavenBuild()) {
                    path = file.getAbsoluteFile().toURI().toString();
                } else {
                    path = desc.relativePath + file.getPath().substring(desc.finalPath.length());
                    if (!Files.exists(IO.toPath(path))) {
                        path = '/' + path.replace('\\', '/').replaceAll("/+", "/");
                    }
                    if (path.startsWith("//")) { // horrible hack!!!
                        path = path.substring(1);
                    }
                }
                depManager.addFile(path, file, notMinimized);
            }
        }

        depManager.validateGraph();

        return depManager;
    }

    private Set<String> parseSetOfNotMinimized(Element jsSource) {
        final NodeList nodeList = jsSource.getElementsByTagName(NOT_MINIMIZED_EL);
        Set<String> notMinimized = new HashSet<String>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element notMinimizedEl = (Element) nodeList.item(i);
            final NodeList files = notMinimizedEl.getElementsByTagName(FILE_EL);
            for (int j = 0; j < files.getLength(); j++) {
                notMinimized.add(files.item(j).getTextContent().trim());
            }
        }
        return notMinimized;
    }

    private ResourceDesc parseSource(final Element sourceEl, String defaultPathOnDisk) {
        ResourceDesc desc = new ResourceDesc();

        if (!sourceEl.hasAttribute(WEBAPP_ATT)) {
            throw new AssertionError("No " + WEBAPP_ATT + " was found on " + JS_SOURCE_EL);
        }

        desc.relativePath = makePathReplacements(sourceEl.getAttribute(WEBAPP_ATT));
        desc.relativePath = desc.relativePath.replace('\\', '/');
        if (!sourceEl.hasAttribute(PATH_ON_DISK_ATT)) {
            desc.pathOnDisk = defaultPathOnDisk;
        } else {
            desc.pathOnDisk = makePathReplacements(sourceEl.getAttribute(PATH_ON_DISK_ATT));
        }

        if (desc.pathOnDisk == null) {
            throw new AssertionError("No " + PATH_ON_DISK_ATT + " was found on " + JS_SOURCE_EL + " or parent Element");
        }

        if (isMavenBuild()) {
            final File pathOnDisk = new File(desc.pathOnDisk);
            if (pathOnDisk.isAbsolute() && pathOnDisk.exists()) {
                desc.finalPath = new File(desc.pathOnDisk, desc.relativePath).getPath();
            } else {
                final File relativePath = new File(desc.relativePath);
                if (relativePath.isAbsolute() && relativePath.exists()) {
                    desc.finalPath = desc.relativePath;
                } else {
                    desc.finalPath = new File(_geonetworkRootDirectory + desc.pathOnDisk, desc.relativePath).getPath();
                }
            }
            if (!new File(desc.finalPath).exists()) {
                throw new AssertionError("Neither '" + desc.finalPath + "' nor '" + new File(desc.pathOnDisk,
                    desc.relativePath) + "' exist");
            }
        } else {
            if (Files.exists(IO.toPath(desc.relativePath))) {
                desc.finalPath = desc.relativePath;
            } else {
                desc.finalPath = _context.getServletContext().getRealPath(desc.relativePath);
            }
        }


        desc.root = new File(desc.finalPath);

        return desc;
    }

    private boolean isMavenBuild() {
        return _context.getServletContext() == null;
    }

    public String getSourcesXmlFile() {
        final String sourcesRawProperty = getConfigProperties().getProperty(WRO_SOURCES_KEY);
        if (_context.getServletContext() != null) {
            final String[] split = sourcesRawProperty.split("WEB-INF/", 2);
            if (split.length == 2) {
                final String path = _context.getServletContext().getRealPath("/WEB-INF/" + split[1]);
                if (path != null) {
                    return path;
                }
            }
        }
        return sourcesRawProperty;
    }

    protected Properties getConfigProperties() {
        return null;
    }

    private static class IncludesStream {
        final InputStream stream;
        final String locationLoadedFrom;

        private IncludesStream(InputStream stream, String locationLoadedFrom) {
            this.stream = stream;
            this.locationLoadedFrom = locationLoadedFrom;
        }
    }

    private static class ResourceDesc {
        String relativePath;
        String pathOnDisk;
        String finalPath;
        File root;

        public Iterable<File> files(final String... extToCollect) {
            return new Iterable<File>() {

                @Override
                public Iterator<File> iterator() {
                    if (!root.exists()) {
                        throw new IllegalArgumentException(String.format("%s doesn't exist. It could be a missing library. " +
                            "Check the source if you have all dependency files required.", root));
                    }
                    if (root.isFile()) {
                        List<String> suffixes = Arrays.stream(extToCollect).map(sufix -> "." + sufix).collect(Collectors.toList());
                        SuffixFileFilter suffixFilter = new SuffixFileFilter(suffixes);
                        if (suffixFilter.accept(root)) {
                            List<File> files = new ArrayList<>(1);
                            files.add(root);
                            return files.iterator();
                        } else {
                            throw new IllegalArgumentException(String.format("%s exist but doesn't match the filter %s",
                                root, Arrays.toString(extToCollect)));
                        }
                    }
                    // More detailed error about
                    // Parameter 'directory' is not a directory
                    // when a missing lib is not found by wro4j when geonetwork initialized.
                    // It may happen when submodules are not loaded properly.
                    if (!root.isDirectory()) {
                        throw new IllegalArgumentException(
                            String.format("Directory '%s' is not a directory. It could be a missing library. Check the source if you have all dependency files required.", root));
                    }
                    return FileUtils.iterateFiles(root, extToCollect, true);
                }
            };

        }
    }
}
