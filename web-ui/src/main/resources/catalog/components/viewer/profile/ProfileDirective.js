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
  goog.provide('gn_profile_directive');


  var module = angular.module('gn_profile_directive', []);

  /**
   * @ngdoc directive
   * @name gn_viewer.directive:gnProfile
   *
   * @description
   * This directive renders a profile graph using ngeo with the given data &
   * settings.
   * Data should be an array containing all the points like so:
   * [{
   *   lon: 12,
   *   lat: 22,
   *   altitudes: {
   *     height1: 123
   *   },
   *   distance: 14
   * },
   *   ...
   * ]
   * Graph options expected format is:
   * {
   *   valuesProperty: (each value in this object will be displayed)
   *   distProperty: (distance property name)
   *   xProperty: (x coordinate property name)
   *   yProperty: (y coordinate property name)
   *   crs: (crs name, see OpenLayers doc)
   * }
   * Data & options can be passed to the directive through the ProfileService.
   * If a map is specified, the hovered profile point will be rendered on it.
   */
  module.directive('gnProfile', [
    function() {
      return {
        restrict: 'E',
        scope: {
          map: '<',
          graphData: '<',
          graphOptions: '<'
        },
        templateUrl: function(elem, attrs) {
          return attrs.template ||
              '../../catalog/components/viewer/profile/partials/profile.html';
        },
        controllerAs: 'ctrl',
        bindToController: true,
        controller: [
          '$scope',
          'gnProfileService',
          function ProfileDirectiveController($scope, gnProfileService) {
            // watch service data: when a profile graph is available, render it
            $scope.$watch(
                function() {
                  return gnProfileService.getProfileGraphData();
                },
                function(newData, oldData) {
                  if (!newData) { return; }
                  this.graphData = newData;
                  this.graphOptions = gnProfileService.getProfileGraphOptions();
                }.bind(this)
            );

            // this is used to render hovered point on the profile
            // if no map given as input, this will be skipped
            if (this.map) {
              // common overlay layer
              var overlayLayer = gnProfileService.getOverlayLayer(this.map);

              // add a point feature to show mouse hover on profile line
              this.hoveredProfilePoint = new ol.Feature();
              overlayLayer.getSource().addFeature(this.hoveredProfilePoint);

              // this will be used to render the actual profile line on the map
              var profileFeature = new ol.Feature({
                type: 'LineString'
              });
              overlayLayer.getSource().addFeature(profileFeature);

              // show height on graph when hovering the feature
              this.profileHighlight = -1;
              this.map.on('pointermove', function(evt) {
                if (evt.dragging || !this.graphData ||
                    !profileFeature.getGeometry()) {
                  return;
                }

                // hide profile info by default
                this.profileHighlight = -1;
                this.hoveredProfilePoint.setGeometry(null);

                var coordinate = this.map.getEventCoordinate(evt.originalEvent);

                // line feature built with graph data
                var closestPoint =
                    profileFeature.getGeometry().getClosestPoint(coordinate);
                var dx = Math.abs(closestPoint[0] - coordinate[0]);
                var dy = Math.abs(closestPoint[1] - coordinate[1]);
                var pixelDist = Math.max(dx, dy) /
                    this.map.getView().getResolution();

                // if close enough to the feature: show profile info
                if (pixelDist < 8) {
                  // update hover point & display info on graph
                  this.profileHighlight = closestPoint[2];
                  $scope.$apply();
                  this.hoveredProfilePoint.setGeometry(
                      new ol.geom.Point(closestPoint));
                }
              }.bind(this));

              // watch graph data change & display the line on the graph
              $scope.$watch('ctrl.graphData', function(newValue, oldValue) {
                if (!newValue) {
                  profileFeature.setGeometry(null);
                  return;
                }

                // create geom from points
                var geom = new ol.geom.LineString(
                    newValue.map(function(point) {
                      return [
                        point[this.graphOptions.xProperty],
                        point[this.graphOptions.yProperty],
                        point[this.graphOptions.distanceProperty]
                      ];
                    }.bind(this)),
                    'XYM'
                    );
                profileFeature.setGeometry(geom.transform(
                    this.graphOptions.crs, this.map.getView().getProjection()
                    ));
              }.bind(this));
            }

            // ngeo profile graph options
            // note: all callbacks use the current graph options
            this.profileOptions = {
              // for older versions of ngeo
              elevationExtractor: {
                dist: function(data) {
                  return data[this.graphOptions.distanceProperty];
                }.bind(this),
                z: function(data) {
                  return data[this.graphOptions.valuesProperty].z;
                }.bind(this)
              },

              // for newer versions of ngeo
              linesConfiguration: {
                'line': {
                  zExtractor: function(data) {
                    return data[this.graphOptions.valuesProperty].z;
                  }.bind(this)
                }
              },
              distanceExtractor: function(data) {
                return data[this.graphOptions.distanceProperty];
              }.bind(this),

              hoverCallback: function(point) {
                if (!this.hoveredProfilePoint) { return; }
                var geom = new ol.geom.Point([
                  point[this.graphOptions.xProperty],
                  point[this.graphOptions.yProperty]
                ]);
                geom.transform(
                    this.graphOptions.crs,
                    this.map.getView().getProjection()
                );
                this.hoveredProfilePoint.setGeometry(geom);
              }.bind(this),
              outCallback: function(point) {
                if (!this.hoveredProfilePoint) { return; }
                this.hoveredProfilePoint.setGeometry(null);
              }.bind(this)
            };

            // closes the profile graph
            this.closeProfileGraph = function() {
              this.graphData = null;
            };
          }
        ]
      };
    }]);
})();
