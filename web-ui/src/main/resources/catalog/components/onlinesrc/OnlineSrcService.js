(function() {
  goog.provide('gn_onlinesrc_service');

  goog.require('gn_urlutils_service');

  var module = angular.module('gn_onlinesrc_service', [
    'gn_urlutils_service'
  ]);

  module.provider('gnOnlinesrc', function() {
    this.$get = ['gnUrlUtils',
                 'gnBatchProcessing',
                 function(gnUrlUtils, gnBatchProcessing) {
        return {
          linkToParent: function(records) {
            if (records) {
              gnBatchProcessing.runProcessNew({
                id: 22, //FIXME : get real value
                parentUuid: records[0]['geonet:info'].uuid,
                process: 'parent-add'
              });
            }
          }
        };
      }];
  });
})();
