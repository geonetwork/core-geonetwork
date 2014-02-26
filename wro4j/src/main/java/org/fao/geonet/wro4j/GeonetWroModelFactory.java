package org.fao.geonet.wro4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates model of views to javascript and css.
 * <p/>
 * User: Jesse
 * Date: 11/22/13
 * Time: 8:28 AM
 */
public class GeonetWroModelFactory implements WroModelFactory {
    private static final Logger LOG = java.util.logging.Logger.getLogger(GeonetWroModelFactory.class.getName());

    private static final String WRO_SOURCES_KEY = "wroSources";
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
    private static final String NOT_MINIMIZED_EL = "notMinimized";
    public static final String GROUP_NAME_CLOSURE_DEPS = "closure_deps";
    
    public static final String TEMPLATE_PATTERN = "directive.js";
    @Inject
    private ReadOnlyContext _context;

    private String _geonetworkRootDirectory = "";

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

        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            stopWatch.stop();
            LOG.info(stopWatch.prettyPrint());
        }
    }

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

    private static class IncludesStream {
        final InputStream stream;
        final String locationLoadedFrom;

        private IncludesStream(InputStream stream, String locationLoadedFrom) {
            this.stream = stream;
            this.locationLoadedFrom = locationLoadedFrom;
        }
    }

    private Collection<IncludesStream> openIncludesStream(String parentSourcesXmlFile, String includeFile) throws IOException {

        if (includeFile.startsWith(CLASSPATH_PREFIX)) {
            final Collection<IncludesStream> includesStreams = loadFromClasspath(includeFile);
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
                System.out.println();
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
        Collection<IncludesStream> results = new ArrayList<IncludesStream>();
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
        String defaultPathOnDisk = doc.getAttribute(PATH_ON_DISK_ATT);
        final NodeList jsSources = doc.getElementsByTagName(JS_SOURCE_EL);

        final ClosureRequireDependencyManager dependencyManager = configureJavascripDependencyManager(jsSources, defaultPathOnDisk);

        Map<String, Group> groups = addJavascriptGroupsByRequireDependencies(model, dependencyManager, closureDepsGroup);

        final NodeList cssSources = doc.getElementsByTagName(CSS_SOURCE_EL);
        addCssGroupsByRequireDependencies(model, groups, cssSources, defaultPathOnDisk);

    }

    private void logModel(WroModel model) {
        if (LOG.isLoggable(Level.INFO)) {
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
            LOG.info(builder.toString());
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
                    throw new IllegalArgumentException("There are at least two css file with the name: " + name + ".  Each css file " +
                                                       "must have unique names");
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
        Map<String, Group> groupMap = new HashMap<String, Group>((int) (moduleIds.size() * 1.5));

        for (String moduleId : moduleIds) {
            Group group = new Group(moduleId);
            groupMap.put(moduleId, group);

            closureDepsGroup.addResource(ClosureDependencyUriLocator.createClosureDepResource(dependencyManager.getNode(moduleId)));
            final Collection<ClosureRequireDependencyManager.Node> deps = dependencyManager.getTransitiveDependenciesFor(moduleId, true);

            List<Resource> resources = new ArrayList<Resource>();
            for (ClosureRequireDependencyManager.Node dep : deps) {
                group.addResource(createResourceFrom(dep));
                addTemplateResourceFrom(resources, dep);
                closureDepsGroup.addResource(ClosureDependencyUriLocator.createClosureDepResource(dep));
            }

            group.addResource(createResourceFrom(dependencyManager.getNode(moduleId)));
            if(resources.size() > 0) {
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
        resource.setUri(dep.path);
        return resource;
    }
    
    private Resource getTemplateResource(final String prefix) {
        Resource resource = new Resource();
        resource.setMinimize(false);
        resource.setType(ResourceType.JS);
        resource.setUri(prefix);
        return resource;
    }

    private void addTemplateResourceFrom(List<Resource> group, ClosureRequireDependencyManager.Node dep) {
        if(dep.path.toLowerCase().endsWith(TEMPLATE_PATTERN)) {
            String dirPath = new File(dep.path).getParent();
            String prefix = TemplatesUriLocator.URI_PREFIX + dirPath + "/partials";
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
                    path = '/' + path.replace('\\', '/').replaceAll("/+", "/");
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

        desc.relativePath = sourceEl.getAttribute(WEBAPP_ATT);
        desc.relativePath = desc.relativePath.replace('\\', '/');
        if (!sourceEl.hasAttribute(PATH_ON_DISK_ATT)) {
            desc.pathOnDisk = defaultPathOnDisk;
        } else {
            desc.pathOnDisk = sourceEl.getAttribute(PATH_ON_DISK_ATT);
        }

        if (desc.pathOnDisk == null) {
            throw new AssertionError("No " + PATH_ON_DISK_ATT + " was found on " + JS_SOURCE_EL + " or parent Element");
        }

        if (isMavenBuild()) {
            final File pathOnDisk = new File(desc.pathOnDisk);
            if (pathOnDisk.isAbsolute() && pathOnDisk.exists()) {
                desc.finalPath = new File(desc.pathOnDisk, desc.relativePath).getPath();
            } else {
                desc.finalPath = new File(_geonetworkRootDirectory + desc.pathOnDisk, desc.relativePath).getPath();
            }
            if (!new File(desc.finalPath).exists()) {
                throw new AssertionError("Neither '" + desc.finalPath + "' nor '" + new File(desc.pathOnDisk,
                        desc.relativePath) + "' exist");
            }
        } else {
            desc.finalPath = _context.getServletContext().getRealPath(desc.relativePath);
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

    private static class ResourceDesc {
        String relativePath;
        String pathOnDisk;
        String finalPath;
        File root;

        public Iterable<File> files(final String... extToCollect) {
            return new Iterable<File>() {

                @Override
                public Iterator<File> iterator() {
                    return FileUtils.iterateFiles(root, extToCollect, true);
                }
            };

        }
    }
}
