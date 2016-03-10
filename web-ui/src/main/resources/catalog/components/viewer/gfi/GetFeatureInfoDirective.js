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

(function() {
  goog.provide('gn_gfi_directive');

  var module = angular.module('gn_gfi_directive', [
    'angular.filter'
  ]);

  var gfiTemplateURL = '../../catalog/components/viewer/gfi/partials/' +
      'gfi-popup.html';

  module.value('gfiTemplateURL', gfiTemplateURL);

  module.directive('gnVectorFeatureToolTip', [function() {
    return {
      restrict: 'A',
      scope: {
        map: '=gnVectorFeatureToolTip'
      },
      link: function(scope, element, attrs) {
        $('body').append('<div id="feature-info" data-content=""' +
            'style="position: absolute; z-index: 100;"/>');
        var info = $('#feature-info');
        info.popover({
          animation: false,
          trigger: 'manual',
          placement: 'top',
          html: true,
          title: 'Feature info'
        });

        var displayFeatureInfo = function(pixel) {
          info.css({
            left: pixel[0] + 'px',
            top: (pixel[1] + 45) + 'px'
          });
          var feature = scope.map.forEachFeatureAtPixel(pixel,
              function(feature, layer) {
                return feature;
              });
          if (feature) {
            var props = feature.getProperties();
            var tooltipContent = '<ul>';
            $.each(props, function(key, values) {
              if (typeof values !== 'object') {
                tooltipContent += '<li>' + key + ': ' + values + '</li>';
              }
            });
            tooltipContent += '</ul>';
            info.popover('hide');
            info.data('bs.popover').options.content = tooltipContent;
            info.popover('show');
          } else {
            info.popover('hide');
          }
        };

        scope.map.on('pointermove', function(evt) {
          if (evt.dragging) {
            //info.hide();
            info.popover('hide');
            return;
          }
          displayFeatureInfo(scope.map.getEventPixel(evt.originalEvent));
        });
      }
    };
  }]);
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
          var fo = new ol.layer.Vector({
            source: new ol.source.Vector({
              useSpatialIndex: false
            }),
            map: map
          });

          scope.results = fo.getSource().getFeatures();
          scope.pending = 0;
          var overlay = new ol.Overlay({
            positioning: 'center-center',
            position: undefined,
            element: $('<span class="marker">+</span>')[0]
          });
          map.addOverlay(overlay);

          scope.close = function() {
            fo.getSource().clear();
            scope.pending = 0;
            overlay.setPosition(undefined);
          };

          map.on('singleclick', function(e) {

            for (var i = 0; i < map.getInteractions().getArray().length; i++) {
              var interaction = map.getInteractions().getArray()[i];
              if ((interaction instanceof ol.interaction.Draw ||
                  interaction instanceof ol.interaction.Select) &&
                  interaction.getActive()) {
                return;
              }
            }

            fo.getSource().clear();
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
                    if (node && node.length > 0) {
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
                  fo.getSource().getFeaturesCollection().extend(features);
                  if (features.length > 0) {
                    overlay.setPosition(coordinate);
                  } else {
                    overlay.setPosition();
                  }
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
