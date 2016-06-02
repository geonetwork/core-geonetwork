package org.fao.geonet.wro4j;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests that javascript dependencies are correctly parsed from javascript files.
 * <p/>
 * User: Jesse Date: 11/21/13 Time: 11:42 AM
 */
public class ClosureRequireDependencyManagerTest {
    private ClosureRequireDependencyManager _depManager;

    static File getJsTestBaseDir() throws Exception {
        final Class<ClosureRequireDependencyManagerTest> cls = ClosureRequireDependencyManagerTest
            .class;
        final URL resource = cls.getResource(cls.getSimpleName() + ".class");
        if (resource == null) {
            throw new Error("Programming error");
        }
        return new File(resource.toURI()).getParentFile();
    }

    @Before
    public void configureTestFile() throws Exception {
        this._depManager = new ClosureRequireDependencyManager();
        final File rootOfSearch = getJsTestBaseDir();
        final Iterator<File> fileIterator = FileUtils.iterateFiles(rootOfSearch, new String[]{"js"}, true);

        while (fileIterator.hasNext()) {
            File file = fileIterator.next();

            if (!file.equals(rootOfSearch) && file.getName().endsWith(".js")) {
                _depManager.addFile(file.getPath().substring(rootOfSearch.getPath().length()), file, Collections.<String>emptySet());
            }
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFileDuplicateModuleId() throws Exception {
        _depManager.addFile("abc", "goog.provide('1a')", Collections.<String>emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFileNoProvideString() throws Exception {
        _depManager.addFile("abc", "goog.provide()", Collections.<String>emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFileNoRequireString() throws Exception {
        _depManager.addFile("abc", "goog.require('qrt)", Collections.<String>emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFileEmptyProvideString() throws Exception {
        _depManager.addFile("abc", "goog.provide('')", Collections.<String>emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFileEmptyRequireString() throws Exception {
        _depManager.addFile("abc", "goog.require('')", Collections.<String>emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFileCyclicDependencyGraph() throws Exception {
        _depManager.addFile("abc", "goog.provide('xa'); goog.require('xb')", Collections.<String>emptySet());
        _depManager.addFile("abc", "goog.provide('xb'); goog.require('xa')", Collections.<String>emptySet());
        _depManager.validateGraph();
    }

    @Test
    public void testAddFileNoProvide() throws Exception {
        final ClosureRequireDependencyManager.Node abc = _depManager.addFile("qrt/abc.js", "goog.require('ab2'", Collections.<String>emptySet());
        assertEquals("abc", abc.id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFileRequiresSelf() throws Exception {
        _depManager.addFile("abc", "goog.provide('xa'); goog.require('xa')", Collections.<String>emptySet());
        _depManager.validateGraph();
    }

    @Test
    public void testAddFileRequiresUsesDoubleQuotes() throws Exception {
        final ClosureRequireDependencyManager.Node node = _depManager.addFile("xb", "goog.provide('xb'); goog.require(\"3a\")",
            Collections.<String>emptySet());
        assertTrue(node.dependencyIds.contains("3a"));
    }

    @Test
    public void testValidateGraph() throws Exception {
        _depManager.validateGraph();
        // no errors indicates correct behaviour
    }

    @Test
    public void testAddFileProvidesUsesDoubleQuotes() throws Exception {
        final ClosureRequireDependencyManager.Node node = _depManager.addFile("xa", "goog.provide(\"xa\")",
            Collections.<String>emptySet());
        Assert.assertEquals("xa", node.id);
        final Set<String> allModuleIds = _depManager.getAllModuleIds();
        assertTrue(allModuleIds.toString(), allModuleIds.contains("xa"));
    }

    @Test
    public void testAddFileProvidesCanBeDeclaredAfterRequire() throws Exception {
        final ClosureRequireDependencyManager.Node node = _depManager.addFile("xb", "goog.require('3a'); goog.provide('xb');",
            Collections.<String>emptySet());
        Assert.assertEquals("xb", node.id);
        assertTrue(node.dependencyIds.contains("3a"));

    }

    @Test
    public void testLineEndingInRequireAndProvide() throws Exception {
        final ClosureRequireDependencyManager.Node node = _depManager.addFile("xb", "goog.\nrequire('3a'); goog.\n\rprovide('xb');",
            Collections.<String>emptySet());
        Assert.assertEquals("xb", node.id);
        assertTrue(node.dependencyIds.contains("3a"));

    }

    @Test
    public void testLineEndingInRequireAndProvide2() throws Exception {
        final ClosureRequireDependencyManager.Node node = _depManager.addFile("xb", "goog\n.require('3a'); goog\n.\rprovide('xb');",
            Collections.<String>emptySet());
        Assert.assertEquals("xb", node.id);
        assertTrue(node.dependencyIds.contains("3a"));

    }

    @Test
    public void testFindTransitiveDependenciesFor() throws Exception {
        ClosureRequireDependencyManager.Node node = _depManager.addFile("xb", "goog.provide('xb');", Collections.singleton("xb"));
        Assert.assertEquals("xb", node.id);
        assertFalse(node.isMinimized);
        assertTrue(node.dependencyIds.isEmpty());

        node = _depManager.addFile("xc", "goog.provide('xc');", Collections.singleton("xb"));
        Assert.assertEquals("xc", node.id);
        assertTrue(node.isMinimized);
        assertTrue(node.dependencyIds.isEmpty());
    }

    @Test
    public void testIsMinimized() throws Exception {
        List<String> a1Deps = Arrays.asList("3a", "2b", "3c", "3b", "2a");
        Collection<ClosureRequireDependencyManager.Node> depNodes = _depManager.getTransitiveDependenciesFor("1a", false);
        List<String> actualIds = Lists.transform(Lists.newArrayList(depNodes), new Function<ClosureRequireDependencyManager.Node,
            String>() {
            @Nullable
            @Override
            public String apply(@Nonnull ClosureRequireDependencyManager.Node input) {
                return input.id;
            }
        });
        assertEquals(a1Deps, actualIds);

        a1Deps = Arrays.asList("3c", "3a", "2b", "3b", "2a", "1a");
        depNodes = _depManager.getTransitiveDependenciesFor("1b", false);
        actualIds = Lists.transform(Lists.newArrayList(depNodes), new Function<ClosureRequireDependencyManager.Node, String>() {
            @Nullable
            @Override
            public String apply(@Nonnull ClosureRequireDependencyManager.Node input) {
                return input.id;
            }
        });
        assertEquals(a1Deps, actualIds);


    }

    @Test
    public void testListAllModuleIds() throws Exception {
        final Set<String> strings = _depManager.getAllModuleIds();
        final String allStrings = strings.toString();
        assertTrue(allStrings, strings.contains("1a"));
        assertTrue(allStrings, strings.contains("1b"));
        assertTrue(allStrings, strings.contains("2a"));
        assertTrue(allStrings, strings.contains("2b"));
        assertTrue(allStrings, strings.contains("3a"));
        assertTrue(allStrings, strings.contains("3b"));
        assertTrue(allStrings, strings.contains("3c"));
    }
}
