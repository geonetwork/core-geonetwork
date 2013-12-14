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
import java.io.*;
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
    public static final String DECLARATIVE_EL = "declarative";
    public static final String DECLARATIVE_NAME_ATT = "name";
    public static final String REQUIRE_EL = "require";
    public static final String CSS_SOURCE_EL = "cssSource";
    public static final String WEBAPP_ATT = "webapp";
    public static final String PATH_ON_DISK_ATT = "pathOnDisk";
    @Inject
    private ReadOnlyContext context;

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
            final WroModel model = createModel(sourcesXmlFile, new FileInputStream(sourcesXmlFile));

            logModel(model);
            return model;
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

    private WroModel createModel(String relativeTo, InputStream sourcesXmlFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(sourcesXmlFile);

        final WroModel model = new WroModel();

        final NodeList includeNodes = doc.getElementsByTagName(INCLUDE_EL);

        for (int i = 0; i < includeNodes.getLength(); i++) {
            Element include = (Element) includeNodes.item(i);
            if (!include.hasAttribute(FILE_ATT)) {
                throw new AssertionError("include elements must have a "+FILE_ATT+" attribute");
            }

            InputStream stream = null;
            try {
                IncludesStream is = openIncludesStream(relativeTo, include.getAttribute(FILE_ATT));
                stream = is.stream;
                WroModel includedModel = createModel(is.locationLoadedFrom, stream);
                for (Group group : includedModel.getGroups()) {
                    model.addGroup(group);
                }
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }

        final NodeList requireNodes = doc.getElementsByTagName(REQUIRE_EL);

        for (int i = 0; i < requireNodes.getLength(); i++) {
            Element require = (Element) requireNodes.item(i);
            loadGroupsUsingRequireDependencyManagement(require, model);
        }
        final NodeList declareNodes = doc.getElementsByTagName(DECLARATIVE_EL);

        for (int i = 0; i < declareNodes.getLength(); i++) {
            Element declared = (Element) declareNodes.item(i);
            addExplicitelyDeclarativeGroups(declared, model);
        }
        return model;
    }

    private static class IncludesStream {
        final InputStream stream;
        final String locationLoadedFrom;

        private IncludesStream(InputStream stream, String locationLoadedFrom) {
            this.stream = stream;
            this.locationLoadedFrom = locationLoadedFrom;
        }
    }
    private IncludesStream openIncludesStream(String relativeToWithName, String includeFile) throws FileNotFoundException {
        final int i = relativeToWithName.replace('\\', '/').lastIndexOf('/');
        String relativeTo;
        if (i > -1) {
            relativeTo = relativeToWithName.substring(0, i);
        } else {
            relativeTo = relativeToWithName;
        }
        IncludesStream stream = tryToLoadAsURL(includeFile);
        if (stream != null) {
            return stream;
        }

        final String pathWithRelativePortion = relativeTo + "/" + includeFile;
        stream = tryToLoadAsURL(pathWithRelativePortion);
        if (stream != null) {
            return stream;
        }


        if (new File(includeFile).exists()) {
            return new IncludesStream(new FileInputStream(includeFile), includeFile);
        }
        final File file = new File(relativeTo, includeFile);
        if (file.exists()) {
            return new IncludesStream(new FileInputStream(file), file.getAbsolutePath());
        }
        if (!isMavenBuild()) {
            final ServletContext servletContext = context.getServletContext();
            File absolute = new File(servletContext.getRealPath(includeFile));
            if (absolute.exists()) {
                return new IncludesStream(new FileInputStream(absolute), absolute.getAbsolutePath());
            }
            File relative = new File(servletContext.getRealPath(pathWithRelativePortion));
            if (relative.exists()) {
                return new IncludesStream(new FileInputStream(relative), relative.getAbsolutePath());
            }
        }

        throw new AssertionError("Unable to locate include xml file. \n\trelativeTo: "+relativeTo+
                                 "\n\tinclude file: "+includeFile);
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

    private void addExplicitelyDeclarativeGroups(Element declareEl, WroModel model) {
        String defaultPathOnDisk = declareEl.getAttribute(PATH_ON_DISK_ATT);
        final NodeList jsSources = declareEl.getElementsByTagName(JS_SOURCE_EL);

        if (!declareEl.hasAttribute(DECLARATIVE_NAME_ATT)) {
            throw new AssertionError(DECLARATIVE_EL +" elements require a "+DECLARATIVE_NAME_ATT+" attribute.");
        }
        String name = declareEl.getAttribute(DECLARATIVE_NAME_ATT);

        Group group = new Group(name);
        for (int i = 0; i < jsSources.getLength(); i++ ) {
            final ResourceDesc desc = parseSource((Element) jsSources.item(i), defaultPathOnDisk);
            Resource resource = createResource(desc, ResourceType.JS);
            group.addResource(resource);
        }

        final NodeList cssSources = declareEl.getElementsByTagName(CSS_SOURCE_EL);

        for (int i = 0; i < cssSources.getLength(); i++ ) {
            final ResourceDesc desc = parseSource((Element) cssSources.item(i), defaultPathOnDisk);
            Resource resource = createResource(desc, ResourceType.CSS);
            group.addResource(resource);

        }

        model.addGroup(group);

    }

    private Resource createResource(ResourceDesc desc, ResourceType type) {
        File file = desc.root;
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(type);
        resource.setUri(file.toURI().toString());
        return resource;
    }

    private void loadGroupsUsingRequireDependencyManagement(Element doc, WroModel model) throws IOException {
        String defaultPathOnDisk = doc.getAttribute(PATH_ON_DISK_ATT);
        final NodeList jsSources = doc.getElementsByTagName(JS_SOURCE_EL);

        final RequireDependencyManager dependencyManager = configureJavascripDependencyManager(jsSources, defaultPathOnDisk);

        Map<String, Group> groups = addJavascriptGroupsByRequireDependencies(model, dependencyManager);

        final NodeList cssSources = doc.getElementsByTagName(CSS_SOURCE_EL);
        addCssGroupsByRequireDependencies(model, groups, cssSources, defaultPathOnDisk);

    }

    private void logModel(WroModel model) {
        if (LOG.isLoggable(Level.INFO)) {
            StringBuilder builder = new StringBuilder();
            final int uriLeng = 60;
            for (Group group : model.getGroups()) {
                builder.append("Group "+group.getName()+" contains:\n");
                for (Resource resource : group.getResources()) {
                    String uri = resource.getUri();
                    int min = Math.max(0, uri.length() - uriLeng);
                    int max = resource.getUri().length();
                    uri = uri.substring(min, max);
                    if (min > 0) {
                        uri = "..."+uri;
                    }
                    builder.append("\t"+uri+"\n");
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

                Resource resource = new Resource();
                resource.setMinimize(true);
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

    private Map<String, Group> addJavascriptGroupsByRequireDependencies(final WroModel model, final RequireDependencyManager
            dependencyManager) {
        final Set<String> moduleIds = dependencyManager.getAllModuleIds();
        Map<String, Group> groupMap = new HashMap<String, Group>((int) (moduleIds.size() * 1.5));

        for (String moduleId : moduleIds) {
            Group group = new Group(moduleId);
            groupMap.put(moduleId, group);

            final Collection<RequireDependencyManager.Node> deps = dependencyManager.getTransitiveDependenciesFor(moduleId, true);

            for (RequireDependencyManager.Node dep : deps) {
                group.addResource(createResourceFrom(dep));
            }

            group.addResource(createResourceFrom(dependencyManager.getNode(moduleId)));
            model.addGroup(group);
        }

        return groupMap;
    }

    private Resource createResourceFrom(RequireDependencyManager.Node dep) {
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(ResourceType.JS);
        resource.setUri(dep.path);
        return resource;
    }

    private RequireDependencyManager configureJavascripDependencyManager(final NodeList jsSources, String defaultPathOnDisk) throws IOException {
        RequireDependencyManager depManager = new RequireDependencyManager();

        for (int i = 0; i < jsSources.getLength(); i++) {
            Node jsSource = jsSources.item(i);
            ResourceDesc desc = parseSource((Element) jsSource, defaultPathOnDisk);
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
                depManager.addFile(path, file);
            }
        }

        depManager.validateGraph();

        return depManager;
    }

    private ResourceDesc parseSource(final Element sourceEl, String defaultPathOnDisk) {
        ResourceDesc desc = new ResourceDesc();

        if (!sourceEl.hasAttribute(WEBAPP_ATT)) {
            throw new AssertionError("No "+WEBAPP_ATT+" was found on "+ JS_SOURCE_EL);
        }

        desc.relativePath = sourceEl.getAttribute(WEBAPP_ATT);
        desc.relativePath = desc.relativePath.replace('\\', '/');
        if (!sourceEl.hasAttribute(PATH_ON_DISK_ATT)) {
            desc.pathOnDisk = defaultPathOnDisk;
        } else {
            desc.pathOnDisk = sourceEl.getAttribute(PATH_ON_DISK_ATT);
        }

        if (desc.pathOnDisk == null) {
            throw new AssertionError("No "+ PATH_ON_DISK_ATT +" was found on "+ JS_SOURCE_EL +" or parent Element");
        }

        if (isMavenBuild()) {
            desc.finalPath = new File(desc.pathOnDisk, desc.relativePath).getPath();
        } else {
            desc.finalPath = context.getServletContext().getRealPath(desc.relativePath);
        }


        desc.root = new File(desc.finalPath);

        return desc;
    }

    private boolean isMavenBuild() {
        return context.getServletContext() == null;
    }

    public String getSourcesXmlFile() {
        final String sourcesRawProperty = getConfigProperties().getProperty(WRO_SOURCES_KEY);
        if (context.getServletContext() != null) {
            final String[] split = sourcesRawProperty.split("WEB-INF/", 2);
            if (split.length == 2) {
                final String path = context.getServletContext().getRealPath("/WEB-INF/" + split[1]);
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

    public void setContext(ReadOnlyContext context) {
        this.context = context;
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
