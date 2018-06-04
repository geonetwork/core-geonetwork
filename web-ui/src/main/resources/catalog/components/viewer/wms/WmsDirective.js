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
  goog.provide('gn_wms_directive');

  goog.require('gn_wms_service');

  var module = angular.module('gn_wms_directive', [
    'gn_wms_service'
  ]);

  module.directive('gnWmsDownload', ['gnWmsService', 'gnGlobalSettings',
    'gnSearchSettings', '$rootScope', '$translate',
    'gnSearchLocation',
    function(gnWmsService, gnGlobalSettings, gnSearchSettings,
             $rootScope, $translate, gnSearchLocation) {
      return {
        restrict: 'A',
        scope: {
          layer: '=gnWmsDownload',
          map: '=',
          md: '='
        },
        templateUrl: '../../catalog/components/' +
            'viewer/wms/partials/wmsDownload.html',
        link: function(scope, element, attrs, ctrls) {
          scope.isMapViewerEnabled = gnGlobalSettings.isMapViewerEnabled;
          scope.capabilities = null;
          scope.layerSelected = null;
          scope.isWmsAvailable = false;
          scope.isLayerInCapabilities = false;
          scope.capabilitiesChecked = false;
          var init = function() {
            try {
              // Get WMS URL from attrs or try by getting the url layer property
              scope.url = attrs['url'] || scope.layer.get('url');
              scope.layerName = attrs['layerName'];

              scope.projections = [];
              scope.checkWmsUrl().then(
                  function() {
                    if (scope.capabilities.layers &&
                    scope.capabilities.layers.length != 0) {
                      scope.isWmsAvailable = true;
                      //here check wms/wmts

                      scope.isLayerInCapabilities =
                      gnWmsService.isLayerInCapabilities(scope.capabilities,
                      scope.layerName);
                    }
                  }, function() {
                  }

              ).finally(function() {
                scope.capabilitiesChecked = true;
              });
            } catch (e) {
              scope.problemContactingServer = true;
              scope.capabilitiesChecked = true;
            }
          };

          scope.checkWmsUrl = function() {
            return gnWmsService.getCapabilities(scope.url)
                .then(function(capabilities) {
                  scope.capabilities = capabilities;
                });
          };

          scope.addSelectedLayerToMap = function(layerSelected) {
            if (!layerSelected) {
              return;
            }
            layerSelected.capRequest = scope.capabilities.Request||null;
            gnWmsService.addLayerToMap(layerSelected,
                gnSearchSettings.viewerMap);
            gnSearchLocation.setMap();

          };

          scope.hasName = function(layer) {
            return ('Name' in layer) && layer.Name;
          };

          scope.addWmsLayer = function() {
            gnWmsService.addWMSToMap(scope.layerName, scope.url,
                scope.md, gnSearchSettings.viewerMap);
            gnSearchLocation.setMap();

          };


          init();
        }
      };
    }
  ]);
})();
