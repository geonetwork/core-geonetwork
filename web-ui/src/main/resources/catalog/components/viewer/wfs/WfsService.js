(function() {
  goog.provide('gn_wfs_service');







  goog.require('Filter_1_1_0');
  goog.require('GML_3_1_1');
  goog.require('OWS_1_0_0');
  goog.require('SMIL_2_0');
  goog.require('SMIL_2_0_Language');
  goog.require('WFS_1_1_0');
  goog.require('XLink_1_0');

  var module = angular.module('gn_wfs_service', []);

  // WFS Client
  // Jsonix wrapper to read or write WFS response or request
  var context = new Jsonix.Context(
      [XLink_1_0, OWS_1_0_0, Filter_1_1_0, GML_3_1_1,
       SMIL_2_0, SMIL_2_0_Language, WFS_1_1_0],
      {
        namespacePrefixes: {
          'http://www.w3.org/1999/xlink': 'xlink',
          'http://www.opengis.net/ows/1.1': 'ows',
          'http://www.opengis.net/wfs': 'wfs'
        }
      }
      );
  var unmarshaller = context.createUnmarshaller();
  var marshaller = context.createMarshaller();

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
      this.getCapabilities = function(url) {
        var defer = $q.defer();
        if (url) {
          url = gnOwsCapabilities.mergeDefaultParams(url, {
            REQUEST: 'GetCapabilities',
            service: 'WFS',
            version: '1.1.1'
          });

          if (gnUrlUtils.isValid(url)) {
            var proxyUrl = gnGlobalSettings.proxyUrl +
                encodeURIComponent(url);
            $http.get(proxyUrl, {
              cache: true
            }).then(function(response) {
              var xfsCap = unmarshaller.unmarshalString(response.data).value;
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
    }
  ]);
})();
