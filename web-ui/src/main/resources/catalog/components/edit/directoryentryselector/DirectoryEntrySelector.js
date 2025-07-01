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
  goog.provide("gn_directory_entry_selector");

  goog.require("gn_editor_xml_service");
  goog.require("gn_metadata_manager_service");
  goog.require("gn_schema_manager_service");
  goog.require("gn_popover");

  var module = angular.module("gn_directory_entry_selector", [
    "gn_metadata_manager_service",
    "gn_schema_manager_service",
    "gn_editor_xml_service",
    "pascalprecht.translate",
    "gn_popover"
  ]);

  /**
   *
   *
   */
  module.directive("gnDirectoryEntrySelector", [
    "$rootScope",
    "$timeout",
    "$q",
    "$http",
    "$translate",
    "gnEditor",
    "gnSchemaManagerService",
    "gnEditorXMLService",
    "gnHttp",
    "gnConfig",
    "gnCurrentEdit",
    "gnConfigService",
    "gnPopup",
    "gnGlobalSettings",
    "gnSearchSettings",
    "gnUrlUtils",
    "gnESFacet",
    function (
      $rootScope,
      $timeout,
      $q,
      $http,
      $translate,
      gnEditor,
      gnSchemaManagerService,
      gnEditorXMLService,
      gnHttp,
      gnConfig,
      gnCurrentEdit,
      gnConfigService,
      gnPopup,
      gnGlobalSettings,
      gnSearchSettings,
      gnUrlUtils,
      gnESFacet
    ) {
      return {
        restrict: "A",
        replace: false,
        transclude: true,
        scope: {
          mode: "@gnDirectoryEntrySelector",
          elementName: "@",
          elementRef: "@",
          domId: "@",
          // Contact subtemplates allows definition
          // of the contact role. For other cases
          // only add action is provided
          templateType: "@",
          // Search option to restrict the subtemplate
          // search query
          filter: "@",
          // Parameters to be send when the subtemplate
          // snippet is retrieved before insertion
          // into the metadata records.
          variables: "@",
          // An optional transformation applies to the subtemplate
          // This may be used when using an ISO19139 contact directory
          // in an ISO19115-3 records.
          transformation: "@",
          // If not using the directive in an editor context, set
          // the schema id to properly retrieve the codelists.
          schema: "@",
          // An optional attribute matching a conditional codelist
          // containing the same value for the displayIf attribute.
          displayIf: "@",
          selectEntryCb: "=",
          // Can restrict how to insert the entry (xlink, text ..)
          // insertModes: '@'
          // If true, will only show entries with a valid status of 1
          showValidOnly: "@",
          // Only required when using tag input mode
          // ie. insertModes === 'tags'
          elementXpath: "@",
          values: "@",
          maxTags: "@"
        },
        templateUrl:
          "../../catalog/components/edit/" +
          "directoryentryselector/partials/" +
          "directoryentryselector.html",
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope) {
              var directorySearchSettings = gnGlobalSettings.gnCfg.mods.directory || {};

              var sortConfig = (
                directorySearchSettings.sortBy || gnSearchSettings.sortBy
              ).split("#");

              scope.searchObj = {
                any: "",
                internal: true,
                configId: "directoryInEditor",
                params: {
                  isTemplate: "s",
                  any: "",
                  from: 1,
                  to: 20,
                  root: "gmd:CI_ResponsibleParty",
                  sortBy: sortConfig[0] || "relevance",
                  sortOrder: sortConfig[1] || "",
                  resultType: "subtemplates",
                  queryBase:
                    directorySearchSettings.queryBase || gnSearchSettings.queryBase
                }
              };

              if (scope.$eval(scope.showValidOnly)) {
                scope.searchObj.params.valid = "1";
              }
              scope.facetConfig = gnESFacet.configs.directoryInEditor.facets;

              scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
            },
            post: function postLink(scope, iElement, iAttrs) {
              var insertModes = iAttrs.insertModes;

              scope.insertAsTags = insertModes === "tags";

              if (insertModes) {
                insertModes = insertModes.split(",");
              }

              scope.insertAsXlink =
                ((insertModes && insertModes.indexOf("xlink") >= 0) ||
                  scope.insertAsTags) &&
                gnConfig[gnConfig.key.isXLinkEnabled];
              scope.insertAsText = !insertModes || insertModes.indexOf("text") >= 0;

              // Separator between each contact XML
              // snippet depends on the insert mode.
              // When using tagsinput, the gn_replace_all is the root
              // with values as children. When using X mode,
              // then each snippet are separated by &&&
              var separator = scope.insertAsTags ? "" : "&&&";

              // Only local mode (faster)
              var url = "local://" + gnGlobalSettings.nodeId + "/api/registries/entries/";

              scope.gnConfig = gnConfig;
              // If true, display button to add the element
              // without using the subtemplate selector.
              scope.templateAddAction = iAttrs.templateAddAction == "true";

              // An optional XML snippet to insert
              scope.templateAddSnippet = iAttrs.templateAddSnippet;

              // If true, display input to search with autocompletion
              scope.searchAction = iAttrs.searchAction == "true";
              // If true, display button to search using the popup selector
              scope.popupAction = iAttrs.popupAction == "true";
              scope.isContact = scope.templateType === "contact";
              scope.hasDynamicVariable =
                scope.variables && scope.variables.match("{.*}") !== null;

              // Search only for contact subtemplate
              // by default.
              if (scope.filter) {
                angular.extend(scope.searchObj.params, angular.fromJson(scope.filter));
              }

              scope.entryFound = null;

              scope.updateParams = function () {
                scope.searchObj.params.any = scope.searchObj.any;
              };

              // On focus check if any entry. If not display alert.
              var firstSearchFinished = scope.$on(
                "searchFinished",
                function (event, args) {
                  scope.entryFound = args.count !== 0;
                  firstSearchFinished();
                }
              );

              scope.snippet = null;
              scope.snippetRef = gnEditor.buildXMLFieldName(
                scope.elementRef,
                scope.elementName,
                scope.insertAsTags ? "_P" : "_X"
              );

              scope.attrs = iAttrs;
              scope.add = function () {
                return gnEditor
                  .add(
                    gnCurrentEdit.id,
                    scope.elementRef,
                    scope.elementName,
                    scope.domId,
                    "before"
                  )
                  .then(function () {
                    if (scope.templateAddAction) {
                      gnEditor.save(gnCurrentEdit.id, true).then(
                        function () {
                          // success. Nothing to do.
                        },
                        function (rejectedValue) {
                          $rootScope.$broadcast("StatusUpdated", {
                            title: $translate.instant("saveMetadataError"),
                            error: rejectedValue,
                            timeout: 0,
                            type: "danger"
                          });
                        }
                      );
                    }
                  });
              };

              // <request><codelist schema="iso19139"
              // name="gmd:CI_RoleCode" /></request>
              scope.addEntry = function (entry, role, usingXlink, saveEdits) {
                var defer = $q.defer();
                gnCurrentEdit.working = saveEdits;
                if (!(entry instanceof Array)) {
                  entry = [entry];
                }
                scope.snippet = "";
                var snippets = [];

                var checkState = function (c) {
                  if (snippets.length === entry.length) {
                    scope.snippet = snippets.join(separator);
                    // Clean results
                    // TODO: should call clean result from
                    // searchFormController
                    //                   scope.searchResults.records = null;
                    //                   scope.searchResults.count = null;
                    // Only if editing.
                    if (gnCurrentEdit.id && saveEdits) {
                      $timeout(function () {
                        // Save the metadata and refresh the form
                        gnEditor.save(gnCurrentEdit.id, true).then(
                          function (r) {
                            defer.resolve(r);
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
                      });
                    }
                    if (angular.isFunction(scope.selectEntryCb)) {
                      scope.selectEntryCb(scope, c, role);
                    }
                  }
                };

                angular.forEach(entry, function (c) {
                  var id = c.id,
                    uuid = c.uuid;
                  var params = {};

                  // For the time being only contact role
                  // could be substitute in directory entry
                  // selector. This is done using the process
                  // parameter of the get subtemplate service.
                  // eg. data-variables="gmd:role/gmd:CI_RoleCode
                  //   /@codeListValue~{role}"
                  // will set the role of the contact.
                  // TODO: this could be applicable
                  // not only to contact role
                  // No use case identified for now.
                  if (scope.hasDynamicVariable && role) {
                    params.process = scope.variables.replace("{role}", role);
                  } else if (scope.variables) {
                    params.process = scope.variables;
                  }

                  if (
                    angular.isString(scope.transformation) &&
                    scope.transformation !== ""
                  ) {
                    params.transformation = scope.transformation;
                  }

                  var langsParam = [];
                  // ISO19110 does not contain other lang
                  if (gnCurrentEdit.mdOtherLanguages != "") {
                    for (var p in JSON.parse(gnCurrentEdit.mdOtherLanguages)) {
                      langsParam.push(p);
                    }
                    if (langsParam.length > 1) {
                      params.lang = langsParam;
                    } else {
                      params.lang = gnCurrentEdit.mdLanguage;
                    }
                  }
                  params.schema =
                    gnCurrentEdit.schema === "iso19110"
                      ? "iso19139"
                      : gnCurrentEdit.schema;

                  if (!params.lang) {
                    console.warn("No lang has been set for the xlink");
                  }
                  var urlParams = decodeURIComponent(gnUrlUtils.toKeyValue(params));

                  if (angular.isString(c) && c.startsWith("<")) {
                    snippets.push(
                      gnEditorXMLService.buildXML(scope.schema, scope.elementName, c)
                    );
                    checkState(c);
                  } else {
                    $http
                      .get("../api/registries/entries/" + encodeURIComponent(uuid), {
                        params: params
                      })
                      .then(function (response) {
                        var xml = response.data;

                        if (usingXlink) {
                          snippets.push(
                            gnEditorXMLService.buildXMLForXlink(
                              scope.schema,
                              scope.elementName,
                              url + uuid + "?" + urlParams
                            )
                          );
                        } else {
                          snippets.push(
                            gnEditorXMLService.buildXML(
                              scope.schema,
                              scope.elementName,
                              xml
                            )
                          );
                        }
                        checkState(c);
                      });
                  }
                });
                return defer.promise;
              };

              var schemaId = gnCurrentEdit.schema || scope.schema;
              gnSchemaManagerService
                .getCodelist(schemaId + "|" + "roleCode", scope.displayIf)
                .then(function (data) {
                  scope.roles = data.entry;
                });

              // Typeahead and tag input mode
              scope.selected = [];
              var id =
                "#tagsinput_" + scope.elementRef + scope.elementName.replace(":", "");

              function buildTag(d) {
                return {
                  uuid: d._id,
                  label:
                    d._source.resourceTitle ||
                    d._source.resourceTitleObject.default ||
                    "-"
                };
              }

              var initTagsInput = function () {
                $timeout(function () {
                  try {
                    $(id).tagsinput({
                      itemValue: "label",
                      itemText: "label",
                      maxTags: scope.maxTags
                    });

                    // Add selection to the list of tags
                    angular.forEach(scope.selected, function (c) {
                      $(id).tagsinput("add", c);
                    });

                    var field = $(id).tagsinput("input");
                    field.attr("placeholder", $translate.instant("searchAcontact"));

                    // Add selection to the list of tags
                    // angular.forEach(scope.selected, function(keyword) {
                    //   $(id).tagsinput('add', keyword);
                    // });

                    var getRecordsAutocompleter = function (config) {
                      var recordsAutocompleter = new Bloodhound({
                        datumTokenizer: Bloodhound.tokenizers.whitespace("title"),
                        queryTokenizer: Bloodhound.tokenizers.whitespace,
                        limit: config.max || 10,
                        remote: {
                          wildcard: "QUERY",
                          url: "../api/search/records/_search",
                          prepare: function (query, settings) {
                            settings.type = "POST";
                            settings.contentType = "application/json; charset=UTF-8";
                            settings.data = JSON.stringify({
                              from: 0,
                              size: 10,
                              query: {
                                bool: {
                                  must: {
                                    query_string: {
                                      query: "any:" + (query || "*")
                                    }
                                  },
                                  filter: [
                                    { term: { isTemplate: "s" } },
                                    {
                                      term: { root: "gmd:CI_ResponsibleParty" }
                                    }
                                  ]
                                }
                              }
                            });
                            return settings;
                          },
                          transform: function (response) {
                            return response.hits.hits.map(function (d, i) {
                              return buildTag(d);
                            });
                          }
                        }
                      });

                      recordsAutocompleter.initialize();
                      return recordsAutocompleter;
                    };
                    var autocompleter = getRecordsAutocompleter({ max: 10 });

                    // Init typeahead
                    field
                      .typeahead(
                        {
                          minLength: 0,
                          highlight: true
                        },
                        {
                          name: "hit",
                          displayKey: "label",
                          limit: Infinity,
                          source: autocompleter.ttAdapter()
                        }
                      )
                      .bind(
                        "typeahead:selected",
                        $.proxy(function (obj, r) {
                          this.tagsinput("add", r);

                          // Clear typeahead
                          this.tagsinput("input").typeahead("val", "");
                          field.blur();
                          field.triggerHandler("input");

                          // Update selection and snippet
                          var ta = this;
                          $timeout(function () {
                            scope.selected = ta.tagsinput("items");
                            //getSnippet(); // FIXME: should not be necessary
                            // as there is a watch on it ?
                          }, 100);
                        }, $(id))
                      );

                    field.bind("keydown keypress", function (event) {
                      if (event.isDefaultPrevented()) {
                        event.stopPropagation(); // need to prevent this from bubbling - or something might action it
                        field.focus(); //allow to type again
                        return false; //this event has already been handled by tt-typeahead, dont do it twice!
                      }
                    });

                    $(id).on("itemRemoved", function () {
                      var ta = $(this);
                      $timeout(function () {
                        scope.selected = ta.tagsinput("items");
                      }, 100);
                    });

                    function getSnippet() {
                      scope
                        .addEntry(
                          scope.selected,
                          scope.withRoleSelection ? role : undefined,
                          scope.insertAsXlink,
                          false
                        )
                        .then(function (r) {
                          console.log(r);
                        });
                    }

                    scope.$watchCollection("selected", getSnippet);
                  } catch (e) {
                    console.warn("No tagsinput for " + id + ", error: " + e.message);
                  }
                });
              };

              if (scope.insertAsTags) {
                // Populate initial values based on Xlink
                // containing the UUID.
                // Then initialize tags input
                var urls = scope.values === "" ? [] : scope.values.split("◿"),
                  i = 0;
                scope.initialStateComplete = undefined;
                var w = scope.$watch("initialStateComplete", function (n, o) {
                  if (n) {
                    initTagsInput();
                    w();
                  }
                });
                scope.initialStateComplete = urls.length === i;

                urls.forEach(function (url, i) {
                  // local://srv/api/registries/entries/af8a36bb-ecea-4880-bf83-26b691e7570e?
                  //  transformation=contact-from-iso19139-to-foaf-agent&lang=eng,fre&schema=dcat2
                  var uuid = url.replace(/.*entries\/(.*)\?.*/, "$1");
                  $http
                    .post(
                      "../api/search/records/_search",
                      {
                        query: {
                          bool: {
                            must: [
                              {
                                multi_match: {
                                  query: uuid,
                                  fields: ["uuid"]
                                }
                              },
                              { terms: { isTemplate: ["s"] } }
                            ]
                          }
                        }
                      },
                      { cache: true }
                    )
                    .then(function (r) {
                      if (r.data.hits.total.value == 1) {
                        i++;
                        var tag = buildTag(r.data.hits.hits[0]);
                        scope.selected.push(tag);
                        scope.initialStateComplete = urls.length === i;
                      }
                    });
                });
              }

              scope.openSelector = function () {
                openModal(
                  {
                    title: $translate.instant("chooseEntry"),
                    content:
                      '<div gn-directory-entry-list-selector="" ' +
                      (scope.$eval(scope.showValidOnly)
                        ? ' show-valid-only="true"'
                        : "") +
                      (iAttrs["defaultRole"]
                        ? ' data-default-role="' + iAttrs["defaultRole"] + '"'
                        : "") +
                      "></div>",
                    class: "gn-modal-lg"
                  },
                  scope,
                  "EntrySelected"
                );
              };
              var popup;
              var openModal = function (o, scope) {
                popup = gnPopup.createModal(o, scope);
              };
              scope.closeModal = function () {
                popup.trigger("hidden.bs.modal");
              };
            }
          };
        }
      };
    }
  ]);

  module.directive("gnDirectoryEntryListSelector", [
    "gnGlobalSettings",
    "gnSearchSettings",
    function (gnGlobalSettings, gnSearchSettings) {
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/edit/" +
          "directoryentryselector/partials/" +
          "directoryentrylistselector.html",

        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope) {
              var directorySearchSettings = gnGlobalSettings.gnCfg.mods.directory || {};

              var sortConfig = (
                directorySearchSettings.sortBy || gnSearchSettings.sortBy
              ).split("#");

              scope.searchObj = {
                internal: true,
                configId: "directoryInEditor",
                defaultParams: {
                  isTemplate: "s",
                  any: "",
                  from: 1,
                  to: 10,
                  root: "gmd:CI_ResponsibleParty",
                  sortBy: sortConfig[0] || "relevance",
                  sortOrder: sortConfig[1] || "",
                  queryBase:
                    directorySearchSettings.queryBase || gnSearchSettings.queryBase
                }
              };
              if (scope.$eval(tAttrs["showValidOnly"])) {
                scope.searchObj.valid = 1;
              }
              scope.searchObj.params = angular.extend(
                {},
                scope.searchObj.params,
                scope.searchObj.defaultParams
              );
              scope.stateObj = {
                selectRecords: []
              };
              if (scope.filter) {
                var filter = angular.fromJson(scope.filter);
                angular.extend(scope.searchObj.params, filter);
                angular.extend(scope.searchObj.defaultParams, filter);
              }
              scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
            },
            post: function postLink(scope, iElement, iAttrs) {
              scope.ctrl = {};

              scope.withRoleSelection = iAttrs["withRoleSelection"] == "false" || true;
              scope.defaultRoleCode = iAttrs["defaultRole"] || null;
              scope.defaultRole = null;
              angular.forEach(scope.roles, function (r) {
                if (r.code == scope.defaultRoleCode) {
                  scope.defaultRole = r;
                  scope.ctrl.role = r;
                }
              });
              scope.addSelectedEntry = function (role, usingXlink) {
                scope
                  .addEntry(
                    scope.stateObj.selectRecords[0],
                    scope.withRoleSelection ? role : undefined,
                    usingXlink,
                    true
                  )
                  .then(function (r) {
                    scope.closeModal();
                  });
              };
            }
          };
        }
      };
    }
  ]);
})();
