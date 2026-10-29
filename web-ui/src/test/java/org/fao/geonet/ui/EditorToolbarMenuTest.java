/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.ui;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for the configurable metadata editor toolbar
 * (<code>mods.editor.recordEditorCustomMenu</code>) and the
 * <code>RECORD_EDITOR_MENU</code> static page section.
 * <p>
 * The AngularJS sources are evaluated with minimal <code>goog</code> and
 * <code>angular</code> stubs so that directive link functions can be tested
 * without a browser. Tests are skipped if no JavaScript engine is available
 * in the JVM (Nashorn is shipped with Java 11).
 */
public class EditorToolbarMenuTest {

    private static final String[] SCRIPTS = {
        "/catalog/components/toolbar/ToolbarDirective.js",
        "/catalog/components/pages/GnStaticPagesDirective.js"
    };

    private static final String HARNESS =
        "var registry = {constants: {}, directives: {}};\n" +
        "var goog = {provide: function () {}, require: function () {}};\n" +
        "var console = {warnings: [], warn: function (m) { console.warnings.push(m); }};\n" +
        "var angular = {\n" +
        "  module: function () {\n" +
        "    var m = {};\n" +
        "    m.constant = function (name, value) { registry.constants[name] = value; return m; };\n" +
        "    m.directive = function (name, def) { registry.directives[name] = def; return m; };\n" +
        "    ['factory', 'service', 'filter', 'controller', 'config', 'run', 'value']\n" +
        "      .forEach(function (k) { m[k] = function () { return m; }; });\n" +
        "    return m;\n" +
        "  },\n" +
        "  isArray: function (v) { return Object.prototype.toString.call(v) === '[object Array]'; },\n" +
        "  isObject: function (v) { return v !== null && typeof v === 'object'; },\n" +
        "  isDefined: function (v) { return typeof v !== 'undefined'; },\n" +
        "  forEach: function (list, fn) { for (var i = 0; i < list.length; i++) { fn(list[i], i); } }\n" +
        "};\n" +
        "function instantiate(def, deps) { return def[def.length - 1].apply(null, deps || []); }\n" +
        // Synchronous promise stub for gnStaticPagesService.loadPages
        "function staticPagesService(pages) {\n" +
        "  return {\n" +
        "    requests: [],\n" +
        "    loadPages: function (language, section) {\n" +
        "      this.requests.push(section);\n" +
        "      return {then: function (ok) { ok({data: pages}); }};\n" +
        "    }\n" +
        "  };\n" +
        "}\n" +
        // Render a gn-static-page-menu and return the labels of the visible pages
        "function renderStaticPageMenu(section, pageIds, pages, context) {\n" +
        "  var directive = instantiate(registry.directives.gnStaticPageMenu,\n" +
        "    [staticPagesService(pages), {gnCfg: {}}]);\n" +
        "  var scope = {pageId: pageIds, language: 'eng', section: section,\n" +
        "    renderAsButton: 'true', context: context, $watch: function () {}};\n" +
        "  directive.link(scope);\n" +
        "  return scope.pagesMenu.map(function (p) {\n" +
        "    return p.type === 'submenu'\n" +
        "      ? p.label + ':' + p.pages.map(function (s) { return s.pageId; }).join('|')\n" +
        "      : p.pageId;\n" +
        "  });\n" +
        "}\n" +
        "function page(id, approved, nonApproved, noWorkflow) {\n" +
        "  return {pageId: id, label: id, format: 'LINK', link: 'https://e.org/{uuid}',\n" +
        "    showOnApproved: approved, showOnNonApproved: nonApproved,\n" +
        "    showWhenWorkflowDisabled: noWorkflow};\n" +
        "}\n" +
        "var pages = [\n" +
        "  page('always', true, true, true),\n" +
        "  page('approvedOnly', true, false, false),\n" +
        "  page('draftOnly', false, true, false),\n" +
        "  page('noWorkflowOnly', false, false, true)\n" +
        "];\n" +
        "var allIds = ['always', 'approvedOnly', 'draftOnly', 'noWorkflowOnly'];\n";

    private ScriptEngine engine;

    @Before
    public void setUp() throws IOException, ScriptException {
        engine = new ScriptEngineManager().getEngineByName("JavaScript");
        Assume.assumeNotNull(engine);

        engine.eval(HARNESS);
        for (String script : SCRIPTS) {
            try (InputStream in = getClass().getResourceAsStream(script)) {
                assertNotNull("Script not found on classpath: " + script, in);
                engine.eval(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
    }

    private String js(String expression) throws ScriptException {
        return String.valueOf(engine.eval("JSON.stringify(" + expression + ")"));
    }

    private void linkEditorToolbar(String editorConfig) throws ScriptException {
        engine.eval(
            "var controllerScope = {groupOwner: 2};\n" +
            "var toolbarScope = Object.create(controllerScope);\n" +
            "var toolbar = instantiate(registry.directives.gnRecordEditorToolbar, [\n" +
            "  registry.constants.GN_DEFAULT_RECORD_EDITOR_MENU,\n" +
            "  {gnCfg: {mods: {editor: " + editorConfig + "}}}]);\n" +
            "toolbar.link.pre(toolbarScope);\n");
    }

    @Test
    public void defaultEditorMenuContainsAllBuiltInEntries() throws ScriptException {
        assertEquals(
            "[\"gn-editor-categories-menu\",\"gn-editor-group-menu\",\"gn-editor-tools-menu\"," +
                "\"gn-editor-validate-menu\",\"gn-editor-cancel-menu\",\"gn-editor-close-menu\"," +
                "\"gn-editor-save-menu\",\"gn-editor-view-menu\"]",
            js("registry.constants.GN_DEFAULT_RECORD_EDITOR_MENU"));
    }

    @Test
    public void editorToolbarUsesDefaultMenuWhenNotConfigured() throws ScriptException {
        for (String config : new String[]{"{}", "{recordEditorCustomMenu: []}",
            "{recordEditorCustomMenu: null}"}) {
            linkEditorToolbar(config);
            assertEquals(config,
                js("registry.constants.GN_DEFAULT_RECORD_EDITOR_MENU"),
                js("toolbarScope.recordEditorMenu"));
        }
    }

    @Test
    public void editorToolbarUsesConfiguredMenu() throws ScriptException {
        linkEditorToolbar("{recordEditorCustomMenu: ['gn-editor-save-menu', 'validator', " +
            "{externalTools: {icon: 'fa-wrench', items: ['portal']}}, 'gn-editor-close-menu']}");
        assertEquals(
            "[\"gn-editor-save-menu\",\"validator\"," +
                "{\"externalTools\":{\"icon\":\"fa-wrench\",\"items\":[\"portal\"]}}," +
                "\"gn-editor-close-menu\"]",
            js("toolbarScope.recordEditorMenu"));
    }

    @Test
    public void editorToolbarIdentifiesStaticPages() throws ScriptException {
        linkEditorToolbar("{}");
        assertEquals("false", js("toolbarScope.isPage('gn-editor-save-menu')"));
        assertEquals("true", js("toolbarScope.isPage('validator')"));
        assertEquals("true", js("toolbarScope.isPage({externalTools: {items: ['a']}})"));
    }

    @Test
    public void editorToolbarSharesStateBetweenEntries() throws ScriptException {
        linkEditorToolbar("{}");
        // Initially inherited from the editor controller scope
        assertEquals("2", js("toolbarScope.editorToolbarState.groupOwner"));

        // Entries are rendered in child scopes (ng-repeat / ng-if). The group
        // updater assigns the new owner, which must be visible to the
        // category updater rendered in another child scope.
        engine.eval(
            "var groupEntryScope = Object.create(toolbarScope);\n" +
            "var categoryEntryScope = Object.create(toolbarScope);\n" +
            "groupEntryScope.editorToolbarState.groupOwner = 5;\n");
        assertEquals("5", js("categoryEntryScope.editorToolbarState.groupOwner"));
        // Shared state is isolated from the toolbar/controller primitive.
        assertEquals("2", js("toolbarScope.groupOwner"));
        assertEquals("2", js("controllerScope.groupOwner"));
    }

    @Test
    public void editorMenuLoadsPagesFromRecordEditorSection() throws ScriptException {
        engine.eval(
            "var service = staticPagesService(pages);\n" +
            "var d = instantiate(registry.directives.gnStaticPageMenu, [service, {gnCfg: {}}]);\n" +
            "d.link({pageId: 'always', language: 'eng', section: 'record_editor_menu',\n" +
            "  $watch: function () {}});\n");
        assertEquals("[\"record_editor_menu\"]", js("service.requests"));
    }

    @Test
    public void editorMenuAppliesWorkflowVisibility() throws ScriptException {
        assertEquals("[\"always\",\"noWorkflowOnly\"]",
            js("renderStaticPageMenu('record_editor_menu', allIds, pages, " +
                "{uuid: 'u', id: 1, mdStatus: 1, isWorkflowEnabled: false})"));
    }

    @Test
    public void editorMenuIgnoresApprovalStatusWhenWorkflowEnabled() throws ScriptException {
        // An approved copy cannot be edited, so approval-based flags are not
        // relevant in the editor: all pages are shown when workflow is enabled.
        for (String status : new String[]{"1", "2", "4"}) {
            assertEquals(status,
                "[\"always\",\"approvedOnly\",\"draftOnly\",\"noWorkflowOnly\"]",
                js("renderStaticPageMenu('record_editor_menu', allIds, pages, " +
                    "{uuid: 'u', id: 1, mdStatus: " + status + ", isWorkflowEnabled: true})"));
        }
    }

    @Test
    public void recordViewMenuAppliesApprovalVisibility() throws ScriptException {
        assertEquals("[\"always\",\"noWorkflowOnly\"]",
            js("renderStaticPageMenu('record_view_menu', allIds, pages, " +
                "{mdStatus: 1, isWorkflowEnabled: false})"));
        assertEquals("[\"always\",\"approvedOnly\"]",
            js("renderStaticPageMenu('record_view_menu', allIds, pages, " +
                "{mdStatus: 2, isWorkflowEnabled: true})"));
        assertEquals("[\"always\",\"draftOnly\"]",
            js("renderStaticPageMenu('record_view_menu', allIds, pages, " +
                "{mdStatus: 4, isWorkflowEnabled: true})"));
    }

    @Test
    public void editorMenuSupportsSubmenus() throws ScriptException {
        assertEquals("[\"tools:always|noWorkflowOnly\"]",
            js("renderStaticPageMenu('record_editor_menu', " +
                "[{tools: {icon: 'fa-wrench', items: ['always', 'approvedOnly', 'noWorkflowOnly']}}], " +
                "pages, {mdStatus: 1, isWorkflowEnabled: false})"));
    }

    @Test
    public void otherSectionsIgnoreWorkflowVisibility() throws ScriptException {
        assertEquals("[\"always\",\"approvedOnly\",\"draftOnly\",\"noWorkflowOnly\"]",
            js("renderStaticPageMenu('top', allIds, pages, " +
                "{mdStatus: 1, isWorkflowEnabled: false})"));
    }
}



