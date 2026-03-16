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
  goog.provide("gn_harvest_settings_controller");

  goog.require("gn_category");
  goog.require("gn_importxsl");

  var module = angular.module("gn_harvest_settings_controller", [
    "ui.bootstrap.typeahead",
    "gn_category",
    "gn_importxsl"
  ]);

  /**
   * GnHarvestSettingsController provides management interface
   * for harvest settings.
   *
   */
  module.controller("GnHarvestSettingsController", [
    "$scope",
    "$q",
    "$http",
    "$translate",
    "$injector",
    "$rootScope",
    "gnSearchManagerService",
    "gnUtilityService",
    "$timeout",
    "Metadata",
    "gnMapsManager",
    "gnGlobalSettings",
    "gnConfig",
    "gnConfigService",
    "gnClipboard",
    "gnSearchSettings",
    "gnLanguageService",
    function (
      $scope,
      $q,
      $http,
      $translate,
      $injector,
      $rootScope,
      gnSearchManagerService,
      gnUtilityService,
      $timeout,
      Metadata,
      gnMapsManager,
      gnGlobalSettings,
      gnConfig,
      gnConfigService,
      gnClipboard,
      gnSearchSettings,
      gnLanguageService
    ) {
      $scope.searchObj = {
        internal: true,
        configId: "harvester",
        defaultParams: {
          isTemplate: ["y", "n", "s", "t"],
          sortBy: "resourceTitleObject.default.sort"
        }
      };
      $scope.searchObj.params = angular.extend(
        {},
        $scope.searchObj.params,
        $scope.searchObj.defaultParams
      );

      $scope.facetConfig = gnGlobalSettings.gnCfg.mods.admin.facetConfig;
      $scope.harvesterTypes = {};
      $scope.harvesterSelected = null;
      $scope.harvesterUpdated = false;
      $scope.harvesterNew = false;
      $scope.harvesterHistory = {};
      $scope.isLoadingOneHarvester = false;

      $scope.harvesterHistoryPaging = {
        page: 1,
        size: 3,
        pages: 0,
        total: 0
      };
      $scope.isLoadingHarvesterHistory = false;
      $scope.deleting = []; // all harvesters being deleted

      var unbindStatusListener = null;

      var bboxProperties = ["bbox-xmin", "bbox-ymin", "bbox-xmax", "bbox-ymax"];

      function loadHarvester(id) {
        $scope.isLoadingOneHarvester = true;
        return $http.get("../api/harvesters?id=" + id).then(
          function (response) {
            var data = response.data;

            if (data && data[0]) {
              $scope.harvesterSelected = data[0];
              $scope.harvesterUpdated = false;
              $scope.harvesterNew = false;
              $scope.harvesterHistory = {};
              $scope.searchResults = null;
              gnUtilityService.parseBoolean($scope.harvesterSelected);
            }
            $scope.isLoadingOneHarvester = false;

            // The backend returns an empty array in json serialization if the field is empty, instead of a string
            if (angular.isArray($scope.harvesterSelected.content.translateContentLangs)) {
              $scope.harvesterSelected.content.translateContentLangs = "";
            }

            if (
              $scope.harvesterSelected.content &&
              $scope.harvesterSelected.content.batchEdits
            ) {
              if (angular.isObject($scope.harvesterSelected.content.batchEdits)) {
                $scope.harvesterSelected.content.batchEdits = angular.toJson(
                  $scope.harvesterSelected.content.batchEdits,
                  true
                );
              } else {
                $scope.harvesterSelected.content.batchEdits = "";
              }
            }
            if ($scope.harvesterSelected.bboxFilter) {
              var s = $scope.harvesterSelected.bboxFilter;
              if ($scope.harvesterSelected.bboxFilter["bbox-xmin"]) {
                bboxProperties.forEach(function (coordinate) {
                  s[coordinate] = parseFloat(s[coordinate]);
                });
                $scope.extent.md = [
                  s["bbox-xmin"],
                  s["bbox-ymin"],
                  s["bbox-xmax"],
                  s["bbox-ymax"]
                ];
                $scope.extent.form = [
                  s["bbox-xmin"],
                  s["bbox-ymin"],
                  s["bbox-xmax"],
                  s["bbox-ymax"]
                ];
              } else {
                s["bbox-xmin"] = NaN;
                s["bbox-ymin"] = NaN;
                s["bbox-xmax"] = NaN;
                s["bbox-ymax"] = NaN;
                $scope.extent = {
                  md: [],
                  map: [],
                  form: []
                };
              }
            }
          },
          function (response) {
            // TODO
            $scope.isLoadingOneHarvester = false;
          }
        );
      }

      function loadHistory(backgroundLoad) {
        var page, size, uuid;
        page = $scope.harvesterHistoryPaging.page - 1;
        size = $scope.harvesterHistoryPaging.size;
        uuid = $scope.harvesterSelected.site.uuid;
        var list;
        $scope.isLoadingHarvesterHistory = true;
        if (!backgroundLoad) {
          $scope.harvesterHistory = undefined;
        } else {
          list = $("ul.timeline, .timeline-panel");
          list.addClass("loading");
        }
        $http
          .get(
            "admin.harvester.history?uuid=" +
              uuid +
              "&page=" +
              page +
              "&size=" +
              size +
              "&_content_type=json"
          )
          .then(
            function (response) {
              var data = response.data;

              $scope.harvesterHistory = data.harvesthistory;
              $scope.harvesterHistoryPaging.pages = parseInt(data.pages);
              $scope.harvesterHistoryPaging.total = parseInt(data.total);
              $scope.isLoadingHarvesterHistory = false;
              if (list) {
                list.removeClass("loading");
              }
            },
            function (response) {
              // TODO
              $scope.isLoadingHarvesterHistory = false;
            }
          );
      }

      $scope.historyFirstPage = function () {
        $scope.harvesterHistoryPaging.page = 1;
        loadHistory(true);
      };
      $scope.historyLastPage = function () {
        $scope.harvesterHistoryPaging.page = $scope.harvesterHistoryPaging.pages;
        loadHistory(true);
      };
      $scope.historyNextPage = function () {
        $scope.harvesterHistoryPaging.page = Math.min(
          $scope.harvesterHistoryPaging.pages,
          $scope.harvesterHistoryPaging.page + 1
        );
        loadHistory(true);
      };
      $scope.historyPreviousPage = function () {
        $scope.harvesterHistoryPaging.page = Math.max(
          1,
          $scope.harvesterHistoryPaging.page - 1
        );
        loadHistory(true);
      };
      function loadHarvesterTypes() {
        $http
          .get("admin.harvester.info?_content_type=json&type=harvesterTypes", {
            cache: true
          })
          .then(
            function (response) {
              angular.forEach(response.data[0], function (value) {
                $scope.harvesterTypes[value] = {
                  type: value,
                  label: "harvester-" + value
                };

                $.getScript("../../catalog/templates/admin/harvest/type/" + value + ".js")
                  .done(function (script, textStatus) {
                    $scope.$apply(function () {
                      $scope.harvesterTypes[value].loaded = true;
                    });
                    // FIXME: could we make those harvester specific
                    // function a controller
                  })
                  .fail(function (jqxhr, settings, exception) {
                    $scope.harvesterTypes[value].loaded = false;
                  });
              });
            },
            function (response) {
              // TODO
            }
          );
      }

      $scope.getTplForHarvester = function () {
        // TODO : return view by calling harvester ?
        if ($scope.harvesterSelected && $scope.harvesterSelected.site) {
          if (
            $scope.harvesterSelected.site.ogctype &&
            $scope.harvesterSelected.site.ogctype.match("^(WPS2)") != null
          ) {
            $scope.metadataTemplateType = $translate.instant("process");
          } else {
            $scope.metadataTemplateType = $translate.instant("layer");
          }
          return (
            "../../catalog/templates/admin/" +
            $scope.pageMenu.folder +
            "type/" +
            $scope.harvesterSelected["@type"] +
            ".html"
          );
        } else {
          return null;
        }
      };
      $scope.updatingHarvester = function () {
        $scope.harvesterUpdated = true;
      };
      $scope.addHarvester = function (type) {
        $scope.harvesterNew = true;
        $scope.harvesterHistory = {};
        $scope.harvesterSelected = window["gnHarvester" + type].createNew();
      };

      $scope.cloneHarvester = function (id) {
        $http.put("../api/harvesters/" + id + "/clone").then(
          function (response) {
            $scope.$parent.loadHarvesters().then(function () {
              // Select the clone
              angular.forEach($scope.$parent.harvesters, function (h) {
                if (h["@id"] == response.data) {
                  $scope.selectHarvester(h);
                }
              });
            });
          },
          function (response) {
            // TODO
          }
        );
      };

      $scope.addFromClipboard = function () {
        $scope.harvesterNew = true;
        $scope.harvesterHistory = {};
        gnClipboard.paste().then(function (text) {
          try {
            var config = JSON.parse(text);
            if (config["@id"]) {
              // Looks like a valid harvester config
              config["@id"] = "";
              $scope.harvesterSelected = config;
            } else {
              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("harvesterConfigIsNotValid"),
                timeout: 2,
                type: "danger"
              });
            }
          } catch (e) {
            $rootScope.$broadcast("StatusUpdated", {
              msg: $translate.instant("harvesterConfigIsNotJson"),
              error: e,
              timeout: 2,
              type: "danger"
            });
          }
        });
      };

      $scope.buildResponseGroup = function (h) {
        var groups = "";
        angular.forEach(h.privileges, function (p) {
          var ops = "";
          angular.forEach(p.operation, function (o) {
            ops += '<operation name="' + o["@name"] + '"/>';
          });
          groups += '<group id="' + p["@id"] + '">' + ops + "</group>";
        });
        return (
          "<privileges>" +
          groups +
          "</privileges>" +
          "<ifRecordExistAppendPrivileges>" +
          h.ifRecordExistAppendPrivileges +
          "</ifRecordExistAppendPrivileges>"
        );
      };
      $scope.buildResponseCategory = function (h) {
        var cats = "";
        angular.forEach(h.categories, function (c) {
          cats += '<category id="' + c["@id"] + '"/>';
        });
        return "<categories>" + cats + "</categories>";
      };

      $scope.saveHarvester = function () {
        var body = window[
          "gnHarvester" + $scope.harvesterSelected["@type"]
        ].buildResponse($scope.harvesterSelected, $scope);
        var deferred = $q.defer();

        $http
          .post(
            "admin.harvester." +
              ($scope.harvesterNew ? "add" : "update") +
              "?_content_type=json",
            body,
            {
              headers: { "Content-type": "application/xml" }
            }
          )
          .then(
            function (response) {
              var data = response.data;

              if (!$scope.harvesterSelected["@id"]) {
                $scope.harvesterSelected["@id"] = data[0];
              }
              // Activate or disable it
              $scope.setHarvesterSchedule().finally(function () {
                $scope.$parent.loadHarvesters().then(refreshSelectedHarvester);
              });

              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("harvesterUpdated"),
                timeout: 2,
                type: "success"
              });
              deferred.resolve(data);
            },
            function (response) {
              deferred.reject(response.data);
              $rootScope.$broadcast("StatusUpdated", {
                msg: $translate.instant("harvesterUpdateError"),
                error: response.data,
                timeout: 2,
                type: "danger"
              });
            }
          );

        return deferred.promise;
      };

      $scope.selectHarvester = function (h) {
        $scope.activeTab.settings = true;
        $scope.setSimpleUrlPagination();

        // TODO: Specific to thredds
        if (h["@type"] === "thredds") {
          $scope.threddsCollectionsMode =
            h.options.outputSchemaOnAtomicsDIF !== "" ? "DIF" : "UNIDATA";
          $scope.threddsAtomicsMode =
            h.options.outputSchemaOnCollectionsDIF !== "" ? "DIF" : "UNIDATA";
        }

        $scope.harvesterSelected = null;

        $scope.harvesterSelected = h;
        $scope.harvesterUpdated = false;
        $scope.harvesterNew = false;
        $scope.harvesterHistory = {};
        $scope.searchResults = null;
        $scope.searchResultsTotal = null;
        $scope.harvesterHistoryPaging = {
          page: 1,
          size: 3,
          pages: 0,
          total: 0
        };

        loadHarvester(h["@id"]).then(function (data) {
          loadHistory();

          // Retrieve records in that harvester
          $scope.searchObj.params.harvesterUuid = $scope.harvesterSelected.site.uuid;
          $scope.$broadcast("resetSearch", $scope.searchObj.params);
        });
      };

      /**
       * Update the total metadata in the metadata tab, ng-search-form is in a child element.
       * Can't be moved to a parent element as causes issues with pagination, due scope
       * conflicts.
       *
       * It's used an event to update the total metadata when the harvested metadata search finishes.
       */
      $scope.$on("searchFinished", function (event, args) {
        $scope.searchResultsTotal = args.count;
      });

      var refreshSelectedHarvester = function () {
        if ($scope.harvesterSelected) {
          // Refresh the selected harvester
          angular.forEach($scope.harvesters, function (h) {
            if (h["@id"] === $scope.harvesterSelected["@id"]) {
              $scope.selectHarvester(h);
            }
          });
        }
      };

      $scope.deleteHarvester = function () {
        $scope.deleting.push($scope.harvesterSelected["@id"]);
        return $http.delete("../api/harvesters/" + $scope.harvesterSelected["@id"]).then(
          function (response) {
            $scope.harvesterSelected = {};
            $scope.harvesterUpdated = false;
            $scope.harvesterNew = false;
            $scope.$parent.loadHarvesters();

            $scope.deleting.splice($scope.deleting.indexOf(3), 1);
          },
          function (response) {
            console.log(response.data);
          }
        );
      };

      $scope.deleteHarvesterRecord = function () {
        return $http
          .put("../api/harvesters/" + $scope.harvesterSelected["@id"] + "/clear")
          .then(
            function (response) {
              $scope.harvesterSelected = {};
              $scope.harvesterUpdated = false;
              $scope.harvesterNew = false;
              $scope.$parent.loadHarvesters();
            },
            function (response) {
              console.log(response.data);
            }
          );
      };
      $scope.assignHarvestedRecordToLocalNode = function () {
        $http
          .post(
            "../api/harvesters/" +
              $scope.harvesterSelected.site.uuid +
              "/assign?source=" +
              gnConfig["system.site.siteId"]
          )
          .then(
            function (response) {
              $scope.harvesterSelected = {};
              $scope.harvesterUpdated = false;
              $scope.harvesterNew = false;
              $scope.$parent.loadHarvesters();
            },
            function (response) {
              console.log(response.data);
            }
          );
      };
      $scope.deleteHarvesterHistory = function () {
        return $http
          .delete("../api/harvesters/" + $scope.harvesterSelected.site.uuid + "/history")
          .then(function (response) {
            $scope.$parent.loadHarvesters().then(function () {
              $scope.selectHarvester($scope.harvesterSelected);
            });
          });
      };
      $scope.runHarvester = function () {
        return $http
          .put("../api/harvesters/" + $scope.harvesterSelected["@id"] + "/run")
          .then(function (response) {
            $scope.$parent.loadHarvesters().then(function () {
              refreshSelectedHarvester();
            });
          });
      };
      $scope.stopping = false;
      $scope.stopHarvester = function () {
        var status = $scope.harvesterSelected.options.status;
        var id = $scope.harvesterSelected["@id"];
        $scope.stopping = true;
        return $http
          .put(
            "../api/harvesters/" +
              $scope.harvesterSelected["@id"] +
              "/stop?status=" +
              status
          )
          .then(function () {
            $scope.$parent.loadHarvesters().then(refreshSelectedHarvester);
            $scope.stopping = false;
          });
      };

      $scope.setHarvesterSchedule = function () {
        var deferred = $q.defer();

        if (!$scope.harvesterSelected) {
          deferred.resolve();
          return deferred.promise;
        }
        var status = $scope.harvesterSelected.options.status;

        var url =
          "../api/harvesters/" +
          $scope.harvesterSelected["@id"] +
          "/" +
          (status === "active" ? "start" : "stop");
        $http.put(url).then(
          function (response) {
            deferred.resolve(response.data);
          },
          function (response) {
            var data = response.data;

            deferred.reject(data);
            $rootScope.$broadcast("StatusUpdated", {
              title: $translate.instant("harvesterSchedule" + status),
              error: data,
              timeout: 0,
              type: "danger"
            });
          }
        );
        return deferred.promise;
      };

      // Register status listener
      //unbindStatusListener =
      // $scope.$watch('harvesterSelected.options.status',
      //    function() {
      //      $scope.setHarvesterSchedule();
      //    });

      // TODO: Check if can be moved to arcsde.js
      $scope.$watch("harvesterSelected.site.connectionType", function (newValue) {
        if (!$scope.harvesterSelected) return;

        if ($scope.harvesterSelected["@type"] === "arcsde") {
          if (newValue === "ARCSDE") {
            $scope.harvesterSelected.site.databaseType = "";
          }
        }
      });

      loadHarvesterTypes();

      $scope.activeTab = {
        settings: true,
        history: false,
        results: false
      };

      // ---------------------------------------
      // Those function are harvester dependant and
      // should move in the harvester code
      // TODO
      $scope.simpleUrlHarvesterHelperConfig = {
        "DCAT feed > ISO": {
          defaultValues: {
            loopElement: "",
            numberOfRecordPath: "",
            pageSizeParam: "",
            pageFromParam: "",
            recordIdPath: "",
            toISOConversion: "schema:iso19115-3.2018:convert/fromSPARQL-DCAT"
          }
        },
        JSON: {
          defaultValues: {
            loopElement: "/datasets",
            numberOfRecordPath: "/total_count",
            pageSizeParam: "limit",
            pageFromParam: "offset",
            recordIdPath: "/dataset/dataset_id",
            toISOConversion: "schema:iso19115-3.2018:convert/fromJsonOpenDataSoft"
          }
        },
        "STAC Collection": {
          defaultValues: {
            loopElement: "/collections",
            numberOfRecordPath: "",
            pageSizeParam: "limit",
            pageFromParam: "page",
            recordIdPath: "/id",
            toISOConversion: "schema:iso19115-3.2018:convert/stac-to-iso19115-3"
          }
        },
        "XML (ISO19115-3)": {
          defaultValues: {
            loopElement: ".",
            numberOfRecordPath: "",
            pageSizeParam: "",
            pageFromParam: "",
            recordIdPath: "mdb:metadataIdentifier/*/mcc:code/*/text()",
            toISOConversion: ""
          }
        },
        "XML (CSW-ISO19139)": {
          defaultValues: {
            loopElement: ".//csw:SearchResults/*",
            numberOfRecordPath: "",
            pageSizeParam: "",
            pageFromParam: "",
            recordIdPath: "gmd:fileIdentifier/*/text()",
            toISOConversion: ""
          }
        }
      };

      $scope.getSimpleUrlConfigHelper = function (configKey) {
        var helper = "";
        angular.forEach(
          $scope.simpleUrlHarvesterHelperConfig[configKey].defaultValues,
          function (v, k) {
            helper += $translate.instant(k) + " (" + k + "): " + v + "\n";
          }
        );
        return helper;
      };
      $scope.setSimpleUrlConfig = function (configKey) {
        angular.forEach(
          $scope.simpleUrlHarvesterHelperConfig[configKey].defaultValues,
          function (v, k) {
            $scope.harvesterSelected.site[k] = v;
          }
        );
        $scope.setSimpleUrlPagination();
      };

      $scope.setSimpleUrlPagination = function () {
        if (
          $scope.harvesterSelected &&
          $scope.harvesterSelected.site &&
          $scope.harvesterSelected.site.numberOfRecordPath
        ) {
          $scope.usePagination = {
            enabled: $scope.harvesterSelected.site.numberOfRecordPath.length > 0
          };
        }
      };

      $scope.geonetworkGetSources = function (url) {
        $http
          .get($scope.proxyUrl + encodeURIComponent(url + "/srv/eng/info?type=sources"))
          .then(
            function (response) {
              $scope.geonetworkSources = [];
              var i = 0;
              var xmlDoc = $.parseXML(response.data);
              var $xml = $(xmlDoc);
              var $sources = $xml.find("uuid");
              var $names = $xml.find("name");

              angular.forEach($sources, function (s) {
                // FIXME: probably some issue on IE ?
                $scope.geonetworkSources.push({
                  uuid: s.textContent,
                  name: $names[i].textContent
                });
                i++;
              });
            },
            function (error) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("harvesterErrorRetrieveSources"),
                error: error.data.message || error.data.error.message,
                timeout: 3,
                type: "danger"
              });
            }
          );
      };

      $scope.geonetworkGetSourcesGn4 = function (url) {
        $http
          .get($scope.proxyUrl + encodeURIComponent(url + "/srv/api/sources?type=portal"))
          .then(
            function (response) {
              var sourcesList = [];

              angular.forEach(response.data, function (source) {
                sourcesList.push({
                  uuid: source.uuid,
                  name: source.name
                });
              });

              $http
                .get(
                  $scope.proxyUrl +
                    encodeURIComponent(url + "/srv/api/sources?type=harvester")
                )
                .then(
                  function (response) {
                    $scope.geonetworkSources = [];
                    $scope.geonetworkSources = sourcesList;

                    angular.forEach(response.data, function (source) {
                      $scope.geonetworkSources.push({
                        uuid: source.uuid,
                        name: source.name
                      });
                    });
                  },
                  function (error) {
                    $rootScope.$broadcast("StatusUpdated", {
                      title: $translate.instant("harvesterErrorRetrieveSources"),
                      error: error.data.message || error.data.error.message,
                      timeout: 3,
                      type: "danger"
                    });
                  }
                );
            },
            function (error) {
              $rootScope.$broadcast("StatusUpdated", {
                title: $translate.instant("harvesterErrorRetrieveSources"),
                error: error.data.message || error.data.error.message,
                timeout: 3,
                type: "danger"
              });
            }
          );
      };

      // OGCWxS
      var ogcwxsGet = function () {
        $scope.ogcwxsTemplates = [];
        var query = {
          size: "200",
          query: {
            bool: {
              must: [
                {
                  terms: {
                    isTemplate: ["y"]
                  }
                },
                {
                  terms: {
                    resourceType: ["service"]
                  }
                }
              ]
            }
          },
          _source: ["resourceTitleObject.default"]
        };
        $http.post("../api/search/records/_search", query).then(
          function (r) {
            // List of template including an empty one if user
            // wants to build the service metadata record
            // from the GetCapabilities only
            var ogcwxsTemplates = [
              {
                _source: { resourceTitleObject: { default: "" } },
                _id: ""
              }
            ];
            $scope.ogcwxsTemplates = ogcwxsTemplates.concat(r.data.hits.hits);
          },
          function (data) {}
        );

        $scope.ogcwxsDatasetTemplates = [];
        var query = {
          size: "200",
          query: {
            bool: {
              must: [
                {
                  terms: {
                    isTemplate: ["y"]
                  }
                },
                {
                  terms: {
                    resourceType: ["dataset"]
                  }
                }
              ]
            }
          },
          _source: ["resourceTitleObject.default"]
        };
        $http.post("../api/search/records/_search", query).then(
          function (r) {
            // List of template including an empty one if user
            // wants to build the service metadata record
            // from the GetCapabilities only
            var ogcwxsDatasetTemplates = [
              {
                _source: { resourceTitleObject: { default: "" } },
                _id: ""
              }
            ];
            $scope.ogcwxsDatasetTemplates = ogcwxsDatasetTemplates.concat(
              r.data.hits.hits
            );
          },
          function (data) {}
        );
      };
      ogcwxsGet();

      // TODO: Should move to OAIPMH
      $scope.oaipmhSets = null;
      $scope.oaipmhPrefix = null;
      $scope.oaipmhInfo = null;
      $scope.oaipmhGet = function () {
        $scope.oaipmhInfoRequested = false;
        $scope.oaipmhInfo = null;
        var body =
          '<request><type url="' +
          $scope.harvesterSelected.site.url +
          '">oaiPmhServer</type></request>';
        $http
          .post("admin.harvester.info?_content_type=json", body, {
            headers: { "Content-type": "application/xml" }
          })
          .then(
            function (response) {
              var data = response.data;

              if (data[0].sets && data[0].formats) {
                $scope.oaipmhSets = data[0].sets;
                $scope.oaipmhPrefix = data[0].formats;
              } else {
                $scope.oaipmhInfo = $translate.instant("oaipmh-FailedToGetSetsAndPrefix");
              }
            },
            function (response) {}
          );
      };

      // TODO : enable watch only if OAIPMH
      $scope.$watch("harvesterSelected.site.url", function () {
        if ($scope.harvesterSelected && $scope.harvesterSelected["@type"] === "oaipmh") {
          //If the url is long, it hangs the server
          if (!$scope.oaipmhInfoRequested) {
            $scope.oaipmhInfoRequested = true;
            $timeout($scope.oaipmhGet, 2000);
          }
        }
      });

      // TODO: Should move to a CSW controller
      $scope.cswCriteria = [];
      $scope.cswCriteriaInfo = null;
      $scope.extent = {
        md: null,
        map: [],
        form: []
      };

      $scope.addBatchEdits = function (e) {
        var array;
        try {
          array = angular.fromJson($scope.harvesterSelected.content.batchEdits);
        } catch (e) {
          console.warn(
            "Harvester batch edit config is not a valid JSON.",
            $scope.harvesterSelected.content.batchEdits,
            e
          );
          array = [];
        }
        if (angular.isArray(array)) {
          e.value = "<" + e.insertMode + ">" + e.value + "</" + e.insertMode + ">";
          delete e.insertMode;
          delete e.field;
          array.push(e);
          $scope.harvesterSelected.content.batchEdits = angular.toJson(array, 2);
        }
      };

      $scope.$watchCollection("extent", function (n, o) {
        if (n !== o) {
          if (!$scope.harvesterSelected.bboxFilter) {
            $scope.harvesterSelected.bboxFilter = {};
          }

          if (n.md != null && $scope.harvesterSelected) {
            $scope.harvesterSelected.bboxFilter["bbox-xmin"] = parseFloat(n.md[0]);
            $scope.harvesterSelected.bboxFilter["bbox-ymin"] = parseFloat(n.md[1]);
            $scope.harvesterSelected.bboxFilter["bbox-xmax"] = parseFloat(n.md[2]);
            $scope.harvesterSelected.bboxFilter["bbox-ymax"] = parseFloat(n.md[3]);
          } else {
            $scope.harvesterSelected.bboxFilter["bbox-xmin"] = NaN;
            $scope.harvesterSelected.bboxFilter["bbox-ymin"] = NaN;
            $scope.harvesterSelected.bboxFilter["bbox-xmax"] = NaN;
            $scope.harvesterSelected.bboxFilter["bbox-ymax"] = NaN;
          }
        }
      });

      /**
       * Retrieve GetCapabilities document to retrieve
       * the list of possible search fields declared
       * in *Queryables.
       *
       * If the service is unavailable for a while and a user
       * go to the admin page, it may loose its filter.
       */
      $scope.cswGetCapabilities = function () {
        $scope.cswCriteriaInfo = null;

        if (
          $scope.harvesterSelected &&
          $scope.harvesterSelected.site &&
          $scope.harvesterSelected.site.capabilitiesUrl &&
          ($scope.harvesterSelected.site.capabilitiesUrl.indexOf("http://") != -1 ||
            $scope.harvesterSelected.site.capabilitiesUrl.indexOf("https://") != -1)
        ) {
          var url = $scope.harvesterSelected.site.capabilitiesUrl;

          // Add GetCapabilities if not already in URL
          // Parameter value is case sensitive.
          // Append a ? if not already in there and if not &
          if (url.indexOf("GetCapabilities") === -1) {
            url +=
              (url.indexOf("?") === -1 ? "?" : "&") +
              "SERVICE=CSW&REQUEST=GetCapabilities&VERSION=2.0.2";
          }

          $http.get($scope.proxyUrl + encodeURIComponent(url)).then(
            function (response) {
              $scope.cswCriteria = [];

              var i = 0;
              try {
                var xmlDoc = $.parseXML(response.data);

                // Create properties in model if no criteria defined
                if (!$scope.harvesterSelected.searches) {
                  $scope.harvesterSelected.searches = [{}];
                }

                var $xml = $(xmlDoc);
                var matches = [
                  "SupportedISOQueryables",
                  "SupportedQueryables",
                  "AdditionalQueryables"
                ];
                var parseCriteriaFn = function () {
                  // If the queryable has a namespace,
                  // replace the : with __
                  // to make valid XML tag name
                  var name = $(this).text().replace(":", "__");
                  $scope.cswCriteria.push(name);
                  if (!$scope.harvesterSelected.searches[0][name]) {
                    $scope.harvesterSelected.searches[0][name] = { value: "" };
                  }
                };
                var parseQueryablesFn = function () {
                  if (matches.indexOf($(this).attr("name")) !== -1) {
                    // Add all queryables to the list of possible parameters
                    // and to the current harvester if not exist.
                    // When harvester is saved only criteria with
                    // value will be saved.
                    $(this).find("Value").each(parseCriteriaFn);
                    $(this).find("ows\\:Value").each(parseCriteriaFn);
                  }
                };
                $scope.cswBboxFilter = false;
                var checkSpatialCapabilities = function () {
                  if ($(this).attr("name") === "BBOX") {
                    $scope.cswBboxFilter = true;
                    if (!$scope.harvesterSelected.bboxFilter) {
                      $scope.harvesterSelected.bboxFilter = {};
                    }
                    for (var i = 0; i < bboxProperties.length; i++) {
                      if (!$scope.harvesterSelected.bboxFilter[bboxProperties[i]]) {
                        $scope.harvesterSelected.bboxFilter[bboxProperties[i]] = NaN;
                      }
                    }
                  }
                };
                // For IE
                $xml.find("Constraint").each(parseQueryablesFn);
                $xml.find("SpatialOperator").each(checkSpatialCapabilities);
                // For Chrome & FF, namespace parsing is different
                if ($scope.cswCriteria.length === 0) {
                  $xml.find("ows\\:Constraint").each(parseQueryablesFn);
                  $xml.find("ogc\\:SpatialOperator").each(checkSpatialCapabilities);
                }

                $scope.cswCriteria.sort();
              } catch (e) {
                $scope.cswCriteriaInfo = $translate.instant(
                  "csw-FailedToParseCapabilities"
                );
              }
            },
            function (response) {
              // TODO
            }
          );
        }
      };

      // Don't launch the getCapabilities request until 750 ms
      // after the last value change.
      // Instantiate these variables outside the watch
      var capabilitiesUrlDelay;
      $scope.$watch("harvesterSelected.site.capabilitiesUrl", function (val) {
        if (capabilitiesUrlDelay) {
          $timeout.cancel(capabilitiesUrlDelay);
        }

        capabilitiesUrlDelay = $timeout(function () {
          $scope.cswGetCapabilities();
        }, 750); // delay 750 ms
      });

      // WFS GetFeature harvester
      $scope.harvesterTemplates = null;
      var loadHarvesterTemplates = function () {
        $http.get("info?_content_type=json&type=templates").then(function (response) {
          $scope.harvesterTemplates = response.data.templates;
        });
      };

      $scope.harvesterGetFeatureXSLT = null;
      var wfsGetFeatureXSLT = function () {
        $scope.oaipmhInfo = null;
        var body =
          "<request><type>wfsFragmentStylesheets</type><schema>" +
          $scope.harvesterSelected.options.outputSchema +
          "</schema></request>";
        $http
          .post("admin.harvester.info?_content_type=json", body, {
            headers: { "Content-type": "application/xml" }
          })
          .then(function (response) {
            $scope.harvesterGetFeatureXSLT = response.data[0];
          });
      };

      // When schema change reload the available XSLTs and templates
      $scope.$watch("harvesterSelected.options.outputSchema", function () {
        if (
          $scope.harvesterSelected &&
          $scope.harvesterSelected["@type"] === "wfsfeatures"
        ) {
          wfsGetFeatureXSLT();
          loadHarvesterTemplates();
        }
      });

      // Z3950 GetFeature harvester
      $scope.harvesterZ3950repositories = null;
      var loadHarvesterZ3950Repositories = function () {
        $http
          .get("info?_content_type=json&type=z3950repositories", { cache: true })
          .then(function (response) {
            $scope.harvesterZ3950repositories = response.data.z3950repositories;
          });
      };
      $scope.$watch("harvesterSelected.site.repositories", function () {
        if ($scope.harvesterSelected && $scope.harvesterSelected["@type"] === "z3950") {
          loadHarvesterZ3950Repositories();
        }
      });

      // Thredds
      $scope.threddsCollectionsMode = "DIF";
      $scope.threddsAtomicsMode = "DIF";
      $scope.harvesterThreddsXSLT = null;
      var threddsGetXSLT = function () {
        $scope.oaipmhInfo = null;
        var opt = $scope.harvesterSelected.options;
        var schema =
          $scope.threddsCollectionsMode === "DIF"
            ? opt.outputSchemaOnCollectionsDIF
            : opt.outputSchemaOnCollectionsFragments;
        var body =
          "<request><type>threddsFragmentStylesheets</type><schema>" +
          schema +
          "</schema></request>";
        $http
          .post("admin.harvester.info?_content_type=json", body, {
            headers: { "Content-type": "application/xml" }
          })
          .then(function (response) {
            $scope.harvesterThreddsXSLT = response.data[0];
          });
      };
      $scope.$watch(
        "harvesterSelected.options.outputSchemaOnCollectionsDIF",
        function () {
          if (
            $scope.harvesterSelected &&
            $scope.harvesterSelected["@type"] === "thredds"
          ) {
            threddsGetXSLT();
            loadHarvesterTemplates();
          }
        }
      );
      $scope.$watch(
        "harvesterSelected.options.outputSchemaOnCollectionsFragments",
        function () {
          if (
            $scope.harvesterSelected &&
            $scope.harvesterSelected["@type"] === "thredds"
          ) {
            threddsGetXSLT();
            loadHarvesterTemplates();
          }
        }
      );
      $scope.getHarvesterTypes = function () {
        var array = [];
        angular.forEach($scope.harvesterTypes, function (h) {
          h.label = $translate.instant(h.label);
          array.push(h);
        });
        return array;
      };
    }
  ]);
})();
