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
  goog.provide("gn_wmsimport");

  var module = angular.module("gn_wmsimport", []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnWmsImport
   *
   * @description
   * Panel to load WMS capabilities service and pick layers.
   * The server list is given in global properties.
   */
  module.directive("gnWmsImport", [
    "gnOwsCapabilities",
    "gnEsriUtils",
    "gnAlertService",
    "gnMap",
    "$translate",
    "$timeout",
    "gnESClient",
    "Metadata",
    "gnViewerSettings",
    "gnGlobalSettings",
    "gnSearchSettings",
    function (
      gnOwsCapabilities,
      gnEsriUtils,
      gnAlertService,
      gnMap,
      $translate,
      $timeout,
      gnESClient,
      Metadata,
      gnViewerSettings,
      gnGlobalSettings,
      gnSearchSettings
    ) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/viewer/wmsimport/" + "partials/wmsimport.html",
        scope: {
          map: "=gnWmsImportMap",
          url: "=?gnWmsImportUrl"
        },
        controller: [
          "$scope",
          function ($scope) {
            /**
             * Transform a capabilities layer into an ol.Layer
             * and add it to the map.
             *
             * @param {Object} getCapLayer
             * @param {string} name of the style to use
             * @return {*}
             */
            this.addLayer = function (getCapLayer, style) {
              getCapLayer.version = $scope.capability.version;
              getCapLayer.capRequest = $scope.capability.Request;

              //check if proxy is needed
              var url = $scope.url.split("/");
              getCapLayer.useProxy = false;
              url = url[0] + "/" + url[1] + "/" + url[2] + "/";
              if ($.inArray(url + "#GET", gnGlobalSettings.requireProxy) >= 0) {
                getCapLayer.useProxy = true;
              }
              if ($scope.format == "wms") {
                var layer = gnMap.addWmsToMapFromCap(
                  $scope.map,
                  getCapLayer,
                  $scope.url,
                  style
                );
                gnAlertService.addAlert(
                  {
                    msg: $translate.instant("layerAdded", {
                      layer: layer.get("label"),
                      extent: layer.get("cextent").toString()
                    }),
                    type: "success"
                  },
                  15
                );
                gnMap.feedLayerMd(layer);
                return layer;
              } else if ($scope.format == "wfs") {
                var layer = gnMap.addWfsToMapFromCap($scope.map, getCapLayer, $scope.url);
                gnMap.feedLayerMd(layer);
                return layer;
              } else if ($scope.format == "wmts") {
                return gnMap.addWmtsToMapFromCap(
                  $scope.map,
                  getCapLayer,
                  $scope.capability
                );
              } else {
                console.log($scope.format + " not supported");
              }
            };

            this.addEsriRestLayer = function (getCapLayer) {
              return gnMap.addEsriRestLayer(
                $scope.map,
                $scope.url + "/" + getCapLayer.id,
                null,
                null,
                null
              );
            };
          }
        ],
        link: function (scope, element, attrs) {
          scope.loading = false;
          scope.error = { wms: null, wmts: null, wfs: null, esrirest: null };
          scope.format = attrs["gnWmsImport"] != "" ? attrs["gnWmsImport"] : "all";
          scope.serviceDesc = null;
          scope.servicesList = gnViewerSettings.servicesUrl[scope.format];
          scope.catServicesList = [];
          var type = scope.format.toUpperCase();

          //Update require proxy
          //this is done because gnGlobalSettings is not configured at this point
          scope.$watch("gnGlobalSettings.requireProxy", function (settings) {
            scope.requireProxy = gnGlobalSettings.requireProxy;
          });

          function addLinks(md, type) {
            angular.forEach(md.getLinksByType(type), function (link) {
              if (link.url) {
                scope.catServicesList.push({
                  title: md.resourceTitle,
                  uuid: md.uuid,
                  name: link.name,
                  desc: link.desc,
                  type: type,
                  url: link.url
                });
              }
            });
          }
          // Get the list of services registered in the catalog
          if (attrs.servicesListFromCatalog) {
            // FIXME: Only load the first 100 services
            var query = {
              from: 0,
              size: 100,
              sort: [{ "resourceTitleObject.default.sort": "asc" }],
              query: {
                bool: {
                  must: [
                    {
                      query_string: {
                        query:
                          "+isTemplate:n " +
                          '+serviceType:("OGC:WMS" OR "OGC:WFS" OR "OGC:WMTS")'
                      }
                    }
                  ]
                }
              }
            };
            if (gnSearchSettings.filters) {
              query.query.bool.filter = gnSearchSettings.filters;
            }
            gnESClient.search(query).then(function (data) {
              angular.forEach(data.hits.hits, function (record) {
                var md = new Metadata(record);
                if (scope.format === "all") {
                  addLinks(md, "wms");
                  addLinks(md, "wfs");
                } else {
                  addLinks(md, scope.format);
                }
              });
            });
          }

          scope.setUrl = function (srv) {
            scope.url = angular.isObject(srv) ? srv.url : srv;
            type = (angular.isObject(srv) && srv.type) || type;
            scope.serviceDesc = angular.isObject(srv) ? srv : null;
            scope.load();
          };

          scope.load = function () {
            if (scope.url) {
              scope.loading = true;
              scope.error[type] = null;
              scope.capability = null;
              scope.capabilityEsriRest = null;
              if (type.toLowerCase() === "esrirest") {
                gnEsriUtils.getCapabilities(scope.url).then(
                  function (capability) {
                    scope.loading = false;
                    scope.capabilityEsriRest = capability;
                  },
                  function (error) {
                    scope.loading = false;
                    scope.error[type] = error;
                  }
                );
              } else {
                gnOwsCapabilities["get" + type.toUpperCase() + "Capabilities"](
                  scope.url
                ).then(
                  function (capability) {
                    scope.loading = false;
                    scope.capability = capability;
                  },
                  function (error) {
                    scope.loading = false;
                    scope.error[type] = error;
                  }
                );
              }
            }
          };

          // reset a service URL and clear the result list
          scope.reset = function () {
            scope.loading = false;
            scope.capability = null;
            scope.capabilityEsriRest = null;
            scope.serviceDesc = null;
            scope.url = "";
          };

          // watch url as input
          scope.$watch("url", function (value) {
            if (value) {
              scope.setUrl({
                url: value,
                type: scope.format
              });
            }
          });
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnKmlImport
   *
   * @description
   * Panel to load KML and KMZ files. You could load them with file input or
   * drag & drop them in the map.
   */

  module.directive("gnKmlImport", [
    "olDecorateLayer",
    "gnAlertService",
    "$translate",
    function (olDecorateLayer, gnAlertService, $translate) {
      return {
        restrict: "A",
        replace: true,
        templateUrl:
          "../../catalog/components/viewer/wmsimport/" + "partials/kmlimport.html",
        scope: {
          map: "=gnKmlImportMap"
        },
        controllerAs: "kmlCtrl",
        controller: [
          "$scope",
          "$http",
          function ($scope, $http) {
            /**
             * Create new vector Kml file from url and add it to
             * the Map.
             *
             * @param {string} url remote url of the kml file
             * @param {ol.map} map
             */
            this.addKml = function (url, map) {
              if (url == "") {
                $scope.validUrl = true;
                return;
              }

              $http.get(gnGlobalSettings.proxyUrl + encodeURIComponent(url)).then(
                function (response) {
                  var kmlSource = new ol.source.Vector();
                  kmlSource.addFeatures(
                    new ol.format.KML().readFeatures(response.data, {
                      featureProjection: $scope.map.getView().getProjection(),
                      dataProjection: "EPSG:4326"
                    })
                  );
                  var vector = new ol.layer.Vector({
                    source: kmlSource,
                    getinfo: true,
                    label: $translate.instant("kmlFile", { layer: url.split("/").pop() })
                  });
                  $scope.addToMap(vector, map);
                  $scope.url = "";
                  $scope.validUrl = true;
                },
                function () {
                  $scope.validUrl = false;
                }
              );
            };

            $scope.addToMap = function (layer, map) {
              olDecorateLayer(layer);
              layer.displayInLayerManager = true;
              map.getLayers().push(layer);
              map.getView().fit(layer.getSource().getExtent(), map.getSize());

              gnAlertService.addAlert({
                msg: $translate.instant("layerAdded", { layer: layer.get("label") }),
                type: "success"
              });
            };
          }
        ],
        link: function (scope, element, attrs) {
          /** Used for ngClass of the input */
          scope.validUrl = true;

          /** File drag & drop support */
          var dragAndDropInteraction = new ol.interaction.DragAndDrop({
            formatConstructors: [
              ol.format.GPX,
              ol.format.GeoJSON,
              ol.format.KML,
              ol.format.TopoJSON
            ]
          });

          var onError = function (msg) {
            gnAlertService.addAlert({
              msg: $translate.instant("mapImportFailure"),
              type: "danger"
            });
          };

          scope.map.getInteractions().push(dragAndDropInteraction);
          dragAndDropInteraction.on("addfeatures", function (event) {
            if (!event.features || event.features.length == 0) {
              onError();
              scope.$apply();
              return;
            }

            var vectorSource = new ol.source.Vector({
              features: event.features,
              projection: event.projection
            });

            var layer = new ol.layer.Vector({
              source: vectorSource,
              getinfo: true,
              label: $translate.instant("localLayerFile", { layer: event.file.name })
            });
            scope.addToMap(layer, scope.map);
            scope.$apply();
          });

          var requestFileSystem =
            window.webkitRequestFileSystem ||
            window.mozRequestFileSystem ||
            window.requestFileSystem;
          var unzipProgress = document.createElement("progress");
          var fileInput = element.find('input[type="file"]')[0];

          var model = (function () {
            var URL = window.webkitURL || window.mozURL || window.URL;

            return {
              getEntries: function (file, onend) {
                zip.createReader(
                  new zip.BlobReader(file),
                  function (zipReader) {
                    zipReader.getEntries(onend);
                  },
                  onerror
                );
              },
              getEntryFile: function (entry, creationMethod, onend, onprogress) {
                var writer, zipFileEntry;

                function getData() {
                  entry.getData(
                    writer,
                    function (blob) {
                      var blobURL = URL.createObjectURL(blob);
                      onend(blobURL);
                    },
                    onprogress
                  );
                }
                writer = new zip.BlobWriter();
                getData();
              }
            };
          })();

          scope.onEntryClick = function (entry, evt) {
            model.getEntryFile(
              entry,
              "Blob",
              function (blobURL) {
                entry.loading = true;
                scope.$apply();
                var source = new ol.source.Vector();
                $.ajax(blobURL).then(function (response) {
                  var format = new ol.format.KML();
                  var features = format.readFeatures(response, {
                    featureProjection: scope.map.getView().getProjection()
                  });
                  source.addFeatures(features);
                });

                var vector = new ol.layer.Vector({
                  label: $translate.instant("localLayerFile", { layer: entry.filename }),
                  getinfo: true,
                  source: source
                });
                var listenerKey = vector.getSource().on("change", function (evt) {
                  if (vector.getSource().getState() == "ready") {
                    ol.Observable.unByKey(listenerKey);
                    scope.addToMap(vector, scope.map);
                    entry.loading = false;
                  } else if (vector.getSource().getState() == "error") {
                  }
                  scope.$apply();
                });
              },
              function (current, total) {
                unzipProgress.value = current;
                unzipProgress.max = total;
                evt.target.appendChild(unzipProgress);
              }
            );
          };

          angular.element(fileInput).bind("change", function (changeEvent) {
            if (fileInput.files.length > 0) {
              model.getEntries(fileInput.files[0], function (entries) {
                scope.kmzEntries = entries;
                scope.$apply();
              });
            }
            $("#kmz-file-input")[0].value = "";
          });
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport.directive:gnCapTreeCol
   *
   * @description
   * Directive to manage a collection of nested layers from
   * the capabilities document. This directive works with
   * gnCapTreeElt directive.
   */
  module.directive("gnCapTreeCol", [
    "$translate",
    function ($translate) {
      var label = $translate.instant("filter");

      return {
        restrict: "E",
        replace: true,
        scope: {
          collection: "="
        },
        template:
          "<ul class='gn-layer-tree'><li data-ng-show='collection.length > 10' >" +
          "<div class='input-group input-group-sm'><span class='input-group-addon'><i class='fa fa-filter'></i></span>" +
          "<input class='form-control' aria-label='" +
          label +
          "' data-ng-model-options='{debounce: 200}' data-ng-model='layerSearchText'/></div>" +
          "</li>" +
          '<gn-cap-tree-elt ng-repeat="member in collection | filter:layerSearchText | orderBy: \'Title\'" member="member">' +
          "</gn-cap-tree-elt></ul>"
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport.directive:gnCapTreeElt
   *
   * @description
   * Directive to manage recursively nested layers from a capabilities
   * document. Will call its own template to display the layer but also
   * call back the gnCapTreeCol for all its children.
   */
  module.directive("gnCapTreeElt", [
    "$compile",
    "$translate",
    "gnAlertService",
    function ($compile, $translate, gnAlertService) {
      return {
        restrict: "E",
        require: "^gnWmsImport",
        replace: true,
        scope: {
          member: "="
        },
        templateUrl: "../../catalog/components/viewer/wmsimport/" + "partials/layer.html",
        link: function (scope, element, attrs, controller) {
          var el = element;

          scope.toggleNode = function (evt) {
            el.find(".fa")
              .first()
              .toggleClass("fa-folder-open-o")
              .toggleClass("fa-folder-o");
            el.children("ul").toggle();
            evt.stopPropagation();
          };

          scope.addLayer = function (c) {
            controller.addLayer(scope.member, c ? c : null);
          };

          scope.isParentNode = angular.isDefined(scope.member.Layer);

          // Add all subchildren
          if (angular.isArray(scope.member.Layer)) {
            element.append(
              "<gn-cap-tree-col " + "collection='member.Layer'></gn-cap-tree-col>"
            );
            $compile(element.find("gn-cap-tree-col"))(scope);
          }
        }
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport.directive:gnEsriRestCapTreeCol
   *
   * @description
   * Directive to manage a collection of nested layers from
   * the ESRI REST capabilities document. This directive works with
   * gnEsriCapTreeElt directive.
   */
  module.directive("gnEsriRestCapTreeCol", [
    function () {
      return {
        restrict: "E",
        replace: true,
        scope: {
          collection: "=",
          filterLabel: "@"
        },
        template:
          "<ul class='gn-layer-tree'><li data-ng-show='collection.length > 10' >" +
          "<div class='input-group input-group-sm'><span class='input-group-addon'><i class='fa fa-filter'></i></span>" +
          "<input class='form-control' aria-label='{{ ::filterLabel }}' data-ng-model-options='{debounce: 200}' data-ng-model='layerSearchText'/></div>" +
          "</li>" +
          '<gn-esri-rest-cap-tree-elt ng-repeat="member in collection | filter:layerSearchText | orderBy: \'name\'" member="member">' +
          "</gn-esri-rest-cap-tree-elt></ul>"
      };
    }
  ]);

  /**
   * @ngdoc directive
   * @name gn_wmsimport.directive:gnEsriCapTreeElt
   *
   * @description
   * Directive to manage layers from an ESRI REST capabilities
   * document. Will call its own template to display the layer.
   */
  module.directive("gnEsriRestCapTreeElt", [
    "$compile",
    "$translate",
    "gnAlertService",
    function ($compile, $translate, gnAlertService) {
      return {
        restrict: "E",
        require: "^gnWmsImport",
        replace: true,
        scope: {
          member: "="
        },
        templateUrl:
          "../../catalog/components/viewer/wmsimport/" + "partials/esrilayer.html",
        link: function (scope, element, attrs, controller) {
          var el = element;

          scope.toggleNode = function (evt) {
            el.find(".fa")
              .first()
              .toggleClass("fa-folder-open-o")
              .toggleClass("fa-folder-o");
            el.children("ul").toggle();
            evt.stopPropagation();
          };

          scope.addLayer = function () {
            controller.addEsriRestLayer(scope.member);
          };
        }
      };
    }
  ]);

  module.directive("gnLayerStyles", [
    function () {
      return {
        restrict: "A",
        templateUrl: "../../catalog/components/viewer/wmsimport/partials/styles.html",
        scope: {
          styles: "=gnLayerStyles",
          onClick: "&gnLayerStylesOnClick",
          current: "=gnLayerStylesCurrent",
          // 'select' or default is list
          layout: "@gnLayerStylesLayout"
        },
        link: function (scope) {
          scope.data = { currentStyle: scope.current };
          scope.$watch("data.currentStyle", function (n, o) {
            if (n && n !== o) {
              scope.clickFn(n);
            }
          });
          scope.clickFn = function (s) {
            scope.onClick({ style: s });
          };
        }
      };
    }
  ]);
})();
