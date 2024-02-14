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
  goog.provide("gn_editor_controller");

  goog.require("gn_batchedit_controller");
  goog.require("gn_directory_controller");
  goog.require("gn_editorboard_controller");
  goog.require("gn_fields");
  goog.require("gn_import_controller");
  goog.require("gn_mdactions_service");
  goog.require("gn_new_metadata_controller");
  goog.require("gn_scroll_spy");
  goog.require("gn_share");
  goog.require("gn_thesaurus");
  goog.require("gn_topiccategory");
  goog.require("gn_utility_directive");

  var module = angular.module("gn_editor_controller", [
    "gn_fields",
    "gn_new_metadata_controller",
    "gn_import_controller",
    "gn_batchedit_controller",
    "gn_editorboard_controller",
    "gn_share",
    "gn_directory_controller",
    "gn_utility_directive",
    "gn_scroll_spy",
    "gn_thesaurus",
    "gn_topiccategory",
    "ui.bootstrap.datetimepicker",
    "ngRoute",
    "gn_mdactions_service",
    "pascalprecht.translate"
  ]);

  var tplFolder = "../../catalog/templates/editor/";

  module.config([
    "$routeProvider",
    "gnGlobalSettings",
    function ($routeProvider, gnGlobalSettings) {
      $routeProvider
        .when("/metadata/:id", {
          templateUrl: tplFolder + "editor.html",
          controller: "GnEditorController",
          reloadOnSearch: false
        })
        .when("/create", {
          templateUrl: gnGlobalSettings.gnCfg.mods.editor.createPageTpl,
          controller: "GnNewMetadataController"
        })
        .when("/create/from/:id/in/:group", {
          templateUrl: tplFolder + "editor.html",
          controller: "GnNewMetadataController"
        })
        .when("/create/from/:id/in/:group/tab/:tab", {
          templateUrl: tplFolder + "editor.html",
          controller: "GnNewMetadataController"
        })
        .when("/create/from/:id/in/:group/template/:template", {
          templateUrl: tplFolder + "editor.html",
          controller: "GnNewMetadataController"
        })
        .when("/directory", {
          templateUrl: tplFolder + "directory.html",
          controller: "GnDirectoryController"
        })
        .when("/directory/type/:type", {
          templateUrl: tplFolder + "directory.html",
          controller: "GnDirectoryController"
        })
        .when("/directory/id/:id", {
          templateUrl: tplFolder + "directory.html",
          controller: "GnDirectoryController"
        })
        .when("/import", {
          templateUrl: tplFolder + "import.html",
          controller: "GnImportController"
        })
        .when("/batchedit", {
          templateUrl: tplFolder + "batchedit.html",
          controller: "GnBatchEditController"
        })
        .when("/accessManager", {
          templateUrl: tplFolder + "accessManager.html",
          controller: "GnAccessManagerController"
        })
        .when("/board", {
          templateUrl: tplFolder + "editorboard.html",
          controller: "GnEditorBoardController"
        })
        .otherwise({
          redirectTo: "/board"
        });
    }
  ]);

  /**
   * Metadata editor controller
   */
  module.controller("GnEditorController", [
    "$q",
    "$scope",
    "$routeParams",
    "$http",
    "$rootScope",
    "$translate",
    "$compile",
    "$timeout",
    "$location",
    "gnEditor",
    "gnSearchManagerService",
    "gnSchemaManagerService",
    "gnConfigService",
    "gnUtilityService",
    "gnOnlinesrc",
    "gnCurrentEdit",
    "gnConfig",
    "gnMetadataActions",
    "Metadata",
    "gnValidation",
    "gnGlobalSettings",
    function (
      $q,
      $scope,
      $routeParams,
      $http,
      $rootScope,
      $translate,
      $compile,
      $timeout,
      $location,
      gnEditor,
      gnSearchManagerService,
      gnSchemaManagerService,
      gnConfigService,
      gnUtilityService,
      gnOnlinesrc,
      gnCurrentEdit,
      gnConfig,
      gnMetadataActions,
      Metadata,
      gnValidation,
      gnGlobalSettings
    ) {
      $scope.savedStatus = null;
      $scope.savedTime = null;
      $scope.formId = null;
      $scope.savedStatus = null;
      $scope.metadataFound = true;
      $scope.gnConfig = gnConfig;
      $scope.unsupportedSchema = false;
      $scope.gnOnlinesrc = gnOnlinesrc;
      $scope.redirectUrl = null;

      /**
       * Animation duration for slide up/down
       */
      var duration = 300;

      /**
       * Function to call after form load
       * to move view menu to top toolbar
       */
      var setViewMenuInTopToolbar = function () {
        // Move view menu to the top toolbar
        var menu = $(".gn-view-menu-button");
        if (menu) {
          menu.empty();
          var button = $("#gn-view-menu-" + gnCurrentEdit.id);
          if (button) {
            menu.append(button);
            $compile(button)($scope);
          }
        }
      };
      // Controller initialization
      var init = function () {
        var promises = [];
        promises.push(gnSchemaManagerService.getNamespaces());
        promises.push(gnConfigService.load());
        $q.all(promises).then(function (c) {
          // Config loaded

          // Enable workflow functions
          $scope.isMdWorkflowEnable = gnConfig["metadata.workflow.enable"];
          $scope.viewConfig = gnGlobalSettings.gnCfg.mods.recordview;

          if ($routeParams.id) {
            // Check requested metadata exists
            $http
              .post("../api/search/records/_search", {
                query: {
                  bool: {
                    must: [
                      {
                        multi_match: {
                          query: $routeParams.id,
                          fields: ["id^2", "uuid"]
                        }
                      },
                      { terms: { draft: ["n", "y", "e"] } },
                      { terms: { isTemplate: ["n", "y", "s"] } }
                    ]
                  }
                }
              })
              .then(function (r) {
                $scope.metadataFound = r.data.hits.total.value !== 0;
                $scope.metadataNotFoundId = $routeParams.id;

                if (!$scope.metadataFound) {
                  $rootScope.$broadcast("StatusUpdated", {
                    title:
                      $translate.instant("metadataNotFound", { id: $routeParams.id }) ||
                      "metadataNotFound",
                    error: $routeParams.id,
                    timeout: 0,
                    type: "danger"
                  });
                  return;
                }

                gnCurrentEdit.metadata = new Metadata(r.data.hits.hits[0]);
                $scope.id = gnCurrentEdit.metadata.id;
                $scope.mdSchema = gnCurrentEdit.metadata.documentStandard;
                gnCurrentEdit.schema = $scope.mdSchema;
                $scope.mdCategories = { values: [] };
                var categories = gnCurrentEdit.metadata.cat;
                if (categories) {
                  if (angular.isArray(categories)) {
                    $scope.mdCategories.values = categories;
                  } else {
                    $scope.mdCategories.values.push(categories);
                  }
                }

                $scope.groupOwner = gnCurrentEdit.metadata.groupOwner;
                $scope.mdTitle = gnCurrentEdit.metadata.resourceTitle;

                $scope.redirectUrl = $location.search()["redirectUrl"];

                if ($scope.metadataFound) {
                  // Default view to display is default
                  // it may be overriden by route params or custom function
                  var defaultTab = "";

                  // It may be overriden by an application
                  // settings. For example, you may have different
                  // types of ISO19139 record with different characteristics
                  // like standardName and would like to open the editor
                  // in custom view based on the standard.
                  var schemaCustomConfig = {
                    // Example : open ISO19139 record having
                    // standardName containing medsea in advanced mode
                    //'iso19139': function (md) {
                    //  if (md.standardName && md.standardName.match(/medsea/i)) {
                    //    return 'identificationInfo';
                    //  }
                    //  return defaultTab;
                    //}
                  };
                  if (schemaCustomConfig) {
                    var fn = schemaCustomConfig[$scope.mdSchema];
                    if (angular.isFunction(fn)) {
                      defaultTab = fn(gnCurrentEdit.metadata);
                    }
                  }

                  // Route param "tab" may also override this setting.

                  // TODO: Set metadata title in page HEAD ?
                  $scope.layout.hideTopToolBar = true;

                  angular.extend(gnCurrentEdit, {
                    id: gnCurrentEdit.metadata.id,
                    formId: "#gn-editor-" + gnCurrentEdit.metadata.id,
                    containerId: "#gn-editor-container-" + gnCurrentEdit.metadata.id,
                    associatedPanelConfigId: "default",
                    tab: $location.search()["tab"] || defaultTab,
                    displayAttributes: $location.search()["displayAttributes"] === "true",
                    displayTooltips: $location.search()["displayTooltips"] === "true",
                    displayTooltipsMode: $location.search()["displayTooltipsMode"] || "",
                    compileScope: $scope,
                    formScope: $scope.$new(),
                    sessionStartTime: moment(),
                    formLoadExtraFn: formLoadExtraFunctions,
                    codelistFilter: "",
                    working: false
                  });

                  $scope.gnCurrentEdit = gnCurrentEdit;
                  $scope.tocIndex = null;

                  // Create URL for loading the metadata form
                  // appending a random int in order to avoid
                  // caching by route.
                  var editorFormUrl =
                    gnEditor.buildEditUrlPrefix("editor") +
                    "&starteditingsession=yes&" +
                    "_random=" +
                    Math.floor(Math.random() * 10000);

                  if (gnCurrentEdit.displayAttributes === true) {
                    editorFormUrl += "&displayAttributes=true";
                  }

                  if (gnCurrentEdit.displayTooltips === true) {
                    editorFormUrl += "&displayTooltips=true";
                  }

                  if (gnCurrentEdit.displayTooltipsMode != "") {
                    editorFormUrl +=
                      "&displayTooltipsMode=" + gnCurrentEdit.displayTooltipsMode;
                  }

                  gnEditor
                    .load(editorFormUrl)
                    .then(function () {
                      // $scope.onFormLoad();
                      // Once the editor form is loaded, then
                      // user might have set a target element
                      // to scroll to.
                      var target = $location.search()["scrollTo"];
                      if (target) {
                        $timeout(function () {
                          gnUtilityService.scrollTo(target);
                        }, 300);
                        // Delayed a bit, the time to the form
                        // and all directives to be rendered which
                        // may affect element positions.
                      }
                    })
                    .catch(function (data) {
                      //an api-error is returned as xml
                      $rootScope.$broadcast("StatusUpdated", {
                        title: $translate.instant("runServiceError"),
                        error: $(data).find("description").text(),
                        timeout: 0,
                        type: "danger"
                      });
                    });

                  window.onbeforeunload = function () {
                    // TODO: could be better to provide
                    // cancelAndClose and saveAndClose button
                    return $translate.instant("beforeUnloadEditor", {
                      timeAgo: moment(gnCurrentEdit.savedTime).fromNow()
                    });
                  };
                }
              });
          }
        });
      };

      /**
       * Extra functions to load after the form is loaded
       */
      var formLoadExtraFunctions = function () {
        setViewMenuInTopToolbar();
        setEditorIndentType();
        refreshMetadata();
      };

      $scope.$on("$locationChangeSuccess", function (e, newUrl, oldUrl) {
        var target = $location.search()["scrollTo"];
        if (target) {
          gnUtilityService.scrollTo(target);
        }
      });

      $scope.$watch("gnCurrentEdit.isMinor", function () {
        if ($("#minor")[0]) {
          $("#minor")[0].value = $scope.gnCurrentEdit.isMinor;
        }
      });

      /**
       * Function to set the editor indent type
       */
      var setEditorIndentType = function () {
        var f = $("form.gn-editor");
        // If CSS class is defined in the view in config-editor.xml, don't do anything
        if (
          !f.hasClass("gn-editor-config-css") &&
          angular.isDefined(gnGlobalSettings.gnCfg.mods.editor.editorIndentType)
        ) {
          // add indent type to editor based on UI configuration
          f.addClass(gnGlobalSettings.gnCfg.mods.editor.editorIndentType);
        }
      };

      /**
       * Refresh the metadata information in the current edit session
       */
      var refreshMetadata = function () {
        // Refresh the metadata property
        $http
          .post("../api/search/records/_search", {
            query: {
              bool: {
                must: [
                  {
                    multi_match: {
                      query: gnCurrentEdit.id,
                      fields: ["id^2", "uuid"]
                    }
                  },
                  { terms: { draft: ["n", "y", "e"] } },
                  { terms: { isTemplate: ["n", "y", "s"] } }
                ]
              }
            }
          })
          .then(function (r) {
            var metadataFound = r.data.hits.total.value !== 0;

            if (metadataFound) {
              $scope.gnCurrentEdit.metadata = new Metadata(r.data.hits.hits[0]);
            }
          });
      };

      /**
       * When the form is loaded, this function is called.
       * Use it to retrieve form variables or initialize
       * elements eg. tooltip ?
       */
      $scope.onFormLoad = function () {
        gnEditor.onFormLoad();
        $scope.isMinor = gnCurrentEdit.isMinor;
        $scope.$watch("tocIndex", function (newValue, oldValue) {
          if (angular.isDefined($scope.tocIndex) && $scope.tocIndex !== null) {
            $timeout(function () {
              $scope.switchToTab(gnCurrentEdit.tab);
            });
          }
        });
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

      $scope.startVersioning = function () {
        return gnMetadataActions.startVersioning(gnCurrentEdit.id);
      };

      /**
       * Update the form according to the target tab
       * properties and save.
       */
      $scope.switchToTab = function (tabIdentifier, mode) {
        // Scroll top
        if (tabIdentifier !== $("#currTab")[0].value) {
          gnUtilityService.scrollTo();
        }

        $("#currTab")[0].value = tabIdentifier;
        $("#flat")[0].value = mode === "flat";

        // Make the current form disapearing TODO: in save
        // Disable form + indicator ?
        //        $($scope.formId + ' > fieldset').fadeOut(duration);
        $scope.save(true);

        $location.search("tab", tabIdentifier).replace();
      };

      /**
       * Set type of record. Update the matching form element.
       */
      $scope.setTemplate = function (isTemplate) {
        gnCurrentEdit.isTemplate = isTemplate ? "y" : "n";
        $("#template")[0].value = gnCurrentEdit.isTemplate;
      };
      $scope.isTemplate = function () {
        return gnCurrentEdit.isTemplate === "y";
      };

      /**
       * Display or not attributes editor.
       *
       * if toggle is true, the current value is inverted.
       */
      $scope.toggleAttributes = function (toggle) {
        if (toggle) {
          gnCurrentEdit.displayAttributes = gnCurrentEdit.displayAttributes === false;
        }

        // Update the form to propagate info when saved
        // or tab switch - Needs to be propagated in Update service
        $("#displayAttributes")[0].value = gnCurrentEdit.displayAttributes;

        // Toggle class on all gn-attr widgets
        if (gnCurrentEdit.displayAttributes) {
          $(".gn-attr").removeClass("hidden");
        } else {
          $(".gn-attr").addClass("hidden");
        }
      };

      $scope.toggleTooltips = function (toggle) {
        if (toggle) {
          gnCurrentEdit.displayTooltips = gnCurrentEdit.displayTooltips === false;
        }

        // Update the form to propagate info when saved
        // or tab switch - Needs to be propagated in Update service
        $("#displayTooltips")[0].value = gnCurrentEdit.displayTooltips;
      };

      $scope.checkField = function (name) {
        return gnEditor[name].$error.required ? "has-error" : "";
      };
      $scope.add = function (ref, name, insertRef, position, attribute) {
        if (attribute) {
          // save the form and add attribute
          // after save is done. When adding an attribute
          // the snippet returned contains the current field
          // and the newly created attributes.
          // Save to not lose current edits in main field.
          return gnEditor.save(false).then(
            function () {
              gnEditor.add(gnCurrentEdit.id, ref, name, insertRef, position, attribute);
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
          return gnEditor.add(
            gnCurrentEdit.id,
            ref,
            name,
            insertRef,
            position,
            attribute
          );
        }
      };
      $scope.addChoice = function (ref, name, insertRef, position) {
        return gnEditor.addChoice(gnCurrentEdit.id, ref, name, insertRef, position);
      };
      $scope.remove = function (ref, parent, domRef) {
        // remove element and save the form
        // after remove is done. When removing an element
        // the button to add the element again is not available
        // in case of elements with max cardinality of 1.
        // Saving the form will show the add button again.
        return gnEditor.remove(gnCurrentEdit.id, ref, parent, domRef).then(
          function () {
            gnEditor.save(true);
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
      };
      $scope.removeAttribute = function (ref) {
        return gnEditor.removeAttribute(gnCurrentEdit.id, ref);
      };
      $scope.switchTypeAndSave = function (refreshForm) {
        $scope.setTemplate(!$scope.isTemplate());
        return $scope.save(refreshForm);
      };

      $scope.save = function (refreshForm, validate) {
        if (angular.isDefined(validate)) {
          $("#showvalidationerrors")[0].value = gnCurrentEdit.showValidationErrors =
            validate;
        }

        $scope.saveError = false;
        var promise = gnEditor.save(refreshForm).then(
          function (form) {
            $scope.savedStatus = gnCurrentEdit.savedStatus;
            $scope.saveError = false;
            $scope.toggleAttributes();
          },
          function (error) {
            $scope.savedStatus = gnCurrentEdit.savedStatus;
            $scope.saveError = true;
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("saveMetadataError"),
              error: error.description,
              timeout: 0,
              type: "danger"
            });
          }
        );
        $scope.savedStatus = gnCurrentEdit.savedStatus;
        return promise;
      };
      var closeEditor = function () {
        $scope.layout.hideTopToolBar = false;
        // Close the editor tab
        window.onbeforeunload = null;

        // if there is no history, attempt to close tab
        if ($scope.redirectUrl != null) {
          window.location.replace($scope.redirectUrl);
        } else if (window.history.length == 1) {
          window.close();
          // This last point may trigger
          // "Scripts may close only the windows that were opened by it."
          // when the editor was not opened by a script.
        } else {
          window.history.back();
        }
      };

      $scope.cancel = function (refreshForm) {
        var doCancel = confirm($translate.instant("confirmCancelEdit"));

        if (!doCancel) {
          return false;
        }
        $scope.savedStatus = gnCurrentEdit.savedStatus;
        if ($location.search()["justcreated"]) {
          // Remove newly created record
          var md = gnCurrentEdit.metadata;
          gnMetadataActions.deleteMd(md).then(
            function (data) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("metadataRemoved", { title: md.resourceTitle }),
                timeout: 2
              });
              closeEditor();
            },
            function (reason) {
              $rootScope.$broadcast("StatusUpdated", {
                title: reason.data.message, //returned error JSON obj
                error: reason.data.description,
                timeout: 0,
                type: "danger"
              });
            }
          );
        } else {
          return gnEditor.cancel(refreshForm).then(
            function (form) {
              // Refresh editor form after cancel
              //  $scope.savedStatus = gnCurrentEdit.savedStatus;
              //  $rootScope.$broadcast('StatusUpdated', {
              //    title: $translate.instant('cancelMetadataSuccess')
              //  });
              //  gnEditor.refreshEditorForm(null, true);
              closeEditor();
            },
            function (error) {
              //an api-error is returned as xml
              $scope.savedStatus = gnCurrentEdit.savedStatus;
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("cancelMetadataError"),
                error: $(data).find("description").text(),
                timeout: 0,
                type: "danger"
              });
            }
          );
        }
      };

      $scope.close = function (submit, approve) {
        // if auto unpublish is not set in the settings, or if MD is not
        // published: close w/o confirmation
        if (
          !$scope.gnCurrentEdit.metadata.isPublished() ||
          !gnConfig["metadata.workflow.automaticUnpublishInvalidMd"]
        ) {
          $scope.confirmClose(submit, approve);
          return;
        }

        // else: do a mandatory validation check to ensure that if the MD is
        // invalid, the user can decide to stay in the editor to fix it
        gnValidation.errorCheck().then(function (hasErrors) {
          // if record unpublish on save is enabled: ask for confirmation
          // before saving and closing the editor
          if (hasErrors) {
            $("#confirm-editor-close").modal("show");
          } else {
            $scope.confirmClose(submit, approve);
          }
        });
      };
      $scope.confirmClose = function (submit, approve) {
        var promise = gnEditor.save(false, null, true, submit, approve).then(
          function (form) {
            closeEditor();
          },
          function (error) {
            // When closing editor and if error occurs,
            // the response is in XML. Try to get the message

            $scope.savedStatus = gnCurrentEdit.savedStatus;
            $scope.saveError = true;

            $rootScope.$broadcast("StatusUpdated", {
              title: error ? error.message : $translate.instant("saveMetadataError"),
              error: error.description,
              timeout: 0,
              type: "danger"
            });
          }
        );
        $scope.savedStatus = gnCurrentEdit.savedStatus;
        return promise;
      };
      $scope.getSaveStatus = function () {
        if (gnCurrentEdit.savedTime) {
          return ($scope.saveStatus = $translate.instant("saveAtimeAgo", {
            timeAgo: moment(gnCurrentEdit.savedTime).fromNow()
          }));
        }
      };
      $scope.getCancelStatus = function () {
        if (gnCurrentEdit.sessionStartTime) {
          return ($scope.cancelStatus = $translate.instant("cancelChangesFromNow", {
            timeAgo: moment(gnCurrentEdit.sessionStartTime).fromNow()
          }));
        }
      };

      $scope.$on(
        "AddElement",
        function (event, ref, name, insertRef, position, attribute) {
          $scope.add(ref, name, insertRef, position, attribute);
        }
      );

      init();
    }
  ]);
})();
