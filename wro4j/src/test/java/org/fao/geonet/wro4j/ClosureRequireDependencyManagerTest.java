package org.fao.geonet.wro4j;

import static org.junit.Assert.*;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Tests that javascript dependencies are correctly parsed from javascript files.
 *
 * User: Jesse
 * Date: 11/21/13
 * Time: 11:42 AM
 */
public class ClosureRequireDependencyManagerTest {
    private ClosureRequireDependencyManager _depManager;

    @Before
    public void configureTestFile() throws IOException {
        this._depManager = new ClosureRequireDependencyManager();
        final File rootOfSearch = getJsTestBaseDir();
        final Iterator<File> fileIterator = FileUtils.iterateFiles(rootOfSearch, new String[]{"js"}, true);

        while (fileIterator.hasNext()) {
            File file = fileIterator.next();

            if (!file.equals(rootOfSearch) && file.getName().endsWith(".js")) {
                _depManager.addFile(file.getPath().substring(rootOfSearch.getPath().length()), file);
            }
        }

    }

    static File getJsTestBaseDir() {
        final Class<ClosureRequireDependencyManagerTest> cls = ClosureRequireDependencyManagerTest
                .class;
        final URL resource = cls.getResource(cls.getSimpleName() + ".class");
        if (resource == null) {
            throw new Error("Programming error");
        }
        return new File(resource.getFile()).getParentFile();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFileDuplicateModuleId() throws Exception {
        _depManager.addFile("abc", "goog.provide('1a')");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testAddFileNoProvideString() throws Exception {
        _depManager.addFile("abc", "goog.provide()");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testAddFileNoRequireString() throws Exception {
        _depManager.addFile("abc", "goog.require()");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testAddFileEmptyProvideString() throws Exception {
        _depManager.addFile("abc", "goog.provide('')");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testAddFileEmptyRequireString() throws Exception {
        _depManager.addFile("abc", "goog.require('')");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testAddFileCyclicDependencyGraph() throws Exception {
        _depManager.addFile("abc", "goog.provide('xa'); goog.require('xb')");
        _depManager.addFile("abc", "goog.provide('xb'); goog.require('xa')");
        _depManager.validateGraph();
    }
    @Test(expected = IllegalArgumentException.class)
    public void testAddFileNoProvide() throws Exception {
        _depManager.addFile("abc", "goog.require('ab2'");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testAddFileRequiresSelf() throws Exception {
        _depManager.addFile("abc", "goog.provide('xa'); goog.require('xa')");
        _depManager.validateGraph();
    }
    @Test
    public void testAddFileRequiresUsesDoubleQuotes() throws Exception {
        final ClosureRequireDependencyManager.Node node = _depManager.addFile("xb", "goog.provide('xb'); goog.require(\"3a\")");
        assertTrue(node.dependencyIds.contains("3a"));
    }
    @Test
    public void testValidateGraph() throws Exception {
        _depManager.validateGraph();
        // no errors indicates correct behaviour
    }
    @Test
    public void testAddFileProvidesUsesDoubleQuotes() throws Exception {
        final ClosureRequireDependencyManager.Node node = _depManager.addFile("xa", "goog.provide(\"xa\")");
        Assert.assertEquals("xa", node.id);
        final Set<String> allModuleIds = _depManager.getAllModuleIds();
        assertTrue(allModuleIds.toString(), allModuleIds.contains("xa"));
    }
    @Test
    public void testAddFileProvidesCanBeDeclaredAfterRequire() throws Exception {
        final ClosureRequireDependencyManager.Node node = _depManager.addFile("xb", "goog.require('3a'); goog.provide('xb');");
        Assert.assertEquals("xb", node.id);
        assertTrue(node.dependencyIds.contains("3a"));

    }

    @Test
    public void testFindTransitiveDependenciesFor() throws Exception {
        List<String> a1Deps = Arrays.asList("3a", "2b", "3c", "3b", "2a");
        Collection<ClosureRequireDependencyManager.Node> depNodes = _depManager.getTransitiveDependenciesFor("1a", false);
        List<String> actualIds = Lists.transform(Lists.newArrayList(depNodes), new Function<ClosureRequireDependencyManager.Node, String>() {
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
