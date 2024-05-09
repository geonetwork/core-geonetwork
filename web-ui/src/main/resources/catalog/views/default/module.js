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
  goog.provide("gn_search_default");

  goog.require("cookie_warning");
  goog.require("gn_mdactions_directive");
  goog.require("gn_related_directive");
  goog.require("gn_search");
  goog.require("gn_search_default_config");
  goog.require("gn_search_default_directive");

  var module = angular.module("gn_search_default", [
    "gn_search",
    "gn_search_default_config",
    "gn_search_default_directive",
    "gn_related_directive",
    "cookie_warning",
    "gn_mdactions_directive"
  ]);

  module.controller("gnsScrollController", [
    "$scope",
    "$location",
    "$anchorScroll",
    function ($scope, $location, $anchorScroll) {
      /***
       * Scroll to an anchor on the page and focus on the first focusable element
       *
       * @param anchor The ID of the anchor to scroll to
       */
      $scope.gotoAnchor = function (anchor) {
        // the element you wish to scroll to.
        $location.hash(anchor);
        // call $anchorScroll()
        $anchorScroll();
        // set the focus on the first focusable element, with a small delay otherwise a search can start
        setTimeout(function () {
          $("#" + anchor)
            .find(":focusable")
            .first()
            .focus();
        }, 500);
      };
    }
  ]);

  module.controller("gnsSearchPopularController", [
    "$scope",
    "gnSearchSettings",
    function ($scope, gnSearchSettings) {
      $scope.searchObj = {
        permalink: false,
        internal: true,
        filters: gnSearchSettings.filters,
        configId: "home",
        params: {
          isTemplate: "n",
          sortBy: "popularity",
          sortOrder: "desc",
          from: 1,
          to: 12
        }
      };
    }
  ]);

  module.controller("gnsSearchLatestController", [
    "$scope",
    "gnSearchSettings",
    function ($scope, gnSearchSettings) {
      $scope.searchObj = {
        permalink: false,
        internal: true,
        filters: gnSearchSettings.filters,
        configId: "home",
        params: {
          isTemplate: "n",
          sortBy: "createDate",
          sortOrder: "desc",
          from: 1,
          to: 12
        }
      };
    }
  ]);

  module.controller("gnsSearchTopEntriesController", [
    "$scope",
    "gnRelatedResources",
    function ($scope, gnRelatedResources) {
      $scope.resultTemplate = {
        tplUrl:
          "../../catalog/components/" +
          "search/resultsview/partials/viewtemplates/grid4maps.html"
      };
      $scope.searchObj = {
        permalink: false,
        internal: true,
        filters: [
          {
            query_string: {
              query: '+resourceType:"map/interactive"'
            }
          }
        ],
        configId: "recordWithLink",
        params: {
          isTemplate: "n",
          sortBy: "changeDate",
          sortOrder: "desc",
          from: 1,
          to: 30
        }
      };

      $scope.loadMap = function (map, md) {
        gnRelatedResources.getAction("MAP")(map, md);
      };
    }
  ]);

  module.config([
    "$LOCALES",
    function ($LOCALES) {
      $LOCALES.push("/../api/i18n/packages/search");
    }
  ]);

  module.controller("gnsDefault", [
    "$scope",
    "$location",
    "$filter",
    "suggestService",
    "$http",
    "$translate",
    "gnUtilityService",
    "gnSearchSettings",
    "gnViewerSettings",
    "gnMap",
    "gnMdView",
    "gnMdViewObj",
    "gnWmsQueue",
    "gnSearchLocation",
    "gnOwsContextService",
    "hotkeys",
    "gnGlobalSettings",
    "gnESClient",
    "gnESFacet",
    "gnFacetSorter",
    "gnExternalViewer",
    "gnUrlUtils",
    "gnAlertService",
    function (
      $scope,
      $location,
      $filter,
      suggestService,
      $http,
      $translate,
      gnUtilityService,
      gnSearchSettings,
      gnViewerSettings,
      gnMap,
      gnMdView,
      mdView,
      gnWmsQueue,
      gnSearchLocation,
      gnOwsContextService,
      hotkeys,
      gnGlobalSettings,
      gnESClient,
      gnESFacet,
      gnFacetSorter,
      gnExternalViewer,
      gnUrlUtils,
      gnAlertService
    ) {
      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;

      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
      $scope.modelOptionsForm = angular.copy(gnGlobalSettings.modelOptions);
      $scope.showMosaic = gnGlobalSettings.gnCfg.mods.home.showMosaic;
      $scope.isFilterTagsDisplayedInSearch =
        gnGlobalSettings.gnCfg.mods.search.isFilterTagsDisplayedInSearch;
      $scope.searchMapPlacement = gnGlobalSettings.gnCfg.mods.search.searchMapPlacement;
      $scope.showStatusFooterFor = gnGlobalSettings.gnCfg.mods.search.showStatusFooterFor;
      $scope.showBatchDropdown = gnGlobalSettings.gnCfg.mods.search.showBatchDropdown;
      $scope.exactMatchToggle = gnGlobalSettings.gnCfg.mods.search.exactMatchToggle;
      $scope.exactTitleToggle = gnGlobalSettings.gnCfg.mods.search.exactTitleToggle;
      $scope.searchOptions = gnGlobalSettings.gnCfg.mods.search.searchOptions;
      $scope.gnWmsQueue = gnWmsQueue;
      $scope.$location = $location;
      $scope.activeTab = "/home";
      $scope.formatter = gnGlobalSettings.gnCfg.mods.search.formatter;
      $scope.listOfResultTemplate = gnGlobalSettings.gnCfg.mods.search.resultViewTpls;
      $scope.resultTemplate = gnGlobalSettings.getDefaultResultTemplate();
      $scope.advandedSearchTemplate = gnSearchSettings.advancedSearchTemplate;
      $scope.facetsSummaryType = gnSearchSettings.facetsSummaryType;
      $scope.facetConfig = gnSearchSettings.facetConfig;
      $scope.facetTabField = gnSearchSettings.facetTabField;
      $scope.location = gnSearchLocation;
      $scope.fluidLayout = gnGlobalSettings.gnCfg.mods.home.fluidLayout;
      $scope.showMaps = gnGlobalSettings.gnCfg.mods.home.showMaps;
      $scope.fluidEditorLayout = gnGlobalSettings.gnCfg.mods.editor.fluidEditorLayout;
      $scope.fluidHeaderLayout = gnGlobalSettings.gnCfg.mods.header.fluidHeaderLayout;
      $scope.showGNName = gnGlobalSettings.gnCfg.mods.header.showGNName;

      $scope.facetSorter = gnFacetSorter.sortByTranslation;

      $scope.addToMapLayerNameUrlParam =
        gnGlobalSettings.gnCfg.mods.search.addWMSLayersToMap.urlLayerParam;

      $scope.sortKeywordsAlphabetically =
        gnGlobalSettings.gnCfg.mods.recordview.sortKeywordsAlphabetically;

      $scope.toggleMap = function () {
        $(searchMap.getTargetElement()).toggle();
        $("button.gn-minimap-toggle > i").toggleClass(
          "fa-angle-double-left fa-angle-double-right"
        );
      };
      hotkeys
        .bindTo($scope)
        .add({
          combo: "h",
          description: $translate.instant("hotkeyHome"),
          callback: function (event) {
            $location.path("/home");
          }
        })
        .add({
          combo: "t",
          description: $translate.instant("hotkeyFocusToSearch"),
          callback: function (event) {
            event.preventDefault();
            var anyField = $("#gn-any-field");
            if (anyField) {
              gnUtilityService.scrollTo();
              $location.path("/search");
              anyField.focus();
            }
          }
        })
        .add({
          combo: "m",
          description: $translate.instant("hotkeyMap"),
          callback: function (event) {
            $location.path("/map");
          }
        });

      // TODO: Previous record should be stored on the client side
      $scope.mdView = mdView;
      gnMdView.initMdView();

      $scope.goToSearch = function (any) {
        $location.path("/search").search({ any: any });
      };
      $scope.canEdit = function (record) {
        // TODO: take catalog config for harvested records
        // TODOES: this property does not exist yet; makes sure it is
        // replaced by a correct one eventually
        if (record && record.edit == "true") {
          return true;
        }
        return false;
      };

      $scope.buildOverviewUrl = function (md) {
        if (md.overview) {
          return md.overview[0].url;
        } else if (md.resourceType && md.resourceType[0] === "feature") {
          // Build a getmap request on the feature
          var t = decodeURIComponent(md.featureTypeId).split("#");

          var getMapRequest =
            t[0].replace(/SERVICE=WFS/i, "") +
            (t[0].indexOf("?" !== -1) ? "&" : "?") +
            "SERVICE=WMS&VERSION=1.1.0&REQUEST=GetMap&FORMAT=image/png&LAYERS=" +
            t[1] +
            "&CRS=EPSG:4326&BBOX=" +
            md.bbox_xmin +
            "," +
            md.bbox_ymin +
            "," +
            md.bbox_xmax +
            "," +
            md.bbox_ymax +
            "&WIDTH=100&HEIGHT=100";

          return getMapRequest;
        } else {
          return "../../catalog/views/default/images/no-thumbnail.png";
        }
      };

      $scope.closeRecord = function () {
        gnMdView.removeLocationUuid();
      };
      $scope.nextPage = function () {
        $scope.$broadcast("nextPage");
      };
      $scope.previousPage = function () {
        $scope.$broadcast("previousPage");
      };

      /**
       * Toggle the list types on the homepage
       * @param  {String} type Type of list selected
       */
      $scope.toggleListType = function (type) {
        $scope.type = type;
      };

      $scope.infoTabs = {
        lastRecords: {
          title: "lastRecords",
          titleInfo: "",
          active: true
        },
        preferredRecords: {
          title: "preferredRecords",
          titleInfo: "",
          active: false
        }
      };

      $scope.$on("layerAddedFromContext", function (e, l) {
        var md = l.get("md");
        if (md) {
          var linkGroup = md.getLinkGroup(l);
          gnMap.feedLayerWithRelated(l, linkGroup);
        }
      });

      function buildAddToMapConfig(link, md) {
        var type = "wms";
        if (link.protocol.indexOf("WMTS") > -1) {
          type = "wmts";
        } else if (
          link.protocol === "ESRI:REST" ||
          link.protocol.startsWith("ESRI REST")
        ) {
          type = "esrirest";
        } else if (link.protocol === "OGC:3DTILES") {
          type = "3dtiles";
        } else if (link.protocol === "OGC:COG") {
          type = "cog";
        }

        var config = {
          uuid: md ? md.uuid : null,
          type,
          url: $filter("gnLocalized")(link.url) || link.url
        };

        var title = link.title;

        var name;

        if ($scope.addToMapLayerNameUrlParam !== "") {
          var params = gnUrlUtils.parseKeyValue(config.url.split("?")[1]);
          name = params[$scope.addToMapLayerNameUrlParam];

          if (angular.isUndefined(name)) {
            name = link.name;
          }
        } else {
          name = link.name;
        }

        if (angular.isObject(link.title)) {
          title = $filter("gnLocalized")(link.title);
        }
        if (angular.isObject(name)) {
          name = $filter("gnLocalized")(name);
        }

        if (name && name !== "") {
          config.name = name;
          config.group = link.group;
          // Related service return a property title for the name
        } else if (title) {
          config.name = title;
        }

        // if an external viewer is defined, use it here
        if (gnExternalViewer.isEnabled()) {
          gnExternalViewer.viewService(
            {
              id: md ? md.id : null,
              uuid: config.uuid
            },
            {
              type: config.type,
              url: config.url,
              name: config.name,
              title: title
            }
          );
          return;
        }

        // no support for COG or 3DTiles for now
        if (config.type === "cog" || config.type === "3dtiles") {
          gnAlertService.addAlert({
            msg: $translate.instant("layerProtocolNotSupported", {
              type: link.protocol
            }),
            delay: 20000,
            type: "warning"
          });
          return;
        }

        return config;
      }

      $scope.resultviewFns = {
        addMdLayerToMap: function (link, md) {
          // This is probably only a service
          // Open the add service layer tab
          var config = buildAddToMapConfig(link, md);
          if (!config) {
            return;
          }
          $location.path("map").search({
            add: encodeURIComponent(angular.toJson([config]))
          });
        },
        addAllMdLayersToMap: function (layers, md) {
          var config = layers
            .map(function (layer) {
              return buildAddToMapConfig(layer, md);
            })
            .filter(function (config) {
              return !!config;
            });
          if (config.length === 0) {
            return;
          }
          $location.path("map").search({
            add: encodeURIComponent(angular.toJson(config))
          });
        },
        loadMap: function (map, md) {
          gnOwsContextService.loadContextFromUrl(map.url, viewerMap);
        }
      };

      // Share map loading functions
      gnViewerSettings.resultviewFns = $scope.resultviewFns;

      // Manage route at start and on $location change
      // depending on configuration
      if (!$location.path()) {
        var m = gnGlobalSettings.gnCfg.mods;
        $location.path(
          m.home.enabled
            ? "/home"
            : m.search.enabled
            ? "/search"
            : m.map.enabled
            ? "/map"
            : "home"
        );
      }
      var setActiveTab = function () {
        $scope.activeTab = $location.path().match(/^(\/[a-zA-Z0-9]*)($|\/.*)/)[1];
      };

      setActiveTab();
      $scope.$on("$locationChangeSuccess", setActiveTab);

      $scope.$on("$locationChangeSuccess", function (next, current) {
        if (
          gnSearchLocation.isSearch() &&
          (!angular.isArray(searchMap.getSize()) || searchMap.getSize()[0] < 0)
        ) {
          setTimeout(function () {
            searchMap.updateSize();
          }, 0);
        }
      });

      var sortConfig = gnSearchSettings.sortBy.split("#");
      angular.extend($scope.searchObj, {
        advancedMode: false,
        from: 1,
        to: 20,
        selectionBucket: "s101",
        viewerMap: viewerMap,
        searchMap: searchMap,
        mapfieldOption: {
          relations: ["within"],
          autoTriggerSearch: true
        },
        hitsperpageValues: gnSearchSettings.hitsperpageValues,
        filters: gnSearchSettings.filters,
        defaultParams: {
          isTemplate: "n",
          resourceTemporalDateRange: {
            range: {
              resourceTemporalDateRange: {
                gte: null,
                lte: null,
                relation: "intersects"
              }
            }
          },
          sortBy: sortConfig[0] || "relevance",
          sortOrder: sortConfig[1] || ""
        },
        params: {
          isTemplate: "n",
          resourceTemporalDateRange: {
            range: {
              resourceTemporalDateRange: {
                gte: null,
                lte: null,
                relation: "intersects"
              }
            }
          },
          sortBy: sortConfig[0] || "relevance",
          sortOrder: sortConfig[1] || ""
        },
        sortbyValues: gnSearchSettings.sortbyValues
      });
    }
  ]);
})();
