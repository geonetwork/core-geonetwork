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
  goog.provide('gn_viewer');
























































































  goog.require('gn_baselayerswitcher');
  goog.require('gn_draw');
  goog.require('gn_featurestable');
  goog.require('gn_geometry');
  goog.require('gn_graticule');
  goog.require('gn_index');
  goog.require('gn_layermanager');
  goog.require('gn_localisation');
  goog.require('gn_measure');
  goog.require('gn_module');
  goog.require('gn_ows');
  goog.require('gn_owscontext');
  goog.require('gn_popup');
  goog.require('gn_print');
  goog.require('gn_searchlayerformap_directive');
  goog.require('gn_terrainswitcher_directive');
  goog.require('gn_viewer_directive');
  goog.require('gn_viewer_service');
  goog.require('gn_wfs');
  goog.require('gn_wfsfilter');
  goog.require('gn_wmsimport');
  goog.require('gn_wps');

  /**
   * @ngdoc overview
   * @name gn_viewer
   *
   * @description
   * Main module for map viewer.
   */

  var module = angular.module('gn_viewer', [
    'gn_viewer_directive',
    'gn_viewer_service',
    'gn_wmsimport',
    'gn_wfs_directive',
    'gn_owscontext',
    'gn_layermanager',
    'gn_baselayerswitcher',
    'gn_measure',
    'gn_draw',
    'gn_ows',
    'gn_localisation',
    'gn_popup',
    'gn_print',
    'gn_module',
    'gn_graticule',
    'gn_searchlayerformap_directive',
    'gn_terrainswitcher_directive',
    'gn_wfsfilter',
    'gn_index',
    'gn_wps',
    'gn_featurestable',
    'gn_geometry'
  ]);

  module.controller('gnViewerController', [
    '$scope',
    '$timeout',
    'gnViewerSettings',
    'gnMap',
    'gnViewerService',
    'gnGeometryService',
    function(
      $scope,
      $timeout,
      gnViewerSettings,
      gnMap,
      gnViewerService,
      gnGeometryService) {

      var map = $scope.searchObj.viewerMap;

      if (gnViewerSettings.wmsUrl && gnViewerSettings.layerName) {
        gnMap.addWmsFromScratch(map, gnViewerSettings.wmsUrl,
            gnViewerSettings.layerName, true).

            then(function(layer) {
              layer.set('group', gnViewerSettings.layerGroup);
              map.addLayer(layer);
            });
      }

      // Display pop up on feature over
      var div = document.createElement('div');
      div.className = 'overlay';
      var overlay = new ol.Overlay({
        element: div,
        positioning: 'bottom-left'
      });
      map.addOverlay(overlay);

      //TODO move it into a directive
      var hidetimer;
      var hovering = false;
      $(map.getViewport()).on('mousemove', function(e) {
        if (hovering) { return; }
        var f;
        var pixel = map.getEventPixel(e.originalEvent);
        var coordinate = map.getEventCoordinate(e.originalEvent);
        map.forEachFeatureAtPixel(pixel, function(feature, layer) {
          if (!layer || !layer.get('getinfo')) { return; }
          $timeout.cancel(hidetimer);
          if (f != feature) {
            f = feature;
            var html = '';
            if (feature.getKeys().indexOf('description') >= 0) {
              html = feature.get('description');
            } else {
              $.each(feature.getKeys(), function(i, key) {
                if (key == feature.getGeometryName() || key == 'styleUrl') {
                  return;
                }
                html += '<dt>' + key + '</dt>';
                html += '<dd>' + feature.get(key) + '</dd>';
              });
              html = '<dl class="dl-horizontal">' + html + '</dl>';
            }
            overlay.getElement().innerHTML = html;
          }
          overlay.setPosition(coordinate);
          $(overlay.getElement()).show();
        }, this, function(layer) {
          return layer.get('getinfo');
        });
        if (!f) {
          hidetimer = $timeout(function() {
            $(div).hide();
          }, 200, false);
        }
      });
      $(div).on('mouseover', function() {
        hovering = true;
      });
      $(div).on('mouseleave', function() {
        hovering = false;
      });

      // watch service data: when a profile graph is available, render it
      var me = this;
      $scope.$watch(
        function() {
          return gnViewerService.getProfileGraphData();
        },
        function(newData, oldData) {
          me.profileGraph = newData && JSON.parse(newData).profile;
        }
      );

      // this is used to render hovered point on the profile
      this.hoveredProfilePoint = new ol.Feature();
      var source = new ol.source.Vector();
      var hoveredPointLayer = new ol.layer.Vector({
        source: source
      });
      source.addFeature(this.hoveredProfilePoint);
      hoveredPointLayer.setZIndex(1000);
      map.addLayer(hoveredPointLayer);

      // let's get the geometry tool layer (where profiles are drawn)
      var profileVectorLayer = gnGeometryService.getCommonLayer(map);

      // show height on graph when hovering the feature
      this.profileHighlight = -1;
      map.on('pointermove', function (evt) {
        if (evt.dragging || !me.profileGraph) {
          return;
        }

        // hide profile info by default
        me.profileHighlight = -1;
        me.hoveredProfilePoint.setGeometry(null);

        var coordinate = map.getEventCoordinate(evt.originalEvent);

        // let's first look for the closest feature & make sure it is a linestring
        var source = profileVectorLayer.getSource();
        var feature = source.getClosestFeatureToCoordinate(coordinate,
          function (feature) {
            return feature.getGeometry().getType() === 'LineString';
          });

        // no linestring found: exit
        if (!feature) {
          return;
        }

        var closestPoint = feature.getGeometry().getClosestPoint(coordinate);
        var dx = Math.abs(closestPoint[0] - coordinate[0]);
        var dy = Math.abs(closestPoint[1] - coordinate[1]);
        var pixelDist = Math.max(dx, dy) / map.getView().getResolution();

        // if close enough to the feature: show profile info
        if (pixelDist < 8) {
          // compute distance from start
          // this was taken from camptocamp/ngeo/gmf profile.js code
          // FIXME: find a cleaner way to handle this, contrib to OL?
          var segment = new ol.geom.LineString();
          var distOnLine = 0;
          var fakeExtent = [
            closestPoint[0] - 0.00000001, closestPoint[1] - 0.00000001,
            closestPoint[0] + 0.00000001, closestPoint[1] + 0.00000001
          ];
          feature.getGeometry().forEachSegment(function (point1, point2) {
            segment.setCoordinates([point1, closestPoint]);
            // segment that hold the point
            if (segment.intersectsExtent(fakeExtent)) {
              return distOnLine += segment.getLength(); // exit loop
            } else {
              segment.setCoordinates([point1, point2]);
              distOnLine += segment.getLength();
            }
          });

          // update hover point & display info on graph
          me.profileHighlight = distOnLine;
          $scope.$apply();
          me.hoveredProfilePoint.setGeometry(new ol.geom.Point(closestPoint));
        }
      });

      // ngeo profile graph options
      this.profileOptions = {
        elevationExtractor: {
          dist: function(data) { return data.dist },
          z: function(data) { return data.values.z }
        },
        linesConfiguration: { },
        hoverCallback: function (point) {
          var geom = new ol.geom.Point([point.lon, point.lat]);
          geom.transform('EPSG:4326', map.getView().getProjection());
          me.hoveredProfilePoint.setGeometry(geom);
        },
        outCallback: function (point) {
          me.hoveredProfilePoint.setGeometry(null);
        }
      };

      // closes the profile graph
      this.closeProfileGraph = function() {
        gnViewerService.clearProfileGraph();
      };
    }]);

})();
