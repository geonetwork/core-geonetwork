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
  goog.require('gn_heatmap');
  goog.require('gn_index');
  goog.require('gn_layermanager');
  goog.require('gn_projectionswitcher');
  goog.require('gn_localisation');
  goog.require('gn_measure');
  goog.require('gn_module');
  goog.require('gn_ows');
  goog.require('gn_owscontext');
  goog.require('gn_popup');
  goog.require('gn_print');
  goog.require('gn_profile');
  goog.require('gn_searchlayerformap_directive');
  goog.require('gn_terrainswitcher_directive');
  goog.require('gn_viewer_directive');
  goog.require('gn_viewer_service');
  goog.require('gn_wfs');
  goog.require('gn_wfsfilter');
  goog.require('gn_wmsimport');
  goog.require('gn_wps');
  goog.require('gn_gazetteer');
  goog.require('gn_legendpanel_directive');
  goog.require('gn_popover');


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
    'gn_projectionswitcher',
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
    'gn_geometry',
    'gn_profile',
    'gn_heatmap',
    'gn_gazetteer',
    'gn_legendpanel_directive',
    'gn_popover'
  ]);

  module.controller('gnViewerController', [
    '$scope',
    '$timeout',
    'gnViewerSettings',
    'gnMap',
    function(
        $scope,
        $timeout,
        gnViewerSettings,
        gnMap) {

      var map = $scope.searchObj.viewerMap;

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
        }.bind(this),  {
          layerFilter: function(layer) {
            return layer.get('getinfo');
        }});
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
    }]);

})();
