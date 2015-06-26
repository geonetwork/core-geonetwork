(function() {
  goog.provide('gn_gfi_directive');

  var module = angular.module('gn_gfi_directive', [
    'angular.filter'
  ]);

  var gfiTemplateURL = '../../catalog/components/viewer/gfi/partials/' +
      'gfi-popup.html';

  module.value('gfiTemplateURL', gfiTemplateURL);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnGfi
   *
   * @description
   * This directive manage the getFeatureInfo process. If added in the
   * map viewer template, the directive listen to mapclick and display the
   * GFI results as an array in a popup.
   * The template could be overriden using `gfiTemplateURL` constant.
   */
  module.directive('gnGfi', ['$http', 'gfiTemplateURL',
    function($http, gfiTemplateURL) {

      return {
        restrict: 'A',
        scope: {
          map: '='
        },
        templateUrl: gfiTemplateURL,
        link: function(scope, element, attrs) {

          var map = scope.map;
          var mapElement = $(map.getTarget());
          mapElement.find('.ol-overlaycontainer-stopevent').append(element);

          var format = new ol.format.WMSGetFeatureInfo();
          var fo = new ol.FeatureOverlay();
          fo.setMap(map);

          scope.results = fo.getFeatures().getArray();
          scope.pending = 0;
          var overlay = new ol.Overlay({
            positioning: 'center-center',
            position: undefined,
            element: $('<span class="marker">+</span>')[0]
          });
          map.addOverlay(overlay);

          scope.close = function() {
            fo.getFeatures().clear();
            scope.pending = 0;
            overlay.setPosition(undefined);
          };

          map.on('singleclick', function(e) {

            for (var i = 0; i < map.getInteractions().getArray().length; i++) {
              var interaction = map.getInteractions().getArray()[i];
              if (interaction instanceof ol.interaction.Draw &&
                  interaction.getActive()) {
                return;
              }
            }

            fo.getFeatures().clear();
            var layers = map.getLayers().getArray().filter(function(layer) {
              return layer.getSource() instanceof ol.source.ImageWMS ||
                  layer.getSource() instanceof ol.source.TileWMS;
            });

            layers.forEach(function(layer) {
              if (!layer.getVisible()) {
                return;
              }
              var uri = layer.getSource().getGetFeatureInfoUrl(e.coordinate,
                  map.getView().getResolution(),
                  map.getView().getProjection(), {
                    INFO_FORMAT: layer.ncInfo ? 'text/xml' :
                        'application/vnd.ogc.gml'
                  });
              var coordinate = e.coordinate;
              var proxyUrl = '../../proxy?url=' + encodeURIComponent(uri);
              scope.pending += 1;
              return $http.get(proxyUrl).success(function(response) {
                var features;
                if (layer.ncInfo) {
                  var doc = ol.xml.parse(response);
                  var props = {};
                  ['longitude', 'latitude',
                    'time', 'value'].forEach(function(v) {
                    var node = doc.getElementsByTagName(v);
                    if (node) {
                      props[v] = ol.xml.getAllTextContent(node[0], true);
                    }
                  });
                  if (props.value && props.value != 'none') {
                    features = [new ol.Feature(props)];
                  }
                } else {
                  features = format.readFeatures(response);
                }
                if (features) {
                  features.forEach(function(f) {
                    f.layer = layer.get('label');
                  });
                  fo.getFeatures().extend(features);
                  overlay.setPosition(coordinate);
                }
                scope.pending = Math.max(0, scope.pending - 1);
              }).error(function() {
                scope.pending = Math.max(0, scope.pending - 1);
              });
            }, this);

          });

          scope.$watch('pending', function(v) {
            mapElement.toggleClass('gn-gfi-loading', (v !== 0));
          });

        }
      };

    }]);

  angular.module('gfiFilters', ['ngSanitize'])

  .filter('attributes', function() {
        return function(properties) {
          var props = {};
          var exclude = ['FID', 'boundedBy', 'the_geom', 'thegeom'];
          Object.keys(properties).forEach(function(k) {
            if (exclude.indexOf(k) !== -1) return;
            if (properties[k]) {
              props[k] = properties[k].toString();
            }
          });
          return props;
        };
      });

})();
