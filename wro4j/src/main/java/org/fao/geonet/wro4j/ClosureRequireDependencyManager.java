package org.fao.geonet.wro4j;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Reads Javascript files to determine the dependency graph between javascript files and allow
 * searching the dependency graph for all descendants of any node in the graph.
 * <p/>
 * Will detect cycles in graph.
 * <p/>
 * User: Jesse Date: 11/21/13 Time: 10:45 AM
 */
public class ClosureRequireDependencyManager {

    private static final String ENCODING = System.getProperty("file.encoding", "UTF-8");
    private Map<String, Node> _modules = new HashMap<String, Node>();

    /**
     * Add a javascript file to process.
     *
     * @param path           the path to the file that will be listed in the WRO4J model to identify
     *                       the javascript.
     * @param javascriptFile the file containing the javascript
     * @return a node object representing the javascript file.
     */
    @Nonnull
    public Node addFile(@Nonnull String path, @Nonnull File javascriptFile, @Nonnull Set<String> notMinimized) throws IOException {
        String javascript = Files.toString(javascriptFile, Charset.forName(ENCODING));
        return addFile(path, javascript, notMinimized);
    }

    /**
     * Add a javascript file to process.
     *
     * @param path       the path to the file that will be listed in the WRO4J model to identify the
     *                   javascript.
     * @param javascript the javascript
     * @return a node object representing the javascript file.
     */
    @Nonnull
    public Node addFile(@Nonnull String path, @Nonnull String javascript, @Nonnull Set<String> notMinimized) {
        boolean isMinimized = true;
        for (String s : notMinimized) {
            if (path.endsWith(s) || path.endsWith(s.replace('/', '\\'))) {
                isMinimized = false;
                break;
            }
        }
        Node node = new Node(path, javascript, isMinimized);
        if (_modules.containsKey(node.id)) {
            throw new IllegalArgumentException("Both '" + node.path + "' and '" + _modules.get(node.id) + "' have the same provide id: " +
                "" + node.id);
        }
        this._modules.put(node.id, node);
        return node;
    }

    /**
     * Get the list of all (transitive included) dependencies of a particular module.
     *
     * @param moduleId the root module.
     * @return the ordered list of dependency paths.  The order is defined as: {deps of
     * dependencyA}, dependencyA, {deps of dependencyB}, dependencyB, ...
     */
    public Collection<Node> getTransitiveDependenciesFor(String moduleId, boolean includeSelf) {
        Node module = getNode(moduleId);
        LinkedHashSet<Node> dependencies = new LinkedHashSet<Node>();
        for (String dependencyId : module.dependencyIds) {
            dependencies.addAll(getTransitiveDependenciesFor(dependencyId, false));
            dependencies.add(getNode(dependencyId));
        }
        return dependencies;
    }

    /**
     * Get the list of all modules.
     *
     * @return the list of all modules.
     */
    public Set<String> getAllModuleIds() {
        return _modules.keySet();
    }

    public Node getNode(String moduleId) {
        Node module = this._modules.get(moduleId);
        isTrue(module != null, moduleId + " does not exist");
        return module;
    }

    private void isTrue(boolean check, String failureMsg) {
        if (!check) {
            throw new AssertionError(failureMsg);
        }
    }

    /**
     * Validate that the graph does not have cycles and all dependencies exist.
     */
    public void validateGraph() {

        for (String moduleId : _modules.keySet()) {
            // use Floyd's cycle-finding algorithm to detect cycle
            detectCyle(moduleId, moduleId, new LinkedHashSet<String>());
        }

    }

    private void detectCyle(String moduleId, String sourceModule, Set<String> previousNodes) {
        if (previousNodes.contains(moduleId)) {
            throw new IllegalArgumentException("Module: " + sourceModule + " contains a cycle in its dependency graph: " + previousNodes);
        }
        previousNodes.add(moduleId);
        final Node moduleNode = _modules.get(moduleId);
        if (moduleNode == null) {
            throw new IllegalArgumentException("Found a dependency: " + moduleId + " that does not exist.  It is a transitive " +
                "dependency of " + sourceModule + ".  Dependency path " + previousNodes);
        }
        for (String node : moduleNode.dependencyIds) {
            detectCyle(node, sourceModule, previousNodes);
        }
        previousNodes.remove(moduleId);
    }

    /**
     * A node in the Dependency Graph.
     */
    static class Node {
        static final String PROVIDES_PATTERN_STRING = "goog\\s*\\.\\s*provide\\s*\\(\\s*(.*?)\\s*\\)";
        static final String REQUIRE_PATTERN_STRING = "goog\\s*\\.\\s*require\\s*\\(\\s*(.*?)\\s*\\)";

        private static final Pattern SCAN_PATTERN = Pattern.compile("(" + PROVIDES_PATTERN_STRING + ")|(" + REQUIRE_PATTERN_STRING + ")");
        final String id;
        final String path;
        final List<String> dependencyIds = new ArrayList<String>();
        final boolean isMinimized;

        private Node(@Nonnull String path, @Nonnull String javascript, boolean isMinimized) {
            this.path = path;
            this.isMinimized = isMinimized;
            this.id = parseJavascript(path, javascript);
        }

        private String parseJavascript(String path, String javascript) {
            String id = null;
            final Matcher matcher = SCAN_PATTERN.matcher(javascript);
            while (matcher != null && matcher.find()) {
                final String match = matcher.group().replace('"', '\'');
                int idStartIdx = match.indexOf('\'') + 1;
                int idEndIdx = match.indexOf('\'', idStartIdx);
                if (idEndIdx < 0) {
                    throw new IllegalArgumentException(match + "does not have an id string");
                }
                String currentId = match.substring(idStartIdx, idEndIdx);
                idNonEmpty(match, currentId);
                if (match.contains("goog") && match.contains("require")) {
                    dependencyIds.add(currentId);
                } else {
                    if (id != null) {
                        throw new IllegalArgumentException("More than one 'goog.provide' was found in the javascript file: " + path);
                    }
                    id = currentId;
                }
            }
            if (id == null) {
                final int index1 = path.lastIndexOf('/');
                final int index2 = path.lastIndexOf('\\');
                if (Math.max(index1, index2) < 0) {
                    return Files.getNameWithoutExtension(path);
                } else {
                    return Files.getNameWithoutExtension(path.substring(Math.max(index1, index2) + 1));
                }
            }
            return id;
        }

        private void idNonEmpty(String match, String provide) {
            if (provide == null || provide.trim().isEmpty()) {
                throw new IllegalArgumentException(match + " has an empty id which is not permitted");
            }
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            if (!id.equals(node.id)) return false;
            if (!path.equals(node.path)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + path.hashCode();
            return result;
        }
    }

}
