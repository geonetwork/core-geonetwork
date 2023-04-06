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
  goog.provide("gn_layermanager");

  var module = angular.module("gn_layermanager", []);

  /**
   * @ngdoc filter
   * @name gn_viewer.filter:gnReverse
   *
   * @description
   * Filter for the gnLayermanager directive's ngRepeat. The filter
   * reverses the array of layers so layers in the layer manager UI
   * have the same order as in the map.
   */
  module.filter("gnReverse", function () {
    return function (items) {
      return items.slice().reverse();
    };
  });

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLayermanager
   *
   * @description
   * The `gnLayermanager` directive display a list of all active layers
   * on the map and provides some tools/actions for each.
   * It also displays info if some layers failed to load.
   */
  module.directive("gnLayermanager", [
    "gnLayerFilters",
    "gnWmsQueue",
    function (gnLayerFilters, gnWmsQueue) {
      return {
        restrict: "A",
        templateUrl:
          "../../catalog/components/viewer/layermanager/" + "partials/layermanager.html",
        scope: {
          map: "=gnLayermanagerMap"
        },
        controllerAs: "gnLayermanagerCtrl",
        controller: [
          "$scope",
          "$compile",
          function ($scope, $compile) {
            /**
             * Change layer index in the map.
             *
             * @param {ol.layer} layer
             * @param {float} delta
             */
            this.moveLayer = function (layer, delta, last) {
              // do not move the last layer down (last parameter only there when moving down)
              if (last) {
                return false;
              }

              var index = $scope.layers.indexOf(layer);
              var layersCollection = $scope.map.getLayers();
              layersCollection.removeAt(index);
              layersCollection.insertAt(index + delta, layer);
            };

            /**
             * Set a property to the layer 'showInfo' to true and
             * false to all other layers. Used to display layer information
             * in the layer manager.
             *
             * @param {ol.layer} layer
             */
            this.showInfo = function (layer) {
              angular.forEach($scope.layers, function (l) {
                if (l != layer) {
                  l.showInfo = false;
                }
              });
              layer.showInfo = !layer.showInfo;
            };

            this.setWPS = function (wpsLink, layer, parent) {
              var scope = $scope.$new();
              wpsLink.layer = layer;
              scope.wpsLink = wpsLink;
              var el = angular.element(
                '<gn-wps-process-form map="map" ' +
                  'data-wps-link="wpsLink"></gn-wps-process-form>'
              );
              $compile(el)(scope);
              parent.append(el);
            };
          }
        ],
        link: function (scope, element, attrs) {
          scope.layers = scope.map.getLayers().getArray();
          scope.layerFilterFn = gnLayerFilters.selected;

          scope.failedLayers = gnWmsQueue.errors[scope.map.get("type")];
          scope.removeFailed = function (layer) {
            gnWmsQueue.removeFromError(layer, scope.map);
          };
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnLayermanagerItem
   *
   * @description
   * The `gnLayermanagerItem` directive display one layer in the layer manager
   * list, and provides all tools to interacts with the layer.
   * <ul>
   *   <li>Show metadata</li>
   *   <li>Zoom to extent</li>
   *   <li>Change order</li>
   * </ul>
   */
  module.directive("gnLayermanagerItem", [
    "gnMdView",
    "$http",
    "gnGlobalSettings",
    "gnMapServicesCache",
    function (gnMdView, $http, gnGlobalSettings, gnMapServicesCache) {
      return {
        require: "^gnLayermanager",
        restrict: "A",
        templateUrl:
          "../../catalog/components/viewer/layermanager/" +
          "partials/layermanageritem.html",
        scope: true,
        link: function (scope, element, attrs, ctrl) {
          scope.layer = scope.$eval(attrs["gnLayermanagerItem"]);
          var layer = scope.layer;

          scope.showInfo = ctrl.showInfo;
          scope.moveLayer = ctrl.moveLayer;

          scope.showMetadata = function () {
            gnMdView.openMdFromLayer(scope.layer);
          };
          function resetPopup() {
            // Hack to remove popup on layer remove eg.
            $("div.popover").each(function (i, mnu) {
              $(mnu).remove();
            });
          }
          scope.removeLayer = function (layer, map) {
            resetPopup();
            map.removeLayer(layer);
          };
          scope.setLayerStyle = function (layer, style) {
            layer.getSource().updateParams({ STYLES: style.Name });

            // If this is an authorized mapservice then we need to adjust the url or add auth headers
            // Only check http url and exclude any urls like data: which should not be changed. Also, there is not need to check proxy urls.
            if (
              style.LegendURL[0] !== null &&
              style.LegendURL[0].OnlineResource !== null &&
              style.LegendURL[0].OnlineResource.startsWith("http") &&
              !style.LegendURL[0].OnlineResource.startsWith(gnGlobalSettings.proxyUrl)
            ) {
              var mapservice = gnMapServicesCache.getMapservice(
                style.LegendURL[0].OnlineResource
              );

              if (mapservice !== null) {
                // If this is an authorized mapservice then use authorization
                var urlGetLegend = style.LegendURL[0].OnlineResource;

                if (mapservice.useProxy) {
                  // If we are using a proxy then adjust the url.
                  urlGetLegend =
                    gnGlobalSettings.proxyUrl + encodeURIComponent(urlGetLegend);
                } else {
                  // If not using the proxy then we need to use a custom loadFunction to set the authentication header
                  var headerDict;
                  if (gnMapServicesCache.getAuthorizationHeaderValue(mapservice)) {
                    headerDict = {
                      Authorization:
                        gnMapServicesCache.getAuthorizationHeaderValue(mapservice)
                    };

                    var requestOptions = {
                      headers: new Headers(headerDict),
                      responseType: "blob"
                    };
                    legendPromise = $http.get(urlGetLegend, requestOptions).then(
                      function (response) {
                        var contentType = headers("content-type");
                        if (contentType.indexOf("image") === 0) {
                          // encode data to base 64 url
                          fileReader = new FileReader();
                          fileReader.onload = function () {
                            style.LegendURL[0].OnlineResource = fileReader.result;
                            layer.set("legend", fileReader.result);
                            layer.set("currentStyle", style);
                          };
                          fileReader.readAsDataURL(response.data);
                        } else {
                          console.log("Error getting legend image (" + contentType + ")");
                          console.log(this.responseText);
                        }
                      },
                      function (response) {
                        console.log("Error getting legend image");
                        console.log(this.responseText);
                      }
                    );
                    return legendPromise;
                  }
                }
              }
            }
            layer.set("legend", style.LegendURL[0].OnlineResource);
            layer.set("currentStyle", style);
          };
          scope.zoomToExtent = function (layer, map) {
            if (layer.get("cextent")) {
              map.getView().fit(layer.get("cextent"), map.getSize());
            } else if (layer.get("extent")) {
              map.getView().fit(layer.get("extent"), map.getSize());
            }
          };

          if (layer.get("md")) {
            var d = layer.get("downloads");
            var downloadable = layer.get("md").download == "true";
            if (angular.isArray(d) && downloadable) {
              scope.download = d[0];
            }

            var wfs = layer.get("wfs");
            if (angular.isArray(wfs) && downloadable) {
              scope.wfs = wfs[0];
            }

            var p = layer.get("processes");
            if (angular.isArray(p)) {
              scope.process = p;
            }
          }

          scope.hasDownload = true;
          if (layer.getSource() instanceof ol.source.ImageArcGISRest) {
            scope.hasDownload = false;
          }

          scope.showWPS = function (process) {
            ctrl.setWPS(process, layer, element);
          };
        }
      };
    }
  ]);

  module.directive("gnLayerDimensions", [
    "$interval",
    "$translate",
    "$filter",
    function ($interval, $translate, $filter) {
      return {
        templateUrl:
          "../../catalog/components/viewer/layermanager/partials/layerdimensions.html",
        scope: true,
        link: function (scope, element, attrs, ctrl) {
          scope.animationInterval = 1.5;
          scope.layer = scope.$eval(attrs["gnLayerDimensions"]);
          scope.dimensions = {};
          scope.parameters = { time: "fa-clock-o", elevation: "fa-signal fa-rotate-90" };

          function initDimension(dimension) {
            var dimensionConfig = scope.layer.get(dimension);
            if (dimensionConfig) {
              var idx = dimensionConfig.values.length - 1;
              for (i = 0; i < dimensionConfig.values.length; i++) {
                if (dimensionConfig.values[i] === dimensionConfig.default) {
                  idx = i;
                  break;
                }
              }
              scope.dimensions[dimension] = {
                current: dimensionConfig.values[idx],
                idx: idx,
                playing: false,
                pause: false,
                stop: undefined,
                filter: "",
                filteredValues: dimensionConfig.values,
                animationInterval: scope.animationInterval,
                info: $translate.instant("wms" + dimension + "-info", {
                  layerName: scope.layer.get("label"),
                  instantNumber: dimensionConfig.values.length,
                  start: dimensionConfig.values[0],
                  end: dimensionConfig.values[dimensionConfig.values.length - 1]
                })
              };
              angular.extend(scope.dimensions[dimension], dimensionConfig);

              scope.$watch("dimensions." + dimension + ".current", function (instant) {
                if (!instant) {
                  return;
                }
                scope.params[dimension.toUpperCase()] = instant;
                scope.layer.getSource().updateParams(scope.params);
              });
              scope.$watch("dimensions." + dimension + ".filter", function (filter) {
                var config = scope.dimensions[dimension];
                if (filter.length > 0) {
                  config.filteredValues = $filter("filter")(config.values, config.filter);
                } else {
                  config.filteredValues = config.values;
                }
              });
            }
          }
          function init() {
            scope.params = scope.layer.getSource().getParams() || {};
            angular.forEach(scope.parameters, function (icon, p) {
              initDimension(p);
            });
          }

          init();

          scope.setIndex = function (dimension, idx) {
            var config = scope.dimensions[dimension],
              values = config.filteredValues;
            if (idx >= 0 && idx < values.length) {
              config.idx = idx;
              config.current = values[idx];
            }
          };

          scope.previous = function (dimension) {
            scope.setIndex(dimension, --scope.dimensions[dimension].idx);
          };

          scope.next = function (dimension) {
            scope.setIndex(dimension, ++scope.dimensions[dimension].idx);
          };

          scope.setTime = function (dimension, instant, index) {
            if (instant) {
              var config = scope.dimensions[dimension];
              config.current = instant;
              if (index) {
                config.idx = index;
              } else {
                config.idx = config.filteredValues.indexOf(instant);
              }
            }
          };

          scope.play = function (dimension, restart) {
            var config = scope.dimensions[dimension];
            if (angular.isDefined(config.stop)) return;

            config.playing = true;
            if (restart !== true) {
              config.idx = -1;
            }

            config.stop = $interval(function () {
              config.playing = config.idx < config.filteredValues.length - 1;
              if (config.playing === false) {
                scope.stop(dimension);
              } else {
                scope.next(dimension);
              }
            }, config.animationInterval * 1000);
          };

          scope.pause = function (dimension) {
            scope.dimensions[dimension].pause = true;
            scope.stop(dimension);
          };

          scope.resume = function (dimension) {
            scope.dimensions[dimension].pause = false;
            scope.play(dimension, true);
          };

          scope.stop = function (dimension) {
            var config = scope.dimensions[dimension];
            if (angular.isDefined(config.stop)) {
              $interval.cancel(config.stop);
              config.stop = undefined;
              config.pause = false;
              config.playing = false;
            }
          };
        }
      };
    }
  ]);
})();
