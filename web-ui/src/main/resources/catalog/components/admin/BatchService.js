(function() {
  goog.provide('gn_batch_service');

  var module = angular.module('gn_batch_service', [
  ]);

  /**
   * @ngdoc service
   * @kind function
   * @name gnBatchProcessing
   * @requires gnHttp
   * @requires gnEditor
   * @requires gnCurrentEdit
   * @requires $q
   *
   * @description
   * The `gnBatchProcessing` service is used to run batch processing service
   * on metadatas during the edition. It is mostly used for the online resources
   * management.
   */
  module.factory('gnBatchProcessing', [
    'gnHttp',
    'gnEditor',
    'gnCurrentEdit',
    '$q',
    function(gnHttp, gnEditor, gnCurrentEdit, $q) {

      var processing = true;
      var processReport = null;
      return {

        /**
         * @name gnBatchProcessing#runProcessMd
         *
         * @description
         * Run process md.processing on the edited
         * metadata after the form has been saved.
         *
         * @param {Object} params to add to the request
         * @return {HttpPromise}
         */
        runProcessMd: function(params) {
          if (!params.id && !params.uuid) {
            angular.extend(params, {
              id: gnCurrentEdit.id
            });
          }
          var defer = $q.defer();
          gnEditor.save(false, true)
                .then(function() {
                gnHttp.callService('processMd', params).then(function(data) {
                  var snippet = $(data.data);
                  gnEditor.refreshEditorForm(snippet);
                  defer.resolve(data);
                });
              });
          return defer.promise;
        },

        runProcessMdXml: function(params) {
          return gnHttp.callService('processXml', params);
        }

        // TODO : write batch processing service here
        // from adminTools controller
      };
    }]);
})();
