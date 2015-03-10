(function() {
  goog.provide('gn_printmap_directive');

  var module = angular.module('gn_printmap_directive', []);

  var mapPrintController = function($scope, gnPrint, $http,
                                    $translate, $window) {

    var printRectangle;
    var deregister;

    var options = {
      printConfigUrl: '../../pdf/info.json?url=..%2F..%2Fpdf',
      legend: false,
      graticule: false
    };

    /**
     * Return print configuration from Mapfishprint service
     * @return {*} promise
     */
    var updatePrintConfig = function() {
      var http = $http.get(options.printConfigUrl);
      http.success(function(data) {

        // default values:
        var layout = data.layouts[0];
        if ($scope.defaultLayout) {
          angular.forEach(data.layouts, function(l) {
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
          scale: data.scales[5]
        };
      });
      return http;
    };

    this.activate = function() {
      var initMapEvents = function() {
        deregister = [
          $scope.map.on('precompose', handlePreCompose),
          $scope.map.on('postcompose', handlePostCompose),
          $scope.map.getView().on('change:resolution', function(event) {
            if ($scope.auto) {
              fitRectangleToView();
              $scope.$apply();
            } else {
              updatePrintRectanglePixels($scope.config.scale);
            }
          }),
          $scope.$watch('auto', function(v) {
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
    };

    this.deactivate = function() {
      if (deregister) {
        for (var i = 0; i < deregister.length; i++) {
          if (angular.isFunction(deregister[i])) {
            deregister[i]();
          } else {
            deregister[i].src.unByKey(deregister[i]);
          }
        }
      }
      refreshComp();
    };


    /**
     * Compose the events
     * @param {Object} evt map.precompose event
     */
    var handlePreCompose = function(evt) {
      var ctx = evt.context;
      ctx.save();
    };

    /**
     * Compose the grey rectangle for print extent
     * @param {Object} evt map.postcompose event
     */
    var handlePostCompose = function(evt) {
      var ctx = evt.context;
      var size = $scope.map.getSize();
      var height = size[1] * ol.has.DEVICE_PIXEL_RATIO;
      var width = size[0] * ol.has.DEVICE_PIXEL_RATIO;

      var minx, miny, maxx, maxy;
      minx = printRectangle[0], miny = printRectangle[1],
      maxx = printRectangle[2], maxy = printRectangle[3];

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

      ctx.fillStyle = 'rgba(0, 5, 25, 0.75)';
      ctx.fill();

      ctx.restore();
    };

    var updatePrintRectanglePixels = function(scale) {
      printRectangle = gnPrint.calculatePageBoundsPixels($scope.map,
          $scope.config.layout, scale);
      $scope.map.render();
    };

    var refreshComp = function() {
      updatePrintRectanglePixels($scope.config.scale);
      $scope.map.render();
    };
    $scope.refreshComp = refreshComp;

    var fitRectangleToView = function() {
      $scope.config.scale = gnPrint.getOptimalScale($scope.map,
          $scope.config.scales,
          $scope.config.layout);

      refreshComp();
    };

    $scope.downloadUrl = function(url) {
      if (8 == 9) {
        $window.open(url);
      } else {
        $window.location = url;
      }
    };

    $scope.submit = function() {
      if (!$scope.printActive) {
        return;
      }
      // http://mapfish.org/doc/print/protocol.html#print-pdf
      var view = $scope.map.getView();
      var proj = view.getProjection();
      var lang = $translate.uses();
      var defaultPage = {
        comment: ''
      };
      defaultPage['lang' + lang] = true;
      var encLayers = [];
      var encLegends;
      var attributions = [];
      var layers = $scope.map.getLayers();
      pdfLegendsToDownload = [];

      angular.forEach(layers, function(layer) {
        if (layer.getVisible()) {
          var attribution = layer.attribution;
          if (attribution !== undefined &&
              attributions.indexOf(attribution) == -1) {
            attributions.push(attribution);
          }
          if (layer instanceof ol.layer.Group) {
            var encs = gnPrint.encoders.layers['Group'].call(this,
                layer, proj);
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

      var scales = $scope.config.scales.map(function(scale) {
        return parseInt(scale.value);
      });
      var spec = {
        layout: $scope.config.layout.name,
        srs: proj.getCode(),
        units: proj.getUnits() || 'm',
        rotation: -((view.getRotation() * 180.0) / Math.PI),
        lang: lang,
        dpi: $scope.config.dpi.value,
        layers: encLayers,
        legends: encLegends,
        enableLegends: (encLegends && encLegends.length > 0),
        pages: [
          angular.extend({
            center: gnPrint.getPrintRectangleCenterCoord(
                $scope.map, printRectangle),
            // scale has to be one of the advertise by the print server
            scale: $scope.config.scale.value,
            dataOwner: '© ' + attributions.join(),
            rotation: -((view.getRotation() * 180.0) / Math.PI)
          }, defaultPage)
        ]
      };
      var http = $http.post($scope.config.createURL + '?url=' +
          encodeURIComponent('../../pdf'), spec);
      http.success(function(data) {
        $scope.downloadUrl(data.getURL);
        //After standard print, download the pdf Legends
        //if there are any
        for (var i = 0; i < pdfLegendsToDownload.length; i++) {
          $window.open(pdfLegendsToDownload[i]);
        }
      });
      http.error(function() {
        gaWaitCursor.remove();
      });
    };

    // Encode ol.Layer to a basic js object
    var encodeLayer = function(layer, proj) {
      var encLayer, encLegend;
      var ext = proj.getExtent();
      var resolution = $scope.map.getView().getResolution();

      if (!(layer instanceof ol.layer.Group)) {
        var src = layer.getSource();
        var layerConfig = {};
        var minResolution = layerConfig.minResolution || 0;
        var maxResolution = layerConfig.maxResolution || Infinity;

        if (resolution <= maxResolution &&
            resolution >= minResolution) {
          if (src instanceof ol.source.WMTS) {
            encLayer = gnPrint.encoders.layers['WMTS'].call(this,
                layer, layerConfig);
          } else if (src instanceof ol.source.OSM) {
            encLayer = gnPrint.encoders.layers['OSM'].call(this,
                layer, layerConfig);
          } else if (src instanceof ol.source.MapQuest) {
            encLayer = gnPrint.encoders.layers['MapQuest'].call(this,
                layer, layerConfig);
          } else if (src instanceof ol.source.BingMaps) {
            encLayer = gnPrint.encoders.layers['Bing'].call(this,
                layer, layerConfig);
          } else if (src instanceof ol.source.ImageWMS ||
              src instanceof ol.source.TileWMS) {
            encLayer = gnPrint.encoders.layers['WMS'].call(this,
                layer, layerConfig);
          } else if (src instanceof ol.source.Vector ||
              src instanceof ol.source.ImageVector) {
            if (src instanceof ol.source.ImageVector) {
              src = src.getSource();
            }
            var features = [];
            src.forEachFeatureInExtent(ext, function(feat) {
              features.push(feat);
            });

            if (features && features.length > 0) {
              encLayer =
                  gnPrint.encoders.layers['Vector'].call(this,
                      layer, features);
            }
          }
        }
      }

      if (options.legend && layerConfig.hasLegend) {
        encLegend = gnPrint.encoders.legends['ga_urllegend'].call(this,
            layer, layerConfig);

        if (encLegend.classes &&
            encLegend.classes[0] &&
            encLegend.classes[0].icon) {
          var legStr = encLegend.classes[0].icon;
          if (legStr.indexOf(pdfLegendString,
              legStr.length - pdfLegendString.length) !== -1) {
            pdfLegendsToDownload.push(legStr);
            encLegend = undefined;
          }
        }
      }
      return {layer: encLayer, legend: encLegend};
    };
  };

  mapPrintController['$inject'] = [
    '$scope',
    'gnPrint',
    '$http',
    '$translate',
    '$window'
  ];

  module.directive('gnMapprint',
      ['gnCurrentEdit', function(gnCurrentEdit) {
        return {
          restrict: 'A',
          require: 'gnMapprint',
          templateUrl: '../../catalog/components/common/map/' +
           'print/partials/printmap.html',
          controller: mapPrintController,
          scope: {
            printActive: '=',
            map: '='
          },
          link: function(scope, elt, attrs, ctrl) {

            scope.defaultLayout = attrs.layout;
            scope.auto = true;

            // Deactivate only if it has been activated once first
            scope.$watch('printActive', function(isActive, old) {
              if (angular.isDefined(isActive) &&
                  (angular.isDefined(old) || isActive)) {
                if (isActive) {
                  ctrl.activate();
                } else {
                  ctrl.deactivate();
                }
              }
            });
          }
        };
      }]);
})();
