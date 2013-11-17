(function() {
  goog.provide('gn_onlinesrc_service');

  var module = angular.module('gn_onlinesrc_service', [
  ]);

  module.provider('gnOnlinesrc', function() {
    this.$get = ['gnBatchProcessing', '$http',
                 function(gnBatchProcessing, $http) {
        return {
          getAllResources: function() {

            $http({method: 'post', url: 'md.relations.get@json',
              headers: {'Content-type': 'application/xml'},
              params: {
                id: 22, //FIXME: get real md id
                fast: false
              }}).success(function(data) {
              console.log(data);
            });
          },

          /**
           * Call md.processing.new in mode 'parent-add'
           * to link a parent to the edited metadata
           */
          linkToParent: function(records) {
            if (records) {
              gnBatchProcessing.runProcessNew({
                id: 22, //FIXME : get real value
                parentUuid: records[0]['geonet:info'].uuid,
                process: 'parent-add'
              });
            }
          },
          addOnlinesrc: function(params) {
            gnBatchProcessing.runProcessNew({
              id: 22, //FIXME : get real value
              process: 'onlinesrc-add',
              extra_metadata_uuid: '',
              url: params.onlinesrcUrl,
              desc: params.onlinesrcDescr,
              protocol: params.onlinesrcProtocol,
              name: params.onlinesrcName
            });
          }
        };
      }];
  });
})();
