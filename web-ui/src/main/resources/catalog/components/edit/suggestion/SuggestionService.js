(function() {
  goog.provide('gn_suggestion_service');

  var module = angular.module('gn_suggestion_service', [
  ]);

  module.factory('gnSuggestion', [
    'gnBatchProcessing',
    'gnHttp',
    'gnEditor',
    'gnCurrentEdit',
    '$q',
    'Metadata',
    function(gnBatchProcessing, gnHttp, gnEditor, gnCurrentEdit, $q, Metadata) {

      var reload = false;


      /**
       * gnSuggestion service PUBLIC METHODS
       * - load
       *******************************************
       */
      return {
        reload: reload,

        load: function() {
          return gnHttp.callService('suggestionsList', {
            id: gnCurrentEdit.id,
            action: 'analyze'
          });
        }
      };
    }]);
})();
