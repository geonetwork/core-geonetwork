(function() {
  goog.provide('inspire-metadata-loader');
  goog.require('inspire_empty_metadata_factory');

  var module = angular.module('inspire_metadata_factory', ['inspire_empty_metadata_factory']);

  module.factory('inspireMetadataLoader', [ 'inspireEmptyMetadataLoader', '$http', '$translate',
    function(inspireEmptyMetadataLoader, $http, $translate) {
    return function(lang, url, mdId) {
        var waitDialog = $('#pleaseWaitDialog');
        if (waitDialog) {
          waitDialog.find('h2').text($translate('loadingMetadata'));
          waitDialog.modal();
        }
        var templateData = inspireEmptyMetadataLoader(lang);
        $http.get(url + 'inspire.edit.model?id=' + mdId).success(function(data){
          angular.copy(data, templateData);
          if (waitDialog) {
            waitDialog.modal('hide');
          }
        }).error(function(err){
          if (waitDialog) {
            waitDialog.modal('hide');
          }
          alert(err);
        });

        return templateData;
      };
  }]);
})();

