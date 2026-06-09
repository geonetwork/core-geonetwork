(function () {
  goog.provide("gn_batchedit_controller");

  goog.require("gn_mdactions_service");
  goog.require("gn_search");
  goog.require("gn_search_form_controller");
  goog.require("gn_utility_service");

  var module = angular.module("gn_batchedit_controller", [
    "gn_search",
    "gn_search_form_controller",
    "gn_mdactions_service",
    "gn_utility_service"
  ]);

  /**
   * Search form for batch editing selection of records.
   *
   * Filters on metadata and template (no subtemplate)
   * and only record that current user can edit (ie. editable=true)
   */
  module.controller("GnBatchEditSearchController", [
    "$scope",
    "$location",
    "$rootScope",
    "$translate",
    "$q",
    "$http",
    "gnSearchSettings",
    "gnSearchManagerService",
    "gnMetadataActions",
    "gnGlobalSettings",
    "Metadata",
    function (
      $scope,
      $location,
      $rootScope,
      $translate,
      $q,
      $http,
      gnSearchSettings,
      gnSearchManagerService,
      gnMetadataActions,
      gnGlobalSettings,
      Metadata
    ) {
      // Search parameters and configuration
      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
      $scope.onlyMyRecord = {
        is: gnGlobalSettings.gnCfg.mods.editor.isUserRecordsOnly
      };
      $scope.isFilterTagsDisplayed =
        gnGlobalSettings.gnCfg.mods.editor.isFilterTagsDisplayed;

      $scope.defaultSearchObj = {
        permalink: false,
        configId: "editor",
        sortbyValues: gnSearchSettings.sortbyValues,
        hitsperpageValues: gnSearchSettings.hitsperpageValues,
        selectionBucket: "e101",
        params: {
          sortBy: "changeDate",
          sortOrder: "desc",
          isTemplate: ["y", "n"],
          editable: "true",
          from: 1,
          to: 20
        }
      };
      angular.extend($scope.searchObj, $scope.defaultSearchObj);

      // Only my record toggle
      $scope.toggleOnlyMyRecord = function (callback) {
        $scope.onlyMyRecord ? setOwner() : unsetOwner();
        callback();
      };
      var setOwner = function () {
        $scope.searchObj.params["owner"] = $scope.user.id;
      };
      var unsetOwner = function () {
        delete $scope.searchObj.params["owner"];
      };
      $scope.$watch("user.id", function (newId) {
        if (angular.isDefined(newId) && $scope.onlyMyRecord) {
          setOwner();
        }
      });

      var maxRecordsDisplayed = 100;

      // When selection change get the current selection uuids
      // and populate a list of currently selected records to be
      // display on summary pages
      $scope.$watch("searchResults.selectedCount", function (newvalue, oldvalue) {
        if (oldvalue != newvalue) {
          $scope.tooManyRecordInSelForSearch = false;
          $scope.tooManyRecordInSelForSearch = false;
          $scope.selectedRecordsCount = 0;
          $scope.selectedStandards = [];
          $scope.selectedRecords = [];
          $http.get("../api/selections/e101").then(function (response) {
            var uuids = response.data;
            $scope.selectedRecordsCount = uuids.length;
            if (uuids.length > 0) {
              var query = {
                size: maxRecordsDisplayed,
                aggs: {
                  schema: {
                    terms: {
                      field: "documentStandard",
                      size: 10
                    }
                  }
                },
                query: {
                  bool: {
                    must: [
                      {
                        terms: {
                          isTemplate: ["y", "n", "s"]
                        }
                      },
                      {
                        terms: {
                          uuid: uuids
                        }
                      }
                    ]
                  }
                },
                _source: ["resourceTitleObject.default"]
              };
              $http.post("../api/search/records/_search", query).then(
                function (r) {
                  $scope.selectedRecords = r.data.hits;
                  $scope.selectedStandards = r.data.aggregations.schema.buckets;
                  $scope.isSelectedAMixOfStandards =
                    $scope.selectedStandards && $scope.selectedStandards.length > 1;
                  // TODO: If too many records - only list the first 20.
                },
                function (r) {
                  // Could produce too long URLs 414 (URI Too Long)
                  console.log(r);
                  if (r.status === 414) {
                    $scope.tooManyRecordInSelForSearch = true;
                  } else {
                    console.warn(r);
                  }
                }
              );
            }
          });
        }
      });
      $scope.hasRecordsInStandard = function (standard) {
        var isFound = false;
        // We can't do this check when too many records in selection.
        if ($scope.tooManyRecordInSelForSearch) {
          return true;
        }
        $.each($scope.selectedStandards, function (idx, facet) {
          if (facet.key == standard) {
            isFound = true;
            return false;
          }
        });
        return isFound;
      };

      // Get current selection which returns the list of uuids.
      // Then search those records.
      $scope.searchSelection = function (params) {
        $http.get("../api/selections/e101").then(function (response) {
          var uuids = response.data;
          $scope.searchObj.params = angular.extend(
            {
              uuid: uuids
            },
            $scope.defaultSearchObj.params
          );
          $scope.triggerSearch();
        });
      };
    }
  ]);

  /**
   * Take care of defining changes and applying them.
   */
  module.controller("GnBatchEditController", [
    "$scope",
    "$location",
    "$http",
    "$compile",
    "$httpParamSerializer",
    "gnSearchSettings",
    "gnGlobalSettings",
    "gnCurrentEdit",
    "gnSchemaManagerService",
    "gnPopup",
    "$translate",
    "gnClipboard",
    "$rootScope",
    function (
      $scope,
      $location,
      $http,
      $compile,
      $httpParamSerializer,
      gnSearchSettings,
      gnGlobalSettings,
      gnCurrentEdit,
      gnSchemaManagerService,
      gnPopup,
      $translate,
      gnClipboard,
      $rootScope
    ) {
      $scope.editTypes = [
        { id: "searchAndReplace", icon: "fa-refresh fa-rotate-90" },
        { id: "xpathEdits", icon: "fa-code" },
        { id: "batchEdits", icon: "fa-wpforms" }
      ];

      // Simple tab handling.
      $scope.selectedStep = 1;
      $scope.setStep = function (step) {
        $scope.selectedStep = step;
        $scope.preview = undefined;
        $scope.previewError = undefined;
      };
      $scope.extraParams = {};
      $scope.$watch("selectedStep", function (newValue) {
        if (newValue === 2) {
          // Initialize map size when tab is rendered.
          var map = $("div.gn-drawmap-panel");
          if (map == undefined) {
            return;
          }
          map.each(function (idx, div) {
            var map = $(div).data("map");
            if (!angular.isArray(map.getSize()) || map.getSize()[0] == 0) {
              setTimeout(function () {
                map.updateSize();
              });
            }
          });
        } else if (newValue === 3) {
          $scope.canPreview =
            ($scope.editType === "searchAndReplace" &&
              $scope.searchAndReplaceChanges[0] &&
              $scope.searchAndReplaceChanges[0].search != "") ||
            ($scope.editType !== "searchAndReplace" && $scope.changes.length > 0);
        }
      });

      // Search setup
      gnSearchSettings.resultViewTpls = [
        {
          tplUrl:
            "../../catalog/components/search/resultsview/" +
            "partials/viewtemplates/titlewithselection.html",
          tooltip: "List",
          icon: "fa-list"
        }
      ];
      gnSearchSettings.resultTemplate = gnSearchSettings.resultViewTpls[0];

      // TODO: Improve for other standards
      // Object used by directory directive
      gnCurrentEdit = {
        schema: "iso19139"
      };

      $scope.searchAndReplaceField = {
        search: "",
        replacement: "",
        useRegexp: false,
        regexpFlags: ""
      };
      $scope.searchAndReplaceChanges = [];
      $scope.searchAndReplaceChanges[0] = $scope.searchAndReplaceField;
      $scope.regexpFlags = ["i", "c", "n", "m"];

      $scope.setType = function (type) {
        $scope.editType = type;
        if (type === "searchAndReplace") {
          $scope.searchAndReplaceChanges[0] = $scope.searchAndReplaceField;
        } else {
          $scope.searchAndReplaceChanges.length = 0;
        }
      };

      $scope.fieldConfig = null; // Configuration per standard
      $scope.changes = [];
      // TODO: Add a mode gn_update_only_if_match ?
      $scope.insertModes = ["gn_add", "gn_replace", "gn_delete"];

      // Add a change to the list
      var insertChange = function (
        field,
        xpath,
        template,
        value,
        index,
        insertMode,
        isXpath,
        condition
      ) {
        $scope.changes[index] = {
          field: field,
          insertMode: insertMode || field.insertMode,
          xpath: xpath,
          value: template && value !== "" ? template.replace("{{value}}", value) : value,
          isXpath: isXpath || false,
          condition: condition || ""
        };
      };

      // Add field with multiple value allowed.
      $scope.addChange = function (field, $event) {
        insertChange(
          field.name,
          field.xpath,
          field.template,
          $event.target.value,
          $scope.changes.length,
          field.insertMode,
          field.condition
        );
      };

      // Add field with only one value allowed.
      $scope.putChange = function (field, $event) {
        var index = $scope.changes.length;

        if ($event && $event.target && $event.target.value === "") {
          $scope.removeChange(field.xpath);
        } else {
          for (var j = 0; j < $scope.changes.length; j++) {
            if ($scope.changes[j].xpath === field.xpath) {
              index = j;
              break;
            }
          }
          insertChange(
            field.name,
            field.xpath,
            field.template,
            $event.target.value,
            index,
            field.insertMode,
            field.condition
          );
        }
      };

      // Remove field. If value is undefined, remove all changes for that field.
      $scope.removeChange = function (xpath, value) {
        for (var j = 0; j < $scope.changes.length; j++) {
          if (
            $scope.changes[j].xpath === xpath &&
            (value === undefined || $scope.changes[j].value === value)
          ) {
            $scope.changes.splice(j, 1);
            return;
          }
        }
      };

      $scope.resetChanges = function () {
        $scope.changes = [];
        $scope.xmlExtents = {};
        $scope.xmlContacts = {};
        $(
          "#gn-batch-changes input, " +
            "#gn-batch-changes textarea, " +
            "#gn-batch-changes select"
        ).each(function (idx, e) {
          $(e).val("");
        });
      };

      $scope.markFieldAsDeleted = function (field, mode) {
        field.isDeleted = !field.isDeleted;
        field.value = "";
        $scope.removeChange(field);
        if (field.isDeleted) {
          insertChange(
            field.name,
            field.xpath,
            field.template,
            "",
            $scope.changes.length,
            mode || "gn_delete",
            field.condition
          );
        }
      };

      // Extents
      $scope.xmlExtents = {};
      $scope.$watchCollection("xmlExtents", function (newValue, oldValue) {
        angular.forEach($scope.xmlExtents, function (value, xpath) {
          $scope.putChange(
            {
              name: "extent",
              xpath: xpath,
              insertMode: "gn_create" // TODO: Should come from config
            },
            {
              target: {
                value: value
              }
            }
          );
        });
      });

      // Contacts
      $scope.xmlContacts = {};
      $scope.addContactCb = function (scope, record, role) {
        var field = angular.fromJson(scope.attrs["field"]);
        if (!$scope.xmlContacts[field.name]) {
          $scope.xmlContacts[field.name] = {
            field: field,
            values: []
          };
        }
        $scope.xmlContacts[field.name].values.push({
          title: record.resourceTitle + (role ? " - " + role : ""),
          xml: scope.snippet
        });
        $scope.addChange(field, {
          target: {
            value: scope.snippet
          }
        });
      };
      $scope.removeContact = function (field, contact) {
        $scope.removeChange(field.xpath, contact.xml);
        for (var j = 0; j < $scope.xmlContacts[field.name].values.length; j++) {
          if ($scope.xmlContacts[field.name].values[j].xml === contact) {
            $scope.xmlContacts[field.name].values.splice(j, 1);
            return;
          }
        }
      };

      // Manual XPath setup
      var xpathCounter = 0; // Counter to identify manual XPath values
      $scope.currentXpath = {}; // The XPath entry manually defined
      $scope.defaultCurrentXpath = {
        field: "",
        xpath: "",
        value: "",
        insertMode: "gn_add",
        condition: ""
      }; // The default value when reset.
      $scope.currentXpath = angular.copy($scope.defaultCurrentXpath, {});

      $scope.addOrUpdateXpathChange = function () {
        var c = $scope.currentXpath;
        xpathCounter++;
        if (c.field == "") {
          c.field = "XPath_" + xpathCounter;
        }

        insertChange(
          c.field,
          c.xpath,
          "",
          c.value,
          $scope.changes.length,
          c.insertMode,
          true,
          c.condition
        );

        $scope.currentXpath = angular.copy($scope.defaultCurrentXpath, {});
      };
      $scope.removeXpathChange = function (c) {
        $scope.removeChange(c.xpath, c.value);
        xpathCounter--;
      };
      $scope.pasteFromClipboard = function () {
        gnClipboard.paste().then(function (text) {
          try {
            var config = JSON.parse(text);
            if (config["insertMode"]) {
              $scope.currentXpath = config;
            } else {
              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("batchEditConfigIsNotValid"),
                timeout: 2,
                type: "danger"
              });
            }
          } catch (e) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("batchEditConfigIsNotJson"),
              error: e,
              timeout: 2,
              type: "danger"
            });
          }
        });
      };
      $scope.editXpathChange = function (c) {
        $scope.removeChange(c.xpath, c.value);
        $scope.currentXpath = c;
      };
      $scope.isXpath = function (value) {
        return value.isXpath || false;
      };

      $scope.processReport = null;

      function buildChanges() {
        var params = [],
          i = 0;
        angular.forEach($scope.changes, function (field) {
          if (field.value != null) {
            var value = field.value,
              xpath = field.xpath;
            if (field.insertMode != null) {
              value =
                "<" +
                field.insertMode +
                ">" +
                field.value +
                "</" +
                field.insertMode +
                ">";
            } else {
              value = value;
            }
            params.push({ xpath: xpath, value: value, condition: field.condition });
            i++;
          }
        });
        return params;
      }

      $scope.diffType = undefined;
      function formatDiff(diff, diffType) {
        var formattedDiff = diff
          .replace('<?xml version="1.0" encoding="UTF-8"?>\n', "")
          .replace(/<preview>(.*)<\/preview>/g, "$1");
        if (diffType === "diffhtml") {
          return formattedDiff
            .replaceAll("&lt;span&gt;", "<span>")
            .replaceAll("&lt;/span&gt;", "</span>")
            .replaceAll(
              '&lt;ins style="background:#e6ffe6;"&gt;',
              '<ins class="text-success">'
            )
            .replaceAll("&lt;/ins&gt;", "</ins>")
            .replaceAll(
              '&lt;del style="background:#ffe6e6;"&gt;',
              '<del class="text-danger">'
            )
            .replaceAll("&lt;/del&gt;", "</del>")
            .replaceAll("&lt;br&gt;", "<br/>")
            .replaceAll("&amp;", "&");
        } else if (diffType === "diff") {
          return formattedDiff.replaceAll("Diff(", "\r\n\r\nDiff(");
        } else if (diffType === "patch") {
          return decodeURIComponent(formattedDiff);
        } else {
          return formattedDiff;
        }
      }

      function buildRequest(isPreview, uuid, bucket) {
        var isSearchAndReplace = $scope.editType === "searchAndReplace";

        var params = isSearchAndReplace
          ? {
              search: $scope.searchAndReplaceChanges[0].search,
              replace: $scope.searchAndReplaceChanges[0].replacement,
              useRegexp: $scope.searchAndReplaceChanges[0].regexpFlags !== "",
              regexpFlags: $scope.searchAndReplaceChanges[0].regexpFlags,
              updateDateStamp: $scope.extraParams.updateDateStamp,
              diffType: $scope.diffType || ""
            }
          : {
              updateDateStamp: $scope.extraParams.updateDateStamp,
              diffType: $scope.diffType || ""
            };

        if (uuid) {
          params.uuids = uuid;
        } else if (bucket) {
          params.bucket = bucket;
        }

        var url =
          (isSearchAndReplace
            ? "../api/processes/db/search-and-replace?"
            : "../api/records/batchediting" + (isPreview ? "/preview" : "") + "?") +
          $httpParamSerializer(params);

        if (isSearchAndReplace) {
          return $http[isPreview ? "get" : "post"](url, {
            headers: {
              accept: "application/xml"
            }
          });
        } else {
          return $http[isPreview ? "post" : "put"](url, buildChanges());
        }
      }

      $scope.previewChanges = function (uuid, diffType) {
        $scope.diffType = diffType;
        $scope.preview = undefined;
        $scope.previewError = undefined;
        return buildRequest(true, uuid, null).then(
          function (r) {
            $scope.preview = formatDiff(r.data, diffType);
          },
          function (r) {
            $scope.previewError = r.data;
          }
        );
      };

      $scope.applyChanges = function () {
        $scope.preview = undefined;
        $scope.previewError = undefined;
        return buildRequest(false, null, "e101").then(
          function (r) {
            $scope.processReport = r.data;
          },
          function (r) {
            $scope.processReport = r.data;
          }
        );
      };

      $scope.setExample = function (e) {
        $scope.currentXpath = e;
      };

      function init() {
        $http.get("../api/standards/batchconfiguration").then(
          function (response) {
            $scope.fieldConfig = response.data;
            gnSchemaManagerService.getNamespaces();
            $scope.setType($scope.editTypes[0].id);
          },
          function (response) {
            console.warn(response.data);
          }
        );
      }
      init();
    }
  ]);
})();
