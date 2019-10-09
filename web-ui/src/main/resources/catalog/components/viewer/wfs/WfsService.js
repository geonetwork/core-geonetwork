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
  goog.provide('gn_wfs_service');




  var module = angular.module('gn_wfs_service', []);

  module.service('gnWfsService', [
    '$http',
    'gnOwsCapabilities',
    'gnUrlUtils',
    'gnGlobalSettings',
    '$q',
    function($http, gnOwsCapabilities, gnUrlUtils, gnGlobalSettings, $q) {

      /**
       * Do a getCapabilies request to the url (service) given in parameter.
       *
       * @param {string} url wfs service url
       * @return {Promise}
       */
      this.getCapabilities = function(url, version) {
        return gnOwsCapabilities.getWFSCapabilities(url, version);
      };

      this.getTypeName = function(capabilities, typename) {
        if (capabilities.featureTypeList) {
          var tokens = typename.split(':'),
              prefix = tokens.length === 1 ? null : tokens[0],
              localPart = tokens.length === 1 ? typename : tokens[1];
          for (var i = 0;
               i < capabilities.featureTypeList.featureType.length;
               i++) {
            var name = capabilities.featureTypeList.
                featureType[i].name;
            if (
                (name.localPart == localPart && prefix == null) ||
                (name.localPart == localPart && name.prefix == prefix)
            ) {
              return capabilities.featureTypeList.featureType[i];
            }
          }
        }
      };

      this.getOutputFormat = function(capabilities, operation) {
        if (capabilities.operationsMetadata) {
          for (var i = 0;
               i < capabilities.operationsMetadata.operation.length; i++) {
            var op = capabilities.operationsMetadata.operation[i];
            if (op.name == operation || op.name == 'GetFeature') {
              if (op.parameter){
                for (var j = 0; j < op.parameter.length; j++) {
                  var f = op.parameter[j];
                  if (f.name == 'outputFormat') {
                    if (capabilities.version=="2.0.0") {
                      //wfs2 exposes outputformats in 'AllowedValues'
                      return f.allowedValues.valueOrRange.map(function(v) { return v.value; })
                    } else {
                      return f.value
                    }
                  }
                }
              } else {
                if (capabilities.version=="2.0.0") {
                  return ["text/xml; subtype=gml/3.2"];
                } else {
                  return ["text/xml; subtype=gml/3.1.1"];
                }
              }
            }
          }
        }
        return [];
      };
      this.getProjection = function(capabilities, layers) {
        if (capabilities.operationsMetadata) {
          for (var i = 0;
               i < capabilities.operationsMetadata.operation.length; i++) {
            var op = capabilities.operationsMetadata.operation[i];
            if (op.name == operation || op.name == 'GetFeature') {
              for (var j = 0; j < op.parameter.length; j++) {
                var f = op.parameter[j];
                if (f.name == 'projection') {
                  return f.value;
                }
              }
            }
          }
        }
        return [];
      };


      // TODO: Add maxFeatures, featureid
      this.download = function(url, version, typename,
                               format, extent, projection) {
        if (url) {
          var defaultVersion = '1.1.0';
          // Params to remove from input URL if existing
          var excludedParams = ['request', 'service', 'version'];
          var params = {
            request: 'GetFeature',
            service: 'WFS',
            version: version || defaultVersion,
            typeName: typename,
            outputFormat: format
          };
          if (extent) {
            params.bbox = extent;
          }
          if (projection) {
            params.srsName = projection;
          }
          url = gnOwsCapabilities.mergeParams(url, params, excludedParams);
          window.open(url);
        } else {
          console.warn('no url');
        }
      };
    }
  ]);
})();
