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

(function () {
  goog.provide("gn_directory_controller");

  goog.require("gn_catalog_service");
  goog.require("gn_directoryassociatedmd");
  goog.require("gn_facets");
  goog.require("gn_mdtypewidget");
  goog.require("gn_mdtypeinspirevalidationwidget");
  goog.require("gn_draftvalidationwidget");
  goog.require("gn_batchtask");

  var module = angular.module("gn_directory_controller", [
    "gn_catalog_service",
    "gn_facets",
    "gn_directoryassociatedmd",
    "pascalprecht.translate",
    "gn_mdtypewidget",
    "gn_mdtypeinspirevalidationwidget",
    "gn_draftvalidationwidget",
    "gn_batchtask"
  ]);

  /**
   * Controller to create new metadata record.
   */
  module.controller("GnDirectoryController", [
    "$scope",
    "$routeParams",
    "$http",
    "$rootScope",
    "$translate",
    "$compile",
    "gnSearchManagerService",
    "gnUtilityService",
    "gnEditor",
    "gnUrlUtils",
    "gnCurrentEdit",
    "gnMetadataManager",
    "gnMetadataActions",
    "gnGlobalSettings",
    "gnSearchSettings",
    "gnConfig",
    "gnConfigService",
    function (
      $scope,
      $routeParams,
      $http,
      $rootScope,
      $translate,
      $compile,
      gnSearchManagerService,
      gnUtilityService,
      gnEditor,
      gnUrlUtils,
      gnCurrentEdit,
      gnMetadataManager,
      gnMetadataActions,
      gnGlobalSettings,
      gnSearchSettings,
      gnConfig,
      gnConfigService
    ) {
      // option to allow only administrators
      // to validate a subtemplate
      // once validated, only administrators can
      // Edit/Delete/Set privileges/Validate/Reject
      // If false, user who can edit, can validate/reject
      $scope.restrictValidationToAdmin = false;

      // when subtemplate is validated,
      // it is also published to internet group (ie. = public)
      $scope.publishToAllWhenValidated = true;

      $scope.gnConfig = gnConfig;
      $scope.hasEntries = false;
      $scope.mdList = null;
      $scope.activeType = null;
      $scope.activeEntry = null;

      var directorySearchSettings = gnGlobalSettings.gnCfg.mods.directory || {};

      $scope.facetConfig = directorySearchSettings.facetConfig;

      $scope.defaultSearchObj = {
        selectionBucket: "d101",
        configId: "directory",
        any: "",
        params: {
          sortBy: directorySearchSettings.sortBy || gnSearchSettings.sortBy,
          isTemplate: ["s"],
          from: 1,
          to: 20,
          queryBase: directorySearchSettings.queryBase || gnSearchSettings.queryBase
        },
        sortbyValues:
          directorySearchSettings.sortbyValues || gnSearchSettings.sortbyValues
      };

      $scope.searchObj = angular.extend({}, $scope.defaultSearchObj);
      $scope.paginationInfo = {
        pages: -1,
        currentPage: 1,
        hitsPerPage: 20
      };

      // can be: newEntry, newTemplate, editEntry, editTemplate
      $scope.currentEditorAction = "";

      // a list of templates (simplified index objects)
      $scope.templates = [];

      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);

      var dataTypesToExclude = [];

      // A map of icon to use for each types
      var icons = {
        "gmd:CI_ResponsibleParty": "fa-user",
        "cit:CI_Responsibility": "fa-user",
        "gmd:MD_Distribution": "fa-link"
      };

      // List of record type to not take into account
      // Could be avoided if a new index field is created FIXME ?
      var defaultType = "gmd:CI_ResponsibleParty";
      var unknownType = "unknownType";
      var fullPrivileges = "true";

      gnConfigService.load();

      $scope.selectType = function (type) {
        $scope.activeType = type;
        $scope.getEntries(type);
      };
      $scope.getTypeIcon = function (type) {
        return icons[type] || "fa-file-o";
      };

      var init = function () {
        $http
          .get("../api/groups?profile=Editor", { cache: true })
          .then(function (response) {
            $scope.groups = response.data;
          });

        refreshEntriesInfo();
      };

      // this refreshes the entry types list & templates available
      // it does NOT fetch actual entries
      var refreshEntriesInfo = function () {
        // fetch templates list & return simplified objects to be used
        // in the template dropdown
        $http
          .post(
            "../api/search/records/_search",
            {
              _source: { includes: ["uuid", "root", "resourceTitle*", "isTemplate"] },
              query: {
                bool: {
                  must: [{ terms: { isTemplate: ["t"] } }]
                }
              },
              size: 1000
            },
            { cache: true }
          )
          .then(function (r) {
            if (r.data.hits.total.value > 0) {
              $scope.templates = r.data.hits.hits.map(function (md) {
                return {
                  root: md._source.root,
                  id: md.id,
                  uuid: md._source.uuid,
                  edit: md._source.edit,
                  selected: md._source.selected,
                  isTemplate: md._source.isTemplate,
                  resourceTitle: md._source.resourceTitleObject
                    ? md._source.resourceTitleObject.default
                    : md._source.resourceTitle
                };
              });
            }
          });

        // fetch all entries + templates
        var entryType = "s or t";
        $http
          .post(
            "../api/search/records/_search",
            {
              size: 0,
              aggs: {
                type: {
                  terms: {
                    field: "root",
                    size: 100
                  }
                }
              },
              query: {
                bool: {
                  must: [{ terms: { isTemplate: ["t", "s"] } }]
                }
              }
            },
            { cache: true }
          )
          .then(function (r) {
            $scope.hasEntries = r.data.hits.total.value > 0;
            if ($scope.hasEntries) {
              $scope.mdTypes = r.data.aggregations.type.buckets.map(function (type) {
                return {
                  name: type.key,
                  count: type.doc_count
                };
              });

              $scope.mdTypes.sort(function (a, b) {
                var nameA = a.name;
                var nameB = b.name;
                return nameA < nameB ? -1 : nameA > nameB ? 1 : 0;
              });
              var typeNames = $scope.mdTypes.map(function (t) {
                return t.name;
              });

              // Select the default one or the first one
              if ($scope.activeType && $.inArray(defaultType, typeNames) !== -1) {
                $scope.selectType($scope.activeType);
              } else if (defaultType && $.inArray(defaultType, typeNames) !== -1) {
                $scope.selectType(defaultType);
              } else if ($scope.mdTypes[0]) {
                $scope.selectType($scope.mdTypes[0].name);
              } else {
                // No templates available ?
              }
            }
          });
      };

      /**
       * Get all the templates for a given type.
       */
      $scope.getEntries = function (type) {
        $scope.$broadcast("resetSearch", $scope.defaultSearchObj.params);
        if (type) {
          $scope.searchObj.params.root = type;
          $scope.defaultSearchObj.params.root = type;
        } else {
          delete $scope.searchObj.params.root;
          delete $scope.defaultSearchObj.params.root;
        }
        $scope.$broadcast("clearResults");
        $scope.$broadcast("search");
        return false;
      };

      /**
       * Update the form according to the target tab
       * properties and save.
       * FIXME: duplicate from EditorController
       */
      $scope.switchToTab = function (tabIdentifier, mode) {
        //          $scope.tab = tabIdentifier;
        //          FIXME: this trigger an edit
        //          better to use ng-model in the form ?
        $("#currTab")[0].value = tabIdentifier;
        $("#flat")[0].value = mode === "flat";
        $scope.save(true);
      };
      /**
       * FIXME: duplicate from EditorController
       */
      $scope.add = function (ref, name, insertRef, position, attribute) {
        if (attribute) {
          // save the form and add attribute
          // after save is done. When adding an attribute
          // the snippet returned contains the current field
          // and the newly created attributes.
          // Save to not lose current edits in main field.
          gnEditor
            .save(false)
            .then(function () {
              gnEditor.add(gnCurrentEdit.id, ref, name, insertRef, position, attribute);
            })
            .then(
              function () {
                // success. Nothing to do.
              },
              function (rejectedValue) {
                $rootScope.$broadcast("StatusUpdated", {
                  title: $translate.instant("runServiceError"),
                  error: rejectedValue,
                  timeout: 0,
                  type: "danger"
                });
              }
            );
        } else {
          gnEditor.add(gnCurrentEdit.id, ref, name, insertRef, position, attribute);
        }
        return false;
      };
      $scope.addChoice = function (ref, name, insertRef, position) {
        gnEditor.addChoice(gnCurrentEdit.id, ref, name, insertRef, position);
        return false;
      };
      $scope.remove = function (ref, parent, domRef) {
        gnEditor.remove(gnCurrentEdit.id, ref, parent, domRef);
        return false;
      };
      $scope.removeAttribute = function (ref) {
        gnEditor.removeAttribute(gnCurrentEdit.id, ref);
        return false;
      };
      $scope.save = function (refreshForm) {
        var promise = gnEditor.save(refreshForm).then(
          function (form) {
            $scope.savedStatus = gnCurrentEdit.savedStatus;
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("saveMetadataSuccess"),
              timeout: 2
            });
          },
          function (error) {
            $scope.savedStatus = gnCurrentEdit.savedStatus;
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("saveMetadataError"),
              error: error,
              timeout: 0,
              type: "danger"
            });
          }
        );
        $scope.savedStatus = gnCurrentEdit.savedStatus;
        return promise;
      };
      $scope.saveAndClose = function () {
        return gnEditor.save(false, null, true).then(
          function (form) {
            $scope.gnCurrentEdit = "";
            $scope.closeEditor();
            refreshEntriesInfo();
          },
          function (error) {
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("saveMetadataError"),
              error: error,
              timeout: 0,
              type: "danger"
            });
          }
        );
      };
      $scope.switchTypeAndSave = function (refreshForm) {
        gnCurrentEdit.isTemplate = gnCurrentEdit.isTemplate === "t" ? "s" : "t";
        $("#template")[0].value = gnCurrentEdit.isTemplate;
        if ($scope.activeEntry) {
          $scope.activeEntry.isTemplate = gnCurrentEdit.isTemplate;
        }
        return $scope.save(refreshForm);
      };

      /**
       * Update textarea containing XML when the ACE editor change.
       * See form-builder-xml.xsl.
       */
      $scope.xmlEditorChange = function (e) {
        // TODO: Here we could check if XML is valid based on ACE info
        // and disable save action ?
        $("textarea[name=data]").val(e[1].getSession().getValue());
      };
      $scope.xmlEditorLoaded = function (e) {
        // TODO: Adjust height of editor based on screen size ?
      };
      /**
       * When the form is loaded, this function is called.
       * Use it to retrieve form variables or initialize
       * elements eg. tooltip ?
       */
      $scope.onFormLoad = function () {
        gnEditor.onFormLoad();
      };

      // Counter to force editor refresh when
      // switching from one entry to another
      var i = 0;

      $scope.importEntry = function (xml) {
        gnMetadataManager
          .importFromXml(gnUrlUtils.toKeyValue($scope.importData), xml)
          .then(function (r) {
            if (r.status === 400) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("directoryManagerError"),
                error: r.data,
                timeout: 0,
                type: "danger"
              });
            } else {
              refreshEntriesInfo();
              $scope.closeEditor();
            }
          })
          .catch(function (f) {
            if (f.status === 400) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("directoryManagerError"),
                error: f.data,
                timeout: 0,
                type: "danger"
              });
            }
          });
      };

      // ACTIONS

      $scope.delEntry = function (e) {
        $scope.delEntryId = e.id;
        $("#gn-confirm-delete").modal("show");
      };
      $scope.confirmDelEntry = function (e) {
        if (!$scope.delEntryId) {
          return;
        }
        gnMetadataManager.remove($scope.delEntryId).then(refreshEntriesInfo);
        $scope.delEntryId = null;
      };

      $scope.copyEntry = function (e) {
        //md.create?id=181&group=2&isTemplate=s&currTab=simple
        gnMetadataManager
          .copy(
            e.id,
            $scope.importData.group,
            fullPrivileges,
            e.isTemplate === "t" ? "TEMPLATE_OF_SUB_TEMPLATE" : "SUB_TEMPLATE"
          )
          .then(refreshEntriesInfo);
      };

      // this is not used for now
      $scope.convertToTemplate = function (e) {
        if (e.isTemplate !== "s") {
          $rootScope.$broadcast("StatusUpdated", {
            title: $translate.instant("notADirectoryEntry"),
            error: "",
            timeout: 0,
            type: "danger"
          });
          return;
        }

        // conversion to template is done by duplicating into template type
        // the original entry is kept
        gnMetadataManager
          .copy(e.id, $scope.importData.group, fullPrivileges, "TEMPLATE_OF_SUB_TEMPLATE")
          .then(refreshEntriesInfo);
      };

      $scope.createFromTemplate = function (e) {
        if (e.isTemplate !== "t") {
          $rootScope.$broadcast("StatusUpdated", {
            title: $translate.instant("notADirectoryEntryTemplate"),
            error: "",
            timeout: 0,
            type: "danger"
          });
          return;
        }

        // a copy of the template is created & opened
        gnMetadataManager
          .copy(e.uuid, $scope.importData.group, fullPrivileges, "SUB_TEMPLATE")
          .then(function (response) {
            refreshEntriesInfo();
            return gnMetadataManager.getMdObjById(response.data, ["s", "t"]);
          })
          .then(function (md) {
            $scope.startEditing(md);
          });
      };

      $scope.validateEntry = function (e) {
        gnMetadataManager
          .validateDirectoryEntry(e.id, true)
          .then(function () {
            if ($scope.publishToAllWhenValidated) {
              gnMetadataActions.publish(e, undefined, undefined, $scope);
            }
            refreshEntriesInfo();
            return gnMetadataManager.getMdObjById(e.id, ["s", "t"]);
          })
          .then(function (e) {
            if ($scope.activeEntry) {
              $scope.activeEntry = e;
            }
          });
      };

      $scope.rejectEntry = function (e) {
        gnMetadataManager
          .validateDirectoryEntry(e.id, false)
          .then(function () {
            refreshEntriesInfo();
            return gnMetadataManager.getMdObjById(e.id, ["s", "t"]);
          })
          .then(function (e) {
            if ($scope.activeEntry) {
              $scope.activeEntry = e;
            }
          });
      };

      $scope.importData = {
        metadataType: "SUB_TEMPLATE",
        group: null
      };

      // begin creation of a new entry
      $scope.startImporting = function (asTemplate) {
        $scope.activeEntry = null;
        $scope.currentEditorAction = asTemplate ? "newTemplate" : "newEntry";

        // import data depends on type (template or entry)
        $scope.importData.metadataType = asTemplate
          ? "TEMPLATE_OF_SUB_TEMPLATE"
          : "SUB_TEMPLATE";
        $scope.importData.group =
          gnConfig["system.metadatacreate.preferredGroup"] || $scope.groups[0].id;
      };

      // begin edition of an entry
      $scope.startEditing = function (e) {
        $scope.activeEntry = e;
        $scope.currentEditorAction = e.isTemplate === "t" ? "editTemplate" : "editEntry";

        var id = e.id;
        angular.extend(gnCurrentEdit, {
          id: id,
          formId: "#gn-editor-" + id,
          containerId: "#gn-editor-container-" + id,
          tab: "simple",
          displayTooltips: false,
          compileScope: $scope,
          sessionStartTime: moment(),
          isTemplate: e.isTemplate
        });

        $scope.gnCurrentEdit = gnCurrentEdit;

        $scope.editorFormUrl =
          gnEditor.buildEditUrlPrefix("editor") +
          "&starteditingsession=yes&random=" +
          i++;

        gnEditor.load($scope.editorFormUrl).then(function () {
          // $scope.onFormLoad();
        });
      };

      $scope.closeEditor = function (e) {
        $scope.activeEntry = null;
        $scope.currentEditorAction = "";
        $scope.xml = "";
      };

      $scope.startPermissionsEdit = function (e) {
        $scope.activeEntry = e;
        $("#gn-share").modal("show");
      };
      $scope.closePermissionsEdit = function () {
        // clear active entry if privileges were updated from the list
        if (!$scope.currentEditorAction) {
          $scope.activeEntry = null;
          $scope.$apply();
        }
      };
      // close modal on privileges update
      $scope.$on("PrivilegesUpdated", function () {
        $("#gn-share").modal("hide");
      });

      // switch to templates (b === true) or entries (b === false)
      $scope.showTemplates = function (b) {
        $scope.searchObj.params.isTemplate = b === true ? "t" : "s"; // temp
        $scope.$broadcast("clearResults");
        $scope.$broadcast("search");
      };
      $scope.templatesShown = function () {
        return $scope.searchObj.params.isTemplate === "t";
      };

      // Append * for like search
      $scope.updateParams = function () {
        $scope.searchObj.params.any = "*" + $scope.searchObj.any + "*";
      };

      init();
    }
  ]);
})();
