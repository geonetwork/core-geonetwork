(function() {
  goog.provide('gn_onlinesrc_service');

  var module = angular.module('gn_onlinesrc_service', [
  ]);

  module.factory('gnOnlinesrc', ['gnBatchProcessing', '$http',
                 function(gnBatchProcessing, $http) {
        return {
          getAllResources: function() {

            // TODO
            $http({method: 'post', url: 'md.relations.get@json',
              headers: {'Content-type': 'application/xml'},
              params: {
                fast: false
              }}).success(function(data) {
            });
          },

          /**
           * Call md.processing.new in mode 'parent-add'
           * to link a parent to the edited metadata
           */
          linkToParent: function(records) {
            if (records) {
              gnBatchProcessing.runProcessMd({
                parentUuid: records[0]['geonet:info'].uuid,
                process: 'parent-add'
              }).then(function() {
                //TODO close modal
              });
            }
          },

          addOnlinesrc: function(params) {
            gnBatchProcessing.runProcessMd({
              process: 'onlinesrc-add',
              extra_metadata_uuid: '',
              url: params.onlinesrcUrl,
              desc: params.onlinesrcDescr,
              protocol: params.onlinesrcProtocol,
              name: params.onlinesrcName
            });
          }
        };
      }]);
})();
