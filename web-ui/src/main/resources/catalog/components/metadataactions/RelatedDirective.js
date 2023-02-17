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
  goog.provide("gn_related_directive");

  goog.require("gn_atom");
  goog.require("gn_related_observer_directive");
  goog.require("gn_relatedresources_service");
  goog.require("gn_wms");
  goog.require("gn_wmts");
  goog.require("gn_external_viewer");

  var module = angular.module("gn_related_directive", [
    "gn_relatedresources_service",
    "gn_related_observer_directive",
    "gn_wms",
    "gn_wmts",
    "gn_atom",
    "gn_external_viewer"
  ]);

  /**
   * Shows a list of related records given an uuid with the actions defined in
   * config.js
   */
  module.service("gnRelatedService", [
    "$http",
    "$q",
    function ($http, $q) {
      this.get = function (uuidOrId, types) {
        var canceller = $q.defer();
        var request = $http({
          method: "get",
          url:
            "../api/records/" +
            uuidOrId +
            "/related?" +
            (types ? "type=" + types.split("|").join("&type=") : ""),
          timeout: canceller.promise,
          cache: true
        });

        var promise = request.then(
          function (response) {
            return response.data;
          },
          function () {
            return $q.reject(
              "Something went wrong loading " + "related records of type " + types
            );
          }
        );

        promise.abort = function () {
          canceller.resolve();
        };

        promise.finally(function () {
          promise.abort = angular.noop;
          canceller = request = promise = null;
        });
        return promise;
      };

      this.getMdsRelated = function (mds, types) {
        var uuids = mds.map(function (md) {
          return md.uuid;
        });
        var url = "../api/related";
        return $http.get(url, {
          params: {
            type: types,
            uuid: uuids
          }
        });
      };
    }
  ]);

  /**
   * Displays a panel with different types of relations available in the metadata object 'md'.
   *  - mode: mode to display the relations.
   *      - tabset: displays the relations in a tabset panel.
   *      - (other value): displays the relations in different div blocks.
   *
   *  - layout: Layout for the relation items.
   *      - card: display the relation items as a card.
   *      - (other value): display the relation items as a list.
   *
   *  - relatedConfig: array with the configuration of the relations to display. For each relation:
   *      - types: a list of relation types separated by '|'.
   *      - filter: Filter a type based on an attribute.
   *                Can't be used when multiple types are requested
   *                eg. data-filter="associationType:upstreamData"
   *                    data-filter="protocol:OGC:.*|ESRI:.*"
   *                    data-filter="-protocol:OGC:.*"
   *      - title: title translation key for the relations section.
   *
   * Example configuration:
   *
   * <div data-gn-related-container="md"
   *      data-mode="tabset"
   *      data-related-config="[{'types': 'onlines', 'filter': 'protocol:OGC:.*|ESRI:.*|atom.*', 'title': 'API'},
   *                      {'types': 'onlines', 'filter': 'protocol:.*DOWNLOAD.*|DB:.*|FILE:.*', 'title': 'download'},
   *                      {'types': 'onlines', 'filter': '-protocol:OGC:.*|ESRI:.*|atom.*|.*DOWNLOAD.*|DB:.*|FILE:.*', 'title': 'links'}]">
   *
   * </div>
   */
  module.directive("gnRelatedContainer", [
    "gnRelatedResources",
    "gnConfigService",
    function (gnRelatedResources, gnConfigService) {
      return {
        restrict: "A",
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/relatedContainer.html"
          );
        },
        scope: {
          md: "=gnRelatedContainer",
          mode: "=",
          relatedConfig: "="
        },
        link: function (scope, element, attrs, controller) {
          scope.lang = scope.lang || scope.$parent.lang;
          scope.relations = {};
          scope.relatedConfigUI = [];
          scope.config = gnRelatedResources;

          scope.relatedConfig.forEach(function (config) {
            var t = config.types.split("|");

            config.relations = {};

            t.forEach(function (type) {
              config.relations[type] =
                (type === "onlines" ? scope.md.link : scope.md.related[type]) || {};
              config.relationFound = config.relations[type].length > 0;

              var value = config.relations[type];

              // Check if tabs needs to be displayed
              if (scope.mode === "tabset" && config.filter && angular.isArray(value)) {
                var filters = gnConfigService.parseFilters(config.filter);

                config.relations[type] = [];
                for (var i = 0; i < value.length; i++) {
                  gnConfigService.testFilters(filters, value[i]) &&
                    config.relations[type].push(value[i]);
                }
                config.relationFound = config.relations[type].length > 0;
              } else {
                config.relations[type] = value;
              }

              scope.relatedConfigUI.push(config);
            });
          });
        }
      };
    }
  ]);

  module.directive("gnRelatedList", [
    "gnRelatedResources",
    function (gnRelatedResources) {
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/metadataactions/partials/relatedSimpleList.html",
        scope: {
          md: "=gnRelatedList",
          user: "=",
          title: "@?"
        },
        link: function (scope, element, attrs, controller) {
          scope.config = gnRelatedResources;
          var count = 0;
          scope.md &&
            scope.md.related &&
            Object.keys(scope.md.related).forEach(function (key) {
              count += scope.md.related[key].length;
            });
          scope.relationsFound = count > 0;
        }
      };
    }
  ]);

  module.directive("gnRelatedEditorList", [
    "gnRelatedResources",
    "$rootScope",
    function (gnRelatedResources, $rootScope) {
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/metadataactions/partials/relatedEditorList.html",
        scope: {
          related: "=gnRelatedEditorList",
          type: "@",
          readonly: "=?",
          removeCb: "&?"
        },
        link: function (scope) {
          scope.md = {
            related: {}
          };
          scope.md.related[scope.type] = scope.related;
          scope.remove = angular.isFunction(scope.removeCb)
            ? function (md) {
                scope.removeCb({ record: md });
              }
            : undefined;

          scope.canRemoveLink = function (record) {
            if (record.origin === "remote") {
              return true;
            } else if (scope.remove) {
              return true;
            }
            return false;
          };
          scope.user = $rootScope.user;
          scope.config = gnRelatedResources;
        }
      };
    }
  ]);

  module.directive("gnRelatedDropdown", [
    "gnRelatedResources",
    function (gnRelatedResources) {
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/metadataactions/partials/relatedDropdown.html",
        scope: {
          md: "=gnRelatedDropdown",
          user: "="
        },
        link: function (scope, element, attrs, controller) {
          scope.config = gnRelatedResources;
          scope.hasRelations = false;
          if (scope.md && scope.md.related) {
            var total = 0;
            Object.keys(scope.md.related).map(function (t) {
              total += scope.md.related[t].length;
            });
            scope.hasRelations = total > 0;
          }
        }
      };
    }
  ]);

  module.directive("gnRecordIsReplacedBy", [
    "$http",
    "Metadata",
    function ($http, Metadata) {
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/metadataactions/partials/relatedReplacedBy.html",
        scope: {
          uuid: "=gnRecordIsReplacedBy"
        },
        link: function (scope) {
          $http
            .post("../api/search/records/_search", {
              query: {
                query_string: {
                  query: '+agg_associated_revisionOf:"' + scope.uuid + '"'
                }
              }
            })
            .then(function (r) {
              scope.items = r.data.hits.hits.map(function (r) {
                return new Metadata(r);
              });
            });
        }
      };
    }
  ]);

  module.directive("gnRelated", [
    "gnRelatedService",
    "gnGlobalSettings",
    "gnSearchSettings",
    "gnRelatedResources",
    "gnExternalViewer",
    "gnConfigService",
    function (
      gnRelatedService,
      gnGlobalSettings,
      gnSearchSettings,
      gnRelatedResources,
      gnExternalViewer,
      gnConfigService
    ) {
      return {
        restrict: "A",
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/related.html"
          );
        },
        scope: {
          md: "=gnRelated",
          template: "@",
          types: "@",
          title: "@",
          altTitle: "@",
          list: "@",
          // Filter a type based on an attribute.
          // Can't be used when multiple types are requested
          // eg. data-filter="associationType:upstreamData"
          // data-filter="protocol:OGC:.*|ESRI:.*"
          // data-filter="-protocol:OGC:.*"
          filter: "@",
          container: "@",
          user: "=",
          hasResults: "=?",
          layout: "@",
          // Only apply to card layout
          size: "@",
          groupSiblingsByType: "=?"
        },
        require: "?^gnRelatedObserver",
        link: function (scope, element, attrs, controller) {
          var promise;
          var elem = element[0];
          scope.lang = scope.lang || scope.$parent.lang;
          element.on("$destroy", function () {
            // Unregister the directive in the observer if it is defined
            if (controller) {
              controller.unregisterGnRelated(elem);
            }
          });

          if (controller) {
            // Register the directive in the observer
            controller.registerGnRelated(elem);
          }

          scope.sizeConfig = {};
          scope.showAllItems = function (type) {
            scope.sizeConfig[type] =
              scope.sizeConfig[type] === scope.size
                ? scope.relations[type].length
                : scope.size;
          };
          scope.loadRelations = function (relation) {
            var relationCount = 0;
            scope.relationFound = false;
            angular.forEach(relation, function (value, idx) {
              if (!value) {
                return;
              }

              // init object if required
              scope.relations = scope.relations || {};
              scope.hasResults = true;

              if (!scope.relations[idx]) {
                scope.relations[idx] = [];
                scope.sizeConfig[idx] = scope.size;
              }
              if (scope.filter && angular.isArray(value)) {
                var filters = gnConfigService.parseFilters(scope.filter);

                scope.relations[idx] = [];
                for (var i = 0; i < value.length; i++) {
                  gnConfigService.testFilters(filters, value[i]) &&
                    scope.relations[idx].push(value[i]);
                }
              } else {
                scope.relations[idx] = value;
              }

              // siblings, children, parent can contain elements from
              // siblings or associated if linking is made in both direction
              // Priority:
              // * children (can also be associated with isComposedOf),
              // and parent (can also be associated with partOfSeamlessDatabase)
              // are preserved
              // * Exclude children and parent from associated and siblings,
              // and also filter siblings from associated to avoid duplicates
              var siblingsCount = 0;
              if (idx === "associated" || idx === "siblings") {
                var indexToRemove = [];
                for (var i = 0; i < scope.relations[idx].length; i++) {
                  if (
                    []
                      .concat(
                        idx === "associated" ? scope.md.related.siblings || [] : [],
                        scope.md.related.parent || [],
                        scope.md.related.children || []
                      )
                      .filter(function (e) {
                        return e && e.id === scope.relations[idx][i].id;
                      }).length > 0
                  ) {
                    // Exclude
                    indexToRemove.push(i);
                  }
                }
                indexToRemove.reverse().forEach(function (value) {
                  scope.relations[idx].splice(value, 1);
                });

                if (
                  scope.relations.siblings &&
                  scope.relations.siblings.map &&
                  scope.groupSiblingsByType
                ) {
                  var siblings = angular.copy(scope.relations.siblings);
                  scope.relations.siblings = [];
                  siblings
                    .map(function (r) {
                      return (r.properties && r.properties.initiativeType) || "";
                    })
                    .filter(function (value, index, self) {
                      return self.indexOf(value) === index;
                    })
                    .forEach(function (type) {
                      scope.relations["siblings" + type] = siblings.filter(function (r) {
                        return r.properties && r.properties.initiativeType === type;
                      });
                      siblingsCount += scope.relations["siblings" + type].length;
                    });
                } else {
                  siblingsCount = scope.relations[idx].length;
                }
              }

              relationCount +=
                idx === "siblings" ? siblingsCount : scope.relations[idx].length;
            });
            scope.relationFound = relationCount > 0;
          };

          scope.updateRelations = function () {
            scope.relations = null;
            if (scope.id) {
              scope.relationFound = false;
              if (controller) {
                controller.startGnRelatedRequest(elem);
              }
              (promise = gnRelatedService.get(scope.id, scope.types)).then(
                function (data) {
                  scope.loadRelations(data);
                  if (angular.isDefined(scope.container) && scope.relations == null) {
                    $(scope.container).hide();
                  }
                  if (controller) {
                    controller.finishRequest(elem, scope.relationFound);
                  }
                },
                function () {
                  if (controller) {
                    controller.finishRequest(elem, false);
                  }
                }
              );
            }
          };

          scope.getTitle = function (link) {
            return link.title["#text"] || link.title;
          };

          scope.getOrderBy = function (link) {
            return link && link.resourceTitle ? link.resourceTitle : link.locTitle;
          };

          scope.externalViewerAction = function (mainType, link, md) {
            gnExternalViewer.viewService(md, link);
          };
          scope.hasAction = gnRelatedResources.hasAction;
          scope.getBadgeLabel = gnRelatedResources.getBadgeLabel;
          scope.isLayerProtocol = gnRelatedResources.isLayerProtocol;
          scope.externalViewerActionEnabled = gnExternalViewer.isEnabledViewAction();

          scope.config = gnRelatedResources;

          scope.$watchCollection("md", function (n, o) {
            if ((n && n !== o) || angular.isUndefined(scope.id)) {
              if (promise && angular.isFunction(promise.abort)) {
                promise.abort();
              }
              if (scope.md != null) {
                if (scope.md.related || scope.md.link) {
                  var relations = {};
                  scope.types.split("|").map(function (t) {
                    relations[t] = t === "onlines" ? scope.md.link : scope.md.related[t];
                  });
                  scope.loadRelations(relations);
                } else {
                  scope.id = scope.md.id;
                  scope.updateRelations();
                }
              }
            }
          });
        }
      };
    }
  ]);

  module.directive("gnRecordsFilters", [
    "$rootScope",
    function ($rootScope) {
      return {
        restrict: "A",
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/recordsFilters.html"
          );
        },
        scope: {
          agg: "=",
          filters: "=",
          title: "@"
        },
        link: function (scope, element, attrs, controller) {
          scope.lang = scope.lang || scope.$parent.lang;
          // Show display type toggle if no type selected only
          scope.showTypes = !angular.isDefined(scope.type);
          scope.type = scope.type || "blocks";
          scope.criteria = { p: {} };

          function removeEmptyFilters(filters, agg) {
            var cleanFilterPos = [];

            Object.keys(agg).forEach(function (key) {
              if (agg[key].buckets.length == 0) {
                cleanFilterPos.push(key);
              }
            });

            _.remove(filters, function (filter) {
              return cleanFilterPos.indexOf(filter) > -1;
            });
          }

          function reset() {
            scope.current = undefined;
            $rootScope.$broadcast("RecordsFiltersUpdated", {
              key: "",
              value: ""
            });
          }

          // Remove the filters without values
          scope.filtersToProcess = scope.filters || Object.keys(scope.agg);
          scope.agg && removeEmptyFilters(scope.filtersToProcess, scope.agg);

          reset();

          scope.filterRecordsBy = function (key, value) {
            var newKey = key + "-" + value;
            if (newKey === scope.current) {
              reset();
              return;
            }
            scope.current = key + "-" + value;

            $rootScope.$broadcast("RecordsFiltersUpdated", {
              key: key,
              value: value
            });
          };
        }
      };
    }
  ]);

  module.directive("gnRelatedWithStats", [
    function () {
      return {
        restrict: "A",
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/relatedWithStats.html"
          );
        },
        scope: {
          children: "=gnRelatedWithStats",
          agg: "=",
          filters: "=",
          sortBy: "@",
          type: "@",
          title: "@"
        },
        link: function (scope, element, attrs, controller) {
          scope.lang = scope.lang || scope.$parent.lang;
          // Show display type toggle if no type selected only
          scope.showTypes = !angular.isDefined(scope.type);
          scope.type = scope.type || "blocks";
          scope.criteria = { p: {} };

          scope.filterRecordsBy = function (key, value) {
            if (key === "" || value === "") {
              reset();
              return;
            }

            scope.displayedRecords = [];
            var b = scope.agg[key].buckets;
            b.forEach(function (k) {
              if (k.key === value) {
                k.docs.hits.hits.forEach(function (r) {
                  scope.displayedRecords = scope.displayedRecords.concat(
                    _.filter(scope.children, { uuid: r._id })
                  );
                });
                sort();
              }
            });
          };

          scope.$on("RecordsFiltersUpdated", function (event, result) {
            scope.filterRecordsBy(result.key, result.value);
          });

          function removeEmptyFilters(filters, agg) {
            var cleanFilterPos = [];

            Object.keys(agg).forEach(function (key) {
              if (agg[key].buckets.length == 0) {
                cleanFilterPos.push(key);
              }
            });

            _.remove(filters, function (filter) {
              return cleanFilterPos.indexOf(filter) > -1;
            });
          }

          function sort() {
            if (scope.sortBy) {
              scope.displayedRecords.sort(function (a, b) {
                return a[scope.sortBy] && a[scope.sortBy].localeCompare(b[scope.sortBy]);
              });
            }
          }

          function reset() {
            scope.displayedRecords = scope.children;
            sort();
          }

          // Remove the filters without values
          scope.filtersToProcess = scope.filters || Object.keys(scope.agg);
          scope.agg && removeEmptyFilters(scope.filtersToProcess, scope.agg);

          reset();

          scope.toggleListType = function (type) {
            scope.type = type;
          };
        }
      };
    }
  ]);

  module.directive("gnMetadataCard", [
    "gnGlobalSettings",
    function (gnGlobalSettings) {
      return {
        restrict: "E",
        transclude: true,
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/metadataCard.html"
          );
        },
        scope: {
          md: "=",
          formatterUrl: "="
        },
        link: function (scope, element, attrs, controller) {
          scope.lang = scope.lang || scope.$parent.lang;
          scope.showStatusFooterFor =
            gnGlobalSettings.gnCfg.mods.search.showStatusFooterFor;
        }
      };
    }
  ]);

  module.directive("relatedTooltip", function () {
    return function (scope, element, attrs) {
      for (var i = 0; i < element.length; i++) {
        element[i].title = scope.$parent.md["@type"];
      }
      element.tooltip();
    };
  });

  module.directive("gnRecordLinksButton", [
    "gnRelatedResources",
    function (gnRelatedResources) {
      return {
        restrict: "A",
        replace: true,
        transclude: true,
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/recordLinksButton.html"
          );
        },
        scope: {
          links: "=gnRecordLinksButton",
          // empty or dropdown or dropdownOrButton (if one link)
          btn: "@",
          btnClass: "@",
          // btnDisabled: '=',
          type: "=",
          title: "@",
          altTitle: "@",
          // none, dropdownOnly
          iconMode: "@",
          iconClass: "@",
          record: "="
        },
        link: function (scope, element, attrs, controller) {
          if (scope.links && scope.links.length > 0) {
            scope.mainType = gnRelatedResources.getType(
              scope.links[0],
              scope.type || "onlines"
            );
            scope.icon =
              scope.iconClass || gnRelatedResources.getClassIcon(scope.mainType);

            scope.btnDisabled = scope.record.isLinkDisabled(scope.links[0]);
          }
        }
      };
    }
  ]);

  /**
   * Can support a link returned by the related API
   * or a link in a metadata record.
   *
   * Related API provides multilingual links and takes care of
   * user privileges. For metadata link, check download/dynamic properties.
   */
  module.directive("gnRecordLinkButton", [
    "gnRelatedResources",
    "gnRelatedService",
    function (gnRelatedResources, gnRelatedService) {
      return {
        restrict: "A",
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/recordLinkButton.html"
          );
        },
        scope: {
          link: "=gnRecordLinkButton",
          btn: "=",
          btnClass: "=",
          // none, only
          iconMode: "=",
          iconClass: "=",
          type: "=",
          record: "="
        },
        link: function (scope, element, attrs, controller) {
          if (scope.link) {
            scope.mainType = gnRelatedResources.getType(
              scope.link,
              scope.type || "onlines"
            );
            scope.badge = gnRelatedResources.getBadgeLabel(scope.mainType, scope.link);
            scope.icon =
              scope.iconClass || gnRelatedResources.getClassIcon(scope.mainType);
            scope.hasAction = gnRelatedResources.hasAction(scope.mainType);
            scope.service = gnRelatedResources;
            scope.isDropDown = scope.btn && scope.btn.indexOf("dropdown") === 0;
            scope.isSibling =
              scope.mainType == "MDSIBLING" &&
              scope.link.associationType &&
              scope.link.associationType != "";

            scope.btnDisabled = scope.record.isLinkDisabled(scope.link);
          }
        }
      };
    }
  ]);

  module.directive("gnRecordsTable", [
    "Metadata",
    "gnRelatedService",
    function (Metadata, gnRelatedService) {
      return {
        restrict: "A",
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/recordsTable.html"
          );
        },
        scope: {
          records: "=gnRecordsTable",
          // Comma separated values. Supported
          // * properties eg. resourceTitle
          // * object path eg. cl_status.key
          // * links by type eg. link:OGC
          columns: "@",
          labels: "@",
          agg: "="
        },
        link: function (scope, element, attrs, controller) {
          var initialized = false;
          scope.columnsConfig = scope.columns.split(",");
          scope.data = [];
          scope.displayedRecords = [];
          scope.headers = [];
          scope.isArray = angular.isArray;

          if (scope.labels) {
            scope.headers = scope.labels.split(",");
          } else {
            scope.columnsConfig.map(function (c) {
              scope.headers.push(c.startsWith("link/") ? c.split("/")[1] : c);
            });
          }

          function loadData() {
            scope.data = [];
            scope.displayedRecords = [];
            scope.records.map(function (r) {
              r = new Metadata(r);
              var recordData = {};

              scope.columnsConfig.map(function (c) {
                if (c.startsWith("link/")) {
                  recordData[c] = r.getLinksByFilter(c.split("/")[1]);
                } else {
                  recordData[c] = c.indexOf(".") != -1 ? _.at(r, c) : r[c];
                }
              });
              recordData.md = r;
              scope.data.push(recordData);
              scope.displayedRecords.push(recordData);
            });

            sort();
          }

          scope.$watchCollection("records", function (n, o) {
            if (n && (n !== o || !initialized)) {
              loadData();
              initialized = true;
            }
          });

          function sort() {
            scope.displayedRecords.sort(function (a, b) {
              var sortBy = scope.columnsConfig[0];
              return a[sortBy] && a[sortBy].localeCompare(b[sortBy]);
            });
          }

          function reset() {
            scope.displayedRecords = scope.data;
            sort();
          }

          scope.$on("RecordsFiltersUpdated", function (event, result) {
            scope.filterRecordsBy(result.key, result.value);
          });

          scope.filterRecordsBy = function (key, value) {
            if (key === "" || value === "") {
              reset();
              return;
            }
            scope.displayedRecords = [];
            var b = scope.agg[key].buckets;
            b.forEach(function (k) {
              if (k.key === value) {
                k.docs.hits.hits.forEach(function (r) {
                  scope.displayedRecords = scope.displayedRecords.concat(
                    _.filter(scope.data, function (item) {
                      return item.md._id === r._id;
                    })
                  );
                });
              }
            });

            sort();
          };
        }
      };
    }
  ]);
})();
