(function() {
  goog.provide('gn_onlinesrc_service');

  var module = angular.module('gn_onlinesrc_service', [
  ]);

  module.provider('gnOnlinesrc', function() {
    this.$get = ['gnBatchProcessing',
                 function(gnBatchProcessing) {
        return {
          linkToParent: function(records) {
            if (records) {
              gnBatchProcessing.runProcessNew({
                id: 22, //FIXME : get real value
                parentUuid: records[0]['geonet:info'].uuid,
                process: 'parent-add'
              });
            }
          },
          addOnlinesrc: function(params, isUpload) {
            if (!isUpload) {
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
            else {
              gnBatchProcessing.runProcessNew({
                id: 22, //FIXME : get real value
                process: 'onlinesrc-add',
                url: params.onlinesrcUrl,
                desc: params.onlinesrcDescr,
                protocol: params.onlinesrcProtocol,
                name: params.onlinesrcName,
                filename: params.onlinesrcFileName,
                overwrite: params.onlinesrcOverWrite
              });
            }
          }
        };
      }];
  });
})();
