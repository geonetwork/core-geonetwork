package org.fao.geonet.wro4j;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.StopWatch;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
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
    public static final String JS_SOURCE = "jsSource";
    public static final String CSS_SOURCE = "cssSource";
    public static final String WEBAPP_ATT = "webapp";
    public static final String PATH_ON_DISK_EL = "pathOnDisk";
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
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(sourcesXmlFile);

            final NodeList jsSources = doc.getElementsByTagName(JS_SOURCE);

            final WroModel model = new WroModel();

            final ClosureRequireDependencyManager dependencyManager = configureJavascripDependencyManager(jsSources);

            Map<String, Group> groups = addJavascriptGroups(model, dependencyManager);

            final NodeList cssSources = doc.getElementsByTagName(CSS_SOURCE);
            addCssGroups(model, groups, cssSources);

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

            return model;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            stopWatch.stop();
            LOG.info(stopWatch.prettyPrint());
        }
    }

    private void addCssGroups(final WroModel model, final Map<String, Group> groups, final NodeList cssSources) {

        HashSet<String> cssGroups = new HashSet<String>();

        for (int i = 0; i < cssSources.getLength(); i++) {
            Node cssSource = cssSources.item(i);

            ResourceDesc desc = parseSource((Element) cssSource);

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

    private Map<String, Group> addJavascriptGroups(final WroModel model, final ClosureRequireDependencyManager dependencyManager) {
        final Set<String> moduleIds = dependencyManager.getAllModuleIds();
        Map<String, Group> groupMap = new HashMap<String, Group>((int) (moduleIds.size() * 1.5));

        for (String moduleId : moduleIds) {
            Group group = new Group(moduleId);
            groupMap.put(moduleId, group);

            final Collection<ClosureRequireDependencyManager.Node> deps = dependencyManager.getTransitiveDependenciesFor(moduleId, true);

            for (ClosureRequireDependencyManager.Node dep : deps) {
                group.addResource(createResourceFrom(dep));
            }

            group.addResource(createResourceFrom(dependencyManager.getNode(moduleId)));
            model.addGroup(group);
        }

        return groupMap;
    }

    private Resource createResourceFrom(ClosureRequireDependencyManager.Node dep) {
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(ResourceType.JS);
        resource.setUri(dep.path);
        return resource;
    }

    private ClosureRequireDependencyManager configureJavascripDependencyManager(final NodeList jsSources) throws IOException {
        ClosureRequireDependencyManager depManager = new ClosureRequireDependencyManager();

        for (int i = 0; i < jsSources.getLength(); i++) {
            Node jsSource = jsSources.item(i);
            ResourceDesc desc = parseSource((Element) jsSource);
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

    private ResourceDesc parseSource(final Element sourceEl) {
        ResourceDesc desc = new ResourceDesc();

        desc.relativePath = sourceEl.getAttribute(WEBAPP_ATT).replace('\\', '/');
        desc.pathOnDisk = sourceEl.getAttribute(PATH_ON_DISK_EL);
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
