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
  goog.provide("gn_viewer_directive");

  var module = angular.module("gn_viewer_directive", []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnMainViewer
   * @deprecated Use gnRegionPicker instead
   *
   * @description
   */
  module.directive("gnMainViewer", [
    "gnMap",
    "gnConfig",
    "gnSearchLocation",
    "gnMetadataManager",
    "gnSearchSettings",
    "gnViewerSettings",
    "gnAlertService",
    "gnMeasure",
    "gnViewerService",
    "$location",
    "$q",
    "$translate",
    "$timeout",
    "$http",
    function (
      gnMap,
      gnConfig,
      gnSearchLocation,
      gnMetadataManager,
      gnSearchSettings,
      gnViewerSettings,
      gnAlertService,
      gnMeasure,
      gnViewerService,
      $location,
      $q,
      $translate,
      $timeout,
      $http
    ) {
      return {
        restrict: "A",
        replace: true,
        scope: true,
        templateUrl: "../../catalog/components/viewer/" + "partials/mainviewer.html",
        compile: function compile(tElement, tAttrs, transclude) {
          return {
            pre: function preLink(scope, iElement, iAttrs, controller) {
              scope.map = scope.$eval(iAttrs["map"]);
              scope.addLayerTabs = {
                search: true,
                wms: false,
                wmts: false,
                kml: false
              };

              /** these URL can be set by the viewer service **/
              scope.addLayerUrl = {
                wms: "",
                wmts: ""
              };

              /** Define object to receive measure info */
              scope.measureObj = {};

              /** measure interaction */
              scope.mInteraction = gnMeasure.create(scope.map, scope.measureObj, scope);

              /** Define vector layer used for drawing */
              scope.drawVector;

              /** active tool selector */
              scope.activeTools = {
                addLayers: false,
                projectionSwitcher: false,
                contexts: false,
                filter: false,
                layers: false,
                legend: false,
                print: false,
                processes: false
              };

              Object.keys(scope.activeTools).forEach(function (key) {
                scope.activeTools[key] = gnViewerSettings.mapConfig.defaultTool === key;
              });

              /** optional tabs **/
              scope.disabledTools = gnViewerSettings.mapConfig.disabledTools;

              /** If only one projection on the list, hide **/
              if (gnViewerSettings.mapConfig.switcherProjectionList.length < 2) {
                scope.disabledTools.projectionSwitcher = true;
              }

              /** wps process tabs */
              function initWpsConfiguration() {
                var tabs = {};
                if (!gnViewerSettings.mapConfig.wpsSources) {
                  gnViewerSettings.mapConfig.wpsSources = ["url", "recent"];
                }
                if (
                  gnViewerSettings.mapConfig.listOfServices.wps &&
                  gnViewerSettings.mapConfig.listOfServices.wps.length > 0 &&
                  gnViewerSettings.mapConfig.wpsSources &&
                  gnViewerSettings.mapConfig.wpsSources.indexOf("list") === -1
                ) {
                  gnViewerSettings.mapConfig.wpsSources.unshift("list");
                }
                gnViewerSettings.mapConfig.wpsSources.map(function (type, index) {
                  return (tabs[type] = index === 0);
                });
                return tabs;
              }
              scope.wpsTabs = initWpsConfiguration();
              scope.selectedWps = {};

              scope.zoom = function (map, delta) {
                gnMap.zoom(map, delta);
              };
              scope.zoomToMaxExtent = function (map) {
                if (gnViewerSettings.initialExtent) {
                  map.getView().fit(gnViewerSettings.initialExtent, map.getSize());
                } else {
                  map
                    .getView()
                    .fit(map.getView().getProjection().getExtent(), map.getSize());
                }
              };
              scope.ol3d = null;

              // 3D mode is allowed and disabled by default
              scope.is3DModeAllowed = gnViewerSettings.mapConfig.is3DModeAllowed || false;
              scope.is3dEnabled = gnConfig["is3dEnabled"] || false;

              // map accessibility is disabled by default
              scope.isAccessible = gnViewerSettings.mapConfig.isAccessible || false;
              // add `tabindex="0"` so the map can get keyboard focus
              //
              // more info: https://openlayers.org/en/latest/examples/accessible.html
              if (scope.isAccessible) {
                iElement.find("#map").attr("tabindex", 0);
              }

              // By default, sync only background layer
              // between main map and search map
              scope.syncAllLayers = false;

              function syncEvent() {
                if (angular.isDefined(gnSearchSettings.searchMap)) {
                  scope.doSync();
                }
              }

              scope.map.getLayers().on("add", syncEvent);
              scope.map.getLayers().on("change:length", syncEvent);

              scope.syncMod = function () {
                scope.syncAllLayers = !scope.syncAllLayers;
                scope.doSync();
              };

              scope.doSync = function () {
                if (scope.syncAllLayers) {
                  var layers = gnSearchSettings.searchMap.getLayers();
                  layers.clear();
                  scope.map.getLayers().forEach(function (l) {
                    layers.push(l);
                  });
                }
              };

              scope.init3dMode = function (map) {
                if (map) {
                  scope.ol3d = new olcs.OLCesium({ map: map });
                  scope.ol3d.enableAutoRenderLoop();
                } else {
                  console.warning(
                    "3D mode can be only by activated" + " on a map instance."
                  );
                }
              };
              scope.switch2D3D = function (map) {
                if (scope.ol3d === null) {
                  scope.init3dMode(map);
                }
                scope.ol3d.setEnabled((scope.is3dEnabled = !scope.ol3d.getEnabled()));
              };

              function addLayerFromLocation(config) {
                if (angular.isUndefined(config.name) && config.type !== "esrirest") {
                  // This is a service without a layer name
                  // Display the add layer from service panel

                  gnViewerService.openTool("addLayers", "services");
                  scope.activeTools.addLayers = true;
                  scope.addLayerTabs.services = true;
                  scope.addLayerUrl[config.type || "wms"] = config.url;
                } else if (config.name || config.type === "esrirest") {
                  gnViewerService.openTool("layers");

                  var loadLayerPromise = gnMap[
                    config.type === "wmts"
                      ? "addWmtsFromScratch"
                      : config.type === "esrirest"
                      ? "addEsriRestLayer"
                      : "addWmsFromScratch"
                  ](scope.map, config.url, config.name, undefined, config.md);

                  loadLayerPromise.then(
                    function (layer) {
                      if (layer) {
                        gnMap.feedLayerWithRelated(layer, config.group);

                        var extent = layer.get("cextent") || layer.get("extent");
                        var autofit = gnViewerSettings.mapConfig.autoFitOnLayer;
                        var message =
                          extent && !autofit ? "layerAdded" : "layerAddedNoExtent";

                        if (autofit) {
                          scope.map.getView().fit(extent);
                        }

                        gnAlertService.addAlert(
                          {
                            msg: $translate.instant(message, {
                              layer: layer.get("label"),
                              extent: extent ? extent.join(",") : ""
                            }),
                            type: "success"
                          },
                          5
                        );
                      }
                    },
                    function (error) {
                      console.log(error);
                    }
                  );
                }
              }

              // Define UI status based on the location parameters
              function initFromLocation() {
                if (
                  !angular.isArray(scope.map.getSize()) ||
                  scope.map.getSize().indexOf(0) >= 0
                ) {
                  $timeout(function () {
                    scope.map.updateSize();
                  }, 300);
                }

                // Add command allows to add element to the map
                // based on an array of objects.
                var addCmd = $location.search()["add"];
                if (addCmd) {
                  addCmd = angular.fromJson(decodeURIComponent(addCmd));

                  angular.forEach(addCmd, function (config) {
                    var layer = gnMap.getLayerInMap(scope.map, config.name, config.url);
                    if (layer !== null) {
                      var extent = layer.get("cextent") || layer.get("extent");
                      gnAlertService.addAlert({
                        msg: $translate.instant("layerIsAlreadyInMap", {
                          layer: config.name,
                          url: config.url,
                          extent: extent ? extent.join(",") : ""
                        }),
                        delay: 5,
                        type: "warning"
                      });
                      // TODO: You may want to add more than one time
                      // a layer with different styling for example ?
                      // Ask confirmation to the user ?
                      gnViewerService.openTool("layers");
                      return;
                    }

                    // Collect the md info from a search
                    if (config.uuid) {
                      gnMetadataManager.getMdObjByUuid(config.uuid).then(
                        function (md) {
                          config.md = md;
                          // TODO : If there is no config.layer
                          // try to extract them from the md and
                          // add all layers from the records.
                          addLayerFromLocation(config);
                        },
                        function (nullMd) {
                          // BTW, the metadataUrl from the capability
                          // may provide a link to the metadata record
                          scope.$emit("StatusUpdated", {
                            msg: $translate.instant(
                              "layerWillBeAddedToMapButRecordNotFound",
                              {
                                uuid: config.uuid,
                                layer: config.name,
                                url: config.url
                              }
                            ),
                            timeout: 0,
                            type: "warning"
                          });
                          addLayerFromLocation(config);
                        }
                      );
                    } else {
                      addLayerFromLocation(config);
                    }
                  });
                }

                var activateCmd = $location.search()["activate"];
                if (activateCmd) {
                  var layers = activateCmd.split(",");
                  for (var i = 0; i < layers.length; i++) {
                    var layer = gnMap.getLayerInMap(scope.map, layers[i]);
                    layer.visible = true;
                  }
                }

                // Define which tool is active
                var toolCmd = $location.search()["tool"];
                if (toolCmd) {
                  scope.activeTools[$location.search()["tool"]] = true;
                }

                if (activateCmd || addCmd || toolCmd) {
                  // Replace location with action by a stateless path
                  // to not being able to replay the action with browser
                  // history.
                  $location
                    .path("/map")
                    .search("add", null)
                    .search("activate", null)
                    .search("tool", null)
                    .replace();
                }

                if ($location.search()["extent"]) {
                  scope.map
                    .getView()
                    .fit(
                      $location.search()["extent"].split(",").map(Number),
                      scope.map.getSize()
                    );
                }
              }

              // Turn off 3D mode when not using it because
              // it slow down the application.
              // TODO: improve
              scope.$on("$locationChangeStart", function () {
                if (!gnSearchLocation.isMap() && scope.is3dEnabled) {
                  scope.switch2D3D(scope.map);
                }
              });

              scope.$on("$locationChangeSuccess", function (evt, next, current, newState, oldState) {
                // When using window.location.hash or replace to change the location
                // $locationChangeSuccess is triggered 2 times.
                // Second time on HashChangeEvent which can be ignored
                // and the state contains a navigationId property.
                if (oldState && oldState.navigationId) {
                  return;
                }
                initFromLocation();
              });

              var div = document.createElement("div");
              div.className = "overlay";
              var overlay = new ol.Overlay({
                element: div,
                positioning: "bottom-left"
              });
              scope.map.addOverlay(overlay);

              // Collect some catalogue info to enable/disable tools
              // depending on catalog content.
              scope.metrics = {};
              $http
                .post(
                  "../api/search/records/_search",
                  {
                    size: 0,
                    aggs: {
                      metrics: {
                        filters: {
                          filters: {
                            maps: {
                              query_string: { query: '+resourceType:"map-interactive"' }
                            }
                          }
                        }
                      }
                    }
                  },
                  { cache: true }
                )
                .then(function (r) {
                  scope.metrics.mapsNumber =
                    r.data.aggregations.metrics.buckets.maps.doc_count;
                });

              // watch open tool specified by the service; this will allow code
              // from anywhere to interact with the viewer tabs
              // note: this uses a deep equality to check the tool properties
              scope.$watchCollection(
                function () {
                  return gnViewerService.getOpenedTool();
                },
                function (openedTool) {
                  // open the correct tool using gi-btn magic
                  switch (openedTool.name.toLowerCase()) {
                    case "addlayers":
                      scope.activeTools.addLayers = true;
                      break;
                    case "projectionSwitcher":
                      scope.activeTools.projectionSwitcher = true;
                      break;
                    case "contexts":
                      scope.activeTools.contexts = true;
                      break;
                    case "filter":
                      scope.activeTools.filter = true;
                      break;
                    case "layers":
                      scope.activeTools.layers = true;
                      break;
                    case "print":
                      scope.activeTools.print = true;
                      break;
                    case "processes":
                      scope.activeTools.processes = true;
                      break;
                    case "measure":
                      scope.mInteraction.active = true;
                      break;
                    case "annotations":
                      scope.drawVector.active = true;
                      break;
                  }

                  // handle addlayers tab & url
                  if (scope.activeTools.addLayers) {
                    switch (openedTool.tab) {
                      case "wms":
                      case "wmts":
                        scope.addLayerUrl[openedTool.tab] = openedTool.url;
                        break;
                    }
                    scope.addLayerTabs.services = true;
                  }

                  // handle processes tool
                  if (scope.activeTools.processes && openedTool.url) {
                    scope.wpsTabs.url = true;
                    scope.selectedWps.url = openedTool.url;
                  }

                  openedTool.name = "";
                },
                true
              );

              // ogc graticule
              var ogcGraticule = gnViewerSettings.mapConfig.graticuleOgcService;
              if (ogcGraticule && ogcGraticule.layer && ogcGraticule.url) {
                scope.graticuleOgcService = ogcGraticule;
              }

              initFromLocation();
            },
            post: function postLink(scope, iElement, iAttrs, controller) {
              //TODO: find another solution to render the map
              setTimeout(function () {
                scope.map.updateSize();
              }, 300);

              if (gnViewerSettings.mapConfig.disabledTools.scaleLine === false) {
                scope.map.addControl(new ol.control.ScaleLine());
              }
            }
          };
        }
      };
    }
  ]);

  module.directive("gnMapMousePosition", function () {
    return {
      restrict: "A",
      replace: true,
      templateUrl: "../../catalog/components/viewer/partials/mouseposition.html",
      controller: [
        "gnViewerSettings",
        "gnGlobalSettings",
        "hotkeys",
        "gnAlertService",
        "$translate",
        "$scope",
        function (
          gnViewerSettings,
          gnGlobalSettings,
          hotkeys,
          gnAlertService,
          $translate,
          scope
        ) {
          scope.switchMousePosition = function () {
            scope.displayMousePosition = !scope.displayMousePosition;
          };

          scope.displayMousePosition = false;
          if (gnViewerSettings.mapConfig.disabledTools.mousePosition === false) {
            scope.coordinateFormats = ["DD", "DMS"];
            scope.coordinateFormat = scope.coordinateFormats[0];
            scope.coordinateFormatPrecision = 4;
            scope.projectionList = gnViewerSettings.mapConfig.projectionList.flatMap(
              function (p) {
                return p.code.endsWith(":4326")
                  ? [].concat(
                      angular.extend({ format: "DD" }, p),
                      angular.extend({ format: "DMS" }, p)
                    )
                  : angular.extend({ format: "DD" }, p);
              }
            );
            scope.coordinateProjectionCode = scope.projectionList[0].code;
            scope.displayMousePosition = true;
            scope.mousePosition = "";

            scope.setMousePositionFormat = function (f) {
              scope.coordinateFormat = f;
            };

            scope.setMousePositionProjection = function (c) {
              scope.coordinateProjectionCode = c.code;
              scope.coordinateFormat = c.format;
              scope.mousePositionControl.setProjection(c.code);
            };

            scope.mousePositionControl = new ol.control.MousePosition({
              coordinateFormat: function (c) {
                scope.mousePosition =
                  scope.coordinateFormat === "DD"
                    ? ol.coordinate.format(c, "{x}, {y}", scope.coordinateFormatPrecision)
                    : ol.coordinate.toStringHDMS(c, scope.coordinateFormatPrecision);
                return scope.mousePosition;
              },
              projection: scope.coordinateProjectionCode,
              target: document.getElementById("gn-map-mouse-position"),
              undefinedHTML: "&nbsp;"
            });
            scope.map.addControl(scope.mousePositionControl);

            if (gnGlobalSettings.gnCfg.mods.global.hotkeys) {
              hotkeys.bindTo(scope).add({
                combo: "c",
                description: $translate.instant("copyMousePosition"),
                callback: function (event) {
                  if (scope.mousePosition != "") {
                    navigator.clipboard.writeText(scope.mousePosition).then(function () {
                      gnAlertService.addAlert(
                        {
                          msg: $translate.instant("mousePositionCopiedToClipboard", {
                            position: scope.mousePosition
                          }),
                          type: "success"
                        },
                        2
                      );
                    });
                  }
                }
              });
            }
          }
        }
      ]
    };
  });

  // TODO : to remove those directives when ngeo allow null class
  module
    .directive("giBtnGroup", function () {
      return {
        restrict: "A",
        controller: [
          "$scope",
          function ($scope) {
            var buttonScopes = [];

            this.activate = function (btnScope) {
              angular.forEach(buttonScopes, function (b) {
                if (b != btnScope) {
                  b.ngModelSet(b, false);
                }
              });
            };

            this.addButton = function (btnScope) {
              buttonScopes.push(btnScope);
            };
          }
        ]
      };
    })
    .directive("giBtn", [
      "$parse",
      function ($parse) {
        return {
          require: ["?^giBtnGroup", "ngModel"],
          restrict: "A",
          //replace: true,
          scope: true,
          link: function (scope, element, attrs, ctrls) {
            var buttonsCtrl = ctrls[0],
              ngModelCtrl = ctrls[1];
            var ngModelGet = $parse(attrs["ngModel"]);
            var cls = attrs["giBtn"];
            scope.ngModelSet = ngModelGet.assign;

            if (buttonsCtrl) buttonsCtrl.addButton(scope);

            //ui->model
            element.bind("click", function () {
              scope.$apply(function () {
                ngModelCtrl.$setViewValue(!ngModelCtrl.$viewValue);
                ngModelCtrl.$render();
              });
            });

            //model -> UI
            ngModelCtrl.$render = function () {
              if (buttonsCtrl && ngModelCtrl.$viewValue) {
                buttonsCtrl.activate(scope);
              }
              if (cls != "") {
                element.toggleClass(cls, ngModelCtrl.$viewValue);
              }
            };
          }
        };
      }
    ]);

  /**
   * @ngdoc directive
   * @name gn_viewer_directive:gnvToolsBtn
   * @deprecated Use giBtn and giBtnGroup instead
   */
  module.directive("gnvToolsBtn", [
    function () {
      return {
        restrict: "A",
        link: function (scope, element, attrs) {
          element.bind("click", function () {
            if (element.hasClass("active")) {
              element.removeClass("active");
              $(element.attr("rel")).addClass("force-hide");
            } else {
              $(".btn").removeClass("active");
              element.addClass("active");
              $(".panel-tools").addClass("force-hide");
              $(element.attr("rel")).removeClass("force-hide");
            }
          });
        }
      };
    }
  ]);

  module.directive("gnvLayerIndicator", [
    "gnWmsQueue",
    function (gnWmsQueue) {
      return {
        restrict: "A",
        templateUrl: "../../catalog/components/viewer/" + "partials/layerindicator.html",
        link: function (scope, element, attrs) {
          scope.mapType = attrs["gnvLayerIndicator"] || "viewer";
          scope.layerQueue = gnWmsQueue.queue;
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer_directive:gnvClosePanel
   * @deprecated Use giBtn and giBtnGroup instead
   */
  module.directive("gnvClosePanel", [
    function () {
      return {
        restrict: "A",
        require: "giBtnGroup",
        scope: true,
        link: function (scope, element, attrs, btngroupCtrl) {
          $(".close").click(function () {
            var t = $(this).parents(".panel-tools");
            t.addClass("force-hide");
            $("[rel=#" + t.attr("id") + "]").removeClass("active");
            scope.$apply(function () {
              btngroupCtrl.activate();
            });
          });
        }
      };
    }
  ]);
})();
