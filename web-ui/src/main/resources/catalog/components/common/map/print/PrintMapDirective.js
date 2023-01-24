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
  goog.provide("gn_printmap_directive");

  var module = angular.module("gn_printmap_directive", []);

  var mapPrintController = function ($scope, gnPrint, $http, $translate, $window) {
    var printRectangle;
    var deregister;

    var options = {
      printConfigUrl: "../../pdf/info.json?url=..%2F..%2Fpdf",
      graticule: false
    };

    $scope.enableLegends = true;

    /**
     * Unmanaged layer with custom render method
     * used to draw the grey rectangle for print extent
     */
    var overlayCanvas = document.createElement("canvas");
    overlayCanvas.style.position = "absolute";
    overlayCanvas.style.width = "100%";
    overlayCanvas.style.height = "100%";
    var overlayLayer = new ol.layer.Layer({
      render: function () {
        // print rectangle might not be ready if config is loading
        if (!printRectangle) {
          return;
        }

        var size = $scope.map.getSize();
        var height = size[1] * ol.has.DEVICE_PIXEL_RATIO;
        var width = size[0] * ol.has.DEVICE_PIXEL_RATIO;
        overlayCanvas.width = width;
        overlayCanvas.height = height;
        var ctx = overlayCanvas.getContext("2d");

        var minx, miny, maxx, maxy;
        (minx = printRectangle[0]),
          (miny = printRectangle[1]),
          (maxx = printRectangle[2]),
          (maxy = printRectangle[3]);

        ctx.beginPath();
        // Outside polygon, must be clockwise
        ctx.moveTo(0, 0);
        ctx.lineTo(width, 0);
        ctx.lineTo(width, height);
        ctx.lineTo(0, height);
        ctx.lineTo(0, 0);
        ctx.closePath();

        // Inner polygon,must be counter-clockwise
        ctx.moveTo(minx, miny);
        ctx.lineTo(minx, maxy);
        ctx.lineTo(maxx, maxy);
        ctx.lineTo(maxx, miny);
        ctx.lineTo(minx, miny);
        ctx.closePath();

        ctx.fillStyle = "rgba(0, 5, 25, 0.75)";
        ctx.fill();

        return overlayCanvas;
      }
    });

    /**
     * Return print configuration from Mapfishprint service
     * @return {*} promise
     */
    var updatePrintConfig = function () {
      var http = $http.get(options.printConfigUrl);
      http.then(function (response) {
        var data = response.data;

        // default values:
        var layout = data.layouts[0];
        if ($scope.defaultLayout) {
          angular.forEach(data.layouts, function (l) {
            if (l.name === $scope.defaultLayout) {
              layout = l;
            }
          });
        }
        $scope.config = {
          createURL: data.createURL,
          layout: layout,
          layouts: data.layouts,
          dpi: data.dpis[1],
          scales: data.scales,
          scale: data.scales[5],
          formats: data.outputFormats,
          format: data.outputFormats[0]
        };
      });
      return http;
    };

    this.activate = function () {
      $scope.unsupportedLayers = gnPrint.getUnsupportedLayerTypes($scope.map);

      var initMapEvents = function () {
        var currZoom = $scope.map.getView().getZoom();
        deregister = [
          $scope.map.on("moveend", function (event) {
            var newZoom = $scope.map.getView().getZoom();
            if (currZoom != newZoom) {
              currZoom = newZoom;
              if ($scope.auto) {
                fitRectangleToView();
                $scope.$apply();
              } else {
                updatePrintRectanglePixels($scope.config.scale);
              }
            }
          }),
          $scope.$watch("auto", function (v) {
            if (v) {
              fitRectangleToView();
            }
          })
        ];
        fitRectangleToView();
      };
      if (angular.isUndefined($scope.config)) {
        updatePrintConfig().then(initMapEvents);
      } else {
        initMapEvents();
      }

      overlayLayer.setMap($scope.map);
    };

    this.deactivate = function () {
      if (deregister) {
        for (var i = 0; i < deregister.length; i++) {
          if (angular.isFunction(deregister[i])) {
            deregister[i]();
          } else {
            // FIXME
            var src = deregister[i].src || deregister[i].target;
            ol.Observable.unByKey(deregister[i]);
          }
        }
      }
      overlayLayer.setMap(null);
      refreshComp();
    };

    var updatePrintRectanglePixels = function (scale) {
      printRectangle = gnPrint.calculatePageBoundsPixels(
        $scope.map,
        $scope.config.layout,
        scale
      );
      $scope.map.render();
    };

    var refreshComp = function () {
      updatePrintRectanglePixels($scope.config.scale);
      $scope.map.render();
    };
    $scope.refreshComp = refreshComp;

    this.fitRectangleToView = function () {
      $scope.config.scale = gnPrint.getOptimalScale(
        $scope.map,
        $scope.config.scales,
        $scope.config.layout
      );

      refreshComp();
    };
    var fitRectangleToView = this.fitRectangleToView;

    $scope.downloadUrl = function (url) {
      $window.location = url;
    };

    $scope.printing = false;

    $scope.unsupportedLayers = gnPrint.getUnsupportedLayerTypes($scope.map);

    $scope.submit = function () {
      if (!$scope.printActive) {
        return;
      }
      $scope.printing = true;
      // http://mapfish.org/doc/print/protocol.html#print-pdf
      var view = $scope.map.getView();
      var proj = view.getProjection();
      var lang = $translate.use();
      var defaultPage = {
        comment: $scope.mapComment || "",
        title: $scope.mapTitle || ""
      };
      defaultPage["lang" + lang] = true;
      var encLayers = [];
      var encLegends = [];
      var attributions = [];
      var layers = $scope.map.getLayers();
      pdfLegendsToDownload = [];

      var sortedZindexLayers = layers.getArray().sort(function (a, b) {
        return a.getZIndex() > b.getZIndex();
      });
      angular.forEach(sortedZindexLayers, function (layer) {
        if (layer.getVisible()) {
          var attribution = layer.attribution;
          if (attribution !== undefined && attributions.indexOf(attribution) == -1) {
            attributions.push(attribution);
          }
          if (layer instanceof ol.layer.Group) {
            var encs = gnPrint.encoders.layers["Group"].call(this, layer, proj);
            encLayers = encLayers.concat(encs);
          } else {
            var enc = encodeLayer(layer, proj);
            if (enc && enc.layer) {
              encLayers.push(enc.layer);
              if (enc.legend) {
                encLegends = encLegends || [];
                encLegends.push(enc.legend);
              }
            }
          }
        }
      });

      var scales = $scope.config.scales.map(function (scale) {
        return parseInt(scale.value);
      });

      var spec = {
        layout: $scope.config.layout.name,
        srs: proj.getCode(),
        units: proj.getUnits() || "m",
        rotation: -((view.getRotation() * 180.0) / Math.PI),
        lang: lang,
        dpi: $scope.config.dpi.value,
        outputFormat: $scope.config.format.name,
        layers: encLayers,
        legends: encLegends,
        enableLegends: $scope.enableLegends && encLegends ? true : false,
        hasTitle: $scope.mapTitle ? true : false,
        hasNoTitle: $scope.mapTitle ? false : true,
        hasAttribution: !!attributions.length,
        pages: [
          angular.extend(
            {
              center: gnPrint.getPrintRectangleCenterCoord($scope.map, printRectangle),
              // scale has to be one of the advertise by the print server
              scale: $scope.config.scale.value,
              dataOwner: "© " + attributions.join(),
              rotation: -((view.getRotation() * 180.0) / Math.PI)
            },
            defaultPage
          )
        ]
      };
      var http = $http.post(
        $scope.config.createURL + "?url=" + encodeURIComponent("../../pdf"),
        spec
      );
      http.then(
        function (response) {
          $scope.printing = false;
          $scope.downloadUrl(response.data.getURL);
          //After standard print, download the pdf Legends
          //if there are any
          for (var i = 0; i < pdfLegendsToDownload.length; i++) {
            $window.open(pdfLegendsToDownload[i]);
          }
        },
        function (response) {
          $scope.printing = false;
        }
      );
    };

    // Encode ol.Layer to a basic js object
    var encodeLayer = function (layer, proj) {
      var encLayer, encLegend;
      var ext = proj.getExtent();
      var resolution = $scope.map.getView().getResolution();

      if (!(layer instanceof ol.layer.Group)) {
        var src = layer.getSource();
        var layerConfig = {};
        var minResolution = layerConfig.minResolution || 0;
        var maxResolution = layerConfig.maxResolution || Infinity;

        if (resolution <= maxResolution && resolution >= minResolution) {
          if (src instanceof ol.source.WMTS) {
            encLayer = gnPrint.encoders.layers["WMTS"].call(
              this,
              layer,
              layerConfig,
              proj
            );
          } else if (src instanceof ol.source.OSM) {
            encLayer = gnPrint.encoders.layers["OSM"].call(this, layer, layerConfig);
          } else if (src instanceof ol.source.BingMaps) {
            encLayer = gnPrint.encoders.layers["Bing"].call(this, layer, layerConfig);
          } else if (
            src instanceof ol.source.ImageWMS ||
            src instanceof ol.source.TileWMS
          ) {
            encLayer = gnPrint.encoders.layers["WMS"].call(
              this,
              layer,
              layerConfig,
              proj
            );
          } else if (src instanceof ol.source.XYZ) {
            encLayer = gnPrint.encoders.layers["XYZ"].call(
              this,
              layer,
              layerConfig,
              proj
            );
          } else if (src instanceof ol.source.Vector) {
            var features = [];
            src.forEachFeatureInExtent(ext, function (feat) {
              features.push(feat);
            });

            if (features && features.length > 0) {
              encLayer = gnPrint.encoders.layers["Vector"].call(this, layer, features);
            }
          }
        }
      }

      encLegend = gnPrint.encoders.legends["base"].call(this, layer, layerConfig);

      if (encLegend && encLegend.classes[0] && !encLegend.classes[0].icon) {
        encLegend = undefined;
      }
      return { layer: encLayer, legend: encLegend };
    };
  };

  mapPrintController["$inject"] = ["$scope", "gnPrint", "$http", "$translate", "$window"];

  module.directive("gnMapprint", [
    "gnCurrentEdit",
    function (gnCurrentEdit) {
      return {
        restrict: "A",
        require: "gnMapprint",
        templateUrl:
          "../../catalog/components/common/map/" + "print/partials/printmap.html",
        controller: mapPrintController,
        scope: {
          printActive: "=",
          map: "="
        },
        link: function (scope, elt, attrs, ctrl) {
          scope.defaultLayout = attrs.layout;
          scope.auto = true;
          scope.activatedOnce = false;

          scope.layersWithWhiteSpaces = false;

          scope.$watchCollection(
            function () {
              return scope.map.getLayers().getArray();
            },
            function (layers) {
              scope.layersWithWhiteSpaces = false;
              layers.forEach(function (layer) {
                if (
                  layer.getSource() &&
                  layer.getSource() instanceof ol.source.TileWMS &&
                  layer.getSource().getParams() &&
                  layer.getSource().getParams().LAYERS &&
                  layer.getSource().getParams().LAYERS.indexOf(" ") >= 0
                ) {
                  scope.layersWithWhiteSpaces = true;
                }
              });
            }
          );

          // Deactivate only if it has been activated once first
          scope.$watch("printActive", function (isActive, old) {
            if (angular.isDefined(isActive) && (scope.activatedOnce || isActive)) {
              if (isActive) {
                ctrl.activate();
                scope.activatedOnce = true;
              } else {
                ctrl.deactivate();
              }
            }
          });

          scope.$watch("config.layout", function (newV, oldV) {
            if (!newV) {
              return;
            }
            ctrl.fitRectangleToView.call(ctrl);
          });
        }
      };
    }
  ]);
})();
