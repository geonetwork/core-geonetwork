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
  goog.provide('gn_graticule');

  goog.require('gn_button');

  var module = angular.module('gn_graticule', ['gn_button']);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnGraticuleBtn
   *
   * @description
   * This directive provides a button to display ol3 graticule on the map.
   * The graticule style is no customisable for the moment.
   */
  module.directive('gnGraticuleBtn', [function() {
    
    return {
      restrict: 'A',
      replace: true,
      templateUrl: '../../catalog/components/viewer/graticule/partials/' +
          'graticule.html',
      scope: true,
      link: function(scope, element, attrs) {
        var graticuleOgcService = scope.$eval(attrs['graticuleOgcService']);
        var graticule = null;

        // for now, only WMS services are supported by the directive
        if (graticuleOgcService) {
          if (!graticuleOgcService.layer || !graticuleOgcService.url) {
            console.error('Missing property for graticule layer ' +
                '(required: \'url\', \'layer\'):\n', graticuleOgcService);
            return;
          }

          graticule = new ol.layer.Image({
            source: new ol.source.ImageWMS({
              url: graticuleOgcService.url,
              params: { 'LAYERS': graticuleOgcService.layer },
              ratio: 1
            })
          });
        } else {
          graticule = new ol.layer.Graticule({
            // the style to use for the lines, optional.
            strokeStyle: new ol.style.Stroke({
              color: 'rgba(255,120,0,0.9)',
              width: 2,
              lineDash: [0.5, 4]
            }),
            showLabels: true,
            wrapX: false
          })
        }

        graticule.background = true;  // do not save it in context
        graticule.setZIndex(1000);    // TODO: uncomment after OL upgrade
        graticule.setVisible(false);  // hidden by default

        // 'active' prop makes ogc layer visible/invisible
        Object.defineProperty(graticule, 'active', {
          get: function() {
            return graticule.getVisible();
          },
          set: function(val) {
            // add layer to map if not already done
            var map = scope.$eval(attrs['gnGraticuleBtn']);
            if (map.getLayers().getArray().indexOf(graticule) === -1) {
              map.addLayer(graticule);
            }
            graticule.setVisible(val);
          }
        });

        scope.graticule = graticule;
      }
    };
  }]);
})();
