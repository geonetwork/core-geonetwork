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
      this.get = function (uuidOrId, types, approved) {
        var canceller = $q.defer();
        var request = $http({
          method: "get",
          url:
            "../api/records/" +
            uuidOrId +
            "/related?type=" +
            (types ? types.split("|").join("&type=") : "") +
            (approved === false ? "&approved=false" : ""),
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
        var approved = mds.map(function (md) {
          return md.draft !== "y";
        });
        var url = "../api/related";
        return $http.get(url, {
          params: {
            type: types,
            uuid: uuids,
            approved: approved
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

  /**
   * Directive to display a panel in the metadata editor with a header and a list of distributions grouped by type.
   *
   */
  module.directive("gnDistributionResourcesPanel", [
    function () {
      return {
        restrict: "A",
        transclude: true,
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/distributionResourcesPanel.html"
          );
        },
        scope: {
          md: "=gnDistributionResourcesPanel",
          mode: "=",
          relatedConfig: "=",
          editorConfig: "@"
        },
        link: function (scope, element, attrs) {}
      };
    }
  ]);

  /**
   * Displays a panel with different types of distributions available in the metadata object 'md'.
   *
   *  - mode: mode to display the distributions.
   *      - tabset: displays the distributions in a tabset panel.
   *      - (other value): displays the distributions in different div blocks.
   *
   *  - layout: Layout for the distribution items.
   *      - card: display the distribution items as a card.
   *      - (other value): display the distribution items as a list.
   *
   *  - editable: when used in the metadata editor, set to true.
   *
   *  - relatedConfig: array with the configuration of the distributions to display. For each distribution:
   *      - filter: Filter a type based on an attribute.
   *                Can't be used when multiple types are requested
   *                eg. data-filter="associationType:upstreamData"
   *                    data-filter="protocol:OGC:.*|ESRI:.*"
   *                    data-filter="-protocol:OGC:.*"
   *      - title: title translation key for the relations section.
   *      - editActions: List of edit actions to add online resources to the distribution.
   *                     eg. editActions: ['addOnlinesrc'] -> adds a button to open the default dialog to add
   *                            an online resource.
   *                         editActions: ['addOnlinesrc', 'onlineDiscoverWMS', 'onlineDiscoverArcGIS'] ->
   *                            adds a button to open the default dialog to add an online resourceand other 2 buttons
   *                            with predefined values to add WMS and ArcGIS resources.
   *
   * Example configuration (view mode):
   *
   * <div data-gn-distribution-resources-container="md"
   *      data-mode="tabset"
   *      data-related-config="[{'filter': 'protocol:OGC:.*|ESRI:.*|atom.*', 'title': 'API'},
   *                      {'filter': 'protocol:.*DOWNLOAD.*|DB:.*|FILE:.*', 'title': 'download'},
   *                      {'filter': '-protocol:OGC:.*|ESRI:.*|atom.*|.*DOWNLOAD.*|DB:.*|FILE:.*', 'title': 'links'}]">
   *
   * </div>
   *
   * Example configuration (edit mode):
   *
   * <div data-gn-distribution-resources-container="md"
   *      data-related-config="[{'filter': 'protocol:OGC:.*|ESRI:.*|atom.*', 'title': 'API', editActions: ['addOnlinesrc']},
   *                      {'filter': 'protocol:.*DOWNLOAD.*|DB:.*|FILE:.*', 'title': 'download', editActions: ['addOnlinesrc']},
   *                      {'filter': '-protocol:OGC:.*|ESRI:.*|atom.*|.*DOWNLOAD.*|DB:.*|FILE:.*', 'title': 'links', editActions: ['addOnlinesrc']}]">
   *
   * </div>
   *
   */
  module.directive("gnDistributionResourcesContainer", [
    "gnRelatedResources",
    "gnConfigService",
    "$injector",
    function (gnRelatedResources, gnConfigService, $injector) {
      return {
        restrict: "A",
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/distributionResourcesContainer.html"
          );
        },
        scope: {
          md: "=gnDistributionResourcesContainer",
          mode: "=",
          relatedConfig: "=",
          editorConfig: "="
        },
        link: function (scope, element, attrs, controller) {
          scope.lang = scope.lang || scope.$parent.lang;
          scope.relations = {};
          scope.relatedConfigUI = [];
          scope.editable = angular.isDefined(scope.editorConfig);
          scope.config = gnRelatedResources;
          if ($injector.has("gnOnlinesrc")) {
            scope.onlinesrcService = $injector.get("gnOnlinesrc");
          }

          scope.relatedConfig.forEach(function (config) {
            config.relations = scope.md.link || {};
            config.relationFound = config.relations.length > 0;

            var value = config.relations;

            // Check if tabs needs to be displayed
            if (scope.mode === "tabset" && config.filter && angular.isArray(value)) {
              var filters = gnConfigService.parseFilters(config.filter);

              config.relations = [];
              for (var j = 0; j < value.length; j++) {
                gnConfigService.testFilters(filters, value[j]) &&
                  config.relations.push(value[j]);
              }
              config.relationFound = config.relations.length > 0;
            } else {
              config.relations = value;
            }

            scope.relatedConfigUI.push(config);
          });
        }
      };
    }
  ]);

  module.directive("gnRelatedDistribution", [
    "gnGlobalSettings",
    "gnSearchSettings",
    "gnRelatedResources",
    "gnExternalViewer",
    "gnConfigService",
    "gnUrlUtils",
    "gnDoiService",
    "$injector",
    function (
      gnGlobalSettings,
      gnSearchSettings,
      gnRelatedResources,
      gnExternalViewer,
      gnConfigService,
      gnUrlUtils,
      gnDoiService,
      $injector
    ) {
      return {
        restrict: "A",
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/relatedDistribution.html"
          );
        },
        scope: {
          md: "=gnRelatedDistribution",
          template: "@",
          title: "@",
          altTitle: "@",
          list: "@",
          // Filter a type based on an attribute.
          // Can't be used when multiple types are requested
          // eg. data-filter="associationType:upstreamData"
          // data-filter="protocol:OGC:.*|ESRI:.*"
          // data-filter="-protocol:OGC:.*"
          filter: "@",
          user: "=",
          hasResults: "=?",
          layout: "@",
          editorConfig: "="
        },
        link: function (scope, element, attrs) {
          scope.canPublishDoiForResource = gnDoiService.canPublishDoiForResource;
          scope.editable = angular.isDefined(scope.editorConfig);
          scope.lang = scope.lang || scope.$parent.lang;

          if ($injector.has("gnOnlinesrc")) {
            scope.onlinesrcService = $injector.get("gnOnlinesrc");
            $injector.get("gnCurrentEdit").associatedPanelConfigId = scope.editorConfig;
          }

          /**
           * The type is use to find the config to use for this
           * type of link. See link-utility.xsl.
           */
          function getType(fn) {
            if (fn === "legend") {
              return fn;
            } else if (fn === "featureCatalogue") {
              return "fcats";
            } else if (fn === "dataQualityReport") {
              return "dq-report";
            }
            return "onlinesrc";
          }

          function convertLangProperties(object) {
            if (!angular.isObject(object)) {
              return;
            }
            var newObject = {};
            Object.keys(object).forEach(function (key) {
              newObject[key.replace(/^lang/, "")] = object[key];
            });
            return newObject;
          }

          scope.convertLinkToEdit = function (link) {
            var convertedLink = {
              id: link.url,
              idx: link.idx,
              hash: link.hash,
              url: convertLangProperties(link.urlObject),
              type: getType(link.function),
              title: convertLangProperties(link.nameObject),
              protocol: link.protocol,
              description: convertLangProperties(link.descriptionObject),
              function: link["function"],
              mimeType: link.mimeType,
              applicationProfile: link.applicationProfile,
              lUrl: link.url,
              locTitle: link.nameObject ? link.nameObject["default"] : "",
              locDescription: link.descriptionObject
                ? link.descriptionObject["default"]
                : "",
              locUrl: link.urlObject["default"]
            };
            return convertedLink;
          };

          scope.loadDistributions = function (distribution) {
            var distributionCount = 0;
            scope.distributionFound = false;
            scope.distributions = [];

            angular.forEach(distribution, function (value) {
              if (!value) {
                return;
              }

              // init object if required
              scope.distributions = scope.distributions || [];
              scope.hasResults = true;

              if (!scope.distributions) {
                scope.distributions = [];
              }
              if (scope.filter) {
                var filters = gnConfigService.parseFilters(scope.filter);

                if (gnConfigService.testFilters(filters, value)) {
                  scope.distributions.push(value);
                }
              } else {
                scope.distributions = value;
              }

              // For draft version append "approved=false" to url
              if (scope.md.draft === "y") {
                for (var i = 0; i < scope.distributions.length; i++) {
                  if (
                    scope.distributions[i].url.match(
                      ".*/api/records/" + scope.md.uuid + "/attachments/.*"
                    ) != null
                  ) {
                    scope.distributions[i].url = gnUrlUtils.remove(
                      scope.distributions[i].url,
                      ["approved"],
                      true
                    );
                    scope.distributions[i].url = gnUrlUtils.append(
                      scope.distributions[i].url,
                      "approved=false"
                    );
                  }
                }
              }

              distributionCount += scope.distributions.length;
            });
            scope.distributionFound = distributionCount > 0;
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

          scope.$watchCollection("md.link", function (n, o) {
            if (scope.md != null && n !== o) {
              scope.loadDistributions(scope.md.link);
            }
          });

          if (scope.md != null) {
            scope.loadDistributions(scope.md.link);
          }
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
    "gnESFacet",
    function ($http, Metadata, gnESFacet) {
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/metadataactions/partials/relatedReplacedBy.html",
        scope: {
          uuid: "=gnRecordIsReplacedBy"
        },
        link: function (scope) {
          $http
            .post(
              "../api/search/records/_search",
              gnESFacet.buildDefaultQuery({
                query_string: {
                  query: '+agg_associated_revisionOf:"' + scope.uuid + '"'
                }
              })
            )
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
    "gnUrlUtils",
    function (
      gnRelatedService,
      gnGlobalSettings,
      gnSearchSettings,
      gnRelatedResources,
      gnExternalViewer,
      gnConfigService,
      gnUrlUtils
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

              // For draft version append "approved=false" to url
              if (scope.md.draft === "y" && idx === "onlines") {
                for (var i = 0; i < scope.relations[idx].length; i++) {
                  if (
                    scope.relations[idx][i].url.match(
                      ".*/api/records/" + scope.md.uuid + "/attachments/.*"
                    ) != null
                  ) {
                    scope.relations[idx][i].url = gnUrlUtils.remove(
                      scope.relations[idx][i].url,
                      ["approved"],
                      true
                    );
                    scope.relations[idx][i].url = gnUrlUtils.append(
                      scope.relations[idx][i].url,
                      "approved=false"
                    );
                  }
                }
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
                      return r.properties
                        ? r.properties.associationType + "-" + r.properties.initiativeType
                        : "";
                    })
                    .filter(function (value, index, self) {
                      return self.indexOf(value) === index;
                    })
                    .forEach(function (type) {
                      scope.relations["siblings" + type] = siblings.filter(function (r) {
                        var key = r.properties
                          ? r.properties.associationType +
                            "-" +
                            r.properties.initiativeType
                          : "";
                        return key === type;
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
              (promise = gnRelatedService.get(
                scope.id,
                scope.types,
                scope.md.draft !== "y"
              )).then(
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
    "gnGlobalSettings",
    "gnFacetMetaLabel",
    function ($rootScope, gnGlobalSettings, gnFacetMetaLabel) {
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
          scope.relatedFacetConfig =
            gnGlobalSettings.gnCfg.mods.recordview.relatedFacetConfig;
          scope.getFacetLabel = gnFacetMetaLabel.getFacetLabel;

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
          scope.filtersToProcess = scope.filters || Object.keys(scope.relatedFacetConfig);
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

          scope.getOverviewUrl = function (md) {
            if (md.overview && md.overview.length > 0) {
              return md.overview[0].url;
              // Related records contain the first overview in the properties.overview property
            } else if (md.properties && md.properties.overview) {
              return md.properties.overview;
            }

            return "";
          };
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

  /**
   * Directive to display a panel in the metadata editor with a header and a list of associated resources.
   *
   */
  module.directive("gnAssociatedResourcesPanel", [
    "gnCurrentEdit",
    function (gnCurrentEdit) {
      return {
        restrict: "A",
        transclude: true,
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/associatedResourcesPanel.html"
          );
        },
        scope: {
          md: "=gnAssociatedResourcesPanel",
          mode: "="
        },
        link: function (scope, element, attrs) {
          gnCurrentEdit.associatedPanelConfigId = attrs["editorConfig"] || "default";
        }
      };
    }
  ]);

  /**
   * Displays a panel with different types of associations defined in the schema configuration and available in the metadata object 'md'.
   *
   */
  module.directive("gnAssociatedResourcesContainer", [
    "gnRelatedResources",
    "gnConfigService",
    "gnOnlinesrc",
    "gnCurrentEdit",
    "gnSchemaManagerService",
    "$injector",
    "$filter",
    function (
      gnRelatedResources,
      gnConfigService,
      gnOnlinesrc,
      gnCurrentEdit,
      gnSchemaManagerService,
      $injector,
      $filter
    ) {
      return {
        restrict: "A",
        templateUrl: function (elem, attrs) {
          return (
            attrs.template ||
            "../../catalog/components/metadataactions/partials/associatedResourcesContainer.html"
          );
        },
        scope: {
          md: "=gnAssociatedResourcesContainer",
          mode: "="
        },
        link: function (scope, element, attrs, controller) {
          scope.lang = scope.lang || scope.$parent.lang;
          scope.gnCurrentEdit = gnCurrentEdit;
          scope.relations = [];
          scope.relatedConfigUI = [];
          scope.relatedResourcesConfig = gnRelatedResources;
          if ($injector.has("gnOnlinesrc")) {
            scope.onlinesrcService = $injector.get("gnOnlinesrc");
          }

          // Values for relation types used in the UI (and also to filter some metadata by resource type: dataset / service
          var UIRelationTypeValues = {
            dataset: "dataset",
            service: "service",
            source: "source",
            parent: "parent",
            fcats: "fcats",
            siblings: "siblings"
          };

          // Values used for the relation types by the /associated API, that doesn't match with the UI
          var APIRelationTypeValues = {
            dataset: "datasets",
            service: "services",
            source: "sources",
            parent: "parent",
            fcats: "fcats",
            siblings: "siblings"
          };

          // A mapper from relation types from the UI to the API values
          scope.mapUIRelationToApi = function (type) {
            return APIRelationTypeValues[type];
          };

          scope.getClass = function (type) {
            if (type === UIRelationTypeValues.dataset) {
              return "fa gn-icon-dataset";
            } else if (type === UIRelationTypeValues.service) {
              return "fa fa-fw fa-cloud";
            } else if (type === UIRelationTypeValues.source) {
              return "fa gn-icon-source";
            } else if (type === UIRelationTypeValues.parent) {
              return "fa gn-icon-series";
            } else if (type === UIRelationTypeValues.fcats) {
              return "fa fa-table";
            } else if (type === UIRelationTypeValues.siblings) {
              return "fa fa-sign-out";
            }

            return "";
          };
          scope.canRemoveLink = function (record, type) {
            if (record.origin === "remote") {
              return true;
            } else {
              return (
                type === UIRelationTypeValues.dataset ||
                type === UIRelationTypeValues.service ||
                type === UIRelationTypeValues.parent ||
                type === UIRelationTypeValues.source ||
                type === UIRelationTypeValues.fcats ||
                type === UIRelationTypeValues.siblings
              );
            }
          };

          scope.remove = function (record, type) {
            if (type === UIRelationTypeValues.dataset) {
              scope.onlinesrcService.removeDataset(record);
            } else if (type === UIRelationTypeValues.service) {
              scope.onlinesrcService.removeService(record);
            } else if (type === UIRelationTypeValues.parent) {
              scope.onlinesrcService.removeMdLink(UIRelationTypeValues.parent, record);
            } else if (type === UIRelationTypeValues.source) {
              scope.onlinesrcService.removeMdLink(UIRelationTypeValues.source, record);
            } else if (type === UIRelationTypeValues.fcats) {
              scope.onlinesrcService.removeFeatureCatalog(record);
            } else if (type === UIRelationTypeValues.siblings) {
              scope.onlinesrcService.removeSibling(record);
            }
          };

          var loadRelations = function (relationTypes) {
            gnOnlinesrc.getAllResources(relationTypes).then(function (data) {
              var res = gnOnlinesrc.formatResources(
                data,
                scope.lang,
                gnCurrentEdit.mdLanguage
              );

              // Change the relation keys from the API response to the UI values
              scope.relations = _.mapKeys(res.relations, function (value, key) {
                if (key === APIRelationTypeValues.service) {
                  return UIRelationTypeValues.service;
                } else if (key === APIRelationTypeValues.dataset) {
                  return UIRelationTypeValues.dataset;
                } else if (key === APIRelationTypeValues.source) {
                  return UIRelationTypeValues.source;
                } else {
                  return key;
                }
              });

              scope.md.related = {};

              scope.relatedConfig.forEach(function (config) {
                config.relations = scope.relations[config.type] || [];

                var processConfig = true;

                // The configuration has an expression to evaluate if it should be processed
                if (config.condition) {
                  processConfig = scope.$eval(config.condition);
                }

                if (processConfig) {
                  scope.md.related[config.type] = scope.relations[config.type] || [];
                  // TODO: Review filter by siblings properties, Metadata instances doesn't have this information
                  if (config.config && config.config.fields) {
                    var filterObject = { properties: {} };

                    for (var item in config.config.fields) {
                      filterObject.properties[item] = config.config.fields[item];
                    }

                    config.relations = $filter("filter")(config.relations, filterObject);
                  }

                  config.relationFound = config.relations.length > 0;
                  // By default, allow to add relations unless explicitly disallowed in the configuration.
                  config.allowToAddRelation = angular.isDefined(config.allowToAddRelation)
                    ? config.allowToAddRelation
                    : true;
                  scope.relatedConfigUI.push(config);
                }
              });
            });
          };

          gnSchemaManagerService
            .getEditorAssociationPanelConfig(
              gnCurrentEdit.schema,
              gnCurrentEdit.associatedPanelConfigId
            )
            .then(function (r) {
              scope.relatedConfig = r.config.associatedResourcesTypes;
              var relationTypes = _.map(scope.relatedConfig, "type");

              // Adapt the UI types to the backend types value: services --> service, datasets --> dataset
              // The UI depends on the values also filter by resource type in
              // the associated resources dialogs.
              relationTypes = _.map(relationTypes, function (value) {
                if (value === UIRelationTypeValues.service) {
                  return APIRelationTypeValues.service;
                } else if (value === UIRelationTypeValues.dataset) {
                  return APIRelationTypeValues.dataset;
                } else if (value === UIRelationTypeValues.source) {
                  return APIRelationTypeValues.source;
                } else {
                  return value;
                }
              });

              loadRelations(relationTypes);
            });
        }
      };
    }
  ]);
})();
