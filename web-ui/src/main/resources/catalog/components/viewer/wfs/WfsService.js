(function() {
  goog.provide('gn_wfs_service');







  goog.require('Filter_1_0_0');
  goog.require('Filter_1_1_0');
  goog.require('GML_2_1_2');
  goog.require('GML_3_1_1');
  goog.require('OWS_1_0_0');
  goog.require('SMIL_2_0');
  goog.require('SMIL_2_0_Language');
  goog.require('WFS_1_0_0');
  goog.require('WFS_1_1_0');
  goog.require('XLink_1_0');

  var module = angular.module('gn_wfs_service', []);

  // WFS Client
  // Jsonix wrapper to read or write WFS response or request
  var context100 = new Jsonix.Context(
      [XLink_1_0, OWS_1_0_0, Filter_1_0_0,
        GML_2_1_2, SMIL_2_0, SMIL_2_0_Language,
        WFS_1_0_0],
      {
        namespacePrefixes: {
          'http://www.opengis.net/wfs': 'wfs'
        }
      }
      );
  var context110 = new Jsonix.Context(
      [XLink_1_0, OWS_1_0_0,
       Filter_1_1_0,
       GML_3_1_1,
       SMIL_2_0, SMIL_2_0_Language,
       WFS_1_1_0],
      {
        namespacePrefixes: {
          'http://www.w3.org/1999/xlink': 'xlink',
          'http://www.opengis.net/ows/1.1': 'ows',
          'http://www.opengis.net/wfs': 'wfs'
        }
      }
      );
  var unmarshaller100 = context100.createUnmarshaller();
  var unmarshaller110 = context110.createUnmarshaller();

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
        var defer = $q.defer(), defaultVersion = '1.1.0';
        if (url) {
          version = version || defaultVersion;
          url = gnOwsCapabilities.mergeDefaultParams(url, {
            REQUEST: 'GetCapabilities',
            service: 'WFS',
            version: version
          });

          if (gnUrlUtils.isValid(url)) {
            var proxyUrl = gnGlobalSettings.proxyUrl +
                encodeURIComponent(url);
            $http.get(proxyUrl, {
              cache: true
            }).then(function(response) {
              //First cleanup not supported INSPIRE extensions:
              var xml = $.parseXML(response.data);
              if (xml.getElementsByTagName('ExtendedCapabilities').length > 0) {
                var cleanup = function(i, el) {
                  if (el.tagName.endsWith('ExtendedCapabilities')) {
                    el.parentNode.removeChild(el);
                  } else {
                    $.each(el.children, cleanup);
                  }
                };

                $.each(xml.childNodes[0].children, cleanup);
              }

              //Now process the capabilities
              var xfsCap;
              if (version === '1.1.0') {
                xfsCap = unmarshaller110.unmarshalDocument(xml).value;
              } else if (version === '1.0.0') {
                xfsCap = unmarshaller100.unmarshalDocument(xml).value;
              }
              if (xfsCap.exception != undefined) {
                defer.reject({msg: 'wfsGetCapabilitiesFailed',
                  owsExceptionReport: xfsCap});
              }
              else {
                defer.resolve(xfsCap);
              }

            }, function(response) {
              defer.reject({msg: 'wfsGetCapabilitiesFailed',
                httpResponse: response});
            }
            );
          }
          return defer.promise;
        }
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
              for (var j = 0; j < op.parameter.length; j++) {
                var f = op.parameter[j];
                if (f.name == 'outputFormat') {
                  return f.value;
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
          url = gnOwsCapabilities.mergeDefaultParams(url, params);
          window.open(url);
        }
      };
    }
  ]);
})();
